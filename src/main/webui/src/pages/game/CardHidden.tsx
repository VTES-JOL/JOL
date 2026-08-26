import type { CardSnapshot } from '../../api/types';

// Mirrors card-hidden.jsp — no card identity crosses the wire for these (see
// GameSnapshotFactory), so there's nothing to render beyond a placeholder and
// the counter badge.
export function CardHidden({ card, region }: { card: CardSnapshot; region: string }) {
  const regionStyle = region === 'REMOVED_FROM_GAME' ? 'opacity-50' : '';
  return (
    <li className={`flex-grow-1 list-group-item d-flex justify-content-between align-items-center p-1 shadow ${regionStyle}`}>
      <div className="mx-1 me-auto w-100 align-items-center">
        <div className="d-flex justify-content-between align-items-center w-100">
          <div className="d-flex flex-column">
            <span>*********</span>
          </div>
          <div className="d-flex flex-column">
            <div className="d-flex justify-content-end align-items-center gap-1">
              {card.counters > 0 && <span className="badge rounded-pill shadow text-bg-danger">{card.counters}</span>}
            </div>
          </div>
        </div>
      </div>
    </li>
  );
}
