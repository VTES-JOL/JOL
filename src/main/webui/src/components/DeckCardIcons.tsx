import { Fragment } from 'react';
import type { CardDetail } from '../api/types';
import { ClanIcon, CostIcon, DisciplineIcon, PathIcon } from './ui/icons';

const SEP = 'text-[10px] text-ink-muted leading-none select-none';

/**
 * The clan / discipline / path / cost icon cluster for one card, driven by its
 * {@link CardDetail}. Crypt cards show what they *have* (clan, disciplines,
 * capacity); library cards show what they *require* (requirement clan/path,
 * or-/and-disciplines) plus their pool/blood cost.
 *
 * Shared by every read-only deck view (lobby / game / tournament preview) via
 * {@link DeckPreview}; the deck editor's own rows still inline an equivalent
 * cluster with editor-specific spacing.
 */
export function DeckCardIcons({ detail, size = 14 }: { detail: CardDetail; size?: number }) {
  if (detail.crypt) {
    return (
      <span className="inline-flex items-center gap-0.5 align-middle">
        {detail.path && <PathIcon path={detail.path} size={size} />}
        {detail.clan && <ClanIcon clan={detail.clan} size={size} />}
        {detail.disciplines.map((d, i) => (
          <DisciplineIcon key={i} discipline={d} size={size} />
        ))}
        {detail.capacity != null && (
          <span className="ml-0.5 inline-flex items-center justify-center min-w-[16px] h-[16px] px-1 rounded bg-blood/15 text-blood-soft text-[10px] font-semibold tabular-nums">
            {detail.capacity}
          </span>
        )}
      </span>
    );
  }

  const hasReq =
    detail.requirementPath != null ||
    detail.requirementClans.length > 0 ||
    detail.orDisciplines.length > 0 ||
    detail.andDisciplines.length > 0;
  const hasCost = detail.poolCost != null || detail.bloodCost != null;
  if (!hasReq && !hasCost) return null;

  return (
    <span className="inline-flex items-center gap-0.5 align-middle">
      {detail.poolCost != null && (
        <CostIcon type="pool" amount={detail.poolCost === -1 ? 'x' : detail.poolCost} size={size + 4} />
      )}
      {detail.bloodCost != null && (
        <CostIcon type="blood" amount={detail.bloodCost === -1 ? 'x' : detail.bloodCost} size={size + 4} />
      )}
      {detail.requirementPath && <PathIcon path={detail.requirementPath} size={size} />}
      {detail.requirementClans.map((c, i) => (
        <ClanIcon key={i} clan={c} size={size} />
      ))}
      {detail.orDisciplines.map((d, i) => (
        <Fragment key={`or-${i}`}>
          {i > 0 && <span className={SEP}>/</span>}
          <DisciplineIcon discipline={d} size={size} />
        </Fragment>
      ))}
      {detail.andDisciplines.map((d, i) => (
        <Fragment key={`and-${i}`}>
          {i > 0 && <span className={SEP}>&amp;</span>}
          <DisciplineIcon discipline={d} size={size} />
        </Fragment>
      ))}
    </span>
  );
}
