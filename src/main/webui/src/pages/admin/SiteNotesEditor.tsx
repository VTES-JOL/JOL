import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { SiteNotes } from '../../api/types';
import { useInvalidate } from '../../api/useInvalidate';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';

const SITE_NOTES_KEY = ['admin-page', 'site-notes'];

export function SiteNotesEditor() {
  const { data } = useQuery({
    queryKey: SITE_NOTES_KEY,
    queryFn: () => api.get<SiteNotes>('/admin-page/site-notes'),
  });
  const [text, setText] = useState('');

  useEffect(() => setText(data?.notes ?? ''), [data]);

  const refresh = useInvalidate(SITE_NOTES_KEY);

  const save = () => {
    runRequest(api.put('/admin-page/site-notes', { notes: text }), 'Failed to save site notes', refresh);
  };

  const clear = async () => {
    if (!(await confirmDialog('Clear the site notes?'))) return;
    runRequest(api.del('/admin-page/site-notes'), 'Failed to clear site notes', refresh);
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
