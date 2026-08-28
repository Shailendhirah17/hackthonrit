import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/client';
import { useAuthStore } from '../store/useAuthStore';
import RoleGuard from '../components/common/RoleGuard';
import { 
  CheckSquare, 
  Plus, 
  Search, 
  Filter, 
  CheckCircle2, 
  Clock, 
  AlertTriangle, 
  X,
  ChevronRight,
  ShieldCheck
} from 'lucide-react';

export const RequirementsPage = () => {
  const queryClient = useQueryClient();
  const { hasPermission } = useAuthStore();

  const [categoryFilter, setCategoryFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  const [newReq, setNewReq] = useState({
    title: '',
    description: '',
    category: 'ROADS',
    priority: 'CRITICAL',
    aiScore: 85.0,
    source: 'AI_SURVEY_ANALYSIS',
    estimatedCost: '18000000',
    suggestedScheme: 'Pradhan Mantri Gram Sadak Yojana (PMGSY)'
  });

  const { data: requirements = [] } = useQuery({
    queryKey: ['requirements-list', categoryFilter, statusFilter, searchQuery],
    queryFn: async () => {
      try {
        let url = '/requirements?size=50';
        if (categoryFilter) url += `&category=${categoryFilter}`;
        if (statusFilter) url += `&status=${statusFilter}`;
        if (searchQuery) url += `&query=${encodeURIComponent(searchQuery)}`;
        const res = await api.get(url);
        return res.data?.content || [];
      } catch (e) {
        return [
          { id: 1, title: 'Construct 12.5 km Bituminous All-Weather Road to Bhamragad Highway', category: 'ROADS', priority: 'CRITICAL', status: 'IN_PROGRESS', villageName: 'Bhamragad', district: 'Gadchiroli', estimatedCost: 21000000, suggestedScheme: 'PMGSY Phase-III', aiScore: 89.5, source: 'AI_SATELLITE_SURVEY' },
          { id: 2, title: 'Deploy Ayushman Bharat Health & Wellness Kiosk with Telemedicine', category: 'HEALTHCARE', priority: 'HIGH', status: 'APPROVED', villageName: 'Bhamragad', district: 'Gadchiroli', estimatedCost: 4500000, suggestedScheme: 'National Health Mission', aiScore: 82.0, source: 'AI_SURVEY_ANALYSIS' },
          { id: 3, title: 'Install 200 kL Piped Drinking Water System with Solar Pump', category: 'WATER_SANITATION', priority: 'HIGH', status: 'IDENTIFIED', villageName: 'Etapalli', district: 'Gadchiroli', estimatedCost: 6800000, suggestedScheme: 'Jal Jeevan Mission', aiScore: 76.0, source: 'FIELD_SURVEY' }
        ];
      }
    }
  });

  const statusMutation = useMutation({
    mutationFn: async ({ id, status }) => {
      const res = await api.patch(`/requirements/${id}/status`, { status });
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['requirements-list']);
    }
  });

  const createMutation = useMutation({
    mutationFn: async (payload) => {
      const res = await api.post('/requirements', payload);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries(['requirements-list']);
      setShowCreateModal(false);
    }
  });

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-lg shadow-emerald-600/30">
            <CheckSquare className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white">Infrastructure Requirements & Gap Triage</h1>
            <p className="text-xs text-slate-400">Review AI-identified deficits, assign government schemes, and approve work orders</p>
          </div>
        </div>

        <RoleGuard permission="REQUIREMENT_CREATE">
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center gap-2 shadow-lg shadow-emerald-600/20"
          >
            <Plus className="w-4 h-4" />
            <span>Create Requirement</span>
          </button>
        </RoleGuard>
      </div>

      {/* Filters */}
      <div className="glass-panel p-4 rounded-xl border border-slate-800 flex flex-wrap items-center justify-between gap-3 text-xs">
        <div className="flex items-center gap-2 flex-1 min-w-[240px]">
          <Search className="w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search requirement titles, villages, schemes..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-transparent text-slate-200 placeholder-slate-500 focus:outline-none"
          />
        </div>

        <div className="flex items-center gap-2">
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
          >
            <option value="">All Categories</option>
            <option value="ROADS">Roads & Connectivity</option>
            <option value="HEALTHCARE">Healthcare</option>
            <option value="EDUCATION">Education</option>
            <option value="WATER_SANITATION">Water & Sanitation</option>
            <option value="DIGITAL_CONNECTIVITY">Digital & Telecom</option>
            <option value="POWER_ENERGY">Power & Energy</option>
          </select>

          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
          >
            <option value="">All Statuses</option>
            <option value="IDENTIFIED">Identified</option>
            <option value="VALIDATING">Validating</option>
            <option value="APPROVED">Approved</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>
      </div>

      {/* Requirements List */}
      <div className="space-y-3">
        {requirements.map((req) => (
          <div key={req.id} className="glass-panel p-5 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 hover:border-slate-700 transition">
            <div className="space-y-1.5 flex-1">
              <div className="flex items-center gap-2 flex-wrap">
                <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-300 border border-slate-700">
                  {req.category}
                </span>
                <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                  req.priority === 'CRITICAL' ? 'bg-rose-500/20 text-rose-300 border border-rose-500/30' : 'bg-orange-500/20 text-orange-300 border border-orange-500/30'
                }`}>
                  {req.priority}
                </span>
                <span className="text-[10px] font-mono text-emerald-400">
                  AI Intensity: {req.aiScore}/100
                </span>
              </div>

              <h3 className="font-bold text-sm text-slate-100">{req.title}</h3>
              <p className="text-xs text-slate-400">{req.villageName}, {req.district} &bull; Scheme: <span className="text-slate-300 font-medium">{req.suggestedScheme}</span></p>
            </div>

            <div className="flex items-center gap-4 w-full md:w-auto justify-between md:justify-end">
              <div className="text-right">
                <div className="text-[10px] text-slate-400">Est. Outlay</div>
                <div className="font-bold text-slate-200 text-sm">&#8377;{((req.estimatedCost || 0) / 100000).toFixed(1)} Lakhs</div>
              </div>

              <RoleGuard permission="PROJECT_CREATE_EDIT">
                <div className="flex items-center gap-1.5">
                  {req.status === 'IDENTIFIED' && (
                    <button
                      onClick={() => statusMutation.mutate({ id: req.id, status: 'APPROVED' })}
                      className="px-3 py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-lg text-xs font-bold shadow-sm transition"
                    >
                      Approve Work
                    </button>
                  )}
                  {req.status === 'APPROVED' && (
                    <button
                      onClick={() => statusMutation.mutate({ id: req.id, status: 'IN_PROGRESS' })}
                      className="px-3 py-1.5 bg-blue-600 hover:bg-blue-500 text-white rounded-lg text-xs font-bold shadow-sm transition"
                    >
                      Start Execution
                    </button>
                  )}
                  {req.status === 'IN_PROGRESS' && (
                    <button
                      onClick={() => statusMutation.mutate({ id: req.id, status: 'RESOLVED' })}
                      className="px-3 py-1.5 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-xs font-bold shadow-sm transition"
                    >
                      Mark Resolved
                    </button>
                  )}
                </div>
              </RoleGuard>
            </div>
          </div>
        ))}
      </div>

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl max-w-lg w-full p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <h2 className="text-lg font-bold text-white">Log Infrastructure Deficit</h2>
              <button onClick={() => setShowCreateModal(false)} className="text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block text-slate-300 mb-1">Requirement Title</label>
                <input
                  type="text"
                  value={newReq.title}
                  onChange={(e) => setNewReq({ ...newReq, title: e.target.value })}
                  placeholder="e.g. Install Solar Submersible Water Purification Unit"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-300 mb-1">Category</label>
                  <select
                    value={newReq.category}
                    onChange={(e) => setNewReq({ ...newReq, category: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  >
                    <option value="ROADS">Roads & Connectivity</option>
                    <option value="HEALTHCARE">Healthcare</option>
                    <option value="EDUCATION">Education</option>
                    <option value="WATER_SANITATION">Water & Sanitation</option>
                    <option value="DIGITAL_CONNECTIVITY">Digital & Telecom</option>
                    <option value="POWER_ENERGY">Power & Energy</option>
                  </select>
                </div>

                <div>
                  <label className="block text-slate-300 mb-1">Priority</label>
                  <select
                    value={newReq.priority}
                    onChange={(e) => setNewReq({ ...newReq, priority: e.target.value })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                  >
                    <option value="CRITICAL">Critical</option>
                    <option value="HIGH">High</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="LOW">Low</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-300 mb-1">Recommended Government Scheme</label>
                <input
                  type="text"
                  value={newReq.suggestedScheme}
                  onChange={(e) => setNewReq({ ...newReq, suggestedScheme: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-700 rounded-lg text-slate-200"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 pt-3 border-t border-slate-800">
              <button onClick={() => setShowCreateModal(false)} className="px-4 py-2 bg-slate-800 text-slate-300 rounded-xl text-xs">
                Cancel
              </button>
              <button
                onClick={() => createMutation.mutate(newReq)}
                disabled={!newReq.title || createMutation.isPending}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold"
              >
                Save Requirement
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RequirementsPage;
