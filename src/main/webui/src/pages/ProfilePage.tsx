import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CountryOption, Profile } from '../api/types';
import { useNavRefresh } from '../nav/useNav';
import { PageLoading } from '../components/PageLoading';
import { ProfileEditor } from './profile/ProfileEditor';
import { AccountEditor } from './profile/AccountEditor';
import { Preferences } from './profile/Preferences';

const PROFILE_QUERY_KEY = ['profile'];

export function ProfilePage() {
  const queryClient = useQueryClient();
  const refreshNav = useNavRefresh();

  const { data: profile } = useQuery({
    queryKey: PROFILE_QUERY_KEY,
    queryFn: () => api.get<Profile>('/profile'),
  });
  const { data: countries = [] } = useQuery({
    queryKey: ['profile', 'countries'],
    queryFn: () => api.get<CountryOption[]>('/profile/countries'),
    staleTime: Infinity,
  });

  // Each ProfileResource PUT returns the updated bean directly — apply it
  // without a second round trip, and pull the TopBar's country flag in
  // immediately rather than waiting for the next unrelated /nav refresh.
  const onSaved = (updated: Profile) => {
    queryClient.setQueryData(PROFILE_QUERY_KEY, updated);
    refreshNav();
  };

  if (!profile) return <PageLoading />;

  return (
    <div className="row g-3 mt-2 p-3">
      <div className="col-12 col-md-6 col-lg-4">
        <ProfileEditor profile={profile} countries={countries} onSaved={onSaved} />
      </div>
      <div className="col-12 col-md-6 col-lg-4">
        <AccountEditor />
      </div>
      <div className="col-12 col-md-6 col-lg-4">
        <Preferences profile={profile} onSaved={onSaved} />
      </div>
    </div>
  );
}
