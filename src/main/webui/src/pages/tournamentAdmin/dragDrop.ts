import type { DragEvent } from 'react';

// Minimal native HTML5 drag-and-drop, replacing jQuery UI's `.sortable()` with
// `connectWith` (ds.js moved player chips between a round's pool and any of
// its tables via that). No new dependency — just dataTransfer carrying which
// player is being dragged and where it came from.
export interface DragPayload {
  player: string;
  from: string | number; // e.g. 'pool'/'table', or a table index within a round
}

export function draggableChip(payload: DragPayload) {
  return {
    draggable: true,
    onDragStart: (e: DragEvent) => {
      e.dataTransfer.setData('application/json', JSON.stringify(payload));
      e.dataTransfer.effectAllowed = 'move';
    },
  };
}

export function dropTarget(onDrop: (payload: DragPayload) => void) {
  return {
    onDragOver: (e: DragEvent) => e.preventDefault(),
    onDrop: (e: DragEvent) => {
      e.preventDefault();
      const raw = e.dataTransfer.getData('application/json');
      if (!raw) return;
      onDrop(JSON.parse(raw) as DragPayload);
    },
  };
}
