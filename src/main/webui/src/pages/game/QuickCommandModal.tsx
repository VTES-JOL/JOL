import { Modal } from '../../components/ui/Modal';

// Mirrors quick-command-modal.jsp — canned command buttons, each just a
// shortcut for typing the same string into the Command field.
const BTN = 'm-1 rounded border px-2.5 py-1 text-sm transition-colors';
const NEUTRAL = `${BTN} border-line-accent text-ink-secondary hover:bg-hover`;
const GOLD = `${BTN} border-gold/50 text-gold hover:bg-gold/10`;
const RED = `${BTN} border-blood/40 text-blood hover:bg-blood/10`;
const GREEN = `${BTN} border-online/40 text-online hover:bg-online/10`;
const HEADING = 'm-1 inline-block text-xs font-semibold uppercase tracking-wide text-ink-muted';

export function QuickCommandModal({ onSend, onClose }: { onSend: (command: string) => void; onClose: () => void }) {
  const send = (command: string) => {
    onSend(command);
    onClose();
  };

  return (
    <Modal size="lg" onClose={onClose} title="Quick Command">
      <div>
        <button type="button" className={NEUTRAL} onClick={() => send('unlock')}>
          Unlock
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('edge')}>
          Edge
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('edge burn')}>
          Burn edge
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('open')}>
          Toggle Open Hand
        </button>
        <button type="button" className={GOLD} title="Gain 1 VP and 6 pool." onClick={() => send('vp +1; pool +6')}>
          Ousted prey!
        </button>
      </div>
      <div>
        <span className={HEADING}>Library/Hand</span>
        <button type="button" className={NEUTRAL} onClick={() => send('draw')}>
          Draw
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('discard random')}>
          Discard random
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('shuffle')}>
          Shuffle
        </button>
      </div>
      <div>
        <span className={HEADING}>Crypt</span>
        <button type="button" className={NEUTRAL} onClick={() => send('draw crypt')}>
          Draw crypt
        </button>
        <button type="button" className={NEUTRAL} onClick={() => send('shuffle crypt')}>
          Shuffle crypt
        </button>
      </div>
      <hr className="my-2 border-line" />
      <div className="flex flex-wrap items-center">
        {[-6, -5, -4, -3, -2, -1].map((n) => (
          <button key={n} type="button" className={RED} onClick={() => send(`pool ${n}`)}>
            {n}
          </button>
        ))}
        <span className={HEADING}>Pool</span>
        {[1, 2, 3, 4, 5, 6].map((n) => (
          <button key={n} type="button" className={GREEN} onClick={() => send(`pool +${n}`)}>
            +{n}
          </button>
        ))}
      </div>
    </Modal>
  );
}
