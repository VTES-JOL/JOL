import type { Decorator, Preview } from '@storybook/react-vite';
import { useEffect } from 'react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

// The same global styles main.tsx bundles at the app root (tokens, Tailwind
// + Preflight, fonts, card-visuals). Icon/font CDN links live in
// preview-head.html.
import '../src/index.css';
import '../src/styles/fonts.css';
import '../src/styles/theme.css';
import '../src/styles/card-visuals.css';
import '../src/styles/tailwind.css';

// index.css makes <html>/<body>/#root full-height flex columns so flex-fill
// layouts (EmptyState, PageLoading, MasterDetailView, ...) have a real
// height to fill — Storybook's own root divs don't, so set it explicitly.
for (const el of [document.documentElement, document.body]) {
  el.style.height = '100%';
}
document.body.style.display = 'flex';
document.body.style.flexDirection = 'column';

const withProviders: Decorator = (Story) => {
  // Fresh client per render so query state (e.g. TopBar's /nav cache) never
  // leaks between stories.
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  return (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        {/* Mirrors the app: Tailwind pages live under a .root so the
            form-control reset in styles/tailwind.css applies. */}
        <div>
          <Story />
        </div>
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
