import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { PlusCircle } from 'lucide-react';
import { api } from '../../api/client';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { InlineAlert } from '../../components/ui/FormFeedback';

export function GameCreateForm({ onCancel, onCreated }: { onCancel: () => void; onCreated: (gameName: string) => void }) {
  const { data: gameFormats = [] } = useQuery({
    queryKey: ['lobby', 'game-formats'],
    queryFn: () => api.get<string[]>('/lobby/game-formats'),
  });

  const [name, setName] = useState('');
  const [visibility, setVisibility] = useState<'PRIVATE' | 'PUBLIC'>('PRIVATE');
  const [format, setFormat] = useState('');
  const [error, setError] = useState('');

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
      // Land on the new game's detail pane — invite players and register your
      // deck there (one invite surface, not two).
      onCreated(trimmedName);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create game.');
      return;
    }
    setName('');
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
      <div className="flex-1 min-h-0 overflow-y-auto p-4">
        <div className="flex max-w-md flex-col gap-3">
          <div>
            <Input
              label="Name"
              maxLength={60}
              placeholder="Game name"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <p className="mt-1 text-xs text-ink-muted">
              Used in the game’s address, so no <code>'</code> or <code>"</code> characters.
            </p>
          </div>
          <Select
            label="Visibility"
            value={visibility}
            onChange={(e) => setVisibility(e.target.value as 'PRIVATE' | 'PUBLIC')}
          >
            <option value="PRIVATE">Private — invite-only</option>
            <option value="PUBLIC">Public — anyone can join</option>
          </Select>
          <Select label="Format" value={format || gameFormats[0] || ''} onChange={(e) => setFormat(e.target.value)}>
            {gameFormats.map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </Select>

          <div className="flex items-center gap-2">
            <Button variant="secondary" size="sm" icon={<PlusCircle size={14} />} onClick={create}>
              Create Game
            </Button>
            {error && <InlineAlert kind="danger">{error}</InlineAlert>}
          </div>
        </div>
      </div>
    </Panel>
  );
}
