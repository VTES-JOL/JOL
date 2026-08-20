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
      className="border rounded p-2 border-secondary d-flex justify-content-between align-items-center"
      {...draggableChip({ player, from })}
    >
      <div className="d-flex flex-column">
        <span>{player}</span>
        <span className="fw-bold">{vekn}</span>
      </div>
      <i className="bi bi-grip-vertical" />
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
      <span className="h4">
        Round {round}
        <i className="bi bi-sort-numeric-down ms-2" role="button" onClick={() => onSortPoolByVekn(round)} />
        <i className="bi bi-sort-alpha-down ms-2" role="button" onClick={() => onSortPoolByName(round)} />
      </span>
      <button
        className="btn btn-outline-secondary text-dark bg-info btn-sm mt-2 w-100"
        onClick={() => onCreateTable(round)}
      >
        Create Table
      </button>
      <ul
        className="list-unstyled d-flex flex-wrap gap-2 p-1 mt-2"
        {...dropTarget((payload) => onMove(round, payload, 'pool'))}
      >
        {pool.map((player) => (
          <PlayerChip key={player} player={player} vekn={playerVekn[player] ?? ''} from="pool" />
        ))}
      </ul>
      <ol className="row g-2 p-1 list-unstyled">
        {tables.map((table, tableIndex) => (
          <li key={tableIndex} className="col-md-3 col-6">
            <div className="card-body border border-success p-1">
              <span className="h5 d-block">Table {tableIndex + 1}</span>
              <button
                className="btn btn-outline-secondary text-dark bg-warning btn-sm mt-1 mb-1"
                onClick={() => onRemoveTable(round, tableIndex)}
              >
                Remove Table
              </button>
              <ul
                className="border list-group"
                style={{ minHeight: 38 }}
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
