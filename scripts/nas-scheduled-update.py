"""NAS scheduler entry: back up ezTicket DB, update only app, audit isolation."""
import argparse
import datetime
import json
import os
from pathlib import Path
import subprocess
import sys

DOCKER = '/var/packages/ContainerManager/target/usr/bin/docker'
ROOT = Path(__file__).resolve().parents[1]
CHANNEL = 'ghcr.io/liang0731/ezticket:auto-deploy'


def run(args, **kwargs):
    return subprocess.run(args, check=True, timeout=kwargs.pop('timeout', 300), **kwargs)


def inspect(names):
    raw = run([DOCKER, 'inspect'] + names, capture_output=True, text=True)
    return {item['Name'].lstrip('/'): {
        'id': item['Id'], 'image': item['Image'],
        'started': item['State']['StartedAt'], 'running': item['State']['Running'],
        'project': item['Config'].get('Labels', {}).get('com.docker.compose.project'),
        'volumes': [{'name': m.get('Name'), 'destination': m['Destination']}
                    for m in item.get('Mounts', []) if m['Type'] == 'volume'],
    } for item in json.loads(raw.stdout)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--report', required=True)
    parser.add_argument('--audit-only', action='store_true')
    args = parser.parse_args()
    os.umask(0o077)
    state = ROOT / '.deploy-state'
    state.mkdir(exist_ok=True)
    report = {'time': datetime.datetime.now(datetime.timezone.utc).isoformat(), 'status': 'starting'}
    log = state / 'scheduler.log'
    if log.exists() and log.stat().st_size > 1048576:
        log.replace(state / 'scheduler.log.1')
    lock = state / 'scheduler.lock'
    locked = False
    code = 1
    try:
        lock.mkdir()
        locked = True
        names = run([DOCKER, 'ps', '-a', '--format', '{{.Names}}'], capture_output=True, text=True).stdout.splitlines()
        before = inspect(names)
        report['before'] = before
        for name in ('ezticket-app', 'ezticket-mysql', 'ezticket-redis'):
            if name not in before or not before[name]['running']:
                raise RuntimeError('Required ezTicket container not running: ' + name)
        project = before['ezticket-app']['project']
        if not project or any(before[n]['project'] != project for n in ('ezticket-mysql', 'ezticket-redis')):
            raise RuntimeError('ezTicket Compose project labels do not match')
        report['project'] = project
        if args.audit_only:
            report['status'] = 'audit_only'
            return 0
        with log.open('a') as output:
            run([DOCKER, 'pull', CHANNEL], stdout=output, stderr=output, timeout=900)
            candidate = run([DOCKER, 'image', 'inspect', '--format', '{{.Id}}', CHANNEL], capture_output=True, text=True).stdout.strip()
            report['candidate'] = candidate
            if candidate == before['ezticket-app']['image']:
                report['status'] = 'unchanged'
                code = 0
            else:
                backup = state / 'backups'
                backup.mkdir(exist_ok=True)
                dump = backup / (datetime.datetime.now().strftime('%Y%m%d-%H%M%S') + '.sql')
                partial = dump.with_suffix('.sql.partial')
                command = ('export MYSQL_PWD="$MYSQL_ROOT_PASSWORD"; '
                           'exec mysqldump -uroot --single-transaction --routines --triggers --events '
                           '--no-tablespaces --set-gtid-purged=OFF --databases "$MYSQL_DATABASE"')
                with partial.open('wb') as destination:
                    run([DOCKER, 'exec', 'ezticket-mysql', 'sh', '-c', command], stdout=destination, stderr=output, timeout=600)
                if partial.stat().st_size < 100:
                    raise RuntimeError('Database dump is unexpectedly small; app unchanged')
                partial.replace(dump)
                report['database_backup'] = {'path': str(dump), 'bytes': dump.stat().st_size}
                env = dict(os.environ, EZTICKET_COMPOSE_PROJECT=project,
                           EZTICKET_TARGET_IMAGE_ID=candidate)
                result = subprocess.run(['/bin/bash', str(ROOT / 'scripts/nas-pull-deploy.sh')],
                                        env=env, stdout=output, stderr=output, timeout=900)
                code = result.returncode
                report['status'] = 'updated' if code == 0 else 'update_failed_check_rollback'
        after = inspect(names)
        report['after'] = after
        changed = [name for name in names if name != 'ezticket-app' and before[name] != after[name]]
        report['unrelated_or_database_changes'] = changed
        if changed:
            report['status'] = 'isolation_check_failed'
            code = 1
        report['app_http_ok'] = subprocess.run(['curl', '--connect-timeout', '2', '--max-time', '10',
                                               '-fsS', '-I', 'http://127.0.0.1:8086/index.html'],
                                              stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL).returncode == 0
        if not report['app_http_ok']:
            code = 1
    except FileExistsError:
        report['status'] = 'locked_no_changes'
    except Exception as error:
        report['status'] = 'failed'
        report['error_type'] = type(error).__name__
    finally:
        if locked:
            lock.rmdir()
        destination = Path(args.report)
        temporary = destination.with_suffix('.tmp')
        temporary.write_text(json.dumps(report, indent=2), encoding='utf-8')
        os.chmod(temporary, 0o644)  # Report contains no environment variables or credentials.
        temporary.replace(destination)
    return code


if __name__ == '__main__':
    sys.exit(main())
