"""Offline contract checks: fake Docker/curl; never contacts the NAS."""
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
BASH = shutil.which('bash') if os.name != 'nt' else 'C:/Program Files/Git/bin/bash.exe'


def posix(path):
    value = str(Path(path).resolve()).replace('\\', '/')
    return '/' + value[0].lower() + value[2:] if len(value) > 1 and value[1] == ':' else value


class PullDeployTest(unittest.TestCase):
    def run_case(self, scenario):
        with tempfile.TemporaryDirectory(prefix='pull-test-', dir=ROOT) as directory:
            root = Path(directory)
            (root / 'scripts').mkdir()
            (root / 'bin').mkdir()
            shutil.copy(ROOT / 'scripts/nas-pull-deploy.sh', root / 'scripts/job.sh')
            (root / '.env').write_text('SERVER_PORT=8085\n')
            fake = '''#!/usr/bin/env bash
echo "$*|image=${EZTICKET_IMAGE:-}" >> "$TEST_ROOT/calls"
case "$1" in
 inspect)
  case "$3" in
   *compose.project*) [[ "$SCENARIO" == mismatch ]] && echo wrong || echo ezticket ;;
   *State.Running*) echo true ;;
   *) printf 'sha256:%064d\\n' 1 ;;
  esac ;;
 pull) [[ "$SCENARIO" != pullfail ]] ;;
 image) [[ "$SCENARIO" == same ]] && printf 'sha256:%064d\\n' 1 || printf 'sha256:%064d\\n' 2 ;;
 compose) echo "$EZTICKET_IMAGE" > "$TEST_ROOT/active" ;;
 *) exit 99 ;;
esac
'''
            (root / 'bin/docker').write_text(fake, encoding='utf-8', newline='\n')
            (root / 'bin/curl').write_text('''#!/usr/bin/env bash
if [[ "$SCENARIO" == rollback ]] && grep -q '2$' "$TEST_ROOT/active"; then exit 1; fi
exit 0
''', encoding='utf-8', newline='\n')
            for path in (root / 'bin').iterdir():
                path.chmod(0o755)
            env = dict(os.environ, TEST_ROOT=posix(root), SCENARIO=scenario,
                       EZTICKET_DOCKER=posix(root / 'bin/docker'),
                       EZTICKET_COMPOSE_PROJECT='ezticket',
                       EZTICKET_HEALTH_ATTEMPTS='1', EZTICKET_HEALTH_INTERVAL='0')
            command = 'export PATH="' + posix(root / 'bin') + ':$PATH"; bash "' + posix(root / 'scripts/job.sh') + '"'
            result = subprocess.run([BASH, '-c', command], env=env, capture_output=True, text=True, timeout=20)
            calls = (root / 'calls').read_text() if (root / 'calls').exists() else ''
            return result.returncode, calls

    def test_unchanged_does_not_recreate(self):
        code, calls = self.run_case('same')
        self.assertEqual(code, 0)
        self.assertNotIn('compose --', calls)

    def test_update_only_recreates_app(self):
        code, calls = self.run_case('update')
        self.assertEqual(code, 0)
        self.assertEqual(calls.count('compose --'), 1)
        self.assertIn('up -d --no-deps app', calls)
        self.assertNotIn('down', calls)

    def test_failed_health_rolls_back(self):
        code, calls = self.run_case('rollback')
        self.assertEqual(code, 1)
        self.assertEqual(calls.count('compose --'), 2)
        self.assertTrue(calls.rstrip().endswith('1'))

    def test_failed_pull_leaves_containers_alone(self):
        code, calls = self.run_case('pullfail')
        self.assertNotEqual(code, 0)
        self.assertNotIn('compose --', calls)

    def test_wrong_project_leaves_containers_alone(self):
        code, calls = self.run_case('mismatch')
        self.assertNotEqual(code, 0)
        self.assertNotIn('pull ', calls)


if __name__ == '__main__':
    unittest.main()
