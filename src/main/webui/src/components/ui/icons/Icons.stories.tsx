import type { Meta, StoryObj } from '@storybook/react-vite';
import { ClanIcon, CostIcon, DisciplineIcon, PathIcon, TypeIcon } from './index';

const meta = { title: 'UI/Icons', parameters: { layout: 'padded' } } satisfies Meta;
export default meta;
type Story = StoryObj;

const Row = ({ label, children }: { label: string; children: React.ReactNode }) => (
  <div className="jt:flex jt:items-center jt:gap-3 jt:py-2 jt:border-b jt:border-line/40">
    <span className="jt:w-24 jt:text-xs jt:text-ink-muted">{label}</span>
    <div className="jt:flex jt:items-center jt:gap-2">{children}</div>
  </div>
);

export const Gallery: Story = {
  render: () => (
    <div className="jt:bg-base jt:p-4">
      <Row label="Types">
        {['Action', 'Action Modifier', 'Combat', 'Equipment', 'Master', 'Political Action'].map((t) => (
          <TypeIcon key={t} type={t} size={28} />
        ))}
      </Row>
      <Row label="Clans">
        {['Brujah', 'Banu Haqim', 'Malkavian', 'Nosferatu', 'Ventrue'].map((c) => (
          <ClanIcon key={c} clan={c} size={28} />
        ))}
      </Row>
      <Row label="Disciplines">
        {['ANI', 'ani', 'DOM', 'dom', 'OBF', 'obf'].map((d) => (
          <DisciplineIcon key={d} discipline={d} size={28} />
        ))}
      </Row>
      <Row label="Costs">
        <CostIcon type="blood" amount={1} size={28} />
        <CostIcon type="blood" amount={4} size={28} />
        <CostIcon type="pool" amount={3} size={28} />
        <CostIcon type="pool" amount="x" size={28} />
      </Row>
      <Row label="Paths">
        {['Caine', 'Cathari', 'Death', 'Power and the Inner Voice'].map((p) => (
          <PathIcon key={p} path={p} size={28} />
        ))}
      </Row>
    </div>
  ),
};
