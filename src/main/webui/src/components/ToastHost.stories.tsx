import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, userEvent, waitFor, within } from 'storybook/test';
import { showError, showSuccess } from '../stores/toast';
import { ToastHost } from './ToastHost';

// Like DialogHost, ToastHost has no props — showError()/showSuccess() (see
// toast.ts) are what any real caller uses to push a toast in, so each
// story's play function does the same. toast.ts's list is module-level
// state shared across every story in this file, so each play function
// clears out whatever an earlier story left mounted before asserting on its
// own toast.
const meta = {
  title: 'Components/ToastHost',
  component: ToastHost,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof ToastHost>;

export default meta;
type Story = StoryObj<typeof meta>;

async function dismissAllToasts(canvasElement: HTMLElement) {
  const canvas = within(canvasElement);
  let closeButton = canvas.queryByRole('button', { name: 'Close' });
  while (closeButton) {
    await userEvent.click(closeButton);
    closeButton = canvas.queryByRole('button', { name: 'Close' });
  }
}

export const NoToasts: Story = {};

export const ErrorToast: Story = {
  play: async ({ canvasElement }) => {
    await dismissAllToasts(canvasElement);
    const canvas = within(canvasElement);
    showError('Failed to save deck.');
    await waitFor(() => expect(canvas.getByText('Failed to save deck.')).toBeInTheDocument());
  },
};

export const SuccessToast: Story = {
  play: async ({ canvasElement }) => {
    await dismissAllToasts(canvasElement);
    const canvas = within(canvasElement);
    showSuccess('Deck saved.');
    await waitFor(() => expect(canvas.getByText('Deck saved.')).toBeInTheDocument());
  },
};

export const DismissedByClose: Story = {
  play: async ({ canvasElement }) => {
    await dismissAllToasts(canvasElement);
    const canvas = within(canvasElement);
    showError('Could not join game.');
    await waitFor(() => expect(canvas.getByText('Could not join game.')).toBeInTheDocument());

    await userEvent.click(canvas.getByRole('button', { name: 'Close' }));
    await waitFor(() => expect(canvas.queryByText('Could not join game.')).not.toBeInTheDocument());
  },
};

export const StacksMultipleToasts: Story = {
  play: async ({ canvasElement }) => {
    await dismissAllToasts(canvasElement);
    const canvas = within(canvasElement);
    showError('First error.');
    showSuccess('Then a success.');
    await waitFor(() => {
      expect(canvas.getByText('First error.')).toBeInTheDocument();
      expect(canvas.getByText('Then a success.')).toBeInTheDocument();
    });
  },
};
