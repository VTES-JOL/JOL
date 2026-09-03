import { useEffect, useState } from 'react';
import { keepPreviousData, useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Button } from '../../components/ui/Button';
import { Textarea } from '../../components/ui/Textarea';
import { TabBar } from '../../components/TabBar';
import { api } from '../../api/client';
import type { SiteNotes } from '../../api/types';
import { useInvalidate } from '../../api/useInvalidate';
import { confirmDialog } from '../../stores/dialog';
import { runRequest } from '../../api/mutate';

const SITE_NOTES_KEY = ['admin-page', 'site-notes'];
type Mode = 'edit' | 'preview';

export function SiteNotesEditor() {
  const { data } = useQuery({
    queryKey: SITE_NOTES_KEY,
    queryFn: () => api.get<SiteNotes>('/admin-page/site-notes'),
  });
  const [text, setText] = useState('');
  const [mode, setMode] = useState<Mode>('edit');

  useEffect(() => setText(data?.notes ?? ''), [data]);

  // Rendered server-side by the same commonmark pipeline the main-page notice
  // uses, so the preview is exactly what visitors will see.
  const { data: previewHtml = '' } = useQuery({
    queryKey: ['admin-page', 'site-notes', 'preview', text],
    queryFn: () =>
      api.post<SiteNotes>('/admin-page/site-notes/preview', { notes: text }).then((r) => r.notes),
    enabled: mode === 'preview',
    placeholderData: keepPreviousData,
  });

  const refresh = useInvalidate(SITE_NOTES_KEY);

  const save = () => {
    runRequest(api.put('/admin-page/site-notes', { notes: text }), 'Failed to save site notes', refresh);
  };

  const clear = async () => {
    if (
      !(await confirmDialog('The notice shown on the main page will be emptied.', {
        title: 'Clear the notice?',
        confirmLabel: 'Clear',
        danger: true,
      }))
    )
      return;
    runRequest(api.del('/admin-page/site-notes'), 'Failed to clear site notes', refresh);
    setText('');
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Notice</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-2">
        <TabBar
          size="sm"
          tabs={[
            { id: 'edit', label: 'Edit' },
            { id: 'preview', label: 'Preview' },
          ]}
          active={mode}
          onChange={setMode}
        />
        {mode === 'edit' ? (
          <Textarea id="siteNotesText" label="Markdown" rows={10} value={text} onChange={(e) => setText(e.target.value)} />
        ) : (
          <div className="min-h-[13rem] rounded border border-line/60 bg-panel/20 p-3">
            {text.trim() ? (
              <div className="markdown-body text-sm text-ink" dangerouslySetInnerHTML={{ __html: previewHtml }} />
            ) : (
              <p className="text-xs italic text-ink-muted">Nothing to preview — the notice is empty.</p>
            )}
          </div>
        )}
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
