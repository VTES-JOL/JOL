import { Modal } from '../../components/Modal';

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

const ROW_STYLE = ['btn-outline-secondary', 'btn-outline-danger', 'btn-outline-danger', 'btn-outline-danger', 'btn-outline-success'];

// Mirrors quick-chat-modal.jsp — canned chat-message buttons for combat callouts.
export function QuickChatModal({ onSend, onClose }: { onSend: (message: string) => void; onClose: () => void }) {
  const send = (message: string) => {
    onSend(message);
    onClose();
  };

  return (
    <Modal size="lg" onClose={onClose}>
      <div className="modal-header">
        <h5 className="modal-title">Quick Chat</h5>
        <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
      </div>
      <div className="modal-body">
        {ROWS.map((row, i) => (
          <div key={i}>
            {row.map((message) => (
              <button key={message} type="button" className={`btn ${ROW_STYLE[i]} m-1`} onClick={() => send(message)}>
                {LABELS[message] ?? message}
              </button>
            ))}
          </div>
        ))}
      </div>
    </Modal>
  );
}
