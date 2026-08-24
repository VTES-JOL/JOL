import { useState } from 'react';
import { api } from '../../api/client';
import { Modal } from '../../components/Modal';

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
    <Modal size="lg" onClose={onClose}>
      <div className="modal-header">
        <h5 className="modal-title">Recreate Table</h5>
        <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
      </div>
      <div className="modal-body">
        <div className="alert alert-danger">
          <strong>This is destructive and cannot be undone.</strong> The existing game <code>{gameName}</code> and
          all of its data (turns, pool, VP) will be permanently deleted and replaced with a new game seated from the
          CSV below.
        </div>
        <p className="text-muted small">
          Paste CSV data with columns <code>Round</code>, <code>Table</code>, <code>Player</code> — every row must be
          for this round/table. The header row is required.
        </p>
        <textarea
          className="form-control font-monospace"
          rows={8}
          placeholder={'"Round","Table","Player"\n"1","1","PlayerOne"'}
          value={csvData}
          onChange={(e) => setCsvData(e.target.value)}
        />
        <div className="mt-3">
          <label className="form-label">
            Type <code>{gameName}</code> to confirm:
          </label>
          <input
            type="text"
            className="form-control"
            autoComplete="off"
            value={confirmText}
            onChange={(e) => setConfirmText(e.target.value)}
          />
        </div>
        {error && <div className="alert alert-danger mt-2">{error}</div>}
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-secondary" onClick={onClose}>
          Cancel
        </button>
        <button type="button" className="btn btn-danger" disabled={confirmText !== gameName} onClick={submit}>
          Recreate Table
        </button>
      </div>
    </Modal>
  );
}
