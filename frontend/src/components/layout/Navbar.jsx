import React, { useState } from 'react';
import { useAuthStore } from '../../store/useAuthStore';
import { 
  ShieldCheck, 
  UserCheck, 
  Bell, 
  Search, 
  LogOut, 
  Sparkles, 
  Layers, 
  ChevronDown,
  Globe,
  Radio
} from 'lucide-react';
import { Link } from 'react-router-dom';

export const Navbar = () => {
  const { user, logout, switchRole } = useAuthStore();
  const [showRoleMenu, setShowRoleMenu] = useState(false);
  const [showUserMenu, setShowUserMenu] = useState(false);

  const rolesList = [
    { key: 'SUPER_ADMIN', label: 'Super Admin', badge: 'Apex Governance', color: 'bg-rose-500/20 text-rose-300 border-rose-500/30' },
    { key: 'ADMIN', label: 'Admin', badge: 'State/District Admin', color: 'bg-purple-500/20 text-purple-300 border-purple-500/30' },
    { key: 'PROJECT_MANAGER', label: 'Project Manager', badge: 'Project Lead', color: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30' },
    { key: 'ANALYST', label: 'Analyst', badge: 'GIS & AI Analyst', color: 'bg-blue-500/20 text-blue-300 border-blue-500/30' },
    { key: 'FIELD_OFFICER', label: 'Field Officer', badge: 'Ground Verification', color: 'bg-amber-500/20 text-amber-300 border-amber-500/30' },
    { key: 'VIEWER', label: 'Viewer', badge: 'Public / Read-only', color: 'bg-slate-500/20 text-slate-300 border-slate-500/30' },
  ];

  const currentRoleConfig = rolesList.find(r => r.key === user?.role) || rolesList[0];

  return (
    <header className="sticky top-0 z-40 w-full bg-slate-900/90 backdrop-blur-md border-b border-slate-800">
      <div className="px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
        {/* Left: Brand Identity */}
        <div className="flex items-center gap-3">
          <Link to="/" className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 p-0.5 shadow-lg shadow-emerald-500/20 flex items-center justify-center">
              <div className="w-full h-full bg-slate-950 rounded-[10px] flex items-center justify-center">
                <Globe className="w-5 h-5 text-emerald-400 animate-pulse" />
              </div>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-lg tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-emerald-400 via-teal-200 to-white">
                  GramDrishti <span className="text-emerald-400">AI</span>
                </span>
                <span className="text-[10px] uppercase font-bold tracking-widest px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded">
                  India GIS
                </span>
              </div>
              <p className="text-[11px] text-slate-400 hidden sm:block">Rural Infrastructure Gap Intelligence Platform</p>
            </div>
          </Link>
        </div>

        {/* Center: Live Role Switcher Banner */}
        <div className="relative">
          <button
            onClick={() => setShowRoleMenu(!showRoleMenu)}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg border text-xs font-semibold transition-all ${currentRoleConfig.color} hover:brightness-110 shadow-sm`}
          >
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span className="hidden md:inline text-slate-400 font-normal">Active Role:</span>
            <span>{currentRoleConfig.label}</span>
            <span className="text-[10px] opacity-75 hidden lg:inline">({currentRoleConfig.badge})</span>
            <ChevronDown className="w-3.5 h-3.5 opacity-60 ml-0.5" />
          </button>

          {showRoleMenu && (
            <div className="absolute right-0 mt-2 w-72 bg-slate-900 border border-slate-700 rounded-xl shadow-2xl py-2 z-50 animate-in fade-in slide-in-from-top-2">
              <div className="px-3 py-1.5 border-b border-slate-800 text-[11px] font-semibold text-slate-400 uppercase tracking-wider flex items-center justify-between">
                <span>Switch Role (Live RBAC Test)</span>
                <Radio className="w-3 h-3 text-emerald-400 animate-pulse" />
              </div>
              {rolesList.map((r) => (
                <button
                  key={r.key}
                  onClick={() => {
                    switchRole(r.key);
                    setShowRoleMenu(false);
                  }}
                  className={`w-full text-left px-3 py-2 text-xs flex items-center justify-between hover:bg-slate-800 transition ${
                    user?.role === r.key ? 'bg-slate-800/80 font-bold text-emerald-400' : 'text-slate-300'
                  }`}
                >
                  <div>
                    <div className="font-medium">{r.label}</div>
                    <div className="text-[10px] text-slate-500">{r.badge}</div>
                  </div>
                  {user?.role === r.key && <span className="w-2 h-2 rounded-full bg-emerald-400"></span>}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Right: Quick actions & User profile */}
        <div className="flex items-center gap-3">
          <div className="hidden sm:flex items-center gap-2 px-2.5 py-1 bg-slate-800/60 border border-slate-700/60 rounded-lg text-xs text-slate-300">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping"></span>
            <span className="text-slate-400">MoRD Closed-Loop Active</span>
          </div>

          {/* User Profile */}
          <div className="relative">
            <button
              onClick={() => setShowUserMenu(!showUserMenu)}
              className="flex items-center gap-2.5 p-1 rounded-lg hover:bg-slate-800 transition"
            >
              <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-slate-700 to-slate-600 border border-slate-600 flex items-center justify-center font-bold text-xs text-emerald-300">
                {user?.name ? user.name.charAt(0) : 'U'}
              </div>
              <div className="text-left hidden lg:block">
                <div className="text-xs font-semibold text-slate-200 leading-tight">{user?.name || 'User'}</div>
                <div className="text-[10px] text-slate-400 truncate max-w-[140px]">{user?.department || 'Official'}</div>
              </div>
              <ChevronDown className="w-3.5 h-3.5 text-slate-400 hidden lg:block" />
            </button>

            {showUserMenu && (
              <div className="absolute right-0 mt-2 w-56 bg-slate-900 border border-slate-700 rounded-xl shadow-2xl py-2 z-50">
                <div className="px-4 py-2 border-b border-slate-800">
                  <p className="text-xs font-semibold text-slate-200">{user?.name}</p>
                  <p className="text-[11px] text-slate-400 truncate">{user?.email}</p>
                  <span className="inline-block mt-1 text-[10px] px-2 py-0.5 bg-emerald-500/10 text-emerald-400 rounded-full border border-emerald-500/20">
                    {user?.role}
                  </span>
                </div>
                <button
                  onClick={logout}
                  className="w-full text-left px-4 py-2 text-xs text-rose-400 hover:bg-rose-500/10 flex items-center gap-2 transition"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  Sign Out
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
