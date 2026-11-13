import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/cars': 'http://localhost:8080',
      '/reservation': 'http://localhost:8080',
      '/reservations': 'http://localhost:8080',
      '/bookReservation': 'http://localhost:8080',
      '/cancelReservation': 'http://localhost:8080',
      '/checkAvailability': 'http://localhost:8080',
    },
  },
})
