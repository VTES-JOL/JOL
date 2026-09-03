import { useEffect, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Textarea';
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
    if (
      !(await confirmDialog('The notes shown on the main page will be emptied.', {
        title: 'Clear the site notes?',
        confirmLabel: 'Clear',
        danger: true,
      }))
    )
      return;
    runRequest(api.del('/admin-page/site-notes'), 'Failed to clear site notes', refresh);
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Site Notes</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-2">
        <Textarea id="siteNotesText" label="Markdown" rows={10} value={text} onChange={(e) => setText(e.target.value)} />
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" onClick={save}>
            Save
          </Button>
          <Button variant="danger" size="sm" onClick={clear}>
            Clear
          </Button>
        </div>
      </CardBody>
    </Card>
  );
}
