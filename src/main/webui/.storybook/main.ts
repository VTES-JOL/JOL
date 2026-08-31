import type { StorybookConfig } from '@storybook/react-vite';

const config: StorybookConfig = {
  // No "../src/**/*.mdx" glob: this app already has its own *.mdx content
  // under src/content/help/ (compiled by main vite.config.ts's own
  // @mdx-js/rollup + remark-gfm setup, for the in-app help pages, unrelated
  // to Storybook docs) — a blanket mdx glob here picks those up too and
  // fails to build under Storybook's own (differently-configured) mdx
  // pipeline. Add a narrower glob (e.g. "../src/**/*.docs.mdx") if
  // Storybook-authored docs pages are ever added.
  "stories": [
    "../src/**/*.stories.@(js|jsx|mjs|ts|tsx)"
  ],
  "addons": [
    "@storybook/addon-vitest",
    "@storybook/addon-a11y",
    "@storybook/addon-docs"
  ],
  "framework": "@storybook/react-vite"
  // App CSS (tokens, Tailwind + Preflight, fonts, card-visuals) is imported
  // in preview.tsx; the icon/font CDN links live in preview-head.html.
};
export default config;