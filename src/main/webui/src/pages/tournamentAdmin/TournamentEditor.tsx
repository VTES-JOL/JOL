import { useEffect, useState, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Save, Send } from 'lucide-react';
import { Panel } from '../../components/ui/Panel';
import { Button } from '../../components/ui/Button';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Textarea } from '../../components/ui/Textarea';
import { api } from '../../api/client';
import type { TournamentDetails, TournamentRegistration } from '../../api/types';
import { confirmDialog } from '../../stores/dialog';

const DEFAULT_SPEC_RULES_CON =
  'The following JOL rules will be enforced for the duration of the rounds with the exception of the period between <Date> and <Date>.';

interface FormState {
  name: string;
  regStart: string;
  regEnd: string;
  playStart: string;
  playEnd: string;
  numOfRounds: string;
  reqId: string;
  tourFormat: string;
  gameFormat: string;
  rules: string[];
  specRulesCon: string;
  specRules: string[];
  status: string;
}

const BLANK: FormState = {
  name: '',
  regStart: '',
  regEnd: '',
  playStart: '',
  playEnd: '',
  numOfRounds: '2',
  reqId: 'true',
  tourFormat: 'SINGLE_DECK',
  gameFormat: 'STANDARD',
  rules: [],
  specRulesCon: DEFAULT_SPEC_RULES_CON,
  specRules: [],
  status: 'EDIT',
};

const DATE_INPUT =
  'w-full rounded border border-line bg-surface/70 px-2 py-1 text-sm text-ink outline-none focus:border-accent/60';

function FormRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="grid grid-cols-[7rem_1fr] items-center gap-2 mb-2">
      <label className="text-xs text-ink-muted">{label}</label>
      <div>{children}</div>
    </div>
  );
}

function RuleList({ rules, onRemove }: { rules: string[]; onRemove: (i: number) => void }) {
  return (
    <div className="mb-2 flex flex-col gap-1">
      {rules.map((rule, i) => (
        <div className="border border-line rounded p-2 flex justify-between items-center gap-2" key={i}>
          <span className="text-sm">{rule}</span>
          <Button variant="secondary" size="sm" onClick={() => onRemove(i)}>
            Remove
          </Button>
        </div>
      ))}
    </div>
  );
}

