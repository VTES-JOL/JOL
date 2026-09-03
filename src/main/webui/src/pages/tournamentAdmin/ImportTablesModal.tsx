import { useState } from 'react';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Textarea';
import { InlineAlert } from '../../components/ui/FormFeedback';

export function ImportTablesModal({
  tournamentName,
  onClose,
  onImported,
}: {
  tournamentName: string;
  onClose: () => void;
  onImported: () => void;
}) {
  const [csvData, setCsvData] = useState('');
  const [error, setError] = useState('');

  const submit = async () => {
    const trimmed = csvData.trim();
    if (!trimmed) {
      setError('Please paste CSV data before importing.');
      return;
    }
    if (
      !(await confirmDialog(
        'This replaces the current round and table assignments. If the tournament has already started, its existing tables and games are deleted.',
        { title: 'Import tables from CSV?', confirmLabel: 'Import', danger: true },
      ))
    ) {
      return;
    }
    setError('');
    api
      .post(`/tournament/${encodeURIComponent(tournamentName)}/rounds/import`, { csvData: trimmed })
      .then(onImported)
      .catch((err) => setError(`Import failed: ${err.message}`));
  };

  return (
    <Modal
      size="lg"
      onClose={onClose}
      title="Import Tables from CSV"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="primary" size="sm" onClick={submit}>
            Import
          </Button>
        </>
      }
    >
      <p className="text-xs text-ink-muted">
        Paste CSV data with columns <code className="bg-hover px-1 rounded">Round</code>,{' '}
        <code className="bg-hover px-1 rounded">Table</code>,{' '}
        <code className="bg-hover px-1 rounded">Player</code>. The header row is required.
      </p>
      <Textarea
        srLabel="CSV data"
        className="font-mono text-xs"
        rows={14}
        placeholder={'"Round","Table","Player"\n"1","1","PlayerOne"\n"1","1","PlayerTwo"'}
        value={csvData}
        onChange={(e) => setCsvData(e.target.value)}
      />
      {error && <InlineAlert kind="danger">{error}</InlineAlert>}
    </Modal>
  );
}
