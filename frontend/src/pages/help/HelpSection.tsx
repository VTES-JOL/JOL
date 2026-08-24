import { lazy, Suspense, useRef, type ComponentType } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import type { MDXProps } from 'mdx/types';
import { HELP_SECTIONS } from '../../content/help/meta';
import { helpMdxComponents } from './mdxComponents';
import { useCardTooltips } from '../../hooks/useCardTooltips';
import { PageLoading } from '../../components/PageLoading';
import { pathForHelp } from '../../routes';
import './HelpSection.css';

type MdxModule = { default: ComponentType<MDXProps> };

// One loader per content/help/*.mdx file, resolved lazily so a section's
// content only downloads when it's actually visited.
const sectionModules = import.meta.glob<MdxModule>('../../content/help/*.mdx');

// lazy() must only ever be called once per module — calling it fresh on
// every render (e.g. from inside useMemo) would remount the section (and
// lose its Suspense-resolved state) any time this component re-renders for
// an unrelated reason. Caching by slug at module scope, rather than per
// render, guarantees each section's lazy component is created exactly once.
const lazyComponentsBySlug = new Map<string, ComponentType<MDXProps>>();

function lazyContentFor(slug: string): ComponentType<MDXProps> {
  let Content = lazyComponentsBySlug.get(slug);
  if (!Content) {
    Content = lazy(sectionModules[`../../content/help/${slug}.mdx`]);
    lazyComponentsBySlug.set(slug, Content);
  }
  return Content;
}

// Rendered *inside* the Suspense boundary below, wrapping the lazy MDX
// Content itself — not just sibling to it — so this component (and its
// useCardTooltips effect) only commits once Content has actually resolved
// and its card-name links exist in the DOM. Attaching the ref/effect to a
// wrapper outside Suspense instead (as an earlier version did) fires the
// effect as soon as the *fallback* commits, finding zero card-name links on
// a fresh/direct page load; it only appeared to work when navigating
// between sections in the same session because a stale scheduling race
// happened to paper over it.
function HelpContentBody({ section, Content }: { section: string; Content: ComponentType<MDXProps> }) {
  const containerRef = useRef<HTMLDivElement>(null);
  useCardTooltips(containerRef, [section]);

  return (
    <div ref={containerRef} className="help-prose">
      <Content components={helpMdxComponents} />
    </div>
  );
}

export function HelpSection() {
  const { section } = useParams<{ section: string }>();

  const isKnownSection = !!section && HELP_SECTIONS.some((s) => s.slug === section);
  if (!isKnownSection || !section) return <Navigate to={pathForHelp(HELP_SECTIONS[0].slug)} replace />;

  return (
    <Suspense fallback={<PageLoading />}>
      <HelpContentBody section={section} Content={lazyContentFor(section)} />
    </Suspense>
  );
}
