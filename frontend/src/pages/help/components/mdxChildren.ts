import { Children, isValidElement, type ComponentType, type ReactElement, type ReactNode } from 'react';

// A handful of Help example components (CardExample nested in RegionExample,
// CommandOption/CommandExample nested in CommandDoc, GameExample nested in
// GameListExample) are written by content authors as plain nested MDX tags
// rather than props, since that's the more approachable authoring shape for
// a non-developer. This pulls just the child elements of one marker
// component out of `children`, in document order, so the parent can build
// its real data structure from their props.
export function childrenOfType<P>(children: ReactNode, type: ComponentType<P>): ReactElement<P>[] {
  const matches: ReactElement<P>[] = [];
  Children.forEach(children, (child) => {
    if (isValidElement(child) && child.type === type) matches.push(child as ReactElement<P>);
  });
  return matches;
}
