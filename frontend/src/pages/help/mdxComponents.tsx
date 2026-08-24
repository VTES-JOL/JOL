import type { AnchorHTMLAttributes } from 'react';
import { Link } from 'react-router-dom';
import type { MDXComponents } from 'mdx/types';
import { CommandDoc, CommandOption, CommandExample } from './components/CommandDoc';
import { Callout } from './components/Callout';
import { CardExample } from './components/CardExample';
import { RegionExample } from './components/RegionExample';
import { GameListExample, GameExample } from './components/GameListExample';
import { DeckPreviewExample } from './components/DeckPreviewExample';
import { CardLink } from './components/CardLink';

// The full set of tags a Help .mdx file can use, passed to each compiled
// MDX component's `components` prop by HelpSection. Custom tags use flat,
// HTML-style attributes (no JS object literals) so a non-developer can write
// and copy/tweak them directly — see the Help route plan's "MDX example
// syntax" decision. table/code get the app's normal Bootstrap treatment
// instead of MDX's unstyled defaults.
export const helpMdxComponents: MDXComponents = {
  CommandDoc,
  CommandOption,
  CommandExample,
  Callout,
  CardExample,
  RegionExample,
  GameListExample,
  GameExample,
  DeckPreviewExample,
  CardLink,
  table: (props) => <table className="table table-sm" {...props} />,
  code: (props) => <code className="bg-body-tertiary px-1 rounded" {...props} />,
  // Cross-links between Help sections (e.g. "see Deck Editor") stay
  // client-side navigation via react-router instead of a full page reload.
  a: ({ href, children, ...rest }: AnchorHTMLAttributes<HTMLAnchorElement>) =>
    href?.startsWith('/') ? (
      <Link to={href} {...rest}>
        {children}
      </Link>
    ) : (
      <a href={href} target="_blank" rel="noreferrer" {...rest}>
        {children}
      </a>
    ),
};
