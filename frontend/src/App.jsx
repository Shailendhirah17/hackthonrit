import React, { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/useAuthStore';

// Layout
import Navbar from './components/layout/Navbar';
import Sidebar from './components/layout/Sidebar';
import BottomNavigation from './components/layout/BottomNavigation';

// Lazy-loaded Pages (Code Splitting & Mobile Performance)
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const GISExplorerPage = lazy(() => import('./pages/GISExplorerPage'));
const AIGapIntelligencePage = lazy(() => import('./pages/AIGapIntelligencePage'));
const ImpactReEvaluationPage = lazy(() => import('./pages/ImpactReEvaluationPage'));
const ProjectsPage = lazy(() => import('./pages/ProjectsPage'));
const RequirementsPage = lazy(() => import('./pages/RequirementsPage'));
const FieldEvidencePage = lazy(() => import('./pages/FieldEvidencePage'));
const AssetManagerPage = lazy(() => import('./pages/AssetManagerPage'));
const AuditLogsPage = lazy(() => import('./pages/AuditLogsPage'));
const UserManagementPage = lazy(() => import('./pages/UserManagementPage'));
const LoginPage = lazy(() => import('./pages/LoginPage'));

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 3, // 3 minutes
      retry: 1,
    },
  },
});

// Mobile Skeleton Fallback
const PageSkeleton = () => (
  <div className="space-y-4 p-4 animate-pulse">
    <div className="h-10 bg-slate-800 rounded-2xl w-3/4"></div>
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      <div className="h-24 bg-slate-800/80 rounded-2xl"></div>
      <div className="h-24 bg-slate-800/80 rounded-2xl"></div>
      <div className="h-24 bg-slate-800/80 rounded-2xl"></div>
      <div className="h-24 bg-slate-800/80 rounded-2xl"></div>
    </div>
    <div className="h-72 bg-slate-800/60 rounded-3xl w-full"></div>
  </div>
);

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100 selection:bg-emerald-500 selection:text-white">
      <Navbar />
      <div className="flex-1 flex overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto p-3 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full pb-24 lg:pb-8">
          <Suspense fallback={<PageSkeleton />}>
            {children}
          </Suspense>
        </main>
      </div>
      {/* Mobile-first Thumb-friendly Bottom Navigation */}
      <BottomNavigation />
    </div>
  );
};

const ProtectedRoute = ({ children }) => {
  const { isAuthenticated } = useAuthStore();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <Layout>{children}</Layout>;
};

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Suspense fallback={<PageSkeleton />}>
          <Routes>
            <Route path="/login" element={<LoginPage />} />

            {/* Core Protected Routes */}
            <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/gis" element={<ProtectedRoute><GISExplorerPage /></ProtectedRoute>} />
            <Route path="/gis-explorer" element={<ProtectedRoute><GISExplorerPage /></ProtectedRoute>} />
            <Route path="/ai-gap" element={<ProtectedRoute><AIGapIntelligencePage /></ProtectedRoute>} />
            <Route path="/ai-intelligence" element={<ProtectedRoute><AIGapIntelligencePage /></ProtectedRoute>} />
            <Route path="/impact-reevaluation" element={<ProtectedRoute><ImpactReEvaluationPage /></ProtectedRoute>} />
            <Route path="/projects" element={<ProtectedRoute><ProjectsPage /></ProtectedRoute>} />
            <Route path="/requirements" element={<ProtectedRoute><RequirementsPage /></ProtectedRoute>} />
            <Route path="/field-evidence" element={<ProtectedRoute><FieldEvidencePage /></ProtectedRoute>} />
            <Route path="/assets" element={<ProtectedRoute><AssetManagerPage /></ProtectedRoute>} />
            <Route path="/audit-logs" element={<ProtectedRoute><AuditLogsPage /></ProtectedRoute>} />
            <Route path="/users" element={<ProtectedRoute><UserManagementPage /></ProtectedRoute>} />

            {/* Catch-all redirect */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
