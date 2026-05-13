import { useState, lazy, Suspense } from 'react'
import { format } from 'sql-formatter'
const SqlEditor = lazy(() => import('./components/SqlEditor').then(m => ({ default: m.SqlEditor })))
import { DatasourceSelect } from './components/DatasourceSelect'
import { ResultTables } from './components/ResultTables'
import { QueryHistory } from './components/QueryHistory'
import { useQueryHistory } from './hooks/useQueryHistory'
import { useSavedQueries } from './hooks/useSavedQueries'
import { executeQuery } from './services/api'
import './App.css'

function App() {
  const [sql, setSql] = useState('')
  const [datasource, setDatasource] = useState('')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [sidebarOpen, setSidebarOpen] = useState(true)

  const { history, addEntry, removeEntry, clearHistory } = useQueryHistory()
  const { saved, saveQuery, removeQuery, clearSaved } = useSavedQueries()

  const handleFormat = () => {
    if (!sql.trim()) return
    try {
      const formatted = format(sql, { language: 'tsql', tabWidth: 4, keywordCase: 'upper' })
      setSql(formatted)
    } catch {
      // sql inválido — não faz nada
    }
  }

  const handleExecute = async () => {
    if (!datasource || !sql.trim() || loading) return

    setLoading(true)
    setError(null)
    setResult(null)

    const entry = {
      id: Date.now(),
      timestamp: Date.now(),
      datasource,
      sql: sql.trim(),
    }

    try {
      const data = await executeQuery(datasource, sql.trim())
      setResult(data)
      const rowCount = (data.results ?? [])
        .filter((r) => r.isResultSet)
        .reduce((sum, r) => sum + (r.rows_count ?? 0), 0)
      addEntry({ ...entry, success: true, rowCount })
    } catch (e) {
      const msg = e.response?.data?.erro || e.message || 'Erro desconhecido'
      setError(msg)
      addEntry({ ...entry, success: false, error: msg })
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    // F5 ou Ctrl+Enter para executar
    if (e.key === 'F5' || (e.ctrlKey && e.key === 'Enter')) {
      e.preventDefault()
      handleExecute()
    }
  }

  const handleSelectHistory = (entry) => {
    setSql(entry.sql)
    setDatasource(entry.datasource)
    setResult(null)
    setError(null)
  }

  const handleSave = () => {
    if (!sql.trim()) return
    saveQuery({
      id: Date.now(),
      timestamp: Date.now(),
      datasource,
      sql: sql.trim(),
    })
  }

  const canExecute = !loading && !!datasource && !!sql.trim()

  return (
    <div className="app" onKeyDown={handleKeyDown}>
      {/* ── Toolbar ── */}
      <header className="toolbar">
        <div className="toolbar-left">
          <svg className="app-icon" width="22" height="22" viewBox="0 0 22 22" fill="none">
            <rect x="2" y="3" width="18" height="16" rx="2" stroke="#6366f1" strokeWidth="1.5"/>
            <path d="M6 8h10M6 11h6M6 14h4" stroke="#6366f1" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <span className="app-title">Queries's App</span>
        </div>

        <div className="toolbar-center">
          <DatasourceSelect value={datasource} onChange={setDatasource} />
        </div>

        <div className="toolbar-right">
          <button
            className="btn btn-secondary"
            onClick={handleFormat}
            disabled={!sql.trim()}
            title="Formatar SQL (Alt+Shift+F)"
          >
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M2 3h10M2 7h6M2 11h8" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
            </svg>
            Formatar
          </button>

          <button
            className="btn btn-save"
            onClick={handleSave}
            disabled={!sql.trim()}
            title="Salvar query"
          >
            <svg width="13" height="14" viewBox="0 0 13 14" fill="none">
              <path d="M2 1h7l2 2v10H2V1z" stroke="currentColor" strokeWidth="1.4" strokeLinejoin="round"/>
              <rect x="4" y="1" width="5" height="3.5" rx="0.5" stroke="currentColor" strokeWidth="1.4"/>
              <path d="M4 8h5M4 10.5h3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
            </svg>
            Salvar
          </button>

          <button
            className="btn btn-primary"
            onClick={handleExecute}
            disabled={!canExecute}
            title="Executar (F5 ou Ctrl+Enter)"
          >
            {loading ? (
              <>
                <span className="btn-spinner" />
                Executando...
              </>
            ) : (
              <>
                <svg width="12" height="14" viewBox="0 0 12 14" fill="none">
                  <path d="M1 1l10 6-10 6V1z" fill="currentColor"/>
                </svg>
                Executar
              </>
            )}
          </button>

          <button
            className={`btn btn-icon ${sidebarOpen ? 'btn-icon-active' : ''}`}
            onClick={() => setSidebarOpen((v) => !v)}
            title="Alternar histórico"
          >
            <svg width="16" height="14" viewBox="0 0 16 14" fill="none">
              <rect x="1" y="1" width="14" height="12" rx="2" stroke="currentColor" strokeWidth="1.3"/>
              <path d="M11 1v12" stroke="currentColor" strokeWidth="1.3"/>
            </svg>
          </button>
        </div>
      </header>

      {/* ── Body ── */}
      <div className="body">
        {/* ── Área principal ── */}
        <main className="main">
          {/* Editor SQL */}
          <section className="editor-section">
            <div className="section-label">
              <span>Editor SQL</span>
              <span className="shortcut-hint">F5 ou Ctrl+Enter para executar</span>
            </div>
            <Suspense fallback={<div className="editor-loading">Carregando editor...</div>}>
              <SqlEditor value={sql} onChange={setSql} />
            </Suspense>
          </section>

          {/* Resultados */}
          <section className="results-section">
            <div className="section-label">
              <span>Resultados</span>
            </div>
            <ResultTables result={result} loading={loading} error={error} />
          </section>
        </main>

        {/* ── Sidebar histórico ── */}
        {sidebarOpen && (
          <QueryHistory
            history={history}
            onSelect={handleSelectHistory}
            onRemove={removeEntry}
            onClear={clearHistory}
            saved={saved}
            onSelectSaved={handleSelectHistory}
            onRemoveSaved={removeQuery}
            onClearSaved={clearSaved}
          />
        )}
      </div>
    </div>
  )
}

export default App
