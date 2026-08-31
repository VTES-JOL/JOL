import { useState } from 'react';
import { api } from '../../api/client';
import { confirmDialog } from '../../stores/dialog';
import { Modal } from '../../components/Modal';

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
        'Import Tournament Tables from CSV? This replaces the current round/table assignments, and if the tournament has already started, existing tables/games for it will be deleted.',
        { danger: true },
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
    <Modal size="lg" onClose={onClose}>
      <div className="modal-header">
        <h5 className="modal-title">Import Tables from CSV</h5>
        <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
      </div>
      <div className="modal-body">
        <p className="text-muted small">
          Paste CSV data with columns <code>Round</code>, <code>Table</code>, <code>Player</code>. The header row is
          required.
        </p>
        <textarea
          className="form-control font-monospace"
          rows={14}
          placeholder={'"Round","Table","Player"\n"1","1","PlayerOne"\n"1","1","PlayerTwo"'}
          value={csvData}
          onChange={(e) => setCsvData(e.target.value)}
        />
        {error && <div className="alert alert-danger mt-2">{error}</div>}
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onClose}>
          Cancel
        </button>
        <button type="button" className="btn btn-primary" onClick={submit}>
          Import
        </button>
      </div>
    </Modal>
  );
}
