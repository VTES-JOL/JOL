import type { Meta, StoryObj } from '@storybook/react-vite';
import { Card, CardHeader, CardTitle } from './Card';

const meta = {
  title: 'Components/Card',
  component: Card,
  parameters: { layout: 'padded' },
} satisfies Meta<typeof Card>;

export default meta;
type Story = StoryObj<typeof meta>;

export const WithHeader: Story = {
  args: {
    children: (
      <>
        <CardHeader>
          <CardTitle>Resources</CardTitle>
        </CardHeader>
        <div className="card-body">Card body content goes here.</div>
      </>
    ),
  },
};

export const BodyOnly: Story = {
  args: {
    children: <div className="card-body">No header, just body content.</div>,
  },
};
