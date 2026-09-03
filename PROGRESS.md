# 專案進度 (PROGRESS)

最後更新：2026-09-03

## 目前狀態
Phase 0～7 已完成（環境準備、後端骨架、資料層、API 層、後端測試、前端骨架、前端 UI 元件、前端 API 串接）。後端 CRUD API 已可用，Repository/Controller 層共 27 個測試 + `contextLoads` 全數通過（`mvn test` BUILD SUCCESS，28 個測試）。前端已完整接上後端：`todoApi.ts` 封裝所有 API 呼叫，`App.tsx` 串接新增/切換/編輯/刪除/loading/error，已用瀏覽器手動整合測試（含重新整理驗證持久化、後端斷線驗證錯誤處理）全數通過。下一步進入 Phase 8（前端測試，可選）或直接跳到 Phase 9（整合與端對端驗證，其中部分項目已在 Phase 7 手動測試涵蓋）。

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
- [x] 建立 `TodoService`（`com.chun.backend.service.TodoService` interface + `TodoServiceImpl`）：`getAllTodos()` / `getAllTodos(Boolean completed)` / `getTodoById` / `createTodo` / `updateTodo` / `toggleCompleted` / `deleteTodo`。查無資料統一丟 `TodoNotFoundException`（`com.chun.backend.exception`），不在 Service 內處理 HTTP status
  - 過程中修正兩個問題：`@Service` 一開始誤放在 interface 上（應放在 `TodoServiceImpl`）；`updateTodo`/`toggleCompleted` 曾用 try-catch 把 `TodoNotFoundException` 包成普通 `RuntimeException`，導致之後 `@ExceptionHandler` 抓不到型別，已移除該 try-catch
- [x] 建立 `TodoController`，實作 REST API：
  - [x] `GET /api/todos`（查全部，`?completed=true/false` 可選篩選）
  - [x] `GET /api/todos/{id}`（查單筆）
  - [x] `POST /api/todos`（新增）
  - [x] `PUT /api/todos/{id}`（更新）
  - [x] `PATCH /api/todos/{id}/toggle`（切換完成狀態）
  - [x] `DELETE /api/todos/{id}`（刪除）
- [x] 加入輸入驗證（`@Valid` + entity 上的 Bean Validation annotation）
- [x] 建立統一錯誤處理：`GlobalExceptionHandler`（`@RestControllerAdvice`），攔截 `TodoNotFoundException` → 404、`MethodArgumentNotValidException` → 400，統一回傳 `{timestamp, status, error, message}` 格式
- [x] 設定 CORS：`CorsConfig`（`WebMvcConfigurer`），允許 `http://localhost:5173` 呼叫 `/api/**`
- [x] 加入 Swagger（`springdoc-openapi-starter-webmvc-ui` 3.0.3，對應 Spring Boot 4 / Spring Framework 7），取代手動 curl/Postman 測試。啟動後可在 `http://localhost:8080/swagger-ui/index.html` 互動測試所有 endpoint
- [x] 手動測試所有 API 端點（用 curl 對已啟動的後端逐一驗證），過程中發現並修正兩個 entity 層的 bug：
  - `Todo` entity 的 `completed`/`createdAt`/`updatedAt` 原本標了 `@NotNull`，但 `@Valid` 驗證在 `@PrePersist` 自動補值**之前**就執行，導致 `POST`/`PUT` 若不手動帶這三個欄位會被擋在 400；也連帶讓 `PUT /api/todos/{不存在的id}` 因為先卡在驗證，永遠拿不到預期的 404。已移除這三個欄位的 `@NotNull`（DB 端 `NOT NULL` + `@ColumnDefault` 已足夠保證非空，`@PrePersist`/`@PreUpdate` 保證存檔前一定有值）
  - `title` 原本只有 `@NotNull` + `@Size(max=100)`，擋不住空字串／純空白字串。已改成 `@NotBlank` + `@Size(max=100)`
  - 修正後重測：`POST`（僅帶 title/description）、`PUT`（含對不存在 id 回 404）、`PATCH toggle`、`DELETE`、`GET`（含 404）、驗證邊界情況（空 title / 純空白 title / 缺 title）全部通過

