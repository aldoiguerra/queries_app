import { useState, useEffect } from 'react'
import { getDatasources } from '../services/api'

export function DatasourceSelect({ value, onChange }) {
  const [datasources, setDatasources] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    getDatasources()
      .then(setDatasources)
      .catch(() => setError('Falha ao carregar datasources'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="ds-select-wrapper">
      <label className="ds-label" htmlFor="datasource-select">
        Datasource
      </label>
      <select
        id="datasource-select"
        className="ds-select"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={loading}
      >
        <option value="">
          {loading ? 'Carregando...' : error ? 'Erro ao carregar' : 'Selecione o datasource'}
        </option>
        {datasources.map((ds) => (
          <option key={ds} value={ds}>
            {ds}
          </option>
        ))}
      </select>
      {error && <span className="ds-error">{error}</span>}
    </div>
  )
}
