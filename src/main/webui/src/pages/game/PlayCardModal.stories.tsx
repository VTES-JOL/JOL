import type { Meta, StoryObj } from '@storybook/react-vite';
import { fn } from 'storybook/test';
import { PlayCardModal } from './PlayCardModal';
import { handCard, mode } from './__fixtures__/gameFixtures';

// Opens when the viewer clicks a card in their own HAND / RESEARCH region. Card
// image, cost, preamble, one button per play mode (multi-select for multiMode
// cards), the "Replace this card" toggle, Discard / Discard+Draw, and a label
// field. Renders as a centered Modal on desktop, a bottom sheet below md.
const meta = {
  title: 'Game/Modals/PlayCardModal',
  component: PlayCardModal,
  parameters: { layout: 'fullscreen' },
  args: {
    ctx: { regionType: 'HAND', regionCommandKey: 'hand', coordinate: '1' },
    viewerName: 'Player1',
    onSubmit: fn(),
    onClose: fn(),
    onRequestTarget: fn(),
  },
} satisfies Meta<typeof PlayCardModal>;

export default meta;
type Story = StoryObj<typeof meta>;

export const SingleMode: Story = {
  args: {
    card: handCard('Villein', { typeClass: 'master', cost: '0 pool', modes: [mode('Put this card on a ready vampire; move 3 blood to your pool.')] }),
  },
};

export const MultiDiscipline: Story = {
  args: {
    card: handCard('Govern the Unaligned', {
      typeClass: 'action',
      cost: '1 blood',
      preamble: 'Superior: 3 blood.',
      multiMode: true,
      modes: [
        mode('Bleed for 3.', { disciplines: ['DOM'] }),
        mode('Move 3 blood from the blood bank to a younger vampire.', { disciplines: ['dom'] }),
      ],
    }),
  },
};

export const NeedsTarget: Story = {
  args: {
    card: handCard('.44 Magnum', {
      typeClass: 'equipment',
      cost: '2 pool',
      modes: [mode('Equip a minion you control.', { target: 'MINION_YOU_CONTROL' })],
    }),
  },
};

export const ResearchCard: Story = {
  args: {
    ctx: { regionType: 'RESEARCH', regionCommandKey: 'research', coordinate: '1' },
    card: handCard('Edge Explosion', { typeClass: 'action', doNotReplace: true, modes: [mode('Burn the Edge.')] }),
  },
};
