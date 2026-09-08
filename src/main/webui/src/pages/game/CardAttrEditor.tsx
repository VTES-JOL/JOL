import type { CardSnapshot } from '../../api/types';
import { Select } from '../../components/ui/Select';
import { CLAN, resolveClan } from './Clan';
import { PATH, resolvePath } from './Path';
import { SECT, resolveSect } from './Sect';

// The clan / path / sect editor, lifted out of the old CardActionModal when the
// desktop menu absorbed it (#8 / D32). Rendered inside a collapsible row in
// both CardContextMenu and CardActionSheet. Only meaningful for a minion on
// your own ready region — callers gate on that.

const CLANS = [...Object.values(CLAN), 'None'];
const PATHS = [...Object.values(PATH), 'None'];
const SECTS = [...Object.values(SECT), 'None'];

function nameToKey(name: string): string {
  return name.toLowerCase().replace(/ /g, '_');
}

const clanName = (value?: string | null) => {
  const code = resolveClan(value);
  return code ? CLAN[code] : undefined;
};
const pathName = (value?: string | null) => {
  const code = resolvePath(value);
  return code ? PATH[code] : undefined;
};
const sectName = (value?: string | null) => {
  const code = resolveSect(value);
  return code ? SECT[code] : undefined;
};

function AttrSelect({
  label,
  value,
  values,
  resolveName,
  onChange,
}: {
  label: string;
  value: string | null | undefined;
  values: string[];
  resolveName: (value?: string | null) => string | undefined;
  onChange: (newKey: string) => void;
}) {
  const display = resolveName(value) ?? 'None';
  return (
    <Select label={label} size="sm" value={nameToKey(display)} onChange={(e) => onChange(e.target.value)}>
      {values.map((v) => (
        <option key={v} value={nameToKey(v)}>
          {v}
        </option>
      ))}
    </Select>
  );
}

export type CardAttr = 'clan' | 'path' | 'sect';

export function CardAttrEditor({
  card,
  onChange,
}: {
  card: CardSnapshot;
  onChange: (attr: CardAttr, key: string) => void;
}) {
  return (
    <div className="grid w-full grid-cols-3 gap-2">
      <AttrSelect label="Clan" value={card.clan} values={CLANS} resolveName={clanName} onChange={(k) => onChange('clan', k)} />
      <AttrSelect label="Path" value={card.path} values={PATHS} resolveName={pathName} onChange={(k) => onChange('path', k)} />
      <AttrSelect label="Sect" value={card.sect} values={SECTS} resolveName={sectName} onChange={(k) => onChange('sect', k)} />
    </div>
  );
}
