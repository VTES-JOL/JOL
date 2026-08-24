import { useEffect, useState } from 'react';
import { api } from '../api/client';
import type { CountryOption, Profile } from '../api/types';
import { useNavRefresh } from '../nav/useNav';
import { showError } from '../components/toast';
import { PageLoading } from '../components/PageLoading';
import { ProfileEditor } from './profile/ProfileEditor';
import { AccountEditor } from './profile/AccountEditor';
import { Preferences } from './profile/Preferences';

export function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [countries, setCountries] = useState<CountryOption[]>([]);
  const refreshNav = useNavRefresh();

  useEffect(() => {
    api
      .get<Profile>('/profile')
      .then(setProfile)
      .catch((err) => {
        console.error('Failed to load profile', err);
        showError('Failed to load profile.');
      });
  }, []);
  useEffect(() => {
    api
      .get<CountryOption[]>('/profile/countries')
      .then(setCountries)
      .catch((err) => console.error('Failed to load countries', err));
  }, []);

  // Each ProfileResource PUT returns the updated bean directly — apply it
  // without a second round trip, and pull the TopBar's country flag in
  // immediately rather than waiting for the next unrelated /nav refresh.
  const onSaved = (updated: Profile) => {
    setProfile(updated);
    refreshNav();
  };

  if (!profile) return <PageLoading />;

  return (
    <div className="row mt-2 p-3">
      <div className="col-lg-3 col-md-6">
        <ProfileEditor profile={profile} countries={countries} onSaved={onSaved} />
      </div>
      <div className="col-lg-3 col-md-6">
        <AccountEditor />
      </div>
      <div className="col-lg-3 col-md-6">
        <Preferences profile={profile} onSaved={onSaved} />
      </div>
    </div>
  );
}
