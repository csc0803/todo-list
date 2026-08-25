# 專案進度 (PROGRESS)

最後更新：2026-08-24

## 目前狀態
規劃階段。技術棧與核心決策已確認，完整分階段計畫如下，尚未開始建立專案骨架。

## 專案結構（規劃）
```
todo-list/
├── PROMPT.md
├── PROGRESS.md
├── docker-compose.yml       # MySQL
├── backend/                 # Spring Boot (Maven)
└── frontend/                # React (Vite)
```

---

## Phase 0：專案初始化與環境準備
- [x] 確認技術棧：Spring Boot + React + MySQL
- [x] 確認不需要登入功能（單人使用）
- [x] 確認後端建構工具使用 Maven
- [x] 確認 MySQL 以 Docker Compose 執行
- [x] 檢查本機開發環境（Java 21、Node 24、npm 11、Docker 29 均已就緒；PATH 中無 mvn，改用 IntelliJ 內建 Maven 或 Maven Wrapper）
- [x] 建立 PROMPT.md / PROGRESS.md 規劃文件
- [x] 確認 Spring Boot 版本 → 4.1.1（backend/pom.xml 已產生，Java 21，與預設假設相符）
- [x] 初始化 git repository
- [x] 確認前端語言 → **TypeScript**（使用者已於 PROMPT.md 更新）
- [x] 補上根目錄 `.gitignore`（涵蓋 Maven target/、Node node_modules・dist、.env、IDE、OS、log 檔）
- [x] 建立第一個 git commit（`c00442b Project init`，working tree 乾淨）
- [x] 本地與遠端分支統一為 `main`（GitHub 預設分支已改為 main，遠端舊 master 已刪除，origin: `github.com/csc0803/todo-list`）

> 註：backend/ 骨架已存在（Spring Boot 4.1.1、groupId `com.chun`、artifactId `backend`，套件路徑 `com.chun.backend`），推測是使用者已自行透過 Spring Initializr 產生，與 PROMPT.md 原規劃的 `com.example` / `todolist` 命名不同。此為 Phase 1 的工作提前完成，細節將在確認命名後同步更新 Phase 1 清單。

## Phase 1：後端骨架建立
- [x] 用 Spring Initializr 產生 backend/ 專案（已存在：groupId `com.chun`、artifactId `backend`，Spring Boot 4.1.1，依賴含 Web、Data JPA、Validation、MySQL Driver、Lombok）
- [x] 確認專案可用 Maven（或 wrapper）成功編譯（`.\mvnw.cmd clean compile` → BUILD SUCCESS）
- [x] 建立 DB script（`db/01-init.sql`）：建立 `tododb` database、`todo` table，並建立專用 app 帳號 `todolist`（僅 SELECT/INSERT/UPDATE/DELETE，不含 DDL 權限，不用 root 連線），已用本機 MySQL 實際執行驗證通過（`SHOW TABLES` / `DESCRIBE todo` 結構正確）
- [x] 設定 `application.yml`：資料庫連線資訊避免寫死密碼，改用 Spring profile 機制隔離機密設定。最終採用方案：
  - `application.yaml`（進 git）：只放非機密設定（URL、username、driver-class-name），指向本機 MySQL `localhost:3306`
  - `application-local.yaml.example`（進 git）：範本檔，標示要複製的欄位（目前僅 `spring.datasource.password`）
  - `application-local.yaml`（`.gitignore` 排除，實際生效檔）：放本機真實密碼
  - IntelliJ Run Configuration 的 **Active profiles** 欄位設為 `local`，啟動時自動套用上述 override
  - 過程中曾嘗試環境變數（`${DB_PASSWORD}` + IntelliJ 手動填 env var）與 EnvFile plugin 兩種方案，後改用上述 profile 方式，不依賴 IDE plugin，`mvn spring-boot:run` / CI 也能一致運作
- [x] **本地端連線測試**：透過 IntelliJ（`Active profiles=local`）啟動後端，成功連上本機 MySQL、無連線錯誤
- [x] 建立 `docker-compose.yml`（MySQL 服務、named volume `mysql-data`、掛載 `./db` 做初始化、healthcheck）。本機原生 MySQL 已佔用 3306，改對外開 3307（`localhost:3307`）避免衝突。已用 `docker compose up -d` 驗證：container 顯示 `healthy`，init script 有正確執行
- [x] 把 `application.yml` 的連線目標切換回 docker 版 MySQL，啟動 MySQL container 並確認後端可連線：新增 `application-docker.yaml`（url 指向 `localhost:3307`、密碼 `todolist_local_dev_only`，跟 `db/01-init.sql` 一致，內容非個人機密可直接進 git），IntelliJ Active profiles 切成 `docker`，實測連線成功

## Phase 2：後端資料層
- [x] 建立 `Todo` entity（id, title, description, completed, createdAt, updatedAt）：`com.chun.backend.entity.Todo`，用 Lombok `@Getter @Setter`，`@Table(name = "todo")` 明確對應 table 名稱，`description` 用 `@Column(columnDefinition = "TEXT")`（原本用 `@Lob` 在 MySQL 上會被 Hibernate 推斷成 `tinytext`，跟 SQL script 建的 `TEXT` 對不上，已修正）。`@PrePersist`/`@PreUpdate` 自動補 `createdAt`/`updatedAt`/`completed` 預設值，避免存檔時 NOT NULL 欄位為 null
- [x] 建立 `TodoRepository`（extends JpaRepository）：`JpaRepository<Todo, Long>`（曾誤寫成 `JpaRepository<Integer, Todo>`，泛型參數順序相反且型別錯誤，已修正）
- [x] 設定 `spring.jpa.hibernate.ddl-auto`：改採 **`validate`**（非原計畫的 `update`），統一放在 base `application.yaml`、`local`/`docker` profile 共用。理由：schema 已有 `db/01-init.sql` 當唯一事實來源，`validate` 只檢查 entity 與現有 DB 是否一致、不會自動改表，避免 schema 分岔；未來若導入 Flyway/Liquibase 再視情況調整
- [x] 驗證資料表對應正確：以 `docker` profile 實際啟動一次（`mvn spring-boot:run`），Hibernate `validate` 通過（過程中先抓到並修正上述 `description` 型別不符的問題），確認 entity 與既有 `todo` table 完全對應

