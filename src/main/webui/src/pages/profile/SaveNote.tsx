import { InlineAlert } from '../../components/ui/FormFeedback';
import type { SaveState } from './saveState';

/** Inline result banner for a profile card save — driven by useSave()'s state. */
export function SaveNote({
  state,
  error,
  savedText = 'Saved.',
}: {
  state: SaveState;
  error?: string | null;
  savedText?: string;
}) {
  if (state === 'saved') return <InlineAlert kind="success">{savedText}</InlineAlert>;
  if (state === 'error') return <InlineAlert kind="danger">{error || 'Something went wrong.'}</InlineAlert>;
  return null;
}
