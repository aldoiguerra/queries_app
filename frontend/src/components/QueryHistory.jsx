function formatDate(timestamp) {
  const d = new Date(timestamp)
  return d.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function truncate(str, max = 80) {
  if (!str) return ''
  const single = str.replace(/\s+/g, ' ').trim()
  return single.length > max ? single.slice(0, max) + '…' : single
}

function HistoryItem({ entry, onSelect, onRemove }) {
  return (
    <li className={`history-item ${entry.success ? '' : 'history-item-error'}`}>
      <button
        className="history-item-body"
        onClick={() => onSelect(entry)}
        title="Clique para carregar esta query"
      >
        <div className="history-item-meta">
          <span className={`history-status ${entry.success ? 'status-ok' : 'status-err'}`}>
            {entry.success ? '✓' : '✗'}
          </span>
          <span className="history-ds">{entry.datasource}</span>
          <span className="history-time">{formatDate(entry.timestamp)}</span>
        </div>
        <p className="history-sql">{truncate(entry.sql)}</p>
        {entry.success && entry.rowCount !== undefined && (
          <span className="history-rows">{entry.rowCount} linha{entry.rowCount !== 1 ? 's' : ''}</span>
        )}
        {!entry.success && entry.error && (
          <span className="history-err-msg">{truncate(entry.error, 60)}</span>
        )}
      </button>
      <button
        className="history-item-remove"
        onClick={(e) => { e.stopPropagation(); onRemove(entry.id) }}
        title="Remover"
      >
        ×
      </button>
    </li>
  )
}

function SavedItem({ entry, onSelect, onRemove }) {
  return (
    <li className="saved-item">
      <button
        className="history-item-body"
        onClick={() => onSelect(entry)}
        title="Clique para carregar esta query"
      >
        <div className="history-item-meta">
          <svg className="saved-icon" width="11" height="12" viewBox="0 0 13 14" fill="none">
            <path d="M2 1h7l2 2v10H2V1z" stroke="currentColor" strokeWidth="1.4" strokeLinejoin="round"/>
            <rect x="4" y="1" width="5" height="3.5" rx="0.5" stroke="currentColor" strokeWidth="1.4"/>
            <path d="M4 8h5M4 10.5h3" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
          </svg>
          {entry.datasource && <span className="history-ds">{entry.datasource}</span>}
          <span className="history-time">{formatDate(entry.timestamp)}</span>
        </div>
        <p className="history-sql">{truncate(entry.sql)}</p>
      </button>
      <button
        className="history-item-remove"
        onClick={(e) => { e.stopPropagation(); onRemove(entry.id) }}
        title="Remover query salva"
      >
        ×
      </button>
    </li>
  )
}

export function QueryHistory({
  history, onSelect, onRemove, onClear,
  saved, onSelectSaved, onRemoveSaved, onClearSaved,
}) {
  return (
    <aside className="sidebar">
      {/* ── Seção Histórico ── */}
      <div className="sidebar-section sidebar-history">
        <div className="sidebar-header">
          <h2 className="sidebar-title">
            Histórico
            {history.length > 0 && <span className="history-count">{history.length}</span>}
          </h2>
          {history.length > 0 && (
            <button className="btn-clear" onClick={onClear} title="Limpar histórico">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              </svg>
            </button>
          )}
        </div>

        {history.length === 0 ? (
          <div className="sidebar-empty">
            <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
              <circle cx="16" cy="16" r="14" stroke="#4a4e69" strokeWidth="1.5"/>
              <path d="M16 9v7.5l4 2" stroke="#4a4e69" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            <p>Nenhuma consulta executada ainda</p>
          </div>
        ) : (
          <ul className="history-list">
            {history.map((entry) => (
              <HistoryItem key={entry.id} entry={entry} onSelect={onSelect} onRemove={onRemove} />
            ))}
          </ul>
        )}
      </div>

      {/* ── Divisor ── */}
      <div className="sidebar-divider" />

      {/* ── Seção Queries Salvas ── */}
      <div className="sidebar-section sidebar-saved">
        <div className="sidebar-header">
          <h2 className="sidebar-title">
            Salvas
            {saved.length > 0 && <span className="history-count saved-count">{saved.length}</span>}
          </h2>
          {saved.length > 0 && (
            <button className="btn-clear" onClick={onClearSaved} title="Limpar queries salvas">
              <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                <path d="M2 2l10 10M12 2L2 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
              </svg>
            </button>
          )}
        </div>

        {saved.length === 0 ? (
          <div className="sidebar-empty">
            <svg width="28" height="28" viewBox="0 0 13 14" fill="none">
              <path d="M2 1h7l2 2v10H2V1z" stroke="#4a4e69" strokeWidth="1.4" strokeLinejoin="round"/>
              <rect x="4" y="1" width="5" height="3.5" rx="0.5" stroke="#4a4e69" strokeWidth="1.4"/>
              <path d="M4 8h5M4 10.5h3" stroke="#4a4e69" strokeWidth="1.4" strokeLinecap="round"/>
            </svg>
            <p>Nenhuma query salva ainda</p>
          </div>
        ) : (
          <ul className="history-list">
            {saved.map((entry) => (
              <SavedItem key={entry.id} entry={entry} onSelect={onSelectSaved} onRemove={onRemoveSaved} />
            ))}
          </ul>
        )}
      </div>
    </aside>
  )
}
