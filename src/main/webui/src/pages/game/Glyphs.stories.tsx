import type { Meta, StoryObj } from '@storybook/react-vite';
import { Clan, CLAN } from './Clan';
import { Sect, SECT } from './Sect';
import { Path, PATH } from './Path';

// The clan / sect / path markers used across every card surface. Clan and Path
// are icon-font glyphs (card-visuals.css); Sect is a normalised word. All three
// accept either the raw enum constant or the human name, and render nothing for
// NONE / blank.
const meta = {
  title: 'Game/Glyphs',
  parameters: { layout: 'padded' },
} satisfies Meta;

export default meta;
type Story = StoryObj;

export const AllClans: Story = {
  render: () => (
    <div className="flex flex-wrap gap-3 text-ink">
      {Object.values(CLAN).map((name) => (
        <span key={name} className="inline-flex items-center gap-1.5 rounded border border-line px-2 py-1 text-xs">
          <Clan value={name} />
          {name}
        </span>
      ))}
    </div>
  ),
};

export const AllSects: Story = {
  render: () => (
    <div className="flex flex-wrap gap-3">
      {Object.values(SECT).map((name) => (
        <span key={name} className="inline-flex items-center gap-1.5 rounded border border-line px-2 py-1 text-xs">
          <Sect value={name} />
        </span>
      ))}
    </div>
  ),
};

export const AllPaths: Story = {
  render: () => (
    <div className="flex flex-wrap gap-3 text-ink">
      {Object.values(PATH).map((name) => (
        <span key={name} className="inline-flex items-center gap-1.5 rounded border border-line px-2 py-1 text-xs">
          <Path value={name} />
          {name}
        </span>
      ))}
    </div>
  ),
};

export const AcceptsEnumConstant: Story = {
  render: () => (
    <div className="flex items-center gap-4 text-ink">
      <span className="inline-flex items-center gap-1">
        <Clan value="TOREADOR_ANTITRIBU" /> from &quot;TOREADOR_ANTITRIBU&quot;
      </span>
      <span className="inline-flex items-center gap-1">
        <Path value="POWER_AND_THE_INNER_VOICE" /> from constant
      </span>
    </div>
  ),
};

export const UnresolvedRendersNothing: Story = {
  render: () => (
    <div className="text-ink text-sm">
      [<Clan value="NONE" />
      <Sect value={null} />
      <Path value="" />] — three empty glyphs between the brackets.
    </div>
  ),
};
