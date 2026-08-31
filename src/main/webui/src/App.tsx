import { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { queryClient } from './api/queryClient';
import { AppRoutes } from './app/AppRoutes';
import { DialogHost } from './components/DialogHost';
import { ToastHost } from './components/ToastHost';
import { UpdateBanner } from './components/UpdateBanner';
import { startUpdateCheck } from './updateCheck';

// Provider stack only — the routing lives in src/app/ (AppRoutes → AuthGate →
// AppShell). DialogHost/ToastHost/UpdateBanner are mounted once here, as
// siblings of the router, since they render app-wide overlays driven by the
// module-singleton stores in src/stores/ rather than by route state.
export function App() {
  useEffect(() => {
    startUpdateCheck();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AppRoutes />
        <DialogHost />
        <ToastHost />
        <UpdateBanner />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