## Phase 4：後端測試
- [x] Repository 層測試（`@DataJpaTest` + H2 in-memory DB，`application-test.yaml` 設定 `jdbc:h2:mem:tododb;MODE=MySQL`、`ddl-auto=create-drop`）：`TodoRepositoryTest`，11 個測試，涵蓋 save（含 `@PrePersist` 預設值、`@NotBlank`/`@Size` 違反）、findById（含清 persistence context 重查、查無資料）、findAll、update（含 `createdAt` 不變/`updatedAt` 有更新）、findAllByCompleted、deleteById（含刪不存在的 id 靜默跳過）
- [x] Controller 層測試（`@WebMvcTest` + MockMvc + `@MockitoBean` mock `TodoService`）：`TodoControllerTest`，16 個測試，涵蓋 6 個 endpoint 的成功案例、404（mock service 丟 `TodoNotFoundException`）、400（bean validation 失敗、JSON 格式壞掉、缺必填欄位），並用 `verify`/`verifyNoMoreInteractions`/`verifyNoInteractions` 確認 Controller 與 Service 的互動（驗證失敗時不該呼叫 Service，找不到 id 時該呼叫 Service 一次）
- [x] 確認 `mvn test` 全數通過：修正 Spring Initializr 預設產生的 `BackendApplicationTests`（`@SpringBootTest` 沒指定 profile，會套用預設 `application.yaml` 連本機真實 MySQL，環境沒開 DB 就會 context load 失敗）→ 補上 `@ActiveProfiles("test")`，比照 Repository 測試改走 H2。全數 28 個測試（11 + 16 + 1）通過

## Phase 5：前端骨架建立
- [x] 用 Vite 建立 frontend/ 專案（`npm create vite@latest frontend -- --template react-ts`，React 19 + TypeScript）
- [x] 確認 `npm install` / `npm run dev` 可正常啟動：`node_modules` 已安裝，實測 `npm run dev` 330ms 內啟動，`http://localhost:5180/` 正常回應
- [x] 建立基本目錄結構：`src/api`、`src/components`、`src/hooks`
- [x] 設定 `.env` / `.env.example`：皆為 `VITE_API_BASE_URL=http://localhost:8080`。`.env` 已被根目錄 `.gitignore` 的 `.env` 規則排除（不進 git，該規則對所有子目錄深度皆生效），`.env.example` 進 git 當範本

## Phase 6：前端 UI 元件（已完成）
- [x] `TodoList`：顯示所有 todo（`src/components/TodoList.tsx`，純顯示元件，`todos` 由外部傳入，本身不 fetch 資料，`onToggle`/`onDelete`/`onUpdate` 回呼往上層傳）。原本 `.map()` 內 `<TodoItem />` 後面多了一個分號（`/>;`）導致編譯不過，已修掉
- [x] `TodoItem`：單筆項目（checkbox 切換完成、標題、刪除按鈕）（`src/components/TodoItem.tsx`，checkbox 為 controlled component，直接綁 `todo.completed`，不自己維護 local state，避免跟未來接上的 API 狀態不同步）
- [x] `AddTodoForm`：新增 todo 的輸入表單（`src/components/AddTodoForm.tsx`）。`useState` 管理輸入中的 title、送出時 `trim()` + 檢查非空、呼叫 `onAdd(title)`、送出後清空欄位。初版有兩個小 bug：`<input>` 沒接 `value`/`onChange`（變成 uncontrolled）、按鈕是 `type="button"` 導致點了不會觸發 `onSubmit`，加上漏寫 `export default`，皆已修正
- [x] 編輯功能（點擊標題進入編輯模式，儲存/取消）— 依討論的設計方向實作：不與 `AddTodoForm` 共用，在 `TodoItem` 內自建 `isEditing`/`editValue` local state 做行內編輯。點標題（非編輯中）切換成 `<input>`，「儲存」`trim()` 後非空才呼叫 `onUpdate(id, title)` 並結束編輯模式，「取消」還原成 `todo.title` 並結束編輯模式
- [x] 空狀態（無 todo 時的提示畫面）— 已包在 `TodoList` 內處理（`todos.length === 0` 時顯示「目前沒有待辦事項」）
- [x] 基本樣式 — 改用 **Bootstrap 5**（`npm install bootstrap`，`main.tsx` 引入 `bootstrap/dist/css/bootstrap.min.css`），而非原計畫的手刻 CSS：`App.css`/`index.css` 已清空 Vite 預設樣式並保持空白，元件改用 Bootstrap class（`list-group`/`list-group-item`、`form-control`/`form-check-input`、`btn btn-primary`/`btn-outline-danger` 等）

