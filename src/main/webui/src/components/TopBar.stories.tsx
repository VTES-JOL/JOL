import type { Meta, StoryObj } from '@storybook/react-vite';
import type { ReactElement } from 'react';
import { expect, userEvent, waitFor, within } from 'storybook/test';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { NavBean } from '../api/types';
import { TopBar } from './TopBar';

// useNav() (src/auth/useNav.ts) reads a react-query cache entry keyed
// ['nav'] fed by GET /nav — there's no backend to answer that here, so each
// story seeds the cache directly with setQueryData instead of mocking
// fetch. This local decorator replaces preview.tsx's default empty
// QueryClientProvider with a pre-populated one (the MemoryRouter it also
// provides is left to preview.tsx's own decorator — nesting a second one
// here throws "You cannot render a <Router> inside another <Router>").
const NAV_QUERY_KEY = ['nav'];

function withNav(nav: NavBean | null) {
  return (Story: () => ReactElement) => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
    queryClient.setQueryData(NAV_QUERY_KEY, nav);
    return (
      <QueryClientProvider client={queryClient}>
        <Story />
      </QueryClientProvider>
    );
  };
}

const loggedInNav: NavBean = {
  player: 'Player1',
  stamp: '2026-08-27T00:00:00Z',
  chats: true,
  notificationsEnabled: true,
  hasSubscriptions: false,
  country: 'US',
  buttons: ['main:Main', 'lobby:Lobby', 'tournament:Tournaments'],
  gameButtons: { g1: 'Game vs Player2', g2: 'Game vs Player3' },
  pendingJudgeRequests: 0,
};

const meta = {
  title: 'Components/TopBar',
  component: TopBar,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof TopBar>;

export default meta;
type Story = StoryObj<typeof meta>;

export const LoggedOut: Story = {
  decorators: [withNav(null)],
};

export const LoggedIn: Story = {
  decorators: [withNav(loggedInNav)],
};

export const OpensGamesDropdown: Story = {
  decorators: [withNav(loggedInNav)],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByText('My Games'));
    await waitFor(() => expect(canvas.getByText('Game vs Player2')).toBeVisible());
  },
};

export const OpensUserMenu: Story = {
  decorators: [withNav(loggedInNav)],
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement);
    await userEvent.click(canvas.getByText('Player1'));
    await waitFor(() => expect(canvas.getByText('Log Out')).toBeVisible());
  },
};
