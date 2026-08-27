import type { Decorator, Preview } from '@storybook/react-vite';
import { useEffect } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// App-authored global styles this app's main.tsx bundles at the app root
// (fonts/tokens/card-visuals) — third-party Bootstrap CSS and icon/font CDN
// links live in preview-head.html instead, mirroring legacyStyles.ts/
// index.html's own split between the two.
import '../src/index.css';
import '../src/styles/fonts.css';
import '../src/styles/theme.css';
import '../src/styles/card-visuals.css';

// index.html gives <html>/<body>/#root these classes so flex-fill layouts
// (SplitLayout, EmptyState, PageLoading, ...) have a real height to fill —
// Storybook's own root divs don't, so components relying on that silently
// collapse to zero height without this.
document.documentElement.classList.add('h-100', 'mh-100');
document.body.classList.add('h-100', 'mh-100', 'd-flex', 'flex-column');

const withProviders: Decorator = (Story) => {
  // Fresh client per render so query state (e.g. TopBar's /nav cache) never
  // leaks between stories.
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <Story />
      </MemoryRouter>
    </QueryClientProvider>
  );
};

// Lets every story be previewed in both themes via the toolbar, matching
// TopBar's real dark-mode toggle (see TopBar.tsx's toggleDarkMode, which
// sets this same body attribute).
const WithTheme: Decorator = (Story, context) => {
  useEffect(() => {
    if (context.globals.theme === 'dark') {
      document.body.setAttribute('data-bs-theme', 'dark');
    } else {
      document.body.removeAttribute('data-bs-theme');
    }
  }, [context.globals.theme]);
  return <Story />;
};

const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    a11y: {
      // 'todo' - show a11y violations in the test UI only
      // 'error' - fail CI on a11y violations
      // 'off' - skip a11y checks entirely
      test: 'todo',
    },
  },
  globalTypes: {
    theme: {
      description: 'Bootstrap color theme',
      toolbar: {
        title: 'Theme',
        icon: 'circlehollow',
        items: [
          { value: 'light', title: 'Light' },
          { value: 'dark', title: 'Dark' },
        ],
        dynamicTitle: true,
      },
    },
  },
  initialGlobals: { theme: 'light' },
  decorators: [WithTheme, withProviders],
};

export default preview;
