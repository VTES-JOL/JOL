// Mirrors quick-command-modal.jsp — canned command buttons, each just a
// shortcut for typing the same string into the Command field.
export function QuickCommandModal({ onSend, onClose }: { onSend: (command: string) => void; onClose: () => void }) {
  const send = (command: string) => {
    onSend(command);
    onClose();
  };

  return (
    <div className="modal d-block" tabIndex={-1} style={{ background: 'rgba(0,0,0,0.5)' }}>
      <div className="modal-dialog modal-lg" role="document">
        <div className="modal-content">
          <div className="modal-header">
            <h5 className="modal-title">Quick Command</h5>
            <button type="button" className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
          <div className="modal-body">
            <div>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('unlock')}>
                Unlock
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('edge')}>
                Edge
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('edge burn')}>
                Burn edge
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('open')}>
                Toggle Open Hand
              </button>
              <button type="button" className="btn btn-warning m-1" title="Gain 1 VP and 6 pool." onClick={() => send('vp +1; pool +6')}>
                Ousted prey!
              </button>
            </div>
            <h6 className="m-1 d-inline btn pe-none bg-secondary-subtle">Library/Hand</h6>
            <div className="d-inline">
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('draw')}>
                Draw
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('discard random')}>
                Discard random
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('shuffle')}>
                Shuffle
              </button>
            </div>
            <h6 className="m-1 d-inline btn pe-none bg-secondary-subtle">Crypt</h6>
            <div className="d-inline">
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('draw crypt')}>
                Draw crypt
              </button>
              <button type="button" className="btn btn-outline-secondary m-1" onClick={() => send('shuffle crypt')}>
                Shuffle crypt
              </button>
            </div>
            <hr />
            <div>
              <div className="d-lg-inline d-block">
                {[-6, -5, -4, -3, -2, -1].map((n) => (
                  <button key={n} type="button" className="btn btn-outline-danger bg-danger-subtle m-1" onClick={() => send(`pool ${n}`)}>
                    {n}
                  </button>
                ))}
              </div>
              <h6 className="d-lg-inline btn pe-none bg-secondary-subtle m-2">Pool</h6>
              <div className="d-lg-inline d-block">
                {[1, 2, 3, 4, 5, 6].map((n) => (
                  <button key={n} type="button" className="btn btn-outline-success bg-success-subtle m-1" onClick={() => send(`pool +${n}`)}>
                    +{n}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
