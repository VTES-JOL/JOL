import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PlusCircle, UserPlus, X } from 'lucide-react';
import { api } from '../../api/client';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { InlineAlert } from '../../components/ui/FormFeedback';

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
    if (!players.includes(trimmed)) {
      setError(`No such player: ${trimmed}`);
      return;
    }
    setError('');
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
    <Panel
      title="New Game"
      right={
        <Button variant="ghost" size="sm" onClick={onCancel}>
          Cancel
        </Button>
      }
    >
      <div className="flex-1 min-h-0 overflow-y-auto p-4 flex flex-col gap-3">
        <Input
          label="Name"
          maxLength={60}
          placeholder="Game name (no ' or &quot; characters)"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <Select
          label="Visibility"
          value={visibility}
          onChange={(e) => setVisibility(e.target.value as 'PRIVATE' | 'PUBLIC')}
        >
          <option value="PRIVATE">Private</option>
          <option value="PUBLIC">Public</option>
        </Select>
        <Select label="Format" value={format || gameFormats[0] || ''} onChange={(e) => setFormat(e.target.value)}>
          {gameFormats.map((f) => (
            <option key={f} value={f}>
              {f}
            </option>
          ))}
        </Select>

        {visibility === 'PRIVATE' && (
          <div>
            <label className="block text-xs text-ink-muted mb-1">Invite Players</label>
            <div className="flex gap-2 mb-2">
              <Input
                srLabel="Player name"
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
              <Button variant="secondary" size="sm" icon={<UserPlus size={14} />} onClick={addInvite}>
                Add
              </Button>
            </div>
            <ul className="text-sm">
              {pendingInvites.map((p) => (
                <li key={p} className="flex justify-between items-center py-1 border-b border-line/50">
                  <span>{p}</span>
                  <Button variant="ghost" size="sm" aria-label="Remove" onClick={() => removeInvite(p)}>
                    <X size={12} />
                  </Button>
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />} onClick={create}>
            Create Game
          </Button>
          {error && <InlineAlert kind="danger">{error}</InlineAlert>}
        </div>
      </div>
    </Panel>
  );
}
