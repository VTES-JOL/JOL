import { useEffect, useRef, type RefObject } from 'react';
import tippy, { hideAll, type Instance } from 'tippy.js';
import 'tippy.js/dist/tippy.css';
import { getBaseUrl } from '../api/config';

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
              instance.setContent(
                `<img width="250" height="358" src="${baseUrl}/${secured}images/${cardId}" alt="${link.textContent ?? ''}" />`,
              );
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