export function TournamentEditor({
  tournamentName,
  onSaved,
  onDirtyChange,
}: {
  tournamentName: string | null;
  onSaved: () => void;
  // Lets the parent guard navigation away from unsaved edits (see
  // TournamentAdminPage's selectTournament/newTournament) — this component
  // has no say over `tournamentName` itself, so it can't block the prop
  // change that would otherwise silently discard the form.
  onDirtyChange?: (dirty: boolean) => void;
}) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(BLANK);
  // The last-loaded (or last-saved) form contents — `form` is compared
  // against this to derive dirtiness, below.
  const [baseline, setBaseline] = useState<FormState>(BLANK);
  const [originalName, setOriginalName] = useState('');
  const [ruleText, setRuleText] = useState('');
  const [specRuleText, setSpecRuleText] = useState('');
  const [msg, setMsg] = useState<{ text: string; kind: 'success' | 'warning' } | null>(null);

  const { data: details } = useQuery({
    queryKey: ['tournament', 'details', tournamentName],
    queryFn: () => api.get<TournamentDetails>(`/tournament/${encodeURIComponent(tournamentName!)}/details`),
    enabled: !!tournamentName,
  });

  const { data: registeredPlayers = [] } = useQuery({
    queryKey: ['tournament', 'registered', tournamentName],
    queryFn: () => api.get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName!)}/registered`),
    enabled: !!tournamentName,
  });

  useEffect(() => {
    if (!tournamentName) {
      setForm(BLANK);
      setBaseline(BLANK);
      setOriginalName('');
      setMsg(null);
      return;
    }
    if (!details) return;
    const loaded: FormState = {
      name: details.name,
      regStart: details.regStart,
      regEnd: details.regEnd,
      playStart: details.playStart,
      playEnd: details.playEnd,
      numOfRounds: String(details.numRounds),
      reqId: details.reqId,
      tourFormat: details.tourFormat,
      gameFormat: details.gameFormat,
      rules: details.rules ?? [],
      specRulesCon: details.specRulesCon,
      specRules: details.specRules ?? [],
      status: details.status,
    };
    setForm(loaded);
    setBaseline(loaded);
    setOriginalName(details.name);
  }, [tournamentName, details]);

  const dirty = JSON.stringify(form) !== JSON.stringify(baseline);
  useEffect(() => {
    onDirtyChange?.(dirty);
    // onDirtyChange isn't expected to be referentially stable across parent
    // renders — only re-notify when dirtiness itself actually changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dirty]);

  const set = <K extends keyof FormState>(key: K, value: FormState[K]) => setForm((prev) => ({ ...prev, [key]: value }));

  const save = () => {
    api
      .post<boolean>('/tournament', {
        tourName: form.name,
        regStart: form.regStart,
        regEnd: form.regEnd,
        playStart: form.playStart,
        playEnd: form.playEnd,
        tourFormat: form.tourFormat,
        gameFormat: form.gameFormat,
        rules: form.rules,
        specRulesCon: form.specRulesCon,
        specRules: form.specRules,
        numberOfRounds: form.numOfRounds,
        reqId: form.reqId,
        originalName,
      })
      .then((success) => {
        if (success) {
          setOriginalName(form.name);
          setBaseline(form);
          setMsg({ text: 'Tournament saved', kind: 'success' });
          queryClient.invalidateQueries({ queryKey: ['tournament'] });
          onSaved();
        } else {
          setMsg({ text: 'Tournament creation failed', kind: 'warning' });
        }
      })
      .catch((err) => {
        console.error('Failed to save tournament', err);
        setMsg({ text: 'Tournament creation failed', kind: 'warning' });
      });
  };

  const publish = async () => {
    if (!form.name) return;
    if (
      !(await confirmDialog('Players will be able to see and register for it.', {
        title: `Publish “${form.name}”?`,
        confirmLabel: 'Publish',
      }))
    )
      return;
    api
      .post<boolean>(`/tournament/${encodeURIComponent(form.name)}/publish`)
      .then((success) => {
        if (success) {
          setForm((prev) => ({ ...prev, status: 'STARTING' }));
          setBaseline((prev) => ({ ...prev, status: 'STARTING' }));
          setMsg({ text: 'Tournament published', kind: 'success' });
          queryClient.invalidateQueries({ queryKey: ['tournament'] });
          onSaved();
        } else {
          setMsg({ text: 'Publish failed', kind: 'warning' });
        }
      })
      .catch((err) => {
        console.error('Failed to publish tournament', err);
        setMsg({ text: 'Publish failed', kind: 'warning' });
      });
  };

  const addRule = () => {
    if (!ruleText) return;
    set('rules', [...form.rules, ruleText]);
    setRuleText('');
  };

  const addSpecRule = () => {
    if (!specRuleText) return;
    set('specRules', [...form.specRules, specRuleText]);
    setSpecRuleText('');
  };

  return (
    <Panel
      title="Tournament"
      right={
        <span className="flex items-center gap-1">
          {msg && (
            <span className={`text-xs ${msg.kind === 'success' ? 'text-online' : 'text-gold'}`}>{msg.text}</span>
          )}
          <Button variant="secondary" size="sm" icon={<Save size={13} />} onClick={save}>
            Save
          </Button>
          {form.status === 'EDIT' && (
            <Button size="sm" icon={<Send size={13} />} className="bg-online text-surface hover:opacity-90" onClick={publish}>
              Publish
            </Button>
          )}
        </span>
      }
    >
      <div className="flex-1 min-h-0 overflow-y-auto p-3 text-sm">
        <FormRow label="Name">
          <input className={DATE_INPUT} value={form.name} onChange={(e) => set('name', e.target.value)} />
        </FormRow>
        <FormRow label="Reg Start">
          <input type="date" className={DATE_INPUT} value={form.regStart} onChange={(e) => set('regStart', e.target.value)} />
        </FormRow>
        <FormRow label="Reg End">
          <input type="date" className={DATE_INPUT} value={form.regEnd} onChange={(e) => set('regEnd', e.target.value)} />
        </FormRow>
        <FormRow label="Play Start">
          <input type="date" className={DATE_INPUT} value={form.playStart} onChange={(e) => set('playStart', e.target.value)} />
        </FormRow>
        <FormRow label="Play End">
          <input type="date" className={DATE_INPUT} value={form.playEnd} onChange={(e) => set('playEnd', e.target.value)} />
        </FormRow>
        <FormRow label="Rounds">
          <Select size="sm" value={form.numOfRounds} onChange={(e) => set('numOfRounds', e.target.value)}>
            {[2, 3].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </Select>
        </FormRow>
        <FormRow label="VEKN ID">
          <Select size="sm" value={form.reqId} onChange={(e) => set('reqId', e.target.value)}>
            <option value="true">Required</option>
            <option value="false">Not Required</option>
          </Select>
        </FormRow>
        <FormRow label="Format">
          <Select size="sm" value={form.tourFormat} onChange={(e) => set('tourFormat', e.target.value)}>
            <option value="SINGLE_DECK">Single Deck</option>
            <option value="MULTI_DECK">Multi-Deck</option>
          </Select>
        </FormRow>
        <FormRow label="Game">
          <Select size="sm" value={form.gameFormat} onChange={(e) => set('gameFormat', e.target.value)}>
            <option value="STANDARD">Standard</option>
            <option value="V5">V5</option>
            <option value="DUEL">Duel</option>
          </Select>
        </FormRow>

        <label className="block text-xs text-ink-muted mb-1">Tournament Rules</label>
        <div className="flex gap-1 mb-1">
          <Input srLabel="Rule" size="sm" placeholder="Add a rule..." value={ruleText} onChange={(e) => setRuleText(e.target.value)} />
          <Button variant="secondary" size="sm" onClick={addRule}>
            Add
          </Button>
        </div>
        <RuleList rules={form.rules} onRemove={(i) => set('rules', form.rules.filter((_, idx) => idx !== i))} />

        <label className="block text-xs text-ink-muted mb-1">Special Rules Condition</label>
        <Textarea
          srLabel="Special rules condition"
          rows={3}
          className="mb-2 text-sm"
          value={form.specRulesCon}
          onChange={(e) => set('specRulesCon', e.target.value)}
        />
        <div className="flex gap-1 mb-1">
          <Input
            srLabel="Special rule"
            size="sm"
            placeholder="Add a special rule..."
            value={specRuleText}
            onChange={(e) => setSpecRuleText(e.target.value)}
          />
          <Button variant="secondary" size="sm" onClick={addSpecRule}>
            Add
          </Button>
        </div>
        <RuleList rules={form.specRules} onRemove={(i) => set('specRules', form.specRules.filter((_, idx) => idx !== i))} />

        <div className="mt-2">
          <strong>Registered Players</strong>
          <ul className="list-none text-sm mb-0">
            {registeredPlayers.map((reg) => (
              <li key={reg.player}>
                {reg.player}
                {reg.vekn ? ` (${reg.vekn})` : ''}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </Panel>
  );
}
