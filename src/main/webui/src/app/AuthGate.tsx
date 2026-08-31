import type { ReactNode } from 'react';
import { useNavAuthState } from '../auth/useNav';
import { PageLoading } from '../components/PageLoading';

// No servlet gates auth server-side before index.html gets served — Tomcat's
// RewriteValve (see rewrite.config) serves it unconditionally for every
// route below /jol/login owns — so this is the actual gate: it holds off
// mounting AppShell — and therefore TopBar and every page's own API calls —
// until /nav has confirmed a session. On a failed check it renders nothing
// rather than redirecting itself: the failed /nav fetch already triggered
// client.ts's global 401 handler (window.location.href = '/jol/login'), so a
// second, competing redirect here would be redundant.
export function AuthGate({ children }: { children: ReactNode }) {
  const { status } = useNavAuthState();
  if (status === 'loading') return <PageLoading />;
  if (status === 'unauthenticated') return null;
  return <>{children}</>;
}
