import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    define: {
        global: 'globalThis',   // <-- Fix for sockjs-client
    },
    server: {
        port: 5173,
        proxy: {
            '/api': {
                target: 'http://localhost:8083',
                changeOrigin: true,
            },
            '/metrics': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                ws: true,
            },
                '/ws/metrics': {
                target: 'http://localhost:8083',
                changeOrigin: true,
                ws: true,  
            },
        },
        },
    })
