import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle, CardBody } from '../../components/ui/Card';
import { Input } from '../../components/ui/Input';
import { Select } from '../../components/ui/Select';
import { Button } from '../../components/ui/Button';
import { FieldHint, InlineAlert } from '../../components/ui/FormFeedback';
import { api } from '../../api/client';
import type { CountryOption, Profile } from '../../api/types';

const digitsOnly = (v: string) => v.replace(/\D+/g, '');

export function ProfileEditor({
  profile,
  countries,
  onSaved,
}: {
  profile: Profile;
  countries: CountryOption[];
  onSaved: (updated: Profile) => void;
}) {
  const [email, setEmail] = useState(profile.email ?? '');
  const [country, setCountry] = useState(profile.country ?? '');
  const [veknID, setVeknID] = useState(profile.veknID ?? '');
  const [discordID, setDiscordID] = useState(profile.discordID ?? '');
  const [status, setStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');

  // Reset local form state to server truth whenever the profile is
  // (re)loaded — same as ds.js only overwriting fields the server told it
  // changed, just simpler: this only runs on load/after-save, not while the
  // player is mid-edit on an unrelated field.
  useEffect(() => {
    setEmail(profile.email ?? '');
    setCountry(profile.country ?? '');
    setVeknID(profile.veknID ?? '');
    setDiscordID(profile.discordID ?? '');
  }, [profile]);

  const submit = () => {
    setStatus('saving');
    api
      .put<Profile>('/profile', { email, discordID, veknID, country })
      .then((updated) => {
        setStatus('saved');
        onSaved(updated);
        setTimeout(() => setStatus('idle'), 2000);
      })
      .catch(() => setStatus('error'));
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Profile</CardTitle>
      </CardHeader>
      <CardBody className="jt:flex jt:flex-col jt:gap-3">
        <Input
          type="email"
          id="profileEmail"
          label="E-mail Address"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <Select
          id="profileCountry"
          label="Country"
          value={country}
          onChange={(e) => setCountry(e.target.value)}
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
            value={veknID}
            onChange={(e) => setVeknID(digitsOnly(e.target.value))}
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
            value={discordID}
            onChange={(e) => setDiscordID(digitsOnly(e.target.value))}
          />
          <FieldHint id="discordIdHelp">
            Link your account below to receive pings in Discord. Install the Discord app and enable push
            notifications to receive pings on your phone. <i>Pro tip:</i> Disable sound notifications for the Discord
            app to receive the visual banners without the pestering dings or vibrations.{' '}
            <a
              className="jt:text-accent jt:underline"
              target="_blank"
              rel="noreferrer"
              href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-"
            >
              This article
            </a>{' '}
            explains how to get your user ID from Discord.
          </FieldHint>
        </div>

        <div className="jt:flex jt:flex-col jt:gap-2 jt:items-start">
          <Button id="updateProfileButton" variant="secondary" size="sm" onClick={submit}>
            Update Profile
          </Button>
          {status === 'saved' && <InlineAlert kind="success">Done!</InlineAlert>}
          {status === 'error' && <InlineAlert kind="danger">An error occurred</InlineAlert>}
        </div>
      </CardBody>
    </Card>
  );
}
