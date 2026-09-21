import type { Decorator } from '@storybook/react-vite';

// Overview glyphs are ~46px, so stories magnify them with CSS zoom (Chromium)
// on a panel-coloured tile. `zoom` also multiplies the packer's scale in the
// "Sizes" story, so the real on-screen size is zoom × scale.
export function withGlyphFrame(zoom = 3): Decorator {
  return (Story) => (
    <div className="inline-block rounded border border-line-accent bg-panel p-4" style={{ zoom }}>
      <Story />
    </div>
  );
}
