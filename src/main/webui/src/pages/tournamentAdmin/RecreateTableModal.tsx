import { useState } from 'react';
import { api } from '../../api/client';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Textarea } from '../../components/ui/Textarea';
import { InlineAlert } from '../../components/ui/FormFeedback';

const CODE = 'bg-hover px-1 rounded';

export function RecreateTableModal({
  tournamentName,
  round,
  table,
  onClose,
  onRecreated,
}: {
  tournamentName: string;
  round: number;
  table: number;
  onClose: () => void;
  onRecreated: () => void;
}) {
  const gameName = `${tournamentName}: Round ${round} - Table ${table}`;
  const [csvData, setCsvData] = useState('');
  const [confirmText, setConfirmText] = useState('');
  const [error, setError] = useState('');

  const submit = () => {
    const trimmed = csvData.trim();
    if (!trimmed) {
      setError('Please paste CSV data before recreating.');
      return;
    }
    if (confirmText !== gameName) {
      setError('Confirmation text does not match.');
      return;
    }
    setError('');
    api
      .post(`/tournament/${encodeURIComponent(tournamentName)}/round/${round}/table/${table}/recreate`, {
        csvData: trimmed,
      })
      .then(onRecreated)
      .catch((err) => setError(`Recreate failed: ${err.message}`));
  };

  return (
    <Modal
      size="lg"
      onClose={onClose}
      title="Recreate Table"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="danger" size="sm" disabled={confirmText !== gameName} onClick={submit}>
            Recreate Table
          </Button>
        </>
      }
    >
      <InlineAlert kind="danger">
        <strong>This is destructive and cannot be undone.</strong> The existing game <code className={CODE}>{gameName}</code>{' '}
        and all of its data (turns, pool, VP) will be permanently deleted and replaced with a new game seated from the CSV
        below.
      </InlineAlert>
      <p className="text-xs text-ink-muted">
        Paste CSV data with columns <code className={CODE}>Round</code>, <code className={CODE}>Table</code>,{' '}
        <code className={CODE}>Player</code> — every row must be for this round/table. The header row is required.
      </p>
      <Textarea
        srLabel="CSV data"
        className="font-mono text-xs"
        rows={8}
        placeholder={'"Round","Table","Player"\n"1","1","PlayerOne"'}
        value={csvData}
        onChange={(e) => setCsvData(e.target.value)}
      />
      <div>
        <label className="block text-xs text-ink-muted mb-1">
          Type <code className={CODE}>{gameName}</code> to confirm:
        </label>
        <Input srLabel="Confirmation" autoComplete="off" value={confirmText} onChange={(e) => setConfirmText(e.target.value)} />
      </div>
      {error && <InlineAlert kind="danger">{error}</InlineAlert>}
    </Modal>
  );
}
