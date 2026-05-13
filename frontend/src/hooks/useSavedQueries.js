import { useState } from 'react'

const STORAGE_KEY = 'qexec_saved'

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

export function useSavedQueries() {
  const [saved, setSaved] = useState(loadFromStorage)

  const saveQuery = (entry) => {
    setSaved((prev) => {
      const next = [entry, ...prev]
      saveToStorage(next)
      return next
    })
  }

  const removeQuery = (id) => {
    setSaved((prev) => {
      const next = prev.filter((e) => e.id !== id)
      saveToStorage(next)
      return next
    })
  }

  const clearSaved = () => {
    setSaved([])
    saveToStorage([])
  }

  return { saved, saveQuery, removeQuery, clearSaved }
}
