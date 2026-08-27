import type { Meta, StoryObj } from '@storybook/react-vite';
import { SplitLayout } from './SplitLayout';
import { Card, CardHeader, CardTitle } from './Card';

const Pane = ({ title, body }: { title: string; body: string }) => (
  <Card>
    <CardHeader>
      <CardTitle>{title}</CardTitle>
    </CardHeader>
    <div className="card-body">{body}</div>
  </Card>
);

const meta = {
  title: 'Components/SplitLayout',
  component: SplitLayout,
  parameters: { layout: 'fullscreen' },
} satisfies Meta<typeof SplitLayout>;

export default meta;
type Story = StoryObj<typeof meta>;

export const DeckPageBreakpoints: Story = {
  args: {
    stackBelowLg: false,
    left: <Pane title="List" body="Narrows from 33% to 25% at 992px+, stacks below 768px." />,
    right: <Pane title="Detail" body="Fills the remaining width." />,
  },
};

export const LobbyPageBreakpoints: Story = {
  args: {
    stackBelowLg: true,
    left: <Pane title="List" body="Flat 33% left pane, stacks below 992px." />,
    right: <Pane title="Detail" body="Fills the remaining width." />,
  },
};
