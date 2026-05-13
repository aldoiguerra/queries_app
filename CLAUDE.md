# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Queries App** is a SQL query runner deployed as a Fluig (TOTVS) widget. It lets authenticated Fluig users execute ad-hoc SQL against JNDI datasources registered in the Fluig platform.

The project has two sub-projects combined into a single WAR:

- `frontend/` — React 19 + Vite SPA (SQL editor UI)
- `backend/` — Java 11 + JAX-RS REST API, packaged as `queries_app.war`

## Commands

### Frontend (run from `frontend/`)

```bash
npm run dev      # Vite dev server with hot reload
npm run build    # Build SPA, copy to backend/src/main/webapp/, then mvn clean install
npm run lint     # ESLint
npm run preview  # Preview the production build locally
```

### Backend only (run from `backend/`)

```bash
export JAVA_HOME=`/usr/libexec/java_home -v 11`
mvn clean install   # Compile and package the WAR
```

There are no automated tests.

## Build & Deploy Flow

`npm run build` in `frontend/` does three things in sequence:
1. Vite builds the SPA into `frontend/dist/`
2. Copies `dist/*` into `backend/src/main/webapp/` (static assets embedded in the WAR)
3. Maven builds `backend/target/queries_app.war`

The WAR is deployed to the Fluig (WildFly/JBoss) server.

## Architecture

### Frontend

All application state lives in `App.jsx`. Key flows:

- **Datasource list**: `DatasourceSelect` fetches `GET /queries_app/rest/query/datasources` on mount.
- **SQL execution**: `App.jsx` → `executeQuery()` in `services/api.js` → `POST /queries_app/rest/query/executar`.
- **Results**: `ResultTables` renders multi-result responses (handles multiple result sets and update counts from one execution).
- **Persistence**: `useQueryHistory` and `useSavedQueries` persist to `localStorage` under keys `qexec_history` and `qexec_saved`.
- **SqlEditor** is lazy-loaded (CodeMirror, T-SQL dialect, One Dark theme).

Keyboard shortcuts: **F5** or **Ctrl+Enter** to execute.

Two critical Vite settings for Fluig compatibility:
- `base: "/queries_app/"` — SPA must resolve assets from Fluig's context path.
- Fixed output filenames (no content hashes) in `rollupOptions` — Fluig cannot handle hashed filenames.

### Backend

REST base path: `/queries_app/rest/` (via `@ApplicationPath("/rest")` in `ApplicationConfig`).

| Method | Path | Description |
|--------|------|-------------|
| GET | `/query/ping` | Health check |
| GET | `/query/datasources` | Lists SQL Server datasources from Fluig's `SERV_DADOS` table |
| POST | `/query/executar` | Executes raw SQL; returns all result sets and update counts |
| POST | `/query/consultar/{lista\|dataset}` | Legacy query endpoint with optional `QueryBuilder` |

Database connections use JNDI lookup `java:/jdbc/<datasource>` via `javax.naming.InitialContext`. No connection pool config in the app — it relies entirely on datasources configured in Fluig.

`Query.executar()` loops through `stmt.getMoreResults()` to capture every result set and update count from a single SQL string, enabling multi-statement batch execution.

### Local Development

To proxy API calls to a running Fluig server during frontend `dev`, set `VITE_API_URL` in a `.env.local` file (see `.env.example`) and uncomment the proxy block in `vite.config.js`.
