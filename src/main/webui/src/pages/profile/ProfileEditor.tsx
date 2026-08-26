import { useEffect, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../components/Card';
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
    <Card className="mb-2">
      <CardHeader>
        <CardTitle>Profile</CardTitle>
      </CardHeader>
      <div className="card-body">
        <label htmlFor="profileEmail" className="form-label">
          E-mail Address
        </label>
        <input
          type="email"
          id="profileEmail"
          className="form-control"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />

        <label htmlFor="profileCountry" className="form-label">
          Country
        </label>
        <select
          id="profileCountry"
          className="form-select"
          value={country}
          onChange={(e) => setCountry(e.target.value)}
        >
          <option value="">-- Don't display country --</option>
          {countries.map((c) => (
            <option key={c.code} value={c.code}>
              {c.name}
            </option>
          ))}
        </select>

        <label htmlFor="veknID" className="form-label mt-2">
          VEKN ID
        </label>
        <input
          type="text"
          id="veknID"
          className="form-control"
          inputMode="numeric"
          pattern="[0-9]*"
          aria-describedby="veknIdHelp"
          value={veknID}
          onChange={(e) => setVeknID(digitsOnly(e.target.value))}
        />
        <div className="form-text" id="veknIdHelp">
          Link your account to your VEKN ID in order to be able to play sanctioned tournaments.
        </div>

        <label htmlFor="discordID" className="form-label mt-2">
          Discord User ID
        </label>
        <input
          type="text"
          id="discordID"
          className="form-control"
          inputMode="numeric"
          pattern="[0-9]*"
          aria-describedby="discordIdHelp"
          value={discordID}
          onChange={(e) => setDiscordID(digitsOnly(e.target.value))}
        />
        <div className="form-text" id="discordIdHelp">
          Link your account below to receive pings in Discord. Install the Discord app and enable push notifications
          to receive pings on your phone.
          <i> Pro tip: </i> Disable sound notifications for the Discord app to receive the visual banners without the
          pestering dings or vibrations.{' '}
          <a
            target="_blank"
            rel="noreferrer"
            href="https://support.discord.com/hc/en-us/articles/206346498-Where-can-I-find-my-User-Server-Message-ID-"
          >
            This article
          </a>{' '}
          explains how to get your user ID from Discord.
        </div>

        <button id="updateProfileButton" className="btn btn-outline-secondary btn-sm mt-2" onClick={submit}>
          Update Profile
        </button>
        <div id="profileUpdateResult" className="mt-2" style={{ color: status === 'error' ? 'red' : 'green' }}>
          {status === 'saved' && 'Done!'}
          {status === 'error' && 'An error occurred'}
        </div>
      </div>
    </Card>
  );
}
