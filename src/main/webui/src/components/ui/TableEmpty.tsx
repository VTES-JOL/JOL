import type { ComponentType } from 'react';
import { EmptyState } from './EmptyState';

/**
 * An <EmptyState> that spans a table, so an empty result shows a message
 * instead of a bare header row over blank space. Render it as its own
 * `<tbody>` in place of the data `<tbody>` — a table with two `<tbody>`
 * elements is valid, and only one is mounted at a time here.
 */
interface TableEmptyProps {
  colSpan: number;
  icon?: ComponentType<{ size?: number; className?: string }>;
  title: string;
  description?: string;
}

export function TableEmpty({ colSpan, icon, title, description }: TableEmptyProps) {
  return (
    <tbody>
      <tr>
        <td colSpan={colSpan} className="py-12">
          <EmptyState icon={icon} title={title} description={description} />
        </td>
      </tr>
    </tbody>
  );
}
