import CodeMirror from '@uiw/react-codemirror'
import { sql } from '@codemirror/lang-sql'
import { oneDark } from '@codemirror/theme-one-dark'
import { EditorView } from '@codemirror/view'

const editorTheme = EditorView.theme({
  '&': {
    fontSize: '13.5px',
    fontFamily: "'Cascadia Code', 'Fira Code', 'JetBrains Mono', Consolas, monospace",
  },
  '.cm-content': {
    padding: '12px 0',
    minHeight: '220px',
  },
  '.cm-gutters': {
    backgroundColor: '#21222c',
    borderRight: '1px solid #383a4a',
    color: '#6272a4',
  },
  '.cm-activeLineGutter': {
    backgroundColor: '#2a2d3e',
  },
  '.cm-activeLine': {
    backgroundColor: 'rgba(99, 102, 241, 0.08)',
  },
  '.cm-focused': {
    outline: 'none',
  },
  '.cm-scroller': {
    fontFamily: 'inherit',
  },
})

export function SqlEditor({ value, onChange }) {
  return (
    <div className="sql-editor-wrapper">
      <CodeMirror
        value={value}
        extensions={[sql(), editorTheme]}
        theme={oneDark}
        onChange={onChange}
        placeholder="-- Digite sua consulta SQL aqui..."
        basicSetup={{
          lineNumbers: true,
          highlightActiveLineGutter: true,
          highlightSpecialChars: true,
          foldGutter: true,
          dropCursor: true,
          allowMultipleSelections: true,
          indentOnInput: true,
          bracketMatching: true,
          closeBrackets: true,
          autocompletion: true,
          rectangularSelection: true,
          crosshairCursor: false,
          highlightActiveLine: true,
          highlightSelectionMatches: true,
          closeBracketsKeymap: true,
          defaultKeymap: true,
          searchKeymap: true,
          historyKeymap: true,
          foldKeymap: true,
          completionKeymap: true,
          lintKeymap: true,
        }}
      />
    </div>
  )
}
