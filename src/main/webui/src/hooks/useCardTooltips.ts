import { useEffect, useRef, type RefObject } from 'react';
import tippy, { hideAll, type Instance } from 'tippy.js';
import 'tippy.js/dist/tippy.css';
import { getBaseUrl } from '../api/config';

function cardImagePlaceholder(label: string): HTMLElement {
  const el = document.createElement('div');
  el.className =
    'card-tooltip-image d-flex flex-column align-items-center justify-content-center gap-2 text-white-50 bg-dark';

  const icon = document.createElement('i');
  icon.className = 'bi bi-image';
  icon.style.fontSize = '2.5rem';

  const text = document.createElement('span');
  text.className = 'px-2 text-center small';
  text.textContent = label || 'Image unavailable';

  el.append(icon, text);
  return el;
}

/**
 * Attaches a hoverable card-image tooltip to every `a.card-name[data-card-id]`
 * link inside containerRef — ParserService.parseGlobalChat already renders
 * card mentions like `[Anarch Convert]` as that exact markup server-side
 * (see net.deckserver.services.ParserService#generateCardLink), this just
 * restores the client-side behavior ds.js's addCardTooltips() provided.
 *
 * Re-runs whenever `deps` changes (e.g. the chat log grows) but only
 * attaches to links that don't already have a tippy instance — checked via
 * tippy's own `_tippy` marker, not a custom DOM attribute, since a custom
 * attribute would survive React 18 StrictMode's dev-only mount→cleanup→
 * remount cycle in a way that leaves tooltips permanently unattached.
 * Instances persist across re-runs and are only destroyed on unmount.
 */
export function useCardTooltips(containerRef: RefObject<HTMLElement | null>, deps: unknown[]) {
  const instancesRef = useRef<Instance[]>([]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    const links = container.querySelectorAll<HTMLElement & { _tippy?: Instance }>('a.card-name');

    links.forEach((link) => {
      if (link._tippy) return;
      // These anchors carry no `href` (React-rendered CardLink/Card.tsx, and
      // server-rendered markup from ParserService/JolGame's generateCardLink)
      // — an <a> without href gets no implicit role and isn't in the tab
      // order, so without this a keyboard or screen-reader user has no way
      // to discover or open the tooltip at all. tippy's default trigger
      // already includes 'focus', so making the element focusable is enough.
      if (!link.hasAttribute('tabindex')) link.tabIndex = 0;
      if (!link.hasAttribute('role')) link.setAttribute('role', 'link');
      instancesRef.current.push(
        tippy(link, {
          placement: 'auto',
          allowHTML: true,
          appendTo: () => document.body,
          // The chat log scrolls (overflow-y: auto) — with the default
          // `strategy: 'absolute'` popper positioning, a tooltip anchored to
          // a link inside that scroll container gets clipped to its bounds
          // (often to the point of being entirely invisible) regardless of
          // appendTo. 'fixed' positions relative to the viewport instead,
          // escaping the clip — same as ds.js's addCardTooltips() used.
          popperOptions: {
            strategy: 'fixed',
            modifiers: [
              { name: 'flip', options: { fallbackPlacements: ['bottom', 'right'] } },
              { name: 'preventOverflow', options: { altAxis: true, tether: false } },
            ],
          },
          onTrigger(_instance, event) {
            event.stopPropagation();
          },
          theme: 'cards',
          touch: 'hold',
          content: 'Loading...',
          onShow(instance) {
            hideAll({ exclude: instance });
            const cardId = link.dataset.cardId;
            const secured = link.dataset.secured === 'true' ? 'secured/' : '';
            getBaseUrl().then((baseUrl) => {
              const img = document.createElement('img');
              img.className = 'card-tooltip-image';
              img.alt = link.textContent ?? '';
              // A missing/broken card asset otherwise renders as the
              // browser's default broken-image icon with the alt text next
              // to it — this swaps it for a plain placeholder instead, so a
              // gap in the local static/ card-asset mirror (see
              // serveCardAssets.ts) doesn't look like the tooltip itself is
              // malfunctioning.
              img.onerror = () => instance.setContent(cardImagePlaceholder(img.alt));
              img.src = `${baseUrl}/${secured}images/${cardId}`;
              instance.setContent(img);
            });
          },
        }),
      );
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  // Destroy everything accumulated across every run, but only on unmount —
  // not on every dependency change, which would tear down tooltips for
  // messages still visible on screen (only new ones were appended).
  useEffect(() => {
    return () => {
      instancesRef.current.forEach((instance) => instance.destroy());
      instancesRef.current = [];
    };
  }, []);
}
