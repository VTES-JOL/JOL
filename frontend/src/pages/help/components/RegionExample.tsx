import type { ReactNode } from 'react';
import type { RegionSnapshot } from '../../../api/types';
import { Region } from '../../game/Region';
import { CardExample, buildCardSnapshot } from './CardExample';
import { childrenOfType } from './mdxChildren';

export interface RegionExampleProps {
  label: string;
  type?: string; // RegionType, e.g. "READY" (default) / "TORPOR" / "UNCONTROLLED"
  children: ReactNode; // one or more <CardExample>
}

// A whole region (e.g. a player's Ready area) built from nested <CardExample>
// children, rendered through the real `pages/game/Region.tsx` — used for
// walking through the [PLAYER] [REGION] [INDEX] targeting system with a
// realistic, numbered card list. Non-interactive: clicks are disabled.
export function RegionExample({ label, type = 'READY', children }: RegionExampleProps) {
  const cards = childrenOfType(children, CardExample).map((child, i) => buildCardSnapshot(child.props, String(i + 1)));

  const region: RegionSnapshot = {
    type,
    commandKey: type.toLowerCase(),
    label,
    simple: false,
    openHand: false,
    hiddenHand: false,
    cards,
  };

  return (
    <div className="my-3" style={{ maxWidth: '24rem' }}>
      <Region
        region={region}
        defaultCollapsed={false}
        controller=""
        controllerPool={0}
        isOwnRegion={false}
        isSeatedPlayer={false}
        onTableCardClick={() => {}}
        onPlayCardClick={() => {}}
      />
    </div>
  );
}
