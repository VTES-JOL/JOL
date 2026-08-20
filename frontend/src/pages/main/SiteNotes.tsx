import { Card, CardHeader, CardTitle } from '../../components/Card';
import type { NotesResponse } from '../../api/types';
import { useApiPoll } from './useApiPoll';

export function SiteNotes() {
  const { notes } = useApiPoll<NotesResponse>('/main/notes', { notes: '' }, 'main:notes');

  if (!notes) return null;
  return (
    <Card>
      <CardHeader>
        <CardTitle>Notice</CardTitle>
      </CardHeader>
      {/* Trusted admin-authored HTML from SiteNotesService — same trust boundary the
          legacy JSP rendered it under (server-side rich-text notes, not user input). */}
      <div className="card-body p-2" dangerouslySetInnerHTML={{ __html: notes }} />
    </Card>
  );
}
