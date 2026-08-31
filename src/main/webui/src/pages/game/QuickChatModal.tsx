import { Modal } from '../../components/ui/Modal';

const ROWS: string[][] = [
  ['Block?', 'No block', 'Blocked', 'Yes', 'No', 'Wait', '1', '2', '3', '4', '5'],
  ['No pre-range', 'No maneuver', 'No pre, no maneuver', 'Long', 'Close'],
  ['No grapple', 'Hands for 1', 'Hands for 2', 'Hands for 3', 'Wave', 'No additional strikes'],
  ['No press', 'Press to continue', 'Press to end', 'Combat ends'],
  ['No sudden/wash', 'No votes / No Delaying Tactics'],
];

// H1/H2/H3 are the button labels for "Hands for N" in quick-chat-modal.jsp —
// the chat text sent differs from the label shown.
const LABELS: Record<string, string> = {
  'Hands for 1': 'H1',
  'Hands for 2': 'H2',
  'Hands for 3': 'H3',
};

const BTN = 'm-1 rounded border px-2.5 py-1 text-sm transition-colors';
const ROW_STYLE = [
  `${BTN} border-line-accent text-ink-secondary hover:bg-hover`,
  `${BTN} border-blood/40 text-blood hover:bg-blood/10`,
  `${BTN} border-blood/40 text-blood hover:bg-blood/10`,
  `${BTN} border-blood/40 text-blood hover:bg-blood/10`,
  `${BTN} border-online/40 text-online hover:bg-online/10`,
];

// Mirrors quick-chat-modal.jsp — canned chat-message buttons for combat callouts.
export function QuickChatModal({ onSend, onClose }: { onSend: (message: string) => void; onClose: () => void }) {
  const send = (message: string) => {
    onSend(message);
    onClose();
  };

  return (
    <Modal size="lg" onClose={onClose} title="Quick Chat">
      {ROWS.map((row, i) => (
        <div key={i} className="flex flex-wrap">
          {row.map((message) => (
            <button key={message} type="button" className={ROW_STYLE[i]} onClick={() => send(message)}>
              {LABELS[message] ?? message}
            </button>
          ))}
        </div>
      ))}
    </Modal>
  );
}
