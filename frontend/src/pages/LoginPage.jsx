import React, { useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { useNavigate } from 'react-router-dom';
import { 
  Globe, 
  ShieldCheck, 
  Lock, 
  Mail, 
  Sparkles, 
  ArrowRight,
  AlertCircle
} from 'lucide-react';

export const LoginPage = () => {
  const navigate = useNavigate();
  const { login, switchRole, isLoading } = useAuthStore();

  const [email, setEmail] = useState('superadmin@gramdrishti.gov.in');
  const [password, setPassword] = useState('Password@123');
  const [errorMessage, setErrorMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    const res = await login(email, password);
    if (res.success) {
      navigate('/');
    } else {
      setErrorMessage(res.message || 'Invalid credentials');
    }
  };

  const handleQuickRole = (roleKey) => {
    switchRole(roleKey);
    navigate('/');
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center py-12 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Ambient background glows */}
      <div className="absolute -top-40 -left-40 w-96 h-96 bg-emerald-500/10 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-teal-500/10 rounded-full blur-3xl pointer-events-none"></div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center space-y-3 z-10">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-emerald-600 to-teal-400 p-0.5 shadow-2xl shadow-emerald-500/30">
          <div className="w-full h-full bg-slate-950 rounded-[14px] flex items-center justify-center">
            <Globe className="w-7 h-7 text-emerald-400" />
          </div>
        </div>

        <div>
          <h2 className="text-3xl font-extrabold tracking-tight text-white">
            GramDrishti <span className="text-emerald-400">AI</span>
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Government of India &bull; Rural Infrastructure Gap Intelligence Platform
          </p>
        </div>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md z-10">
        <div className="glass-panel py-8 px-6 sm:px-8 rounded-3xl border border-slate-800 shadow-2xl space-y-6">
          {errorMessage && (
            <div className="p-3 rounded-xl bg-rose-950/40 border border-rose-500/40 text-rose-300 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-400" />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4 text-xs">
            <div>
              <label className="block text-slate-300 mb-1 font-semibold">Government Email ID</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="name@gramdrishti.gov.in"
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 focus:outline-none focus:border-emerald-500 transition"
                />
              </div>
            </div>

            <div>
              <label className="block text-slate-300 mb-1 font-semibold">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="password"
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full pl-9 pr-3 py-2.5 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 focus:outline-none focus:border-emerald-500 transition"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-white rounded-xl font-bold shadow-xl shadow-emerald-600/30 transition flex items-center justify-center gap-2"
            >
              <span>{isLoading ? 'Authenticating...' : 'Sign In with SSO / JWT'}</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Quick Demo Access Bar */}
          <div className="pt-4 border-t border-slate-800 space-y-2">
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider text-center flex items-center justify-center gap-1.5">
              <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
              <span>1-Click Role Login for Evaluation</span>
            </div>

            <div className="grid grid-cols-2 gap-2 text-[11px]">
              <button
                onClick={() => handleQuickRole('SUPER_ADMIN')}
                className="p-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-slate-200 font-semibold transition text-left"
              >
                <div className="text-emerald-400 font-bold">Super Admin</div>
                <div className="text-[10px] text-slate-500">Apex National MoRD</div>
              </button>

              <button
                onClick={() => handleQuickRole('ADMIN')}
                className="p-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-slate-200 font-semibold transition text-left"
              >
                <div className="text-purple-400 font-bold">State Admin</div>
                <div className="text-[10px] text-slate-500">Planning Board</div>
              </button>

              <button
                onClick={() => handleQuickRole('PROJECT_MANAGER')}
                className="p-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-slate-200 font-semibold transition text-left"
              >
                <div className="text-blue-400 font-bold">Project Manager</div>
                <div className="text-[10px] text-slate-500">PMGSY Infrastructure</div>
              </button>

              <button
                onClick={() => handleQuickRole('FIELD_OFFICER')}
                className="p-2 bg-slate-900 hover:bg-slate-800 border border-slate-800 rounded-xl text-slate-200 font-semibold transition text-left"
              >
                <div className="text-amber-400 font-bold">Field Officer</div>
                <div className="text-[10px] text-slate-500">Panchayat Ground Entry</div>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
