import { useState } from 'react'
import * as XLSX from 'xlsx'

// ── Export helpers ──────────────────────────────────────────────────────────

function rawValue(v) {
  return v === null || v === undefined ? '' : v
}

function cellToCSV(v) {
  if (v === null || v === undefined) return ''
  const s = String(v)
  return s.includes(',') || s.includes('"') || s.includes('\n') || s.includes('\r')
    ? `"${s.replace(/"/g, '""')}"`
    : s
}

function downloadBlob(content, filename, type) {
  const blob = new Blob([content], { type })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function exportCSV(resultSets) {
  resultSets.forEach((item, idx) => {
    const { fields, records } = item.result
    const header = fields.map(f => cellToCSV(f.name)).join(',')
    const rows = records.map(row => row.map(cellToCSV).join(','))
    const csv = '﻿' + [header, ...rows].join('\r\n')
    const suffix = resultSets.length > 1 ? `_${idx + 1}` : ''
    downloadBlob(csv, `resultado${suffix}.csv`, 'text/csv;charset=utf-8')
  })
}

function exportExcel(resultSets) {
  const wb = XLSX.utils.book_new()
  resultSets.forEach((item, idx) => {
    const { fields, records } = item.result
    const data = [fields.map(f => f.name), ...records.map(row => row.map(rawValue))]
    const ws = XLSX.utils.aoa_to_sheet(data)
    XLSX.utils.book_append_sheet(wb, ws, `Resultado ${idx + 1}`)
  })
  XLSX.writeFile(wb, 'resultado.xlsx')
}

// ── Render helpers ──────────────────────────────────────────────────────────

function formatValue(value) {
  if (value === null || value === undefined) return <span className="cell-null">NULL</span>
  if (typeof value === 'boolean') return <span className="cell-bool">{String(value)}</span>
  return String(value)
}

function ResultTable({ fields, records }) {
  const [page, setPage] = useState(0)
  const pageSize = 100
  const totalPages = Math.ceil(records.length / pageSize)
  const pageRecords = records.slice(page * pageSize, (page + 1) * pageSize)

  return (
    <div className="result-table-container">
      <div className="result-table-scroll">
        <table className="result-table">
          <thead>
            <tr>
              <th className="row-num-col">#</th>
              {fields.map((f) => (
                <th key={f.name} title={f.type}>
                  {f.name}
                  <span className="col-type">{f.type}</span>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {pageRecords.map((row, rowIdx) => (
              <tr key={rowIdx} className={rowIdx % 2 === 0 ? 'row-even' : 'row-odd'}>
                <td className="row-num-col">{page * pageSize + rowIdx + 1}</td>
                {row.map((cell, cellIdx) => (
                  <td key={cellIdx}>{formatValue(cell)}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button onClick={() => setPage(0)} disabled={page === 0}>«</button>
          <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>‹</button>
          <span>Página {page + 1} de {totalPages}</span>
          <button onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>›</button>
          <button onClick={() => setPage(totalPages - 1)} disabled={page >= totalPages - 1}>»</button>
        </div>
      )}
    </div>
  )
}

function TimeChip({ label, value }) {
  return (
    <span className="time-chip">
      <svg width="11" height="11" viewBox="0 0 12 12" fill="none">
        <circle cx="6" cy="6" r="5" stroke="currentColor" strokeWidth="1.2"/>
        <path d="M6 3v3.5l2 1" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
      </svg>
      {label} <strong>{Number(value).toFixed(3)}s</strong>
    </span>
  )
}

function ResultBlock({ item, index }) {
  return (
    <div className="result-block">
      <div className="result-block-header">
        <span className="result-block-index">#{index + 1}</span>
        <TimeChip label="fetch" value={item.tempo_fetch} />
        <TimeChip label="json" value={item.tempo_json} />
        <span className="result-block-rows">
          {item.rows_count} {item.rows_count === 1 ? 'linha' : 'linhas'}
        </span>
      </div>

      <div className="result-block-body">
        {item.isResultSet ? (
          <ResultTable fields={item.result.fields} records={item.result.records} />
        ) : (
          <div className="result-rowcount">
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <rect x="1" y="1" width="12" height="12" rx="2" stroke="currentColor" strokeWidth="1.3"/>
              <path d="M4 7h6M4 4.5h6M4 9.5h4" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round"/>
            </svg>
            Rows Count: <strong>{item.rows_count}</strong>
          </div>
        )}
      </div>
    </div>
  )
}

export function ResultTables({ result, loading, error }) {
  if (loading) {
    return (
      <div className="results-state">
        <div className="spinner" />
        <span>Executando consulta...</span>
      </div>
    )
  }

  if (error) {
    return (
      <div className="results-state results-error">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <circle cx="10" cy="10" r="9" stroke="#f87171" strokeWidth="1.5"/>
          <path d="M10 6v5M10 14h.01" stroke="#f87171" strokeWidth="1.5" strokeLinecap="round"/>
        </svg>
        <pre className="error-message">{error}</pre>
      </div>
    )
  }

  if (!result) {
    return (
      <div className="results-state results-empty">
        <svg width="40" height="40" viewBox="0 0 40 40" fill="none">
          <rect x="6" y="10" width="28" height="22" rx="2" stroke="#4a4e69" strokeWidth="1.5"/>
          <path d="M6 16h28M14 10v6M26 10v6" stroke="#4a4e69" strokeWidth="1.5" strokeLinecap="round"/>
        </svg>
        <span>Execute uma consulta para ver os resultados</span>
      </div>
    )
  }

  const { tempo_execucao, tempo_total, status, erro, results = [] } = result
  const exportable = results.filter(r => r.isResultSet)

  return (
    <div className="results-wrapper">
      {/* ── Cabeçalho global ── */}
      <div className="results-summary">
        <TimeChip label="execução" value={tempo_execucao} />
        <TimeChip label="total" value={tempo_total} />
        {status && (
          <span className="results-count">
            {results.length} {results.length === 1 ? 'resultado' : 'resultados'}
          </span>
        )}

        <div className="export-actions">
          {exportable.length > 0 && (
            <>
              <button
                className="btn btn-export"
                onClick={() => exportCSV(exportable)}
                title="Exportar como CSV"
              >
                <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                  <rect x="1" y="1" width="11" height="11" rx="1.5" stroke="currentColor" strokeWidth="1.3"/>
                  <path d="M1 4.5h11M1 7.5h11M4.5 4.5v7" stroke="currentColor" strokeWidth="1.1" strokeLinecap="round"/>
                </svg>
                CSV
              </button>
              <button
                className="btn btn-export btn-export-excel"
                onClick={() => exportExcel(exportable)}
                title="Exportar como Excel"
              >
                <svg width="13" height="13" viewBox="0 0 13 13" fill="none">
                  <rect x="1" y="1" width="11" height="11" rx="1.5" stroke="currentColor" strokeWidth="1.3"/>
                  <path d="M4 4l2 2.5L4 9M7 4l2 2.5L7 9" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" strokeLinejoin="round"/>
                </svg>
                Excel
              </button>
            </>
          )}
          <span className={`results-status-badge ${status ? 'badge-ok' : 'badge-err'}`}>
            {status ? '✓ OK' : '✗ Erro'}
          </span>
        </div>
      </div>

      {/* ── Erro global ── */}
      {!status && erro && (
        <div className="results-state results-error" style={{ flex: 'none', padding: '20px 16px' }}>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="10" cy="10" r="9" stroke="#f87171" strokeWidth="1.5"/>
            <path d="M10 6v5M10 14h.01" stroke="#f87171" strokeWidth="1.5" strokeLinecap="round"/>
          </svg>
          <pre className="error-message">{erro}</pre>
        </div>
      )}

      {/* ── Blocos de resultado ── */}
      {status && (
        <div className="results-blocks">
          {results.map((item, idx) => (
            <ResultBlock key={idx} item={item} index={idx} />
          ))}
        </div>
      )}
    </div>
  )
}
