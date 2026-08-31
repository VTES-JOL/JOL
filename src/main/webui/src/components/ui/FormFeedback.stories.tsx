import type { Meta, StoryObj } from '@storybook/react-vite';
import { FieldHint, InlineAlert } from './FormFeedback';

const meta: Meta = {
  title: 'Components/FormFeedback',
  parameters: { layout: 'padded' },
};

export default meta;
type Story = StoryObj;

export const Hint: Story = {
  render: () => <FieldHint>Link your account to your VEKN ID to play sanctioned tournaments.</FieldHint>,
};

export const Alerts: Story = {
  render: () => (
    <div className="jt:flex jt:flex-col jt:gap-2" style={{ maxWidth: 320 }}>
      <InlineAlert kind="success">Profile updated.</InlineAlert>
      <InlineAlert kind="danger">Password confirmation does not match.</InlineAlert>
    </div>
  ),
};
