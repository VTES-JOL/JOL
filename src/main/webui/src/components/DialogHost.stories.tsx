import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, userEvent, waitFor, within } from 'storybook/test';
import { alertDialog, confirmDialog } from '../stores/dialog';
import { DialogHost } from './DialogHost';

// DialogHost renders whatever confirmDialog()/alertDialog() is currently
// pending (see dialog.ts) — there's no prop to drive it, so each story's
// play function triggers a real request and asserts on the resolved value,
// the same way a page's own "are you sure?" click would.
const meta = {
  title: 'Components/DialogHost',
  component: DialogHost,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof DialogHost>;

export default meta;
type Story = StoryObj<typeof meta>;

export const NoPendingRequest: Story = {};

export const ConfirmAccepted: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement.ownerDocument.body);
    let resolved: boolean | undefined;
    void confirmDialog('Leave this game?', { title: 'Leave Game', danger: true }).then((value) => {
      resolved = value;
    });

    await waitFor(() => expect(canvas.getByText('Leave Game')).toBeInTheDocument());
    await userEvent.click(canvas.getByRole('button', { name: 'Confirm' }));

    await waitFor(() => expect(resolved).toBe(true));
    expect(canvas.queryByText('Leave Game')).not.toBeInTheDocument();
  },
};

export const ConfirmCancelled: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement.ownerDocument.body);
    let resolved: boolean | undefined;
    void confirmDialog('Discard unsaved changes?').then((value) => {
      resolved = value;
    });

    await waitFor(() => expect(canvas.getByText('Discard unsaved changes?')).toBeInTheDocument());
    await userEvent.click(canvas.getByRole('button', { name: 'Cancel' }));

    await waitFor(() => expect(resolved).toBe(false));
  },
};

export const Alert: Story = {
  play: async ({ canvasElement }) => {
    const canvas = within(canvasElement.ownerDocument.body);
    let resolvedCount = 0;
    void alertDialog('Your deck is invalid.', { title: 'Invalid Deck' }).then(() => {
      resolvedCount += 1;
    });

    await waitFor(() => expect(canvas.getByText('Invalid Deck')).toBeInTheDocument());
    // Alert mode has no Cancel button, only a single acknowledgement action.
    expect(canvas.queryByRole('button', { name: 'Cancel' })).not.toBeInTheDocument();
    await userEvent.click(canvas.getByRole('button', { name: 'OK' }));

    await waitFor(() => expect(resolvedCount).toBe(1));
  },
};
