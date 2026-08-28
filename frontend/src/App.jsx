import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useAuthStore } from './store/useAuthStore';

// Layout
import Navbar from './components/layout/Navbar';
import Sidebar from './components/layout/Sidebar';

// Pages
import DashboardPage from './pages/DashboardPage';
import GISExplorerPage from './pages/GISExplorerPage';
import AIGapIntelligencePage from './pages/AIGapIntelligencePage';
import ImpactReEvaluationPage from './pages/ImpactReEvaluationPage';
import ProjectsPage from './pages/ProjectsPage';
import RequirementsPage from './pages/RequirementsPage';
import FieldEvidencePage from './pages/FieldEvidencePage';
import AssetManagerPage from './pages/AssetManagerPage';
import AuditLogsPage from './pages/AuditLogsPage';
import UserManagementPage from './pages/UserManagementPage';
import LoginPage from './pages/LoginPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 2, // 2 minutes
    },
  },
});

const Layout = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col bg-slate-950 text-slate-100">
      <Navbar />
      <div className="flex-1 flex overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto w-full">
          {children}
        </main>
      </div>
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
        <Routes>
          <Route path="/login" element={<LoginPage />} />

          <Route path="/" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="/gis-explorer" element={<ProtectedRoute><GISExplorerPage /></ProtectedRoute>} />
          <Route path="/ai-intelligence" element={<ProtectedRoute><AIGapIntelligencePage /></ProtectedRoute>} />
          <Route path="/impact-reevaluation" element={<ProtectedRoute><ImpactReEvaluationPage /></ProtectedRoute>} />
          <Route path="/projects" element={<ProtectedRoute><ProjectsPage /></ProtectedRoute>} />
          <Route path="/requirements" element={<ProtectedRoute><RequirementsPage /></ProtectedRoute>} />
          <Route path="/field-evidence" element={<ProtectedRoute><FieldEvidencePage /></ProtectedRoute>} />
          <Route path="/assets" element={<ProtectedRoute><AssetManagerPage /></ProtectedRoute>} />
          <Route path="/audit-logs" element={<ProtectedRoute><AuditLogsPage /></ProtectedRoute>} />
          <Route path="/users" element={<ProtectedRoute><UserManagementPage /></ProtectedRoute>} />

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
