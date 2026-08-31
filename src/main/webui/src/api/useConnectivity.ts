import { useSyncExternalStore } from 'react';
import { getConnectivity, subscribeConnectivity, type ConnectivityState } from '../stores/connectivity';

// useSyncExternalStore, not useState+useEffect: the latter has a real gap —
// the initial useState() value is captured at first render, but the effect
// subscribing to changes only attaches after that commit, so a connectivity
// change landing in that window would be missed until some other re-render
// happened to catch up. useSyncExternalStore closes that gap and is safe
// under concurrent rendering, which the old version wasn't.
export function useConnectivity(): ConnectivityState {
  return useSyncExternalStore(subscribeConnectivity, getConnectivity);
}
