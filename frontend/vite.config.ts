import { fileURLToPath, URL } from 'url';

import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import { config } from 'dotenv';

// https://vitejs.dev/config/
export default ({ mode }) => {
  config({ path: `./.env.${mode}` });

  return defineConfig({
    base: process.env.BASE_URL,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: 9000,
    },
  });
};
