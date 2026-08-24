# 專案需求 (PROMPT)

## 目標
建立一個 Todo List 網頁應用程式。

## 技術棧
- 後端：Spring Boot (Java)
- 前端：React
- 資料庫：MySQL

## 已確認的決策
| 項目 | 決策 |
|---|---|
| 使用者登入功能 | 不需要，單人使用（無帳號/註冊/登入） |
| 後端建構工具 | Maven |
| MySQL 執行方式 | Docker Compose |

## 預設假設（尚未經使用者明確確認，如有不同想法可隨時調整）
| 項目 | 預設決策 | 說明 |
|---|---|---|
| 前端語言 | TypeScript | React |
| Spring Boot 版本 | 使用 Spring Initializr 當前預設最新穩定版 | 本機 Java 21 相容 |
| Todo 資料欄位 | id, title, description, completed, createdAt, updatedAt | 基本欄位，之後可擴充 |
| 進階欄位（分類/標籤/截止日期/優先順序） | 不在第一版範圍內 | 列為 Phase 12 stretch goal |
| 部署方式 | 僅本機開發環境（Docker Compose 只跑 MySQL，後端/前端本機執行） | 全部 Docker 化列為 stretch goal |

完整分階段任務規劃請見 [PROGRESS.md](./PROGRESS.md)。

## 本機環境資訊
- Java: 21.0.11
- Node: v24.13.1
- npm: 11.8.0
- Docker: 29.4.2
- Maven: 未安裝於 PATH，但可用 IntelliJ 內建 Maven，或改用 Maven Wrapper (mvnw)
