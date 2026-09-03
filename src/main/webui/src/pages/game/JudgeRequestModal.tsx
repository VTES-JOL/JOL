import { useRef, useState } from 'react';
import { api } from '../../api/client';
import { runRequest } from '../../api/mutate';
import type { GameSnapshot, JudgeRequestCategory, JudgeRequestSnapshot } from '../../api/types';
import { Modal } from '../../components/ui/Modal';
import { Button } from '../../components/ui/Button';
import { Select } from '../../components/ui/Select';
import { Textarea } from '../../components/ui/Textarea';
import { Badge } from '../../components/ui/Badge';
import { MessageContent } from '../../components/MessageContent';
import { useCardTooltips } from '../../hooks/useCardTooltips';
import { confirmDialog } from '../../stores/dialog';

export const JUDGE_CATEGORY_OPTIONS: { value: JudgeRequestCategory; label: string }[] = [
  { value: 'INCORRECT_PLAY', label: 'Incorrect play' },
  { value: 'CARD_RULING', label: 'Card ruling' },
  { value: 'OTHER', label: 'Other' },
];

export function categoryLabel(category: JudgeRequestCategory): string {
  return JUDGE_CATEGORY_OPTIONS.find((o) => o.value === category)?.label ?? category;
}

const DETAILS_HINT = 'Reference a card with [Card Name], same as game chat.';

export function JudgeRequestModal({
  gameId,
  request,
  onUpdated,
  onClose,
  submitting,
  guard,
}: {
  gameId: string;
  request: JudgeRequestSnapshot | null;
  onUpdated: (updated: GameSnapshot) => void;
  onClose: () => void;
  submitting: boolean;
  guard: <T>(run: () => Promise<T>) => Promise<T | undefined>;
}) {
  const detailsRef = useRef<HTMLDivElement>(null);
  useCardTooltips(detailsRef, [request?.details]);

  const [category, setCategory] = useState<JudgeRequestCategory>(request?.category ?? 'INCORRECT_PLAY');
  const [details, setDetails] = useState(request?.rawDetails ?? '');
  const [notes, setNotes] = useState('');

  const done = (updated: GameSnapshot) => {
    onUpdated(updated);
    onClose();
  };

  const call = (run: () => Promise<unknown>) => guard(() => run() as Promise<GameSnapshot | undefined>);

  const create = () =>
    call(() =>
      runRequest(api.post<GameSnapshot>(`/game/${gameId}/judge-request`, { category, details }), 'Failed to call a judge', done),
    );

  const saveEdit = () =>
    call(() =>
      runRequest(
        api.put<GameSnapshot>(`/game/${gameId}/judge-request`, { category, details }),
        'Failed to update the judge request',
        done,
      ),
    );

  const retract = async () => {
    if (
      !(await confirmDialog('The judge will no longer see this request.', {
        title: 'Retract this request?',
        confirmLabel: 'Retract',
      }))
    )
      return;
    call(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/judge-request/retract`),
        'Failed to retract the judge request',
        done,
      ),
    );
  };

  const resolve = () =>
    call(() =>
      runRequest(
        api.post<GameSnapshot>(`/game/${gameId}/judge-request/resolve`, { notes }),
        'Failed to resolve the judge request',
        done,
      ),
    );

  const categoryField = (
    <Select
      id="judge-category"
      label="Type of request"
      value={category}
      onChange={(e) => setCategory(e.target.value as JudgeRequestCategory)}
    >
      {JUDGE_CATEGORY_OPTIONS.map((o) => (
        <option key={o.value} value={o.value}>
          {o.label}
        </option>
      ))}
    </Select>
  );

  const detailsField = (
    <Textarea
      id="judge-details"
      label="What do you need a ruling on?"
      rows={5}
      value={details}
      onChange={(e) => setDetails(e.target.value)}
      hint={DETAILS_HINT}
      placeholder="Describe the situation, the cards involved, and the question for the judge."
    />
  );

  // ── No open request: the "call a judge" form ─────────────────────────────
  if (!request) {
    return (
      <Modal
        onClose={onClose}
        title="Call a judge"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button size="sm" onClick={create} disabled={submitting || !details.trim()}>
              {submitting ? 'Sending…' : 'Call judge'}
            </Button>
          </>
        }
      >
        {categoryField}
        {detailsField}
      </Modal>
    );
  }

  const meta = (
    <div className="text-xs text-ink-muted flex items-center gap-2 flex-wrap">
      <Badge variant="muted" size="xs">
        {categoryLabel(request.category)}
      </Badge>
      <span>
        called by <span className="text-ink-secondary">{request.requester}</span>
      </span>
      <span>· {new Date(request.createdAt).toLocaleString()}</span>
      {request.updatedAt !== request.createdAt && <span>· edited</span>}
    </div>
  );

  // ── Requester: edit / retract ───────────────────────────────────────────
  if (request.canEdit) {
    return (
      <Modal
        onClose={onClose}
        title="Your judge request"
        footer={
          <>
            <Button variant="secondary" size="sm" onClick={onClose} disabled={submitting}>
              Cancel
            </Button>
            <Button variant="danger" size="sm" onClick={retract} disabled={submitting}>
              Retract
            </Button>
            <Button size="sm" onClick={saveEdit} disabled={submitting || !details.trim()}>
              {submitting ? 'Saving…' : 'Save changes'}
            </Button>
          </>
        }
      >
        {meta}
        {categoryField}
        {detailsField}
      </Modal>
    );
  }

  // ── Everyone else: read-only, plus a resolve panel for a non-seated judge ─
  return (
    <Modal
      onClose={onClose}
      title="Judge called"
      footer={
        <Button variant="secondary" size="sm" onClick={onClose} disabled={submitting}>
          Close
        </Button>
      }
    >
      {meta}
      <div
        ref={detailsRef}
        className="rounded border border-line bg-surface/60 p-3 text-sm text-ink whitespace-pre-wrap"
      >
        <MessageContent message={request.details} viewer={null} />
      </div>
      {request.canResolve && (
        <div className="mt-1 border-t border-line/60 pt-3">
          <Textarea
            id="judge-resolution"
            label="Resolution notes"
            rows={4}
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            hint={DETAILS_HINT}
            placeholder="Record your ruling. Shown at the table and kept in the ruling history."
          />
          <div className="mt-2 flex justify-end">
            <Button size="sm" onClick={resolve} disabled={submitting || !notes.trim()}>
              {submitting ? 'Resolving…' : 'Resolve request'}
            </Button>
          </div>
        </div>
      )}
    </Modal>
  );
}
