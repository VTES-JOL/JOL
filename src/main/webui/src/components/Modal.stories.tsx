import type { Meta, StoryObj } from '@storybook/react-vite';
import { expect, fn, userEvent, within } from 'storybook/test';
import { Modal } from './Modal';

const meta = {
  title: 'Components/Modal',
  component: Modal,
  parameters: { layout: 'fullscreen' },
  args: { onClose: fn() },
} satisfies Meta<typeof Modal>;

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {
  args: {
    children: (
      <>
        <div className="modal-header">
          <h5 className="modal-title">Confirm</h5>
        </div>
        <div className="modal-body">
          <p className="mb-0">Are you sure you want to leave this game?</p>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-secondary">
            Cancel
          </button>
          <button type="button" className="btn btn-danger">
            Leave
          </button>
        </div>
      </>
    ),
  },
};

export const Large: Story = {
  args: {
    ...Default.args,
    size: 'lg',
  },
};

export const ClosesOnEscape: Story = {
  args: Default.args,
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    await expect(canvas.getByText('Confirm')).toBeInTheDocument();
    await userEvent.keyboard('{Escape}');
    await expect(args.onClose).toHaveBeenCalledTimes(1);
  },
};

export const ClosesOnBackdropClick: Story = {
  args: Default.args,
  play: async ({ canvasElement, args }) => {
    const canvas = within(canvasElement);
    // The backdrop is the outer `.modal` div itself — clicking the dialog
    // content must NOT close it (see Modal.tsx's onBackdropMouseDown, which
    // only closes when the mousedown target is the backdrop element itself).
    await userEvent.click(canvas.getByText('Are you sure you want to leave this game?'));
    await expect(args.onClose).not.toHaveBeenCalled();

    await userEvent.click(canvasElement.querySelector('.modal')!);
    await expect(args.onClose).toHaveBeenCalledTimes(1);
  },
};
