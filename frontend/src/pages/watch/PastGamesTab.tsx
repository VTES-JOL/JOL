import { api } from '../../api/client';
import type { GameHistory } from '../../api/types';

const DATE_FORMAT = new Intl.DateTimeFormat('en-GB', {
  timeZone: 'UTC',
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

function downloadCsv(data: string, filename: string) {
  const blob = new Blob([data], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export function PastGamesTab({ history }: { history: GameHistory[] }) {
  const exportCsv = () => {
    api
      .getText('/admin/export/games.csv')
      .then((data) => downloadCsv(data, 'past-games.csv'))
      .catch((err) => console.error('Failed to export past games', err));
  };

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">Past Games</span>
        <button className="btn btn-outline-secondary btn-sm" onClick={exportCsv}>
          Export CSV <i className="bi-download" />
        </button>
      </div>
      <div className="flex-fill min-h-0" style={{ overflowY: 'auto', overflowX: 'clip' }}>
        <table className="table table-sm table-hover mb-0">
          <thead>
            <tr>
              <th>Game</th>
              <th>Started</th>
              <th>Ended</th>
              <th colSpan={3}>Results</th>
            </tr>
          </thead>
          <tbody>
            {history.flatMap((g) =>
              g.results.map((r, i) => (
                <tr
                  key={`${g.name}-${r.playerName}`}
                  className={i === 0 ? 'border-3 border-top border-bottom-0 border-start-0 border-end-0' : 'border-top'}
                >
                  {i === 0 && (
                    <>
                      <td rowSpan={g.results.length}>{g.name}</td>
                      <td rowSpan={g.results.length}>{DATE_FORMAT.format(new Date(g.started))} UTC</td>
                      <td rowSpan={g.results.length}>{DATE_FORMAT.format(new Date(g.ended))} UTC</td>
                    </>
                  )}
                  <td>{r.playerName}</td>
                  <td>{r.deckName.length > 50 ? `${r.deckName.slice(0, 50)}...` : r.deckName}</td>
                  <td>
                    {r.victoryPoints} VP
                    {r.gameWin ? ', 1 GW' : ''}
                  </td>
                </tr>
              )),
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
