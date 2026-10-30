# NAS 主動拉取部署草稿（尚未套用）

GitHub 只建置並發布 GHCR app 映像；NAS 排程主動拉取。舊 SSH／rsync 部署由新的 image workflow 取代，不需要對 NAS 開放公網 SSH。映像內不得放 NAS `.env`、私鑰或 Docker registry 憑證。

## 保留現有服務

- `.env` 留在 `/var/services/homes/github-runner/ezticket`，不從 GitHub Secrets 覆寫。
- 使用原有 Compose project 名稱，禁止另建同名容器或新 MySQL volume。
- app 仍透過 `mysql:3306`、`redis:6379` 連線；發布埠 3307、6380、8086 維持原狀。
- 排程只執行 `up -d --no-deps app`。不執行 `down`、`down -v`、清除 volume、SQL 初始化或重建資料庫。
- 映像相同不重建；更新失敗嘗試回復原 app 映像。這不會回復應用程式已經寫入或遷移的資料庫內容。
- 不更改其他 NAS 專案、Photos、Drive、反向代理。

## 切換前必做

1. 用管理權限確認現有 app 的 `com.docker.compose.project` 與 working directory；目前一般 SSH 帳號無 Docker socket 權限，尚未完成這項確認。
2. 確認資料庫有可用備份，並記錄現有 app image ID、MySQL volume、三個容器的身分與啟動時間。
3. 發布 image workflow，確認 CI build 與映像發布成功。若 GHCR 套件為 private，沿用 NAS 的 registry 登入方式或建立最小唯讀憑證；不把憑證寫進腳本。
4. 將已審查的 `docker-compose.pull.yml` 與腳本放進原部署目錄；先執行 Compose config 的靜態驗證，避免輸出機密。
5. 先人工執行一次，再安排 NAS 排程。建議獨立的 ezTicket 任務，不修改既有 04:00 的其他專案排程；待驗證後才決定是否整併。

排程命令範本（PROJECT_LABEL 必須以實際查到的值取代）：

```sh
cd /var/services/homes/github-runner/ezticket
export EZTICKET_COMPOSE_PROJECT='PROJECT_LABEL'
bash scripts/nas-pull-deploy.sh >> .deploy-state/deploy.log 2>&1
```

先建立 `.deploy-state` 並限制權限，另加日誌輪替。不要直接複製未填寫的範本啟用排程。

## 驗證與復原

- 網站首頁 HTTP 通過只代表基本可達；仍需本人測試登入與重要操作。
- 核對 MySQL／Redis 容器 ID 與啟動時間不變，既有資料仍可讀。
- 再執行一次應顯示 unchanged，app 不被重建。
- 切換前的本機 build 部署檔保留；若回復，使用原目錄、原 project，不執行 `down -v`。
- 五個離線 stub 測試已涵蓋 unchanged、app-only update、拉取失敗、健康失敗回復及 project 不符。尚未做真實 Docker build、GHCR 發布或 NAS 部署。

## 完全關閉公網的額外限制

程式碼存在綠界付款回呼 `/ecpay/return`、`/ecpay/Treturn`。若仍使用付款流程，完全關閉公網後外部支付系統無法直接回呼；必須先確認這些功能是否仍需要。此草稿不替它們開放例外入口。
