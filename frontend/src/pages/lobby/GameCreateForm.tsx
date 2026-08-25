import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../../api/client';

export function GameCreateForm({ onCancel, onCreated }: { onCancel: () => void; onCreated: (gameName: string) => void }) {
  const { data: players = [] } = useQuery({
    queryKey: ['lobby', 'players'],
    queryFn: () => api.get<string[]>('/lobby/players'),
  });
  const { data: gameFormats = [] } = useQuery({
    queryKey: ['lobby', 'game-formats'],
    queryFn: () => api.get<string[]>('/lobby/game-formats'),
  });

  const [name, setName] = useState('');
  const [visibility, setVisibility] = useState<'PRIVATE' | 'PUBLIC'>('PRIVATE');
  const [format, setFormat] = useState('');
  const [inviteInput, setInviteInput] = useState('');
  const [pendingInvites, setPendingInvites] = useState<string[]>([]);
  const [error, setError] = useState('');

  const addInvite = () => {
    const trimmed = inviteInput.trim();
    if (!trimmed || pendingInvites.includes(trimmed)) return;
    setPendingInvites((prev) => [...prev, trimmed]);
    setInviteInput('');
  };

  const removeInvite = (p: string) => setPendingInvites((prev) => prev.filter((x) => x !== p));

  const create = async () => {
    const trimmedName = name.trim();
    if (!trimmedName) {
      setError('Name is required.');
      return;
    }
    if (trimmedName.includes("'") || trimmedName.includes('"')) {
      setError('Name cannot contain \' or " characters.');
      return;
    }
    setError('');
    try {
      await api.post('/lobby/player/games', { name: trimmedName, publicFlag: visibility, format: format || gameFormats[0] });
      if (visibility === 'PRIVATE') {
        for (const p of pendingInvites) {
          await api.post(`/lobby/player/games/${encodeURIComponent(trimmedName)}/invite`, { player: p });
        }
      }
      onCreated(trimmedName);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create game.');
      return;
    }
    setName('');
    setPendingInvites([]);
  };

  return (
    <div className="card shadow flex-fill d-flex flex-column">
      <div className="card-header bg-body-secondary d-flex justify-content-between align-items-center">
        <span className="fw-semibold">New Game</span>
        <button className="btn btn-sm btn-outline-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
      <div className="card-body p-3 overflow-auto min-h-0">
        <div className="mb-3">
          <label className="form-label">Name</label>
          <input
            type="text"
            className="form-control"
            maxLength={60}
            placeholder="Game name (no ' or &quot; characters)"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>
        <div className="mb-3">
          <label className="form-label">Visibility</label>
          <select
            className="form-select"
            value={visibility}
            onChange={(e) => setVisibility(e.target.value as 'PRIVATE' | 'PUBLIC')}
          >
            <option value="PRIVATE">Private</option>
            <option value="PUBLIC">Public</option>
          </select>
        </div>
        <div className="mb-3">
          <label className="form-label">Format</label>
          <select className="form-select" value={format || gameFormats[0] || ''} onChange={(e) => setFormat(e.target.value)}>
            {gameFormats.map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </select>
        </div>
        {visibility === 'PRIVATE' && (
          <div className="mb-3">
            <label className="form-label">Invite Players</label>
            <div className="d-flex gap-2 mb-2">
              <input
                className="form-control"
                list="lobby-players"
                placeholder="Start typing a player name"
                value={inviteInput}
                onChange={(e) => setInviteInput(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') addInvite();
                }}
              />
              <datalist id="lobby-players">
                {players.map((p) => (
                  <option key={p} value={p} />
                ))}
              </datalist>
              <button className="btn btn-outline-secondary btn-sm text-nowrap" onClick={addInvite}>
                Add <i className="bi-person-plus" />
              </button>
            </div>
            <ul className="list-group list-group-flush small">
              {pendingInvites.map((p) => (
                <li key={p} className="list-group-item d-flex justify-content-between align-items-center py-1 px-2">
                  <span>{p}</span>
                  <button
                    type="button"
                    className="btn-close btn-sm"
                    aria-label="Remove"
                    onClick={() => removeInvite(p)}
                  />
                </li>
              ))}
            </ul>
          </div>
        )}
        <button className="btn btn-outline-secondary" onClick={create}>
          Create Game <i className="bi-plus-circle" />
        </button>
        <span className="text-danger small ms-2">{error}</span>
      </div>
    </div>
  );
}
