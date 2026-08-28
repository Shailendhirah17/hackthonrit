import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import {
  LayoutDashboard,
  MapPin,
  Sparkles,
  FolderKanban,
  CheckSquare,
  Camera,
  FolderArchive,
  BarChart3,
  ShieldAlert,
  Users,
  TrendingUp,
  Activity,
  Layers
} from 'lucide-react';

export const Sidebar = () => {
  const { hasPermission, hasRole } = useAuthStore();

  const navigation = [
    { name: 'Executive Dashboard', href: '/', icon: LayoutDashboard, permission: null },
    { name: 'GIS Gap Explorer', href: '/gis-explorer', icon: MapPin, permission: 'GIS_ANALYSIS' },
    { name: 'AI Gap Intelligence', href: '/ai-intelligence', icon: Sparkles, permission: 'AI_ANALYSIS' },
    { name: 'Closed-Loop Impact', href: '/impact-reevaluation', icon: TrendingUp, permission: null },
    { name: 'Projects Tracker', href: '/projects', icon: FolderKanban, permission: 'PROJECT_VIEW' },
    { name: 'Requirements Board', href: '/requirements', icon: CheckSquare, permission: 'REQUIREMENT_CREATE' },
    { name: 'Field Evidence Upload', href: '/field-evidence', icon: Camera, permission: 'FIELD_DATA_ENTRY' },
    { name: 'Digital Asset Manager', href: '/assets', icon: FolderArchive, permission: 'FILE_UPLOAD' },
    { name: 'Audit & Governance', href: '/audit-logs', icon: ShieldAlert, permission: 'AUDIT_LOGS' },
    { name: 'User Management', href: '/users', icon: Users, permission: 'USER_MANAGEMENT' },
  ];

  return (
    <aside className="w-64 bg-slate-900/70 border-r border-slate-800 flex-shrink-0 hidden md:flex md:flex-col justify-between p-4">
      <div className="space-y-6">
        <div>
          <div className="text-[11px] font-bold uppercase tracking-wider text-slate-400 px-3 mb-2">
            Intelligence Modules
          </div>
          <nav className="space-y-1">
            {navigation.map((item) => {
              if (item.permission && !hasPermission(item.permission)) {
                return null;
              }

              return (
                <NavLink
                  key={item.name}
                  to={item.href}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-medium transition-all ${
                      isActive
                        ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-600/20 font-semibold'
                        : 'text-slate-300 hover:bg-slate-800/80 hover:text-white'
                    }`
                  }
                >
                  <item.icon className="w-4 h-4 flex-shrink-0" />
                  <span>{item.name}</span>
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Closed-Loop Status Box */}
        <div className="p-3.5 rounded-xl bg-slate-800/50 border border-slate-700/60 text-xs">
          <div className="flex items-center gap-2 text-emerald-400 font-semibold mb-1.5">
            <Activity className="w-3.5 h-3.5 animate-pulse" />
            <span>Closed-Loop Engine</span>
          </div>
          <p className="text-[11px] text-slate-400 leading-relaxed">
            AI re-scoring automatically tracks pre vs post project intervention deficit delta across all Indian habitations.
          </p>
        </div>
      </div>

      <div className="pt-4 border-t border-slate-800/80 text-[11px] text-slate-400 flex items-center justify-between px-2">
        <span>GramDrishti v1.0</span>
        <span className="text-emerald-400 font-mono text-[10px]">PROD-READY</span>
      </div>
    </aside>
  );
};

export default Sidebar;