> 註：`TodoList`/`TodoItem`/`AddTodoForm` 已接進 `App.tsx`：`App` 用 `useState<Todo[]>` 管理本地 todos 清單，`handleAdd`/`handleToggle`/`handleDelete`/`handleUpdate` 對應傳給三個元件。這是暫時的本地 state，尚未呼叫後端 API（那是 Phase 7 的範圍）。`src/api/types.ts` 已建立 `Todo` 型別定義（尚未有實際 fetch 邏輯）。`tsc -b --noEmit` 已驗證無型別錯誤，`vite build` 可正常打包。

## Phase 7：前端 API 串接（已完成）
- [x] 建立 API service 模組（`frontend/src/api/todoApi.ts`）：統一 `request<T>()` 封裝 fetch，base URL 讀 `VITE_API_BASE_URL`，非 2xx 回應解析後端 `{timestamp, status, error, message}` 格式並丟出自訂 `ApiError`（帶 `status`），204 回應回傳 `undefined`
- [x] 串接 `GET /api/todos`：`App.tsx` 用 `useEffect` 在頁面載入時呼叫 `getTodos()` 填入 `todos` state
- [x] 串接 `POST /api/todos`：`handleAdd` 呼叫 `createTodo`，成功後把後端回傳的完整 todo（含 id/createdAt）append 進畫面
- [x] 串接 `PATCH /toggle`：`handleToggle` 呼叫 `toggleTodo`，用回傳結果替換該筆
- [x] 串接 `PUT /api/todos/{id}`：`handleUpdate` 儲存。過程中修正一個設計問題：PUT 是整筆替換，原本只傳新 `title` 字串會漏掉 `description`/`completed` 導致型別不合、語意也不對（等於用 undefined 覆蓋掉這兩欄）。改成 `TodoItem` 编辑時把自己手上已有的 `todo.description`/`todo.completed` 一起透過 `onUpdate(id, title, description, completed)` 往上傳（`TodoItem` → `TodoList` → `App`），`App` 端直接組成 `TodoUpdateInput` 送出，不需要額外呼叫 `getTodo` 補資料，省一次來回
- [x] 串接 `DELETE /api/todos/{id}`：`handleDelete` 呼叫後從畫面移除該筆
- [x] Loading / Error 狀態處理：`isLoading` 控制初次載入畫面（顯示「載入中...」，不會白屏空畫面）；`error` 顯示紅字提示（新增/更新/刪除/載入各自有對應訊息），每次操作開始時先 `setError(null)` 清掉舊錯誤，避免舊錯誤一直卡著

> 手動整合測試（`docker` MySQL 未啟動，改用本機 MySQL80 服務 + `local` profile 啟動後端、`npm run dev` 啟動前端，透過瀏覽器實測）：新增 → 顯示 → 勾選完成（打勾即 strikethrough，用 computed style 確認 `text-decoration-line-through` 有生效，畫面上因線很細不易用截圖肉眼辨識）→ 行內編輯（含編輯後 `completed` 狀態不會被覆蓋掉）→ 重新整理頁面資料仍在（驗證有寫進 MySQL）→ 刪除 → 空狀態正確顯示，全部通過。另外手動把後端 process 砍掉測試錯誤情境：新增失敗時畫面顯示「新增失敗」紅字且不會白屏，恢復後端後新增立即恢復正常、舊錯誤訊息也正確被蓋掉。

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
- [ ] Phase 8（可選）：補前端測試（Vitest + React Testing Library），或直接跳到 Phase 9 整合驗證收尾

## 備註
- 正式環境不應使用 `ddl-auto=update`，建議之後導入 Flyway/Liquibase 做 schema migration（已列入 Phase 2 備註，暫不影響第一版開發）。
