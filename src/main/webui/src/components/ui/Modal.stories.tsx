import type { Meta, StoryObj } from '@storybook/react-vite';
import { useState } from 'react';
import { expect, userEvent, within } from 'storybook/test';
import { Modal } from './Modal';
import { Button } from './Button';

const meta: Meta = {
  title: 'Components/Modal (ui)',
  component: Modal as never,
  parameters: { layout: 'fullscreen' },
};

export default meta;
type Story = StoryObj;

function Harness() {
  const [open, setOpen] = useState(true);
  return (
    <div className="jt:p-4">
      <Button onClick={() => setOpen(true)}>Open</Button>
      {open && (
        <Modal
          onClose={() => setOpen(false)}
          title="Import Deck"
          footer={
            <>
              <Button variant="secondary" size="sm" onClick={() => setOpen(false)}>
                Cancel
              </Button>
              <Button variant="primary" size="sm" onClick={() => setOpen(false)}>
                Create
              </Button>
            </>
          }
        >
          <p className="jt:text-sm jt:text-ink-secondary">Body content goes here.</p>
        </Modal>
      )}
    </div>
  );
}

export const Default: Story = { render: () => <Harness /> };

export const ClosesOnEscape: Story = {
  render: () => <Harness />,
  play: async ({ canvasElement }) => {
    // Modal portals to document.body, so query the whole document.
    const body = within(canvasElement.ownerDocument.body);
    await expect(body.getByRole('dialog')).toBeInTheDocument();
    await userEvent.keyboard('{Escape}');
    await expect(body.queryByRole('dialog')).not.toBeInTheDocument();
  },
};