## Phase 3：後端 API 層
- [ ] 建立 `TodoController`，實作 REST API：
  - [ ] `GET /api/todos`（查全部，支援依 completed 篩選，可選）
  - [ ] `GET /api/todos/{id}`（查單筆）
  - [ ] `POST /api/todos`（新增）
  - [ ] `PUT /api/todos/{id}`（更新）
  - [ ] `PATCH /api/todos/{id}/toggle`（切換完成狀態）
  - [ ] `DELETE /api/todos/{id}`（刪除）
- [ ] 建立 `TodoService`（商業邏輯與 Repository 之間的中介層）
- [ ] 加入輸入驗證（title 不可為空、長度限制等，使用 `@Valid`）
- [ ] 建立統一錯誤處理（`@ControllerAdvice`，404 / 400 回應格式一致）
- [ ] 設定 CORS，允許前端開發伺服器（例如 http://localhost:5173）呼叫 API
- [ ] 手動用 curl / Postman 測試所有 API 端點

## Phase 4：後端測試
- [ ] Repository 層測試（`@DataJpaTest`，可選用 H2 in-memory DB 加速）
- [ ] Controller 層測試（`@WebMvcTest` + MockMvc，涵蓋成功與錯誤情境）
- [ ] 確認 `mvn test` 全數通過

## Phase 5：前端骨架建立
- [ ] 用 Vite 建立 frontend/ 專案（React + 選定語言）
- [ ] 確認 `npm install` / `npm run dev` 可正常啟動
- [ ] 建立基本目錄結構（components/、api/、hooks/ 或依實際需求調整）
- [ ] 設定 `.env` / `.env.example`（`VITE_API_BASE_URL`）

## Phase 6：前端 UI 元件
- [ ] `TodoList`：顯示所有 todo
- [ ] `TodoItem`：單筆項目（checkbox 切換完成、標題、刪除按鈕）
- [ ] `AddTodoForm`：新增 todo 的輸入表單
- [ ] 編輯功能（點擊標題進入編輯模式，儲存/取消）
- [ ] 空狀態（無 todo 時的提示畫面）
- [ ] 基本樣式（CSS，簡潔可用即可，不追求視覺設計）

## Phase 7：前端 API 串接
- [ ] 建立 API service 模組（封裝 fetch，統一處理 base URL 與錯誤）
- [ ] 串接 `GET /api/todos`：頁面載入時抓取清單
- [ ] 串接 `POST /api/todos`：新增後更新畫面
- [ ] 串接 `PATCH /toggle`：切換完成狀態
- [ ] 串接 `PUT /api/todos/{id}`：編輯儲存
- [ ] 串接 `DELETE /api/todos/{id}`：刪除
- [ ] Loading / Error 狀態處理（API 失敗時顯示提示，不讓畫面白屏）

## Phase 8：前端測試（可選，視時間決定是否納入第一版）
- [ ] 設定測試工具（Vitest + React Testing Library）
- [ ] 針對關鍵元件（AddTodoForm、TodoItem）撰寫基本測試

## Phase 9：整合與端對端驗證
- [ ] 同時啟動 MySQL（docker compose up）、後端（mvn spring-boot:run）、前端（npm run dev）
- [ ] 手動測試主要流程：新增 → 顯示 → 切換完成 → 編輯 → 刪除
- [ ] 確認重新整理頁面後資料仍保留在 MySQL 中（驗證持久化）
- [ ] 檢查瀏覽器 console 無錯誤、CORS 正常

## Phase 10：文件
- [ ] 撰寫 README.md：專案簡介、技術棧、啟動步驟（含 docker-compose、後端、前端三部分）
- [ ] 記錄環境變數需求（`.env.example`）
- [ ] 記錄 API 規格（可用簡易表格或 Postman collection）

## Phase 11（Stretch）：容器化與一鍵啟動
- [ ] 撰寫 backend Dockerfile（multi-stage build）
- [ ] 撰寫 frontend Dockerfile（build + nginx 靜態服務，或 vite preview）
- [ ] 擴充 docker-compose.yml，納入 backend + frontend + MySQL 三個服務
- [ ] 驗證 `docker compose up` 即可讓整個系統可用

## Phase 12（Stretch）：進階功能
- [ ] 截止日期（due date）與逾期提示
- [ ] 優先順序（priority）
- [ ] 分類 / 標籤
- [ ] 搜尋與篩選（依關鍵字、狀態、標籤）
- [ ] 排序（依建立時間、截止日期、優先順序）
- [ ] 分頁（若資料量變大）

---

## 下一步（建議立即執行）
- [ ] 確認 Phase 0 剩餘兩項決策（前端語言、Spring Boot 版本），或直接採用預設值開始 Phase 1

## 備註
- 目前工作目錄尚未初始化為 git repository。
- 正式環境不應使用 `ddl-auto=update`，建議之後導入 Flyway/Liquibase 做 schema migration（已列入 Phase 2 備註，暫不影響第一版開發）。
