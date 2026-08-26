import { useQuery } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { NotesResponse } from '../../api/types';

export function SiteNotes() {
  const { data } = useQuery({
    queryKey: ['main-notes'],
    queryFn: () => api.get<NotesResponse>('/main/notes'),
  });
  const notes = data?.notes;

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
