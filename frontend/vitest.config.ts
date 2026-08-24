import { defineConfig } from 'vitest/config'

// Deliberately standalone, not merged with vite.config.ts — that config's
// plugins (basicSsl, serveCardAssets) and dev-server/proxy settings exist
// only to run the app locally against tomcat9:run and have no bearing on
// (and could only complicate) running unit tests.
export default defineConfig({
  test: {
    environment: 'node',
  },
})
