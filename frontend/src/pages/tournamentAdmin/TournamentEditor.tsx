import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
import { api } from '../../api/client';
import type { TournamentDetails, TournamentRegistration } from '../../api/types';

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
}: {
  tournamentName: string | null;
  onSaved: () => void;
}) {
  const [form, setForm] = useState<FormState>(BLANK);
  const [originalName, setOriginalName] = useState('');
  const [registeredPlayers, setRegisteredPlayers] = useState<TournamentRegistration[]>([]);
  const [ruleText, setRuleText] = useState('');
  const [specRuleText, setSpecRuleText] = useState('');
  const [msg, setMsg] = useState<{ text: string; kind: 'success' | 'warning' } | null>(null);

  useEffect(() => {
    if (!tournamentName) {
      setForm(BLANK);
      setOriginalName('');
      setRegisteredPlayers([]);
      setMsg(null);
      return;
    }
    api
      .get<TournamentDetails>(`/tournament/${encodeURIComponent(tournamentName)}/details`)
      .then((data) => {
        setForm({
          name: data.name,
          regStart: data.regStart,
          regEnd: data.regEnd,
          playStart: data.playStart,
          playEnd: data.playEnd,
          numOfRounds: String(data.numRounds),
          reqId: data.reqId,
          tourFormat: data.tourFormat,
          gameFormat: data.gameFormat,
          rules: data.rules ?? [],
          specRulesCon: data.specRulesCon,
          specRules: data.specRules ?? [],
          status: data.status,
        });
        setOriginalName(data.name);
      })
      .catch((err) => console.error('Failed to load tournament details', err));
    api
      .get<TournamentRegistration[]>(`/tournament/${encodeURIComponent(tournamentName)}/registered`)
      .then(setRegisteredPlayers)
      .catch((err) => console.error('Failed to load registered players', err));
  }, [tournamentName]);

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
          setMsg({ text: 'Tournament saved', kind: 'success' });
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

  const publish = () => {
    if (!form.name) return;
    if (!confirm(`Publish "${form.name}"? Players will be able to see and register for this tournament.`)) return;
    api
      .post<boolean>(`/tournament/${encodeURIComponent(form.name)}/publish`)
      .then((success) => {
        if (success) {
          setForm((prev) => ({ ...prev, status: 'STARTING' }));
          setMsg({ text: 'Tournament published', kind: 'success' });
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
        <div className="row mb-2">
          <label className="col-form-label col-3">Name</label>
          <div className="col-9">
            <input className="form-control form-control-sm" value={form.name} onChange={(e) => set('name', e.target.value)} />
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Reg Start</label>
          <div className="col-9">
            <input
              type="date"
              className="form-control form-control-sm"
              value={form.regStart}
              onChange={(e) => set('regStart', e.target.value)}
            />
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Reg End</label>
          <div className="col-9">
            <input
              type="date"
              className="form-control form-control-sm"
              value={form.regEnd}
              onChange={(e) => set('regEnd', e.target.value)}
            />
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Play Start</label>
          <div className="col-9">
            <input
              type="date"
              className="form-control form-control-sm"
              value={form.playStart}
              onChange={(e) => set('playStart', e.target.value)}
            />
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Play End</label>
          <div className="col-9">
            <input
              type="date"
              className="form-control form-control-sm"
              value={form.playEnd}
              onChange={(e) => set('playEnd', e.target.value)}
            />
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Rounds</label>
          <div className="col-9">
            <select
              className="form-select form-select-sm"
              value={form.numOfRounds}
              onChange={(e) => set('numOfRounds', e.target.value)}
            >
              {[2, 3].map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">VEKN ID</label>
          <div className="col-9">
            <select className="form-select form-select-sm" value={form.reqId} onChange={(e) => set('reqId', e.target.value)}>
              <option value="true">Required</option>
              <option value="false">Not Required</option>
            </select>
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Format</label>
          <div className="col-9">
            <select
              className="form-select form-select-sm"
              value={form.tourFormat}
              onChange={(e) => set('tourFormat', e.target.value)}
            >
              <option value="SINGLE_DECK">Single Deck</option>
              <option value="MULTI_DECK">Multi-Deck</option>
            </select>
          </div>
        </div>
        <div className="row mb-2">
          <label className="col-form-label col-3">Game</label>
          <div className="col-9">
            <select
              className="form-select form-select-sm"
              value={form.gameFormat}
              onChange={(e) => set('gameFormat', e.target.value)}
            >
              <option value="STANDARD">Standard</option>
              <option value="V5">V5</option>
              <option value="DUEL">Duel</option>
              <option value="PLAYTEST">Playtest</option>
            </select>
          </div>
        </div>
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
