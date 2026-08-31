import { useEffect, useState, type ReactNode } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { Card, CardHeader, CardTitle } from '../../components/Card';
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

function FormRow({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="row mb-2">
      <label className="col-form-label col-3">{label}</label>
      <div className="col-9">{children}</div>
    </div>
  );
}

function RuleList({ rules, onRemove }: { rules: string[]; onRemove: (i: number) => void }) {
  return (
    <div className="mb-2">
      {rules.map((rule, i) => (
        <div className="border rounded m-1" key={i}>
          <label className="form-label m-1">{rule}</label>
          <button
            className="btn btn-outline-secondary btn-sm mt-2 form-control m-1"
            onClick={() => onRemove(i)}
          >
            Remove Rule
          </button>
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
    if (!(await confirmDialog(`Publish "${form.name}"? Players will be able to see and register for this tournament.`))) return;
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
    <Card className="flex-fill d-flex flex-column">
      <CardHeader>
        <span className="d-flex justify-content-between align-items-center w-100">
          <CardTitle>Tournament</CardTitle>
          <span className="d-flex gap-1 align-items-center">
            {msg && (
              <span className={`badge text-bg-light me-1 ${msg.kind === 'success' ? 'text-success' : 'text-warning'}`}>
                {msg.text}
              </span>
            )}
            <button className="btn btn-sm btn-outline-secondary" onClick={save}>
              Save <i className="bi-floppy" />
            </button>
            {form.status === 'EDIT' && (
              <button className="btn btn-sm btn-success" onClick={publish}>
                Publish <i className="bi-send" />
              </button>
            )}
          </span>
        </span>
      </CardHeader>
      <div className="card-body p-2 flex-fill overflow-auto px-3 min-h-0">
        <FormRow label="Name">
          <input className="form-control form-control-sm" value={form.name} onChange={(e) => set('name', e.target.value)} />
        </FormRow>
        <FormRow label="Reg Start">
          <input
            type="date"
            className="form-control form-control-sm"
            value={form.regStart}
            onChange={(e) => set('regStart', e.target.value)}
          />
        </FormRow>
        <FormRow label="Reg End">
          <input
            type="date"
            className="form-control form-control-sm"
            value={form.regEnd}
            onChange={(e) => set('regEnd', e.target.value)}
          />
        </FormRow>
        <FormRow label="Play Start">
          <input
            type="date"
            className="form-control form-control-sm"
            value={form.playStart}
            onChange={(e) => set('playStart', e.target.value)}
          />
        </FormRow>
        <FormRow label="Play End">
          <input
            type="date"
            className="form-control form-control-sm"
            value={form.playEnd}
            onChange={(e) => set('playEnd', e.target.value)}
          />
        </FormRow>
        <FormRow label="Rounds">
          <select className="form-select form-select-sm" value={form.numOfRounds} onChange={(e) => set('numOfRounds', e.target.value)}>
            {[2, 3].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </FormRow>
        <FormRow label="VEKN ID">
          <select className="form-select form-select-sm" value={form.reqId} onChange={(e) => set('reqId', e.target.value)}>
            <option value="true">Required</option>
            <option value="false">Not Required</option>
          </select>
        </FormRow>
        <FormRow label="Format">
          <select className="form-select form-select-sm" value={form.tourFormat} onChange={(e) => set('tourFormat', e.target.value)}>
            <option value="SINGLE_DECK">Single Deck</option>
            <option value="MULTI_DECK">Multi-Deck</option>
          </select>
        </FormRow>
        <FormRow label="Game">
          <select className="form-select form-select-sm" value={form.gameFormat} onChange={(e) => set('gameFormat', e.target.value)}>
            <option value="STANDARD">Standard</option>
            <option value="V5">V5</option>
            <option value="DUEL">Duel</option>
            <option value="PLAYTEST">Playtest</option>
          </select>
        </FormRow>
        <label className="form-label small text-muted mb-1">Tournament Rules</label>
        <div className="input-group input-group-sm mb-1">
          <input
            className="form-control form-control-sm"
            placeholder="Add a rule..."
            value={ruleText}
            onChange={(e) => setRuleText(e.target.value)}
          />
          <button onClick={addRule} className="btn btn-outline-secondary btn-sm">
            Add
          </button>
        </div>
        <RuleList rules={form.rules} onRemove={(i) => set('rules', form.rules.filter((_, idx) => idx !== i))} />
        <label className="form-label small text-muted mb-1">Special Rules Condition</label>
        <textarea
          rows={3}
          className="form-control form-control-sm mb-2"
          value={form.specRulesCon}
          onChange={(e) => set('specRulesCon', e.target.value)}
        />
        <div className="input-group input-group-sm mb-1">
          <input
            className="form-control form-control-sm"
            placeholder="Add a special rule..."
            value={specRuleText}
            onChange={(e) => setSpecRuleText(e.target.value)}
          />
          <button onClick={addSpecRule} className="btn btn-outline-secondary btn-sm">
            Add
          </button>
        </div>
        <RuleList rules={form.specRules} onRemove={(i) => set('specRules', form.specRules.filter((_, idx) => idx !== i))} />
        <div className="mt-2">
          <strong>Registered Players</strong>
          <ul className="list-unstyled small mb-0">
            {registeredPlayers.map((reg) => (
              <li key={reg.player}>
                {reg.player}
                {reg.vekn ? ` (${reg.vekn})` : ''}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </Card>
  );
}
