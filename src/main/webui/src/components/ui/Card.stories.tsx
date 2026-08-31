import type { Meta, StoryObj } from '@storybook/react-vite';
import { Card, CardHeader, CardTitle, CardBody } from './Card';

const meta: Meta = {
  title: 'Components/Card (ui)',
  component: Card as never,
  parameters: { layout: 'padded' },
};

export default meta;
type Story = StoryObj;

export const Default: Story = {
  render: () => (
    <div style={{ maxWidth: 360 }}>
      <Card>
        <CardHeader>
          <CardTitle>Player Roles</CardTitle>
        </CardHeader>
        <CardBody>
          <p className="jt:text-sm jt:text-ink-secondary">Body content sits in a padded region.</p>
        </CardBody>
      </Card>
    </div>
  ),
};
