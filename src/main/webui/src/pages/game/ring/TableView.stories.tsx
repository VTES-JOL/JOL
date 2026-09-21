import type { ComponentType } from 'react';
import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, fireEvent, userEvent, waitFor, within } from 'storybook/test';
import { TableView } from './TableView';
import { SeatLegend } from './SeatLegend';
import { buildRingModel } from './ringModel';
import { TURN9_VIEWER, turn9Table } from './__fixtures__/ringFixtures';

// The table overview — Wedge (C) or Panels (D) — inside its zoom / pan viewport,
// filling a frame. Wheel = zoom about the cursor; right-drag = pan. Both layouts
// share the viewport, props and interactions, so the tests below run on each.
const { players, seating } = turn9Table(5);
const frame = (w: number, h: number) => (Story: ComponentType) => (
  <div style={{ height: h, width: w, maxWidth: '100%', padding: 8 }}>
    <Story />
  </div>
);
const model = buildRingModel(players, seating, { viewerName: TURN9_VIEWER });

const meta = {
  title: 'Game/Ring/TableView',
  component: TableView,
  parameters: { layout: 'fullscreen' },
  decorators: [frame(1100, 760)],
  args: { model, viewerName: TURN9_VIEWER, showNames: true, hub: ['Turn 9', 'Minion Phase', 'Predator right · Prey left'] },
} satisfies Meta<typeof TableView>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};

const zoomOf = (c: HTMLElement) => c.querySelector('[data-testid=ring-zoom]')!.textContent;

export const ZoomAndPan: Story = {
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    const vp = await c.findByTestId('ring-viewport');
    await expect(zoomOf(canvasElement)).toBe('100%');

    // Wheel up zooms in, and the page must not scroll (non-passive listener).
    const ev = new WheelEvent('wheel', { deltaY: -200, clientX: 300, clientY: 200, bubbles: true, cancelable: true });
    vp.dispatchEvent(ev);
    await expect(ev.defaultPrevented).toBe(true);
    await waitFor(() => expect(parseInt(zoomOf(canvasElement)!)).toBeGreaterThan(100));

    // Buttons.
    await userEvent.click(c.getByRole('button', { name: 'Zoom in' }));
    const zoomed = parseInt(zoomOf(canvasElement)!);
    await userEvent.click(c.getByRole('button', { name: 'Zoom out' }));
    await expect(parseInt(zoomOf(canvasElement)!)).toBeLessThan(zoomed);

    // Right-drag pans the world; left-drag does not.
    const world = c.getByTestId('ring-world');
    const before = world.style.transform;
    fireEvent.pointerDown(vp, { button: 0, buttons: 1, clientX: 400, clientY: 300, pointerId: 1 });
    fireEvent.pointerMove(vp, { buttons: 1, clientX: 450, clientY: 340, pointerId: 1 });
    fireEvent.pointerUp(vp, { button: 0, pointerId: 1 });
    await expect(world.style.transform).toBe(before);
    fireEvent.pointerDown(vp, { button: 2, buttons: 2, clientX: 400, clientY: 300, pointerId: 1 });
    fireEvent.pointerMove(vp, { buttons: 2, clientX: 450, clientY: 340, pointerId: 1 });
    fireEvent.pointerUp(vp, { button: 2, pointerId: 1 });
    await waitFor(() => expect(world.style.transform).not.toBe(before));

    // The context menu is suppressed over the ring.
    const menu = new MouseEvent('contextmenu', { bubbles: true, cancelable: true });
    vp.dispatchEvent(menu);
    await expect(menu.defaultPrevented).toBe(true);

    // Reset returns to 100% and disables itself.
    await userEvent.click(c.getByRole('button', { name: 'Reset' }));
    await expect(zoomOf(canvasElement)).toBe('100%');
    await expect(c.getByRole('button', { name: 'Reset' })).toBeDisabled();
  },
};

