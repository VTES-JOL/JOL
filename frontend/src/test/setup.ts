import '@testing-library/jest-dom/vitest';
import { afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';

// vitest.config.ts doesn't set test.globals, so afterEach isn't injected
// globally — without this, RTL never unmounts a rendered component between
// tests and later tests see duplicate DOM from earlier ones.
afterEach(cleanup);
