# Todo List

單人使用的待辦事項管理系統，後端 Spring Boot + MySQL，前端 React + TypeScript。

## 技術棧

| 分類 | 技術 |
|---|---|
| 後端 | Spring Boot 4.1.1（Java 21）、Spring Data JPA、Spring Validation、springdoc-openapi（Swagger UI） |
| 前端 | React 19 + TypeScript、Vite、Bootstrap 5 |
| 資料庫 | MySQL 8（Docker Compose 或本機安裝皆可） |
| 建構工具 | Maven（含 wrapper）、npm |
| 測試 | 後端：JUnit + `@DataJpaTest`（H2）+ `@WebMvcTest`；前端：Vitest + React Testing Library |

## 專案結構

```
todo-list/
├── db/01-init.sql          # 資料庫初始化腳本（建 database、table、app 帳號）
├── docker-compose.yml      # MySQL + backend + frontend 三個服務
├── backend/                # Spring Boot（Maven），含 Dockerfile
└── frontend/               # React（Vite），含 Dockerfile
```

## 快速啟動（Docker Compose 一鍵跑全部）

不需要本機裝 Java / Node，只要有 Docker：

```bash
docker compose up -d --build
```

會依序建置並啟動三個容器：
- `mysql`：`localhost:3307`，自動執行 `db/01-init.sql` 初始化
- `backend`：`localhost:8080`（等 MySQL healthcheck 通過才會啟動）
- `frontend`：`localhost:5173`（nginx 靜態服務 production build）

改完程式碼後要套用變更，重新建置：

```bash
docker compose up -d --build
```

停止並移除容器（保留資料庫資料，`mysql-data` 是獨立 volume）：

```bash
docker compose down
```

## 環境需求（本機開發模式）

以下為不使用 Docker、想在本機直接跑 `npm run dev` / `mvn spring-boot:run` 做開發時需要的環境：

- Java 21
- Node.js 18+ / npm
- Docker Desktop（若用 Docker Compose 跑 MySQL）**或** 本機已安裝 MySQL 8

## 啟動步驟（本機開發模式）

### 1. 啟動 MySQL

擇一：

**方式 A：Docker Compose（建議）**

```bash
docker compose up -d
```

會在 `localhost:3307` 開一個 MySQL 8 服務，並自動執行 `db/01-init.sql` 建立：
- database `tododb`
- table `todo`
- 專用帳號 `todolist`（僅 `SELECT/INSERT/UPDATE/DELETE` 權限，不含 DDL）

**方式 B：本機 MySQL（`localhost:3306`）**

自行安裝 MySQL 8 後，手動執行 `db/01-init.sql` 建立上述 database / table / 帳號。

### 2. 設定後端資料庫密碼

後端連線設定透過 Spring profile 切換：

- `application.yaml`：非機密設定（URL、帳號），預設指向本機 `localhost:3306`
- `application-docker.yaml`：指向 Docker Compose 的 `localhost:3307`，密碼與 `db/01-init.sql` 一致（開發用固定值，可直接使用）
- `application-local.yaml`（需自建，已被 `.gitignore` 排除）：本機 MySQL 的真實密碼

若使用本機 MySQL，複製範本並填入密碼：

```bash
cd backend/src/main/resources
cp application-local.yaml.example application-local.yaml
# 編輯 application-local.yaml，填入本機 MySQL 的 todolist 帳號密碼
```

### 3. 啟動後端

```bash
cd backend

# 搭配 Docker Compose 的 MySQL（localhost:3307）
./mvnw spring-boot:run -Dspring-boot.run.profiles=docker

# 或搭配本機 MySQL（localhost:3306）
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Windows PowerShell 用 `.\mvnw.cmd` 取代 `./mvnw`。

啟動後：
- API base URL：`http://localhost:8080`
- Swagger UI：`http://localhost:8080/swagger-ui/index.html`

### 4. 啟動前端

```bash
cd frontend
npm install
cp .env.example .env   # 預設 VITE_API_BASE_URL=http://localhost:8080，一般不需修改
npm run dev
```

啟動後：`http://localhost:5173`

## 環境變數

| 位置 | 變數 | 說明 |
|---|---|---|
| `frontend/.env` | `VITE_API_BASE_URL` | 前端呼叫後端 API 的 base URL，預設 `http://localhost:8080` |
| `backend/src/main/resources/application-local.yaml` | `spring.datasource.password` | 本機 MySQL 的 `todolist` 帳號密碼（僅 `local` profile 需要，不進 git） |

## 測試

```bash
# 後端（H2 in-memory DB，不需要啟動 MySQL）
cd backend
./mvnw test

# 前端
cd frontend
npm run test
```

## API 規格

Base path：`/api/todos`

| Method | Path | 說明 | 成功回應 |
|---|---|---|---|
| GET | `/api/todos` | 查詢所有待辦事項，可選 `?completed=true/false` 篩選 | `200` + `Todo[]` |
| GET | `/api/todos/{id}` | 查詢單筆 | `200` + `Todo`，找不到則 `404` |
| POST | `/api/todos` | 新增（`title` 必填，`description` 可選） | `201` + `Todo` |
| PUT | `/api/todos/{id}` | 整筆更新（`title`/`description`/`completed` 皆需帶入） | `200` + `Todo`，找不到則 `404` |
| PATCH | `/api/todos/{id}/toggle` | 切換完成狀態 | `200` + `Todo`，找不到則 `404` |
| DELETE | `/api/todos/{id}` | 刪除 | `204`，找不到則 `404` |

**Todo 物件**

```json
{
  "id": 1,
  "title": "買牛奶",
  "description": "全脂",
  "completed": false,
  "createdAt": "2026-09-09T07:38:43Z",
  "updatedAt": "2026-09-09T07:38:43Z"
}
```

**錯誤回應格式**（`404` / `400` 皆同一格式）

```json
{
  "timestamp": "2026-09-09T07:38:43Z",
  "status": 404,
  "error": "Not Found",
  "message": "Todo not found with id: 999"
}
```

驗證規則：`title` 不可為空白（`@NotBlank`）、最長 100 字元。

完整互動式文件見啟動後端後的 Swagger UI：`http://localhost:8080/swagger-ui/index.html`。

## CORS

後端預設只允許 `http://localhost:5173` 呼叫 `/api/**`（本機開發模式的 Vite dev server、Docker Compose 的 frontend 容器都映射到這個 host port，瀏覽器看到的 Origin 相同，不需另外調整）。
