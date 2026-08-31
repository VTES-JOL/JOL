import { useQuery, useQueryClient } from '@tanstack/react-query';
import { api } from '../api/client';
import type { CountryOption, Profile } from '../api/types';
import { useNavRefresh } from '../auth/useNav';
import { Spinner } from '../components/ui/Spinner';
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

  if (!profile) {
    return (
      <div className="flex flex-1 min-h-0 items-center justify-center bg-base">
        <Spinner />
      </div>
    );
  }

  return (
    <div className="grid gap-4 p-4 bg-base md:grid-cols-2 lg:grid-cols-3 content-start">
      <ProfileEditor profile={profile} countries={countries} onSaved={onSaved} />
      <AccountEditor />
      <Preferences profile={profile} onSaved={onSaved} />
    </div>
  );
}
