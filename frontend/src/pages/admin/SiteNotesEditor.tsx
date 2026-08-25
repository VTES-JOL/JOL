import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import { confirmDialog } from '../../components/dialog';
import { runRequest } from '../../api/mutate';

export function SiteNotesEditor({ notes, onSaved }: { notes: string; onSaved: () => void }) {
  const [text, setText] = useState(notes);

  useEffect(() => setText(notes), [notes]);

  const save = () => {
    runRequest(api.put('/admin-page/site-notes', { notes: text }), 'Failed to save site notes', onSaved);
  };

  const clear = async () => {
    if (!(await confirmDialog('Clear the site notes?'))) return;
    runRequest(api.del('/admin-page/site-notes'), 'Failed to clear site notes', onSaved);
  };

  return (
    <Card className="mt-2">
      <CardHeader>
        <CardTitle>Site Notes</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="siteNotesText" className="form-label">
          Markdown
        </label>
        <textarea
          id="siteNotesText"
          className="form-control"
          rows={6}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button onClick={save} className="btn btn-outline-secondary btn-sm mt-2">
          Save
        </button>
        <button onClick={clear} className="btn btn-outline-danger btn-sm mt-2">
          Clear
        </button>
      </div>
    </Card>
  );
}
