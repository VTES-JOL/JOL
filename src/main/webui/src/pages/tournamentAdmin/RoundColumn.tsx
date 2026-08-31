import { ArrowDownAZ, ArrowDown01, GripVertical } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { draggableChip, dropTarget, type DragPayload } from './dragDrop';

function PlayerChip({
  player,
  vekn,
  from,
}: {
  player: string;
  vekn: string;
  from: 'pool' | number;
}) {
  return (
    <li
      className="border border-line-accent rounded p-2 flex justify-between items-center gap-2 bg-surface cursor-grab"
      {...draggableChip({ player, from })}
    >
      <div className="flex flex-col">
        <span>{player}</span>
        <span className="font-bold text-xs">{vekn}</span>
      </div>
      <GripVertical size={14} className="text-ink-muted" />
    </li>
  );
}

export function RoundColumn({
  round,
  pool,
  tables,
  playerVekn,
  onMove,
  onCreateTable,
  onRemoveTable,
  onSortPoolByVekn,
  onSortPoolByName,
}: {
  round: number;
  pool: string[];
  tables: string[][];
  playerVekn: Record<string, string>;
  onMove: (round: number, payload: DragPayload, to: 'pool' | number) => void;
  onCreateTable: (round: number) => void;
  onRemoveTable: (round: number, tableIndex: number) => void;
  onSortPoolByVekn: (round: number) => void;
  onSortPoolByName: (round: number) => void;
}) {
  return (
    <div className="mb-3">
      <div className="flex items-center gap-2 text-lg font-semibold">
        Round {round}
        <ArrowDown01 size={16} role="button" className="cursor-pointer text-ink-muted hover:text-ink" onClick={() => onSortPoolByVekn(round)} />
        <ArrowDownAZ size={16} role="button" className="cursor-pointer text-ink-muted hover:text-ink" onClick={() => onSortPoolByName(round)} />
      </div>
      <Button variant="secondary" size="sm" className="mt-2 w-full" onClick={() => onCreateTable(round)}>
        Create Table
      </Button>
      <ul
        className="list-none flex flex-wrap gap-2 p-1 mt-2 min-h-8"
        {...dropTarget((payload) => onMove(round, payload, 'pool'))}
      >
        {pool.map((player) => (
          <PlayerChip key={player} player={player} vekn={playerVekn[player] ?? ''} from="pool" />
        ))}
      </ul>
      <ol className="grid grid-cols-2 md:grid-cols-4 gap-2 p-1 list-none">
        {tables.map((table, tableIndex) => (
          <li key={tableIndex}>
            <div className="border border-online/40 rounded p-1">
              <span className="block font-semibold">Table {tableIndex + 1}</span>
              <Button variant="secondary" size="sm" className="my-1" onClick={() => onRemoveTable(round, tableIndex)}>
                Remove Table
              </Button>
              <ul
                className="border border-line rounded list-none min-h-9 p-1 flex flex-col gap-1"
                {...dropTarget((payload) => onMove(round, payload, tableIndex))}
              >
                {table.map((player) => (
                  <PlayerChip key={player} player={player} vekn={playerVekn[player] ?? ''} from={tableIndex} />
                ))}
              </ul>
            </div>
          </li>
        ))}
      </ol>
    </div>
  );
}
