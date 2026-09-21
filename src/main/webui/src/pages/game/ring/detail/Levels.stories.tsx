import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, userEvent, waitFor, within } from 'storybook/test';
import { BoardView } from '../BoardView';
import { TURN9_VIEWER, turn9Table } from '../__fixtures__/ringFixtures';
import { card, hiddenCard, region } from '../../__fixtures__/gameFixtures';
import type { PlayerSnapshot } from '../../../../api/types';

// The whole board with its level switch: Table (Wedge / Panels) → Triad → Seat.
// Same controls for a player, a judge and a spectator.
const five = turn9Table(5);
const withHands = (ps: PlayerSnapshot[], readable: string[]): PlayerSnapshot[] =>
  ps.map((p) => ({
    ...p,
    regions: [
      ...p.regions,
      region('HAND', readable.includes(p.name) ? [card('Deep Song'), card('Blood Doll'), card('Direct Intervention')] : [hiddenCard(), hiddenCard(), hiddenCard()]),
    ],
  }));

const meta = {
  title: 'Game/Ring/BoardLevels',
  component: BoardView,
  parameters: { layout: 'fullscreen' },
  decorators: [(Story) => (<div style={{ height: 780, width: 1400, maxWidth: '100%', padding: 8 }}><Story /></div>)],
  args: {
    players: withHands(five.players, [TURN9_VIEWER]),
    seating: five.seating,
    viewerName: TURN9_VIEWER,
    gameId: 'story',
    hub: ['Turn 9', 'Minion Phase', 'Predator right · Prey left'],
  },
  // A previous story's saved level must not leak into the next one.
  beforeEach: () => {
    sessionStorage.clear();
  },
} satisfies Meta<typeof BoardView>;

export default meta;
type Story = StoryObj<typeof meta>;

export const PlayerTable: Story = { name: 'Player — Table (Wedge)', args: { profileLayout: 'wedge' } };
export const PlayerTablePanels: Story = { name: 'Player — Table (Panels)', args: { profileLayout: 'panels' } };
export const PlayerTriad: Story = { name: 'Player — Triad', args: { defaultLevel: 'triad' } };
export const PlayerSeat: Story = { name: 'Player — Seat', args: { defaultLevel: 'seat' } };
export const JudgeTable: Story = { name: 'Judge — Table', args: { viewerName: null, showHands: true, players: withHands(five.players, five.seating) } };
export const JudgeTriad: Story = { name: 'Judge — Triad (focus on edge holder)', args: { viewerName: null, showHands: true, defaultLevel: 'triad', players: withHands(five.players, five.seating) } };
export const SpectatorSeat: Story = { name: 'Spectator — Seat', args: { viewerName: null, defaultLevel: 'seat' } };

const two = turn9Table(2);
export const TwoPlayers: Story = {
  name: 'Two players — no Triad',
  args: { players: two.players, seating: two.seating, viewerName: 'Marcus Kane' },
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    const levels = await c.findByRole('radiogroup', { name: 'Detail level' });
    await expect(within(levels).queryByRole('radio', { name: 'Triad' })).toBeNull();
    await expect(within(levels).getByRole('radio', { name: 'Seat' })).toBeInTheDocument();
  },
};

export const SwitchingLevels: Story = {
  args: { profileLayout: 'wedge' },
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    const levels = within(await c.findByRole('radiogroup', { name: 'Detail level' }));

    // Table → Triad: focus starts on the viewer, prey left, predator right.
    await userEvent.click(levels.getByRole('radio', { name: 'Triad' }));
    await waitFor(() => expect(c.getByTestId('triad-focus')).toHaveTextContent('Lysette Marchetti'));
    await expect(c.getByTestId('triad-prey')).toHaveTextContent('Ilya Rostova');
    await expect(c.getByTestId('triad-predator')).toHaveTextContent('Sable Voss');

    // Re-centre on the prey: its neighbours follow.
    await userEvent.click(within(c.getByTestId('triad-prey')).getByRole('button', { name: /^Focus / }));
    await waitFor(() => expect(c.getByTestId('triad-focus')).toHaveTextContent('Ilya Rostova'));
    await expect(c.getByTestId('triad-prey')).toHaveTextContent('The Baron');

    // Triad → Seat keeps the focus; stepping walks the table.
    await userEvent.click(levels.getByRole('radio', { name: 'Seat' }));
    await waitFor(() => expect(c.getByTestId('seat-detail')).toHaveTextContent('Ilya Rostova'));
    await userEvent.click(c.getByRole('button', { name: /^Predator: / }));
    await waitFor(() => expect(c.getByTestId('seat-detail')).toHaveTextContent('Lysette Marchetti'));

    // Back to Table: layout toggle appears; Panels then Wedge both keep the level control.
    await userEvent.click(levels.getByRole('radio', { name: 'Table' }));
    const layout = within(await c.findByRole('radiogroup', { name: 'Table layout' }));
    await userEvent.click(layout.getByRole('radio', { name: 'Panels' }));
    await waitFor(() => expect(c.getByTestId('seat-panels')).toBeInTheDocument());
    await userEvent.click(layout.getByRole('radio', { name: 'Wedge' }));
    await waitFor(() => expect(c.getByTestId('ring-board')).toBeInTheDocument());
  },
};

export const ClickASeatToOpenIt: Story = {
  args: { profileLayout: 'wedge' },
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    const levels = within(await c.findByRole('radiogroup', { name: 'Detail level' }));
    // One click on another player's wedge opens their Triad.
    await userEvent.click(await c.findByRole('button', { name: /^Sable Voss: / }));
    await waitFor(() => expect(levels.getByRole('radio', { name: 'Triad' })).toHaveAttribute('aria-checked', 'true'));
    await expect(c.getByTestId('triad-focus')).toHaveTextContent('Sable Voss');
  },
};

export const MeterSurvivesLayoutSwitch: Story = {
  args: { profileLayout: 'wedge' },
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    await userEvent.click(within(await c.findByRole('radiogroup', { name: 'Card blood display' })).getByRole('radio', { name: 'Pips' }));
    const layout = within(c.getByRole('radiogroup', { name: 'Table layout' }));
    await userEvent.click(layout.getByRole('radio', { name: 'Panels' }));
    await waitFor(() => expect(c.getByTestId('seat-panels')).toBeInTheDocument());
    await expect(canvasElement.querySelectorAll('[data-meter=pips]').length).toBeGreaterThan(0);
    await expect(canvasElement.querySelectorAll('[data-meter=gauge]').length).toBe(0);
  },
};

export const CardClickBubblesUp: Story = {
  args: { defaultLevel: 'seat', onCardClick: fn(), onCounter: fn() },
  play: async ({ canvasElement, args }) => {
    const c = within(canvasElement);
    await userEvent.click(await c.findByRole('button', { name: /^Ayelech, blood 6 of 7/ }));
    await expect(args.onCardClick).toHaveBeenCalledWith(expect.objectContaining({ name: 'Lysette Marchetti' }), expect.objectContaining({ name: 'Ayelech' }));
    await userEvent.click(c.getAllByRole('button', { name: /^Add blood to Ayelech/ })[0]);
    await expect(args.onCounter).toHaveBeenCalledTimes(1);
  },
};
