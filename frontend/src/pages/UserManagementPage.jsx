import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/client';
import { useAuthStore } from '../store/useAuthStore';
import RoleGuard from '../components/common/RoleGuard';
import { 
  Users, 
  UserPlus, 
  ShieldCheck, 
  Search, 
  Lock, 
  CheckCircle, 
  XCircle, 
  Trash2, 
  X,
  Table
} from 'lucide-react';

export const UserManagementPage = () => {
  const queryClient = useQueryClient();
  const { user: currentUser } = useAuthStore();

  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [activeTab, setActiveTab] = useState('users'); // 'users' or 'matrix'

  const [newUser, setNewUser] = useState({
    name: '',
    email: '',
    mobile: '',
    password: 'Password@123',
    role: 'FIELD_OFFICER',
    department: 'Block Development Office',
    jurisdictionState: 'Maharashtra',
    jurisdictionDistrict: 'Gadchiroli'
  });

  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users-list', searchQuery],
    queryFn: async () => {
      try {
        let url = '/users?size=50';
        if (searchQuery) url += `&query=${encodeURIComponent(searchQuery)}`;
        const res = await api.get(url);
        return res.data?.content || [];
      } catch (e) {
        return [
          { id: 1, name: 'Dr. Rajesh Verma', email: 'superadmin@gramdrishti.gov.in', role: 'SUPER_ADMIN', department: 'Ministry of Rural Development (MoRD)', jurisdictionState: 'National', status: 'ACTIVE' },
          { id: 2, name: 'Pooja Sharma (IAS)', email: 'admin@gramdrishti.gov.in', role: 'ADMIN', department: 'State Planning Board', jurisdictionState: 'Maharashtra', status: 'ACTIVE' },
          { id: 3, name: 'Vikramaditya Rao', email: 'pm@gramdrishti.gov.in', role: 'PROJECT_MANAGER', department: 'PMGSY Directorate', jurisdictionState: 'Odisha', status: 'ACTIVE' },
          { id: 4, name: 'Ananya Sengupta', email: 'analyst@gramdrishti.gov.in', role: 'ANALYST', department: 'GIS Cell', jurisdictionState: 'Rajasthan', status: 'ACTIVE' },
          { id: 5, name: 'Suresh Naik', email: 'field@gramdrishti.gov.in', role: 'FIELD_OFFICER', department: 'Panchayat Office', jurisdictionState: 'Maharashtra', status: 'ACTIVE' },
          { id: 6, name: 'Kavita Nair', email: 'viewer@gramdrishti.gov.in', role: 'VIEWER', department: 'Media Portal', jurisdictionState: 'National', status: 'ACTIVE' }
        ];
      }
    }
  });

  const createMutation = useMutation({
    mutationFn: async (payload) => {
      const res = await api.post('/users', payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['users-list']);
      setShowCreateModal(false);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id) => {
      await api.delete(`/users/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['users-list']);
    }
  });

  const permissionMatrix = [
    { permission: 'User Management', superAdmin: true, admin: true, pm: false, analyst: false, field: false, viewer: false },
    { permission: 'Role Management', superAdmin: true, admin: false, pm: false, analyst: false, field: false, viewer: false },
    { permission: 'Project Create / Edit', superAdmin: true, admin: true, pm: true, analyst: false, field: false, viewer: false },
    { permission: 'Project View', superAdmin: true, admin: true, pm: true, analyst: true, field: true, viewer: true },
    { permission: 'Requirement Create', superAdmin: true, admin: true, pm: true, analyst: true, field: false, viewer: false },
    { permission: 'Field Data Entry', superAdmin: true, admin: true, pm: true, analyst: false, field: true, viewer: false },
    { permission: 'AI Analysis & CV', superAdmin: true, admin: true, pm: true, analyst: true, field: false, viewer: false },
    { permission: 'GIS Analysis', superAdmin: true, admin: true, pm: true, analyst: true, field: false, viewer: true },
    { permission: 'Reports & Analytics', superAdmin: true, admin: true, pm: true, analyst: true, field: 'Limited', viewer: 'View' },
    { permission: 'File Upload', superAdmin: true, admin: true, pm: true, analyst: true, field: true, viewer: false },
    { permission: 'File Delete', superAdmin: true, admin: true, pm: true, analyst: false, field: false, viewer: false },
    { permission: 'Audit Logs', superAdmin: true, admin: true, pm: 'Limited', analyst: false, field: false, viewer: false },
    { permission: 'System Settings', superAdmin: true, admin: 'Limited', pm: false, analyst: false, field: false, viewer: false },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-500 flex items-center justify-center text-white shadow-lg shadow-purple-600/30">
            <Users className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white">User Management & RBAC Permissions</h1>
            <p className="text-xs text-slate-400">Server-enforced role hierarchy, user authentication states, and permission matrix</p>
          </div>
        </div>

        <RoleGuard permission="USER_MANAGEMENT">
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center gap-2 shadow-lg shadow-emerald-600/20"
          >
            <UserPlus className="w-4 h-4" />
            <span>Create User Account</span>
          </button>
        </RoleGuard>
      </div>

      {/* Tab Switcher */}
      <div className="flex items-center gap-2 border-b border-slate-800 pb-2">
        <button
          onClick={() => setActiveTab('users')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition ${
            activeTab === 'users' ? 'bg-slate-800 text-emerald-400 border border-slate-700' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          User Accounts Directory ({users.length})
        </button>
        <button
          onClick={() => setActiveTab('matrix')}
          className={`px-4 py-2 rounded-xl text-xs font-bold transition ${
            activeTab === 'matrix' ? 'bg-slate-800 text-emerald-400 border border-slate-700' : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          RBAC Security Matrix Reference
        </button>
      </div>

      {activeTab === 'users' ? (
        <div className="space-y-4">
          <div className="glass-panel p-3 rounded-xl border border-slate-800 flex items-center justify-between gap-3 text-xs">
            <div className="flex items-center gap-2 flex-1">
              <Search className="w-4 h-4 text-slate-400" />
              <input
                type="text"
                placeholder="Search users by name, email, department..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-transparent text-slate-200 placeholder-slate-500 focus:outline-none"
              />
            </div>
          </div>

          <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead className="bg-slate-900/90 text-slate-400 border-b border-slate-800 font-semibold uppercase tracking-wider text-[10px]">
                  <tr>
                    <th className="py-3 px-4">User</th>
                    <th className="py-3 px-4">Role</th>
                    <th className="py-3 px-4">Department</th>
                    <th className="py-3 px-4">Jurisdiction</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-slate-200">
                  {users.map((u) => (
                    <tr key={u.id} className="hover:bg-slate-850/60 transition">
                      <td className="py-3 px-4">
                        <div className="font-bold text-slate-100">{u.name}</div>
                        <div className="text-slate-400 text-[11px]">{u.email}</div>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                          u.role === 'SUPER_ADMIN' ? 'bg-rose-500/20 text-rose-300' :
                          u.role === 'ADMIN' ? 'bg-purple-500/20 text-purple-300' :
                          u.role === 'PROJECT_MANAGER' ? 'bg-emerald-500/20 text-emerald-300' :
                          u.role === 'ANALYST' ? 'bg-blue-500/20 text-blue-300' :
                          'bg-slate-800 text-slate-300'
                        }`}>
                          {u.role}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-slate-300">{u.department || 'MoRD'}</td>
                      <td className="py-3 px-4 text-slate-300">{u.jurisdictionState || 'National'}</td>
                      <td className="py-3 px-4">
                        <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                          {u.status || 'ACTIVE'}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right">
                        <RoleGuard permission="USER_MANAGEMENT">
                          {u.email !== 'superadmin@gramdrishti.gov.in' && (
                            <button
                              onClick={() => {
                                if (confirm(`Soft delete user ${u.email}?`)) {
                                  deleteMutation.mutate(u.id);
                                }
                              }}
                              className="p-1 text-rose-400 hover:bg-rose-500/10 rounded"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          )}
                        </RoleGuard>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : (
        /* Permission Matrix Reference Table */
        <div className="glass-panel rounded-2xl border border-slate-800 overflow-hidden">
          <div className="p-4 border-b border-slate-800 flex items-center justify-between">
            <h3 className="text-sm font-bold text-slate-200">Server-Side Permission Enforcement Matrix</h3>
            <span className="text-[11px] text-emerald-400 font-mono">Source of Truth: Spring Security</span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-900/90 text-slate-400 border-b border-slate-800 font-semibold uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-3 px-4">Permission Module</th>
                  <th className="py-3 px-4 text-center">Super Admin</th>
                  <th className="py-3 px-4 text-center">Admin</th>
                  <th className="py-3 px-4 text-center">PM</th>
                  <th className="py-3 px-4 text-center">Analyst</th>
                  <th className="py-3 px-4 text-center">Field Officer</th>
                  <th className="py-3 px-4 text-center">Viewer</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60">
                {permissionMatrix.map((row, idx) => (
                  <tr key={idx} className="hover:bg-slate-850/60 transition">
                    <td className="py-3 px-4 font-semibold text-slate-200">{row.permission}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.superAdmin)}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.admin)}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.pm)}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.analyst)}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.field)}</td>
                    <td className="py-3 px-4 text-center">{renderCell(row.viewer)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-lg font-bold text-white">Create Official Account</h2>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-400 mb-1">Full Name</label>
                <input
                  type="text"
                  value={newUser.name}
                  onChange={(e) => setNewUser({ ...newUser, name: e.target.value })}
                  placeholder="e.g. Ramesh Kumar"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                />
              </div>

              <div>
                <label className="block text-slate-400 mb-1">Email (Government Domain)</label>
                <input
                  type="email"
                  value={newUser.email}
                  onChange={(e) => setNewUser({ ...newUser, email: e.target.value })}
                  placeholder="e.g. ramesh.kumar@gramdrishti.gov.in"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-400 mb-1">Role</label>
                  <select
                    value={newUser.role}
                    onChange={(e) => setNewUser({ ...newUser, role: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  >
                    <option value="ADMIN">Admin</option>
                    <option value="PROJECT_MANAGER">Project Manager</option>
                    <option value="ANALYST">Analyst</option>
                    <option value="FIELD_OFFICER">Field Officer</option>
                    <option value="VIEWER">Viewer</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-400 mb-1">Department</label>
                  <input
                    type="text"
                    value={newUser.department}
                    onChange={(e) => setNewUser({ ...newUser, department: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <button onClick={() => setShowCreateModal(false)} className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs">
                Cancel
              </button>
              <button
                onClick={() => createMutation.mutate(newUser)}
                disabled={!newUser.name || !newUser.email}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold"
              >
                Create Account
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

function renderCell(val) {
  if (val === true) return <span className="text-emerald-400 font-bold">&#9989;</span>;
  if (val === false) return <span className="text-rose-500 font-bold">&#10060;</span>;
  return <span className="text-amber-400 font-semibold text-[11px]">{val}</span>;
}

export default UserManagementPage;
