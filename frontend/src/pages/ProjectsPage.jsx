import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import api from '../api/client';
import { useAuthStore } from '../store/useAuthStore';
import RoleGuard from '../components/common/RoleGuard';
import { 
  FolderKanban, 
  Plus, 
  Search, 
  Filter, 
  Calendar, 
  DollarSign, 
  UserPlus, 
  Trash2, 
  Edit3, 
  CheckCircle2, 
  Clock, 
  AlertCircle,
  X,
  ArrowUpRight
} from 'lucide-react';

export const ProjectsPage = () => {
  const [searchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const { user, hasPermission } = useAuthStore();

  const [statusFilter, setStatusFilter] = useState('');
  const [priorityFilter, setPriorityFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedProjectForAssign, setSelectedProjectForAssign] = useState(null);

  // New Project Form State
  const [newProject, setNewProject] = useState({
    projectName: '',
    description: '',
    state: searchParams.get('state') || 'Maharashtra',
    district: searchParams.get('district') || 'Gadchiroli',
    villageName: searchParams.get('villageName') || '',
    projectType: 'Rural Road & Connectivity Upgrade',
    priority: 'CRITICAL',
    budgetAllocated: '25000000',
    baselineGapScore: searchParams.get('baselineScore') ? parseFloat(searchParams.get('baselineScore')) : 84.0,
    startDate: new Date().toISOString().split('T')[0],
    targetDate: '2026-12-31'
  });

  const [assignUserForm, setAssignUserForm] = useState({
    userId: '5',
    roleInProject: 'LEAD_FIELD_EVALUATOR'
  });

  useEffect(() => {
    if (searchParams.get('create') === 'true') {
      setShowCreateModal(true);
    }
  }, [searchParams]);

  const { data: projects = [], isLoading } = useQuery({
    queryKey: ['projects-list', statusFilter, priorityFilter, searchQuery],
    queryFn: async () => {
      try {
        let url = `/projects?size=50`;
        if (statusFilter) url += `&status=${statusFilter}`;
        if (priorityFilter) url += `&priority=${priorityFilter}`;
        if (searchQuery) url += `&query=${encodeURIComponent(searchQuery)}`;
        const res = await api.get(url);
        return res.data?.content || [];
      } catch (e) {
        return [
          {
            id: 1,
            projectCode: 'GD-OD-2025-001',
            projectName: 'Thuamul Rampur Multi-Modal Access & Jal Jeevan Integration',
            state: 'Odisha',
            district: 'Kalahandi',
            villageName: 'Thuamul Rampur',
            projectType: 'Integrated Infrastructure',
            priority: 'CRITICAL',
            status: 'COMPLETED',
            budgetAllocated: 48500000,
            budgetSpent: 46200000,
            baselineGapScore: 88.0,
            currentGapScore: 34.2,
            gapReductionPct: 61.1,
            startDate: '2025-01-15',
            targetDate: '2025-12-31',
            assignments: [{ userName: 'Suresh Naik', roleInProject: 'FIELD_OFFICER' }]
          },
          {
            id: 2,
            projectCode: 'GD-MH-2026-002',
            projectName: 'Bhamragad Tribal Corridor Road Paving & Telemedicine Sub-Centre',
            state: 'Maharashtra',
            district: 'Gadchiroli',
            villageName: 'Bhamragad',
            projectType: 'PMGSY Road & Health Kiosk',
            priority: 'CRITICAL',
            status: 'ACTIVE',
            budgetAllocated: 32000000,
            budgetSpent: 14500000,
            baselineGapScore: 86.4,
            currentGapScore: 62.0,
            gapReductionPct: 28.2,
            startDate: '2026-02-01',
            targetDate: '2026-11-30',
            assignments: [{ userName: 'Suresh Naik', roleInProject: 'NODAL_OFFICER' }]
          }
        ];
      }
    }
  });

  const createMutation = useMutation({
    mutationFn: async (payload) => {
      const res = await api.post('/projects', payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['projects-list']);
      setShowCreateModal(false);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id) => {
      await api.delete(`/projects/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['projects-list']);
    }
  });

  const assignMutation = useMutation({
    mutationFn: async ({ projectId, payload }) => {
      const res = await api.post(`/projects/${projectId}/assign`, payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['projects-list']);
      setShowAssignModal(false);
    }
  });

  const getStatusBadge = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30';
      case 'ACTIVE':
        return 'bg-blue-500/20 text-blue-300 border-blue-500/30';
      case 'PLANNED':
        return 'bg-purple-500/20 text-purple-300 border-purple-500/30';
      case 'ON_HOLD':
        return 'bg-amber-500/20 text-amber-300 border-amber-500/30';
      default:
        return 'bg-slate-500/20 text-slate-300 border-slate-500/30';
    }
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-lg shadow-emerald-600/30">
            <FolderKanban className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white">Project Pipeline & Lifecycle Tracker</h1>
            <p className="text-xs text-slate-400">Track rural infrastructure interventions from gap discovery to measured outcome</p>
          </div>
        </div>

        <RoleGuard permission="PROJECT_CREATE_EDIT">
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center gap-2 shadow-lg shadow-emerald-600/20"
          >
            <Plus className="w-4 h-4" />
            <span>Create New Project</span>
          </button>
        </RoleGuard>
      </div>

      {/* Filters Bar */}
      <div className="glass-panel p-4 rounded-xl border border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2 flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search by code, title, or village..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-transparent text-slate-200 placeholder-slate-500 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-2">
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
          >
            <option value="">All Statuses</option>
            <option value="DRAFT">Draft</option>
            <option value="PLANNED">Planned</option>
            <option value="ACTIVE">Active</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="COMPLETED">Completed</option>
          </select>

          <select
            value={priorityFilter}
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
          >
            <option value="">All Priorities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </div>
      </div>

      {/* Projects Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {projects.map((project) => (
          <div key={project.id} className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4 hover:border-slate-700 transition">
            <div className="flex items-start justify-between">
              <div>
                <span className="text-[10px] font-mono font-bold text-emerald-400 px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20">
                  {project.projectCode}
                </span>
                <h3 className="font-bold text-base text-slate-100 mt-1">{project.projectName}</h3>
                <p className="text-xs text-slate-400">{project.villageName} &bull; {project.district}, {project.state}</p>
              </div>

              <span className={`text-xs px-2.5 py-1 rounded-full font-bold border ${getStatusBadge(project.status)}`}>
                {project.status}
              </span>
            </div>

            {/* Closed Loop Scoreboard */}
            <div className="p-3 bg-slate-900/90 rounded-xl border border-slate-800 flex items-center justify-between text-xs">
              <div>
                <div className="text-[10px] text-slate-400">Baseline Deficit</div>
                <div className="font-bold text-rose-400 text-sm">{project.baselineGapScore || 'N/A'}</div>
              </div>
              <div className="text-center">
                <div className="text-[10px] text-slate-400">Impact Delta</div>
                <div className="font-bold text-emerald-400 text-sm">
                  {project.gapReductionPct ? `-${project.gapReductionPct}%` : 'In Progress'}
                </div>
              </div>
              <div className="text-right">
                <div className="text-[10px] text-slate-400">Current Score</div>
                <div className="font-bold text-slate-100 text-sm">{project.currentGapScore || project.baselineGapScore}</div>
              </div>
            </div>

            {/* Financial Outlay */}
            <div className="flex items-center justify-between text-xs text-slate-400 pt-1 border-t border-slate-800">
              <div className="flex items-center gap-1.5">
                <DollarSign className="w-3.5 h-3.5 text-teal-400" />
                <span>Outlay: &#8377;{((project.budgetAllocated || 0) / 10000000).toFixed(2)} Cr</span>
              </div>
              <div className="flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>Target: {project.targetDate}</span>
              </div>
            </div>

            {/* Assigned Personnel */}
            <div className="flex items-center justify-between pt-1 text-xs">
              <div className="text-slate-400 flex items-center gap-1">
                <span>Field Team:</span>
                <span className="font-semibold text-slate-200">
                  {project.assignments?.length ? project.assignments[0].userName : 'Unassigned'}
                </span>
              </div>

              <div className="flex items-center gap-2">
                <RoleGuard permission="PROJECT_CREATE_EDIT">
                  <button
                    onClick={() => {
                      setSelectedProjectForAssign(project);
                      setShowAssignModal(true);
                    }}
                    className="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg text-xs font-medium flex items-center gap-1 border border-slate-700"
                    title="Assign Field Officer"
                  >
                    <UserPlus className="w-3.5 h-3.5" />
                    <span>Assign</span>
                  </button>

                  <button
                    onClick={() => {
                      if (confirm(`Are you sure you want to delete project ${project.projectCode}?`)) {
                        deleteMutation.mutate(project.id);
                      }
                    }}
                    className="p-1.5 text-rose-400 hover:bg-rose-500/10 rounded-lg"
                    title="Delete Project"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </RoleGuard>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Create Project Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full p-6 space-y-4 shadow-2xl animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-lg font-bold text-white">Create Infrastructure Project</h2>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 mb-1">Project Name</label>
                <input
                  type="text"
                  value={newProject.projectName}
                  onChange={(e) => setNewProject({ ...newProject, projectName: e.target.value })}
                  placeholder="e.g. Bhamragad All-Weather Road & Sub-Centre Upgrade"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1">Village</label>
                  <input
                    type="text"
                    value={newProject.villageName}
                    onChange={(e) => setNewProject({ ...newProject, villageName: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  />
                </div>
                <div>
                  <label className="block text-slate-300 mb-1">District</label>
                  <input
                    type="text"
                    value={newProject.district}
                    onChange={(e) => setNewProject({ ...newProject, district: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1">Priority</label>
                  <select
                    value={newProject.priority}
                    onChange={(e) => setNewProject({ ...newProject, priority: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  >
                    <option value="CRITICAL">Critical Deficit</option>
                    <option value="HIGH">High Priority</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>
                <div>
                  <label className="block text-slate-300 mb-1">Budget Outlay (INR)</label>
                  <input
                    type="number"
                    value={newProject.budgetAllocated}
                    onChange={(e) => setNewProject({ ...newProject, budgetAllocated: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1">Description</label>
                <textarea
                  rows="3"
                  value={newProject.description}
                  onChange={(e) => setNewProject({ ...newProject, description: e.target.value })}
                  placeholder="Outline project scope, proposed schemes, and target outcomes..."
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <button
                onClick={() => setShowCreateModal(false)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-semibold"
              >
                Cancel
              </button>
              <button
                onClick={() => createMutation.mutate(newProject)}
                disabled={!newProject.projectName || createMutation.isPending}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold shadow-lg shadow-emerald-600/30 disabled:opacity-50"
              >
                {createMutation.isPending ? 'Creating...' : 'Create Project'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Assign User Modal */}
      {showAssignModal && selectedProjectForAssign && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-lg font-bold text-white">Assign Field Personnel</h2>
              <button onClick={() => setShowAssignModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <p className="text-slate-300">Assigning field evaluator for: <span className="font-bold text-emerald-400">{selectedProjectForAssign.projectCode}</span></p>

              <div>
                <label className="block text-slate-400 mb-1">Select Field Officer</label>
                <select
                  value={assignUserForm.userId}
                  onChange={(e) => setAssignUserForm({ ...assignUserForm, userId: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                >
                  <option value="5">Suresh Naik (Field Officer - Gadchiroli)</option>
                  <option value="4">Ananya Sengupta (GIS Analyst)</option>
                  <option value="3">Vikramaditya Rao (Project Manager)</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1">Role in Project</label>
                <select
                  value={assignUserForm.roleInProject}
                  onChange={(e) => setAssignUserForm({ ...assignUserForm, roleInProject: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                >
                  <option value="LEAD_FIELD_EVALUATOR">Lead Field Evaluator</option>
                  <option value="NODAL_OFFICER">Block Nodal Officer</option>
                  <option value="QUALITY_INSPECTOR">Quality Inspector</option>
                </select>
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <button onClick={() => setShowAssignModal(false)} className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs">
                Cancel
              </button>
              <button
                onClick={() => assignMutation.mutate({
                  projectId: selectedProjectForAssign.id,
                  payload: { userId: Number(assignUserForm.userId), roleInProject: assignUserForm.roleInProject }
                })}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold"
              >
                Confirm Assignment
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProjectsPage;
