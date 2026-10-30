# ezTicket NAS 主動拉取部署

已於 2026-10-30 完成切換。GitHub 只建置並發布 GHCR app 映像，不再使用 SSH／rsync 連入 NAS。

## 日常使用

推送 `main` 後，等待 GitHub「Build NAS image (no inbound deployment)」成功。NAS 的獨立排程每天 04:20（Asia/Taipei）拉取 `ghcr.io/liang0731/ezticket:auto-deploy`。本次建立的 DSM 任務名稱為 `Task 12`，已啟用；其他 NAS 專案的 04:00 排程維持原狀。

排程先驗證三個部署檔的 SHA-256，再執行 `scripts/nas-scheduled-update.py`。因此手動更改部署腳本或 Compose 後，必須先審查變更並更新 DSM 中的對應雜湊；單純發布新版 app 映像不需修改排程。

## 資料與更新範圍

- 原部署目錄、NAS `.env`、Compose project `ezticket` 均保留。
- app 透過 Docker 內部 `mysql:3306`、`redis:6379` 連線。發布埠 3307、6380、8086 未改。
- 有新映像時，先以 MySQL single-transaction dump 備份 ezTicket 資料庫，再只執行 `up -d --no-deps app`。
- 不停止或重建 MySQL／Redis，不執行 `down`、`down -v`、SQL 初始化或移除資料卷。
- 映像相同不重建。app 健康檢查失敗時嘗試回復舊 app 映像；這不會回復 app 已寫入或遷移的資料庫內容。
- 比對更新前後所有其他容器的 ID、映像、啟動時間與命名資料卷，異常時回報失敗。
- 資料庫 dump 與部署日誌保存在原部署目錄的 `.deploy-state/`，權限受限；不要提交 Git。它是同 NAS 備份，不等於獨立或異地備份。後續應依容量安排備份保留政策。

## 已完成的驗證

- 五項離線測試通過：無變更、只更新 app、拉取失敗不動容器、健康失敗回復、project 不符停止。
- GitHub 建置成功，映像版本來源：`99b4d887265c0761f625c2fa1bbf8dfd42943ae0`。
- 首次 NAS 執行回報 `updated`，建立 25,840,605 bytes 的 SQL dump，app HTTP 檢查通過。
- MySQL／Redis 的容器 ID、啟動時間與資料卷未變；其他八個容器未變。
- 第二次執行回報 `unchanged`，app 容器 ID 未變，HTTP 檢查通過。
- 每日 04:20 排程已儲存並重新讀取確認。未把未來的排程觸發或完整業務操作測試視為已驗證。

## 操作與復原

需要人工執行時，從 DSM 選取上述獨立任務執行。不要直接使用舊 SSH 部署指令或 `nas-clean-deploy.sh`；後者會刪除資料庫 volume。

部署結果報告的實際位置記錄於 DSM 任務命令；報告不包含環境變數或密碼。詳細日誌為 `.deploy-state/scheduler.log`，失敗時先檢查報告與日誌，不要重置資料庫。

原有本機建置的 `docker-compose.yml` 保留供人工維護與復原。使用時必須保留原 project 及資料卷，不執行 `down -v`。

程式碼仍有綠界付款回呼 `/ecpay/return`、`/ecpay/Treturn`；私人網路部署不代表外部支付系統可以回呼。此切換未替付款功能新增公網入口。
