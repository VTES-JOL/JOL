import { useEffect, useRef, type RefObject } from 'react';
import tippy, { type Instance } from 'tippy.js';

/**
 * Attaches a plain text tooltip (tippy reads content from the element's own
 * data-tippy-content attribute automatically) to every matching element
 * inside containerRef — same accumulate-and-only-destroy-on-unmount pattern
 * as useCardTooltips, for the same reasons (see its comment): destroying on
 * every dependency change would tear down tooltips for still-visible
 * elements, and a custom "already attached" marker breaks under React 18
 * StrictMode's dev-only double-invoke, so tippy's own `_tippy` marker is
 * used instead.
 */
export function useSimpleTooltips(containerRef: RefObject<HTMLElement | null>, deps: unknown[]) {
  const instancesRef = useRef<Instance[]>([]);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;
    const targets = container.querySelectorAll<HTMLElement & { _tippy?: Instance }>('[data-tippy-content]');
    targets.forEach((el) => {
      if (el._tippy) return;
      instancesRef.current.push(tippy(el, { theme: 'light' }));
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    return () => {
      instancesRef.current.forEach((instance) => instance.destroy());
      instancesRef.current = [];
    };
  }, []);
}
