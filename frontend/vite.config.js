import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// =====================================================================
//  Vite — dev server avec proxy vers le backend Spring Boot (port 8080).
//  Les appels /api/** et /auth/** sont relayés, ce qui évite les soucis
//  CORS en développement (le front et l'API semblent partager l'origine).
// =====================================================================
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
