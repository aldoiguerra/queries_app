import { useState } from 'react'

const STORAGE_KEY = 'qexec_history'
const MAX_ENTRIES = 100

function loadFromStorage() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

function saveToStorage(entries) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(entries))
  } catch {
    // localStorage pode estar indisponível no contexto do Fluig
  }
}

export function useQueryHistory() {
  const [history, setHistory] = useState(loadFromStorage)

  const addEntry = (entry) => {
    setHistory((prev) => {
      const next = [entry, ...prev].slice(0, MAX_ENTRIES)
      saveToStorage(next)
      return next
    })
  }

  const removeEntry = (id) => {
    setHistory((prev) => {
      const next = prev.filter((e) => e.id !== id)
      saveToStorage(next)
      return next
    })
  }

  const clearHistory = () => {
    setHistory([])
    saveToStorage([])
  }

  return { history, addEntry, removeEntry, clearHistory }
}
