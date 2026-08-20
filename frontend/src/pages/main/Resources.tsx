import { Card, CardHeader, CardTitle } from '../../components/Card';

// Static content — mirrors src/main/webapp/WEB-INF/jsps/main/resources.jsp,
// with one deliberate deviation: the "Resources" label moved from a plain
// <p> inline in the card body into a real CardHeader, matching every other
// widget's now-consistent header instead of legacy's one-off inline-text
// pattern. This supersedes links.jsp/dark-pack.jsp: neither is referenced by
// main/layout.jsp (or anywhere else) anymore — only resources.jsp is, having
// consolidated both into one compact card.
const RESOURCE_LINKS: { href: string; icon: string; label: string }[] = [
  { href: 'http://www.vekn.net/rulebook', icon: 'bi-book', label: 'Rulebook' },
  { href: 'https://codex-of-the-damned.org/', icon: 'bi-journal-text', label: 'Codex' },
  { href: 'https://amaranth.vtes.co.nz/', icon: 'bi-collection', label: 'Amaranth' },
  { href: 'https://vdb.im/', icon: 'bi-database', label: 'VDB' },
  { href: 'https://discord.gg/fJjac75', icon: 'bi-discord', label: 'Discord' },
  { href: 'https://www.facebook.com/groups/jolstatus/', icon: 'bi-facebook', label: 'Facebook' },
  { href: 'https://github.com/VTES-JOL/JOL/issues', icon: 'bi-bug', label: 'Report Bug' },
  { href: 'https://www.paypal.com/donate/?hosted_button_id=A8PKSSCTV92A2', icon: 'bi-heart', label: 'Donate' },
];

export function Resources() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Resources</CardTitle>
      </CardHeader>
      <div className="card-body p-2">
        <div className="row row-cols-2 g-1 mb-2">
          {RESOURCE_LINKS.map(({ href, icon, label }) => (
            <div key={href} className="col">
              <a
                className="btn btn-sm btn-outline-secondary w-100 text-start d-flex align-items-center gap-2"
                href={href}
                target="_blank"
                rel="noreferrer"
              >
                <i className={`bi ${icon}`} />
                <span>{label}</span>
              </a>
            </div>
          ))}
        </div>
        <details className="small text-muted">
          <summary className="d-flex align-items-center gap-2 mb-1" style={{ cursor: 'pointer', listStyle: 'none' }}>
            <img
              src="https://static.deckserver.net/assets/images/darkpack_logo2.png"
              style={{ height: '1.2rem' }}
              alt="Dark Pack"
            />
            <span>Licensing</span>
          </summary>
          <p className="mb-0 ps-1">
            JOL is not an official World of Darkness product. Portions of the materials are the copyrights and
            trademarks of Paradox Interactive AB, and are used with permission. All rights reserved. For more
            information please visit worldofdarkness.com.
          </p>
        </details>
      </div>
    </Card>
  );
}
