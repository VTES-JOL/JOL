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
  "framework": "@storybook/react-vite",
  // The app's third-party CSS (bootstrap.min.css, dark-mode.css, light.css)
  // is served at runtime from src/main/resources/META-INF/resources/css (see
  // legacyStyles.ts) rather than being part of the Vite bundle — mirror that
  // here so stories render with real Bootstrap styling instead of unstyled
  // markup. Loaded via .storybook/preview-head.html at the same /jol/css
  // path the real app uses.
  "staticDirs": [
    { "from": "../../resources/META-INF/resources/css", "to": "/jol/css" }
  ]
};
export default config;