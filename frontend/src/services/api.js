import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
})

/**
 * Busca a lista de datasources disponíveis.
 * Retorna um array de strings com os nomes dos datasources.
 */
export async function getDatasources() {
  const { data } = await api.get('/queries_app/rest/query/datasources')
  return (data.records || []).map((row) => row[0]).filter(Boolean)
}

/**
 * Executa uma consulta SQL no datasource informado.
 * @param {string} datasource
 * @param {string} consulta
 * @returns {{ fields: Array<{name: string}>, records: Array<Array<any>>, infos: object }}
 */
export async function executeQuery(datasource, consulta) {
  const { data } = await api.post('/queries_app/rest/query/executar', {
    datasource,
    consulta,
  })
  return data
}
