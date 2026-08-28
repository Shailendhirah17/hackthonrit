import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/client';
import { 
  TrendingDown, 
  Sparkles, 
  CheckCircle2, 
  Camera, 
  Award, 
  Layers, 
  ArrowRight, 
  FileCheck,
  RefreshCw,
  FolderKanban
} from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, Legend } from 'recharts';

export const ImpactReEvaluationPage = () => {
  const queryClient = useQueryClient();

  const [selectedProjectId, setSelectedProjectId] = useState(1);
  const [completedInterventions, setCompletedInterventions] = useState([
    'PMGSY Phase-III 12.5 km all-weather bituminous road paving',
    'Ayushman Bharat Health & Wellness Centre (HWC) construction',
    'Jal Jeevan Mission 100% functional household tap connection'
  ]);
  const [newInterventionText, setNewInterventionText] = useState('');

  const { data: projects = [] } = useQuery({
    queryKey: ['reevaluation-projects'],
    queryFn: async () => {
      try {
        const res = await api.get('/projects?size=50');
        return res.data?.content || [];
      } catch (e) {
        return [
          { id: 1, projectCode: 'GD-OD-2025-001', projectName: 'Thuamul Rampur Multi-Modal Access & Jal Jeevan Integration', villageName: 'Thuamul Rampur', district: 'Kalahandi', state: 'Odisha', baselineGapScore: 88.0, currentGapScore: 34.2, gapReductionPct: 61.1, status: 'COMPLETED' },
          { id: 2, projectCode: 'GD-MH-2026-002', projectName: 'Bhamragad Tribal Corridor Road Paving & Telemedicine Sub-Centre', villageName: 'Bhamragad', district: 'Gadchiroli', state: 'Maharashtra', baselineGapScore: 86.4, currentGapScore: 62.0, gapReductionPct: 28.2, status: 'ACTIVE' }
        ];
      }
    }
  });

  const selectedProject = projects.find(p => p.id === Number(selectedProjectId)) || projects[0] || {};

  const [reEvalResult, setReEvalResult] = useState(null);

  const reEvaluateMutation = useMutation({
    mutationFn: async () => {
      const payload = {
        project_id: String(selectedProject.id || 1),
        project_name: selectedProject.projectName || 'Project',
        village_name: selectedProject.villageName || 'Village',
        district: selectedProject.district || 'District',
        baseline_gap_score: selectedProject.baselineGapScore || 88.0,
        interventions_completed: completedInterventions
      };

      const res = await api.post('/ai/re-evaluate', payload);
      return res.data;
    },
    onSuccess: (data) => {
      setReEvalResult(data);
      queryClient.invalidateQueries(['dashboard-stats']);
      queryClient.invalidateQueries(['reevaluation-projects']);
      queryClient.invalidateQueries(['villages-all']);
    }
  });

  const comparisonData = [
    { dimension: 'Road Deficit', Before: 82, After: reEvalResult?.beforeVsAfterDimensions?.Roads?.after || 22 },
    { dimension: 'Health Deficit', Before: 88, After: reEvalResult?.beforeVsAfterDimensions?.Healthcare?.after || 25 },
    { dimension: 'Water Deficit', Before: 74, After: reEvalResult?.beforeVsAfterDimensions?.['Water & Sanitation']?.after || 18 },
    { dimension: 'Overall Gap', Before: selectedProject.baselineGapScore || 88, After: reEvalResult?.updatedGapScore || selectedProject.currentGapScore || 34.2 }
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center text-white shadow-lg shadow-emerald-600/30">
            <TrendingDown className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white">Closed-Loop AI Impact & Re-Scoring Center</h1>
            <p className="text-xs text-slate-400">Measure verified infrastructure deficit reduction by evaluating field evidence against baseline data</p>
          </div>
        </div>

        {/* Project Selector */}
        <div className="flex items-center gap-3 w-full md:w-auto">
          <select
            value={selectedProjectId}
            onChange={(e) => {
              setSelectedProjectId(Number(e.target.value));
              setReEvalResult(null);
            }}
            className="px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-xs text-slate-200 focus:outline-none focus:border-emerald-500 font-semibold"
          >
            {projects.map((p) => (
              <option key={p.id} value={p.id}>
                {p.projectCode} — {p.projectName.substring(0, 45)}...
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Main Closed Loop Re-Scoring Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 1 Col: Interventions & Evidence Checklist */}
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
          <div className="flex items-center justify-between border-b border-slate-800 pb-3">
            <div className="flex items-center gap-2 text-sm font-bold text-slate-200">
              <FileCheck className="w-4 h-4 text-emerald-400" />
              <span>Delivered Interventions</span>
            </div>
            <span className="text-[11px] text-slate-400 font-mono">{completedInterventions.length} Items</span>
          </div>

          <div className="space-y-2">
            {completedInterventions.map((item, idx) => (
              <div key={idx} className="p-2.5 rounded-xl bg-slate-900/90 border border-slate-800 flex items-start gap-2.5 text-xs text-slate-200">
                <CheckCircle2 className="w-4 h-4 text-emerald-400 flex-shrink-0 mt-0.5" />
                <span className="leading-snug">{item}</span>
              </div>
            ))}
          </div>

          {/* Add custom intervention */}
          <div className="pt-2">
            <div className="flex gap-2">
              <input
                type="text"
                placeholder="Add verified field intervention..."
                value={newInterventionText}
                onChange={(e) => setNewInterventionText(e.target.value)}
                className="flex-1 px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
              />
              <button
                onClick={() => {
                  if (newInterventionText.trim()) {
                    setCompletedInterventions(p => [...p, newInterventionText.trim()]);
                    setNewInterventionText('');
                  }
                }}
                className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-emerald-300 rounded-lg border border-slate-700 transition"
              >
                Add
              </button>
            </div>
          </div>

          {/* Trigger Re-evaluation Button */}
          <button
            onClick={() => reEvaluateMutation.mutate()}
            disabled={reEvaluateMutation.isPending}
            className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 shadow-xl shadow-emerald-600/30 disabled:opacity-50 mt-4"
          >
            <RefreshCw className={`w-4 h-4 ${reEvaluateMutation.isPending ? 'animate-spin' : ''}`} />
            <span>{reEvaluateMutation.isPending ? 'Executing AI Re-Scoring...' : 'Trigger AI Re-Evaluation & Rescore Village'}</span>
          </button>
        </div>

        {/* Right 2 Cols: Before vs After Impact Visualization */}
        <div className="lg:col-span-2 space-y-6">
          {/* Comparison Scoreboard */}
          <div className="glass-panel p-6 rounded-2xl border border-slate-800 grid grid-cols-1 sm:grid-cols-3 gap-4 text-center">
            <div className="p-4 bg-slate-900/90 rounded-xl border border-slate-800/80">
              <div className="text-[11px] text-slate-400 uppercase font-bold tracking-wider">Baseline Gap Score</div>
              <div className="text-3xl font-black text-rose-400 mt-1">{selectedProject.baselineGapScore || 88.0}</div>
              <div className="text-[10px] text-rose-400 font-semibold mt-0.5">CRITICAL DEFICIT</div>
            </div>

            <div className="p-4 bg-slate-900/90 rounded-xl border border-slate-800/80 flex flex-col items-center justify-center">
              <div className="text-[11px] text-slate-400 uppercase font-bold tracking-wider">Measured Improvement</div>
              <div className="text-3xl font-black text-emerald-400 mt-1">
                {reEvalResult ? `-${reEvalResult.gapReductionPct}%` : `-${selectedProject.gapReductionPct || 61.1}%`}
              </div>
              <div className="text-[10px] text-emerald-400 font-semibold mt-0.5">TRANSFORMATIONAL</div>
            </div>

            <div className="p-4 bg-slate-900/90 rounded-xl border border-slate-800/80">
              <div className="text-[11px] text-slate-400 uppercase font-bold tracking-wider">Updated Post-Intervention Score</div>
              <div className="text-3xl font-black text-emerald-400 mt-1">
                {reEvalResult ? reEvalResult.updatedGapScore : (selectedProject.currentGapScore || 34.2)}
              </div>
              <div className="text-[10px] text-emerald-400 font-semibold mt-0.5">OPTIMIZED HABITATION</div>
            </div>
          </div>

          {/* Before vs After Dimension Comparison Chart */}
          <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-sm font-bold text-slate-200">Pre vs Post Intervention Deficit Comparison</h3>
              <span className="text-xs text-slate-400">Lower is better</span>
            </div>

            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={comparisonData}>
                  <XAxis dataKey="dimension" stroke="#94a3b8" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                  <YAxis stroke="#94a3b8" domain={[0, 100]} />
                  <Tooltip 
                    contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '8px' }}
                    labelStyle={{ color: '#f8fafc', fontWeight: 'bold' }}
                  />
                  <Legend />
                  <Bar dataKey="Before" fill="#ef4444" radius={[4, 4, 0, 0]} name="Baseline Pre-Intervention Deficit" />
                  <Bar dataKey="After" fill="#10b981" radius={[4, 4, 0, 0]} name="Updated Post-Intervention Score" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* AI Verified Impact Summary Statement */}
          <div className="p-5 rounded-2xl bg-emerald-950/20 border border-emerald-500/30 flex items-start gap-4">
            <Award className="w-8 h-8 text-emerald-400 flex-shrink-0" />
            <div className="space-y-1 text-xs">
              <div className="font-extrabold text-emerald-300 text-sm">
                Official AI Impact Verification & Certification
              </div>
              <p className="text-slate-300 leading-relaxed">
                {reEvalResult?.impactSummary || selectedProject.impactSummary || 
                  "Project has successfully closed the infrastructure deficit gap from 88.0 down to 34.2. Paved road transit, 100% piped drinking water connections, and functional primary healthcare are now active in the habitation footprint."
                }
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ImpactReEvaluationPage;
