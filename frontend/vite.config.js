import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        entryFileNames: `[name].js`,
        chunkFileNames: `[name].js`,
        assetFileNames: `[name].[ext]`,
      },
    },
  },
  plugins: [react()],
  base: "/queries_app/",
  server: {
    proxy: {
      // // Redireciona /query/* para o backend Fluig em dev
      // '/query': {
      //   target: 'http://localhost:8080',
      //   changeOrigin: true,
      // },
    },
  },
})
