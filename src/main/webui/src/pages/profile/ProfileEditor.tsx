import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { FieldHint } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import type { CountryOption, Profile } from '../../api/types';
import { useSave } from './saveState';
import { SaveNote } from './SaveNote';

const digitsOnly = (v: string) => v.replace(/\D+/g, '');

interface Form {
  email: string;
  country: string;
  veknID: string;
  discordID: string;
}

const toForm = (p: Profile): Form => ({
  email: p.email ?? '',
  country: p.country ?? '',
  veknID: p.veknID ?? '',
  discordID: p.discordID ?? '',
});

export function ProfileEditor({
  profile,
  countries,
  onSaved,
}: {
  profile: Profile;
  countries: CountryOption[];
  onSaved: (updated: Profile) => void;
}) {
  const [form, setForm] = useState<Form>(() => toForm(profile));
  const { state, run } = useSave();

  // The server profile is the source of truth on (re)load and after a save
  // (onSaved swaps in a fresh object). Re-seed the fields when that object
  // identity changes — the documented "adjust state while rendering" pattern
  // (react.dev, "You Might Not Need an Effect"): no extra render pass, no
  // set-state-in-effect. Between saves the prop is stable, so in-progress edits
  // are never clobbered by a background refetch.
  const [seenProfile, setSeenProfile] = useState(profile);
  if (seenProfile !== profile) {
    setSeenProfile(profile);
    setForm(toForm(profile));
  }

  const set = <K extends keyof Form>(key: K, value: Form[K]) =>
    setForm((f) => ({ ...f, [key]: value }));

  const base = toForm(profile);
  const dirty = (Object.keys(form) as (keyof Form)[]).some((k) => form[k] !== base[k]);

  const submit = () => run(api.put<Profile>('/profile', form).then(onSaved));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Profile</CardTitle>
      </CardHeader>
      <CardBody className="flex flex-col gap-3">
        <Input
          type="email"
          id="profileEmail"
          label="E-mail Address"
          autoComplete="email"
          value={form.email}
          onChange={(e) => set('email', e.target.value)}
        />

        <Select
          id="profileCountry"
          label="Country"
          value={form.country}
          onChange={(e) => set('country', e.target.value)}
        >
          <option value="">-- Don't display country --</option>
          {countries.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </Select>

        <div>
          <Input
            type="text"
            id="veknID"
            label="VEKN ID"
            inputMode="numeric"
            pattern="[0-9]*"
            aria-describedby="veknIdHelp"
            value={form.veknID}
            onChange={(e) => set('veknID', digitsOnly(e.target.value))}
          />
          <FieldHint id="veknIdHelp">
            Link your account to your VEKN ID in order to be able to play sanctioned tournaments.
          </FieldHint>
        </div>

        <div>
          <Input
            type="text"
            id="discordID"
            label="Discord User ID"
            inputMode="numeric"
            pattern="[0-9]*"
            aria-describedby="discordIdHelp"
            value={form.discordID}
            onChange={(e) => set('discordID', digitsOnly(e.target.value))}
          />
          <FieldHint id="discordIdHelp">
            Enter your numeric Discord user ID to receive turn pings in Discord. Install the Discord app and enable
            push notifications to get them on your phone. <i>Pro tip:</i> mute the Discord app's sounds to keep the
            visual banners without the dings.{' '}
            <a
              className="text-accent underline"
              target="_blank"
              rel="noreferrer"
              href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-"
            >
              This article
            </a>{' '}
            explains how to find it.
          </FieldHint>
        </div>

        <div className="flex flex-col gap-2 items-start">
          <Button
            id="updateProfileButton"
            variant="primary"
            size="sm"
            disabled={!dirty || state === 'saving'}
            onClick={submit}
          >
            Update Profile
          </Button>
          <SaveNote state={state} />
        </div>
      </CardBody>
    </Card>
  );
}
