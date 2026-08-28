import React, { useState } from 'react';
import { NavLink, useNavigate, useLocation } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map, 
  Building2, 
  FolderKanban, 
  MoreHorizontal, 
  Sparkles, 
  Camera, 
  CheckSquare, 
  ShieldAlert, 
  LogOut, 
  X,
  User
} from 'lucide-react';
import { useAuthStore } from '../../store/useAuthStore';

export default function BottomNavigation() {
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const handleLogout = async () => {
    setShowMoreMenu(false);
    await logout();
    navigate('/login');
  };

  const navItems = [
    { name: 'Home', path: '/', icon: LayoutDashboard },
    { name: 'GIS Map', path: '/gis', icon: Map },
    { name: 'AI Gap', path: '/ai-gap', icon: Sparkles },
    { name: 'Projects', path: '/projects', icon: FolderKanban },
  ];

  const moreItems = [
    { name: 'Field Evidence', path: '/field-evidence', icon: Camera, desc: 'Upload survey photos & drone orthos' },
    { name: 'Requirements', path: '/requirements', icon: CheckSquare, desc: 'Gap sanctioning & approval' },
    { name: 'Asset DAM', path: '/assets', icon: Building2, desc: 'Digital asset management storage' },
    { name: 'Audit Trail', path: '/audit-logs', icon: ShieldAlert, desc: 'Security compliance logs' },
  ];

  return (
    <>
      {/* 1. Fixed Bottom Navigation Bar (Visible on mobile/tablet screens < lg) */}
      <nav 
        className="lg:hidden fixed bottom-0 left-0 right-0 z-40 bg-slate-900/95 backdrop-blur-md border-t border-slate-800/80 px-2 py-1 shadow-2xl safe-area-pb"
        aria-label="Mobile Navigation"
      >
        <div className="flex items-center justify-around h-16 max-w-lg mx-auto">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <NavLink
                key={item.name}
                to={item.path}
                className={`flex flex-col items-center justify-center w-16 h-14 rounded-xl transition-all duration-200 min-h-[48px] min-w-[48px] active:scale-95 ${
                  isActive 
                    ? 'text-emerald-400 font-semibold' 
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <div className={`relative p-1 rounded-lg ${isActive ? 'bg-emerald-500/10' : ''}`}>
                  <Icon className="w-5 h-5" />
                  {isActive && (
                    <span className="absolute -bottom-1 left-1/2 transform -translate-x-1/2 w-1.5 h-1.5 bg-emerald-400 rounded-full" />
                  )}
                </div>
                <span className="text-[10px] mt-0.5 tracking-tight">{item.name}</span>
              </NavLink>
            );
          })}

          {/* More Button */}
          <button
            type="button"
            onClick={() => setShowMoreMenu(true)}
            className={`flex flex-col items-center justify-center w-16 h-14 rounded-xl transition-all duration-200 min-h-[48px] min-w-[48px] active:scale-95 ${
              showMoreMenu ? 'text-emerald-400' : 'text-slate-400 hover:text-slate-200'
            }`}
            aria-label="More navigation options"
          >
            <div className="p-1">
              <MoreHorizontal className="w-5 h-5" />
            </div>
            <span className="text-[10px] mt-0.5 tracking-tight">More</span>
          </button>
        </div>
      </nav>

      {/* 2. Mobile More Menu (Bottom Sheet Modal) */}
      {showMoreMenu && (
        <div className="lg:hidden fixed inset-0 z-50 flex flex-col justify-end bg-slate-950/80 backdrop-blur-sm animate-fade-in">
          {/* Backdrop click area */}
          <div className="flex-1" onClick={() => setShowMoreMenu(false)} />

          {/* Bottom Sheet Drawer */}
          <div className="bg-slate-900 border-t border-slate-800 rounded-t-3xl p-5 shadow-2xl max-h-[85vh] overflow-y-auto animate-slide-up">
            {/* Header / Grab Bar */}
            <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-4">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400 font-bold">
                  {user?.name ? user.name[0] : 'U'}
                </div>
                <div>
                  <h3 className="text-sm font-semibold text-slate-100">{user?.name || 'User'}</h3>
                  <p className="text-xs text-emerald-400 font-medium">{user?.role || 'Stakeholder'}</p>
                </div>
              </div>
              <button
                type="button"
                onClick={() => setShowMoreMenu(false)}
                className="w-9 h-9 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 hover:text-white"
                aria-label="Close menu"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Menu Items */}
            <div className="space-y-2 mb-6">
              {moreItems.map((item) => {
                const Icon = item.icon;
                return (
                  <NavLink
                    key={item.name}
                    to={item.path}
                    onClick={() => setShowMoreMenu(false)}
                    className="flex items-center space-x-3 p-3 rounded-2xl bg-slate-800/60 hover:bg-slate-800 border border-slate-700/40 text-slate-200 transition-colors min-h-[48px]"
                  >
                    <div className="w-9 h-9 rounded-xl bg-slate-700/50 flex items-center justify-center text-emerald-400">
                      <Icon className="w-5 h-5" />
                    </div>
                    <div className="flex-1">
                      <p className="text-xs font-semibold text-slate-100">{item.name}</p>
                      <p className="text-[11px] text-slate-400">{item.desc}</p>
                    </div>
                  </NavLink>
                );
              })}
            </div>

            {/* Logout button */}
            <button
              type="button"
              onClick={handleLogout}
              className="w-full flex items-center justify-center space-x-2 py-3 px-4 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 text-rose-400 font-medium text-xs transition-colors min-h-[48px] active:scale-98"
            >
              <LogOut className="w-4 h-4" />
              <span>Sign Out Session</span>
            </button>
          </div>
        </div>
      )}
    </>
  );
}
