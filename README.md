
---

# ezTicket

ezTicket 是一個基於 Web 的簡單票券管理系統，屬於 CGA106_Group2 組員共同開發，旨在實現票券管理相關功能。

## 專案來源

此專案屬於 CGA106_Group2 組員共同開發 Project，主要用於學習與練習前端與後端的整合技術。

## 技術組成

本專案主要使用以下技術：

- **HTML (49.3%)**：用於網頁結構設計
- **Java (27.2%)**：負責後端邏輯處理
- **JavaScript (15.8%)**：實現前端互動
- **CSS (7.5%)**：進行樣式設計

## 技術細節

- **前端技術**：
  - 使用 **HTML** 負責網頁結構設計，確保語意化與可維護性。
  - 使用 **CSS**（含 Flexbox 與 Grid 技術）進行版面設計與樣式美化，支援響應式設計。
  - 使用 **JavaScript** 實現前端互動，包括表單驗證與即時更新。

- **後端技術**：
  - 使用 **Spring Boot** 作為核心後端框架，實現快速開發與模組化架構。
  - 整合 **Spring Data JPA** 與 **MySQL** 進行資料庫操作，提供簡潔且高效的 ORM 支援。
  - 提供 RESTful API，支援前後端分離架構與跨平台請求。

- **資料庫**：
  - 使用 **MySQL** 作為資料庫，設計高效結構化表格以儲存票券與使用者資料。
  - 支援多表關聯查詢，例如票券與使用者的關聯。

- **其他技術**：
  - **Lombok**：簡化 Java 程式碼中的 Getter/Setter 與建構子。
  - **GitHub**：進行版本控制與專案協作。

## 實作功能

- **票券管理**：
  - 創建、刪除、修改票券功能。
  - 支援票券狀態（已使用、未使用）標註與篩選。

- **使用者系統**：
  - 提供使用者登入與註冊功能。

- **查詢功能**：
  - 支援根據日期篩選票券。
  - 提供即時搜尋功能，快速查找所需票券。

## 技術架構圖

```
[Client (HTML/CSS/JavaScript)] <---> [Server (Spring Boot)] <---> [Database (MySQL)]
```

## 授權

本專案採用 [MIT License](https://opensource.org/licenses/MIT)。

---

