/// <reference types="vitest/config" />
import { federation } from '@module-federation/vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath } from 'node:url';
import { defineConfig, loadEnv } from 'vite';

/**
 * Host do Module Federation: carrega os dois MFs pela rede, em tempo de execução.
 *   mfClientes/ClientesApp  →  ${VITE_MF_CLIENTES_URL}/remoteEntry.js
 *   mfOportunidades/OportunidadesApp  →  ${VITE_MF_OPORTUNIDADES_URL}/remoteEntry.js
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const falso = (arquivo: string) => fileURLToPath(new URL(`./src/testes/${arquivo}`, import.meta.url));
  // Nos testes não há rede: os remotes viram componentes falsos.
  const alias: Record<string, string> =
    mode === 'test'
      ? { 'mfClientes/ClientesApp': falso('ClientesAppFalso.tsx'), 'mfOportunidades/OportunidadesApp': falso('OportunidadesAppFalso.tsx') }
      : {};
  return {
    plugins: [
      react(),
      mode !== 'test' &&
        federation({
          name: 'shell',
          remotes: {
            mfClientes: { type: 'module', name: 'mfClientes', entry: `${env.VITE_MF_CLIENTES_URL}/remoteEntry.js` },
            mfOportunidades: { type: 'module', name: 'mfOportunidades', entry: `${env.VITE_MF_OPORTUNIDADES_URL}/remoteEntry.js` },
          },
          shared: { react: { singleton: true }, 'react-dom': { singleton: true } },
          dts: false,
        }),
    ],
    server: { port: 5000, strictPort: true },
    preview: { port: 5000, strictPort: true },
    build: { target: 'esnext' },
    resolve: { alias },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./src/testes/setup.ts'],
      css: false,
    },
  };
});
