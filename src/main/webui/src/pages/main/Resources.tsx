import type { ComponentType } from 'react';
import { BookOpen, Bug, ChevronRight, Database, Heart, Layers, MessageCircle, ScrollText, Users } from 'lucide-react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';

// Static content — mirrors src/main/webapp/WEB-INF/jsps/main/resources.jsp,
// consolidating the old links.jsp/dark-pack.jsp into one compact card.
const RESOURCE_LINKS: { href: string; icon: ComponentType<{ size?: number }>; label: string }[] = [
  { href: 'http://www.vekn.net/rulebook', icon: BookOpen, label: 'Rulebook' },
  { href: 'https://codex-of-the-damned.org/', icon: ScrollText, label: 'Codex' },
  { href: 'https://amaranth.vtes.co.nz/', icon: Layers, label: 'Amaranth' },
  { href: 'https://vdb.im/', icon: Database, label: 'VDB' },
  { href: 'https://discord.gg/fJjac75', icon: MessageCircle, label: 'Discord' },
  { href: 'https://www.facebook.com/groups/jolstatus/', icon: Users, label: 'Facebook' },
  { href: 'https://github.com/VTES-JOL/JOL/issues', icon: Bug, label: 'Report Bug' },
  { href: 'https://www.paypal.com/donate/?hosted_button_id=A8PKSSCTV92A2', icon: Heart, label: 'Donate' },
];

export function Resources() {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Resources</CardTitle>
      </CardHeader>
      <CardBody className="p-2">
        <div className="grid grid-cols-2 gap-1 mb-2">
          {RESOURCE_LINKS.map(({ href, icon: Icon, label }) => (
            <a
              key={href}
              className="flex items-center gap-2 px-2 py-1 text-sm rounded border border-line-accent text-ink-secondary hover:text-ink hover:bg-hover no-underline"
              href={href}
              target="_blank"
              rel="noreferrer"
            >
              <Icon size={14} />
              <span>{label}</span>
            </a>
          ))}
        </div>
        <details className="group text-xs text-ink-muted">
          <summary className="flex items-center gap-2 px-2 py-1 rounded border border-line-accent cursor-pointer list-none text-ink-secondary hover:text-ink hover:bg-hover">
            <img
              src="https://static.deckserver.net/assets/images/darkpack_logo2.png"
              className="h-[1.1rem]"
              alt="Dark Pack"
            />
            <span className="flex-1">Licensing</span>
            <ChevronRight size={13} className="transition-transform group-open:rotate-90" />
          </summary>
          <p className="mt-1 mb-0 pl-1">
            JOL is not an official World of Darkness product. Portions of the materials are the copyrights and
            trademarks of Paradox Interactive AB, and are used with permission. All rights reserved. For more
            information please visit worldofdarkness.com.
          </p>
        </details>
      </CardBody>
    </Card>
  );
}