export const KeyboardZoomAndPan: Story = {
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    (await c.findByTestId('ring-viewport')).focus();
    await userEvent.keyboard('+');
    await expect(parseInt(zoomOf(canvasElement)!)).toBe(125);
    await userEvent.keyboard('{ArrowLeft}');
    await userEvent.keyboard('0');
    await expect(zoomOf(canvasElement)).toBe('100%');
  },
};

export const Panels: Story = { args: { layout: 'panels' } };
export const PanelsPips: Story = { args: { layout: 'panels', meter: 'pips' } };

// ── same interaction contract on the Panels layout ─────────────────────────
export const PanelsZoomAndPan: Story = {
  ...ZoomAndPan,
  args: { layout: 'panels' },
};
export const PanelsInteractive: Story = {
  args: { layout: 'panels', onSeatClick: fn(), onCardClick: fn(), onMarkerClick: fn() },
  play: async ({ canvasElement, args }) => {
    const c = within(canvasElement);
    await userEvent.click(await c.findByRole('button', { name: /^Sable Voss: / }));
    await expect(args.onSeatClick).toHaveBeenCalledWith(expect.objectContaining({ name: 'Sable Voss' }));
    await userEvent.click(c.getByRole('button', { name: /^Ayelech, blood 6 of 7/ }));
    await expect(args.onCardClick).toHaveBeenCalledTimes(1);
    await expect(args.onSeatClick).toHaveBeenCalledTimes(1);
    await userEvent.click(c.getAllByRole('button', { name: /Hector Trelane, uncontrolled/ })[0]);
    await expect(args.onMarkerClick).toHaveBeenCalledTimes(1);
  },
};

// ── responsive matrix (board pane sizes from the prototype) ────────────────
export const Mid768Panels: Story = { name: 'Mid 768×620 — Panels', args: { layout: 'panels' }, decorators: [frame(768, 620)] };
export const Tablet1180Wedge: Story = { name: 'Tablet 1180×700 — Wedge', args: { layout: 'wedge' }, decorators: [frame(1180, 700)] };
export const Tablet1180Panels: Story = { name: 'Tablet 1180×700 — Panels', args: { layout: 'panels' }, decorators: [frame(1180, 700)] };
export const Desktop1920Wedge: Story = { name: 'Desktop board pane 1334×824 — Wedge', args: { layout: 'wedge' }, decorators: [frame(1334, 824)] };
export const Desktop1920Panels: Story = { name: 'Desktop board pane 1334×824 — Panels', args: { layout: 'panels' }, decorators: [frame(1334, 824)] };
export const UltraWidePanels: Story = { name: 'Ultra-wide board pane 1700×1000 — Panels', args: { layout: 'panels' }, decorators: [frame(1700, 1000)] };

/** Legend top-left + board, as composed in the 1920 desktop layout. */
export const WithLegend: Story = {
  parameters: { layout: 'fullscreen' },
  decorators: [],
  render: (args) => (
    <div style={{ display: 'flex', gap: 12, padding: 8, height: 824, width: 1560, maxWidth: '100%' }}>
      <SeatLegend seats={args.model.seats} viewerName={args.viewerName} />
      <div style={{ flex: 1, minWidth: 0 }}>
        <TableView {...args} />
      </div>
    </div>
  ),
};

export const SwitchCardMeter: Story = {
  play: async ({ canvasElement }) => {
    const c = within(canvasElement);
    const group = within(await c.findByRole('radiogroup', { name: 'Card blood display' }));
    const count = (m: string) => canvasElement.querySelectorAll(`[data-meter=${m}]`).length;
    await expect(count('gauge')).toBeGreaterThan(0);
    await expect(count('pips')).toBe(0);
    await userEvent.click(group.getByRole('radio', { name: 'Pips' }));
    await waitFor(() => expect(count('gauge')).toBe(0));
    await expect(count('pips')).toBeGreaterThan(0);
    await userEvent.click(group.getByRole('radio', { name: 'Gauge' }));
    await waitFor(() => expect(count('pips')).toBe(0));
  },
};
export const SwitchCardMeterPanels: Story = { ...SwitchCardMeter, args: { layout: 'panels' } };

export const Judge: Story = { args: { model: buildRingModel(players, seating), viewerName: null } };
