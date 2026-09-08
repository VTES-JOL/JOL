import type { ReactNode } from 'react';
import { useIsMobile } from '../../hooks/useMediaQuery';
import { Modal } from '../../components/ui/Modal';
import { BottomSheet } from './BottomSheet';

// #14 (D32): a card dialog that renders as a centered <Modal> on pointer /
// desktop and as a bottom <BottomSheet> below md — so playing or acting on a
// card on a phone is a thumb-reachable sheet, not a scrolling centered dialog.
// Mirrors the CardContextMenu → CardActionSheet split (D29) so the two touch
// surfaces behave the same way.
export function CardDialog({
  title,
  onClose,
  bodyClassName,
  children,
}: {
  title: ReactNode;
  onClose: () => void;
  /** Applied to the body wrapper in both presentations. */
  bodyClassName?: string;
  children: ReactNode;
}) {
  const isMobile = useIsMobile();

  if (isMobile) {
    return (
      <BottomSheet
        open
        onClose={onClose}
        label={typeof title === 'string' ? title : undefined}
        header={<span className="min-w-0 flex-1 truncate text-sm font-semibold text-ink">{title}</span>}
      >
        <div className={bodyClassName ?? 'flex flex-col gap-3 p-4'}>{children}</div>
      </BottomSheet>
    );
  }

  return (
    <Modal onClose={onClose} title={title} bodyClassName={bodyClassName}>
      {children}
    </Modal>
  );
}
