/// <reference types="vitest/config" />
import { federation } from '@module-federation/vite';
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vite';

/**
 * Remote do Module Federation. O shell (host) carrega
 *   <URL deste app>/remoteEntry.js  →  import('mfClientes/ClientesApp')
 */
const PORTA = 5001;

export default defineConfig(({ mode }) => ({
  plugins: [
    react(),
    mode !== 'test' &&
      federation({
        name: 'mfClientes',
        filename: 'remoteEntry.js',
        exposes: {
          './ClientesApp': './src/ClientesApp.tsx',
        },
        shared: {
          react: { singleton: true },
          'react-dom': { singleton: true },
        },
        dts: false,
      }),
  ],
  server: { port: PORTA, strictPort: true, origin: `http://localhost:${PORTA}`, cors: true },
  preview: { port: PORTA, strictPort: true, cors: true },
  build: { target: 'esnext' },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/testes/setup.ts'],
    css: false,
  },
}));
