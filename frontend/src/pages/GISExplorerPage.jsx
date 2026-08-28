import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import GISMapView from '../components/gis/GISMapView';
import { 
  MapPin, 
  Filter, 
  Search, 
  Sparkles, 
  FolderPlus, 
  Building, 
  Layers, 
  Info, 
  ArrowRight,
  TrendingDown,
  X,
  ChevronUp,
  ChevronDown
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export const GISExplorerPage = () => {
  const navigate = useNavigate();
  const [selectedVillage, setSelectedVillage] = useState(null);
  const [filterState, setFilterState] = useState('');
  const [filterDistrict, setFilterDistrict] = useState('');
  const [filterPriority, setFilterPriority] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showMobileFilters, setShowMobileFilters] = useState(false);
  const [mobileSheetExpanded, setMobileSheetExpanded] = useState(false);

  const { data: villages = [] } = useQuery({
    queryKey: ['gis-villages', filterState, filterDistrict, filterPriority, searchQuery],
    queryFn: async () => {
      try {
        const res = await api.get('/villages/all');
        let list = res.data || [];
        if (filterState) list = list.filter(v => v.state === filterState);
        if (filterPriority) list = list.filter(v => v.priority === filterPriority);
        if (searchQuery) {
          list = list.filter(v => 
            v.villageName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
            v.district?.toLowerCase().includes(searchQuery.toLowerCase())
          );
        }
        return list;
      } catch (e) {
        return [];
      }
    }
  });

  const { data: infrastructure = [] } = useQuery({
    queryKey: ['gis-infra'],
    queryFn: async () => {
      try {
        const res = await api.get('/infrastructure');
        return res.data || [];
      } catch (e) {
        return [];
      }
    }
  });

  const { data: gapZones = [] } = useQuery({
    queryKey: ['gis-gap-zones'],
    queryFn: async () => {
      try {
        const res = await api.get('/gis/gap-zones');
        return res.features || [];
      } catch (e) {
        return [];
      }
    }
  });

  const { data: projects = [] } = useQuery({
    queryKey: ['gis-projects'],
    queryFn: async () => {
      try {
        const res = await api.get('/projects?size=50');
        return res.data?.content || [];
      } catch (e) {
        return [];
      }
    }
  });

  const handleSelectVillage = (v) => {
    setSelectedVillage(v);
    setMobileSheetExpanded(true);
  };

  return (
    <div className="space-y-4 pb-20 lg:pb-8">
      {/* Top Controls Bar (Desktop & Mobile header) */}
      <div className="glass-panel p-3.5 sm:p-4 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="flex items-center justify-between w-full md:w-auto">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400 font-bold">
              <MapPin className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-base sm:text-lg font-bold text-white leading-tight">GIS Spatial Intelligence Console</h1>
              <p className="text-[11px] sm:text-xs text-slate-400">Query geospatial buffers, deficit heat zones & village gaps</p>
            </div>
          </div>

          {/* Mobile Filter Toggle Button */}
          <button
            type="button"
            onClick={() => setShowMobileFilters(!showMobileFilters)}
            className="md:hidden flex items-center justify-center w-10 h-10 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 active:scale-95"
            aria-label="Toggle Filters"
          >
            <Filter className="w-4 h-4 text-emerald-400" />
          </button>
        </div>

        {/* Filters (Always visible on desktop, toggleable on mobile) */}
        <div className={`${showMobileFilters ? 'flex' : 'hidden'} md:flex flex-wrap items-center gap-2 text-xs w-full md:w-auto animate-fade-in`}>
          <div className="relative flex-1 sm:w-48 w-full">
            <Search className="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search village or district..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-8 pr-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500 min-h-[40px]"
            />
          </div>

          <select
            value={filterState}
            onChange={(e) => setFilterState(e.target.value)}
            className="flex-1 sm:flex-initial px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 focus:outline-none focus:border-emerald-500 min-h-[40px]"
          >
            <option value="">All States</option>
            <option value="Maharashtra">Maharashtra</option>
            <option value="Odisha">Odisha</option>
            <option value="Rajasthan">Rajasthan</option>
            <option value="Chhattisgarh">Chhattisgarh</option>
            <option value="Karnataka">Karnataka</option>
            <option value="Kerala">Kerala</option>
          </select>

          <select
            value={filterPriority}
            onChange={(e) => setFilterPriority(e.target.value)}
            className="flex-1 sm:flex-initial px-3 py-2 bg-slate-900 border border-slate-700 rounded-xl text-slate-200 focus:outline-none focus:border-emerald-500 min-h-[40px]"
          >
            <option value="">All Priorities</option>
            <option value="CRITICAL">🔴 Critical Deficit (75+)</option>
            <option value="HIGH">🟠 High Priority (55-74)</option>
            <option value="MEDIUM">🟡 Medium Priority (35-54)</option>
            <option value="LOW">🟢 Optimized (&lt;35)</option>
          </select>
        </div>
      </div>

      {/* Main Map & Detail Panel Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-4 relative">
        {/* Left 3 Cols: Leaflet GIS Map */}
        <div className="lg:col-span-3 rounded-2xl overflow-hidden shadow-xl border border-slate-800 relative">
          <GISMapView
            villages={villages}
            infrastructure={infrastructure}
            gapZones={gapZones}
            projects={projects}
            onSelectVillage={handleSelectVillage}
            selectedVillageId={selectedVillage?.id}
            height="560px"
          />

          {/* Mobile Floating Tap Prompt if no village selected */}
          {!selectedVillage && (
            <div className="lg:hidden absolute top-3 left-3 right-3 z-[1000] bg-slate-900/90 backdrop-blur-md border border-slate-700/80 rounded-xl px-3 py-2 text-center text-xs text-slate-300 shadow-lg pointer-events-none">
              📍 <span className="font-semibold text-emerald-400">Tap any colored marker</span> to inspect village intelligence
            </div>
          )}
        </div>

        {/* Right 1 Col: Desktop Village Inspector Panel */}
        <div className="hidden lg:block glass-panel p-5 rounded-2xl border border-slate-800 space-y-4 max-h-[560px] overflow-y-auto">
          {selectedVillage ? (
            <div className="space-y-4">
              <div className="flex items-start justify-between pb-3 border-b border-slate-800">
                <div>
                  <span className="text-[10px] uppercase font-bold text-slate-400 font-mono">
                    {selectedVillage.censusCode || 'HABITATION'}
                  </span>
                  <h2 className="text-xl font-black text-white">{selectedVillage.villageName}</h2>
                  <p className="text-xs text-slate-400">{selectedVillage.block}, {selectedVillage.district}, {selectedVillage.state}</p>
                </div>
                <div className={`px-2.5 py-1 rounded-xl text-center font-black ${
                  selectedVillage.gapScore >= 75 ? 'bg-rose-500/20 text-rose-400 border border-rose-500/30' : 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30'
                }`}>
                  <div className="text-base leading-none">{selectedVillage.gapScore}</div>
                  <div className="text-[9px] uppercase tracking-wider mt-0.5">Gap Score</div>
                </div>
              </div>

              {/* Multi-Dimensional Indicator Ratings */}
              <div className="space-y-2">
                <div className="text-xs font-bold text-slate-300">Accessibility & Service Metrics</div>

                <div className="space-y-2 text-xs">
                  <div>
                    <div className="flex justify-between text-slate-400 text-[11px] mb-1">
                      <span>Road Connectivity</span>
                      <span className="font-bold text-amber-400">{selectedVillage.roadConnectivityIndex || 24}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-amber-400" style={{ width: `${selectedVillage.roadConnectivityIndex || 24}%` }}></div>
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-slate-400 text-[11px] mb-1">
                      <span>Healthcare Coverage</span>
                      <span className="font-bold text-pink-400">{selectedVillage.healthAccessIndex || 18}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-pink-400" style={{ width: `${selectedVillage.healthAccessIndex || 18}%` }}></div>
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-slate-400 text-[11px] mb-1">
                      <span>Piped Water Access</span>
                      <span className="font-bold text-cyan-400">{selectedVillage.waterSanitationIndex || 31}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-cyan-400" style={{ width: `${selectedVillage.waterSanitationIndex || 31}%` }}></div>
                    </div>
                  </div>

                  <div>
                    <div className="flex justify-between text-slate-400 text-[11px] mb-1">
                      <span>Digital & Telecom</span>
                      <span className="font-bold text-blue-400">{selectedVillage.digitalConnectivityIndex || 15}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-blue-400" style={{ width: `${selectedVillage.digitalConnectivityIndex || 15}%` }}></div>
                    </div>
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="pt-4 border-t border-slate-800 space-y-2">
                <button
                  type="button"
                  onClick={() => navigate(`/ai-intelligence?village=${selectedVillage.id}`)}
                  className="w-full py-3 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/20 min-h-[48px] active:scale-98"
                >
                  <Sparkles className="w-4 h-4" />
                  <span>Run AI Gap Analysis & Triage</span>
                </button>

                <button
                  type="button"
                  onClick={() => navigate(`/projects?create=true&villageId=${selectedVillage.id}&villageName=${selectedVillage.villageName}&state=${selectedVillage.state}&district=${selectedVillage.district}`)}
                  className="w-full py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold transition flex items-center justify-center gap-2 border border-slate-700 min-h-[48px] active:scale-98"
                >
                  <FolderPlus className="w-4 h-4 text-emerald-400" />
                  <span>Create Infrastructure Project</span>
                </button>
              </div>
            </div>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-3 text-slate-400">
              <div className="w-12 h-12 rounded-2xl bg-slate-800/80 border border-slate-700 flex items-center justify-center text-emerald-400">
                <Info className="w-6 h-6" />
              </div>
              <h3 className="text-sm font-bold text-slate-200">Select Any Village on Map</h3>
              <p className="text-xs leading-relaxed">
                Click on any colored marker on the GIS map to inspect composite gap breakdown, infrastructure conditions, and launch targeted interventions.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Mobile Swipeable Bottom Sheet for Selected Village (lg:hidden) */}
      {selectedVillage && (
        <div className="lg:hidden fixed inset-x-0 bottom-16 z-50 bg-slate-900/98 backdrop-blur-xl border-t border-slate-700 rounded-t-3xl shadow-2xl transition-all duration-300 max-h-[80vh] overflow-y-auto animate-slide-up">
          <div className="p-4 space-y-3">
            {/* Grab Bar & Header */}
            <div className="flex items-center justify-between pb-2 border-b border-slate-800">
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <h2 className="text-base font-black text-white">{selectedVillage.villageName}</h2>
                  <span className={`px-2 py-0.5 rounded-lg text-[10px] font-black ${
                    selectedVillage.gapScore >= 75 ? 'bg-rose-500/20 text-rose-400' : 'bg-emerald-500/20 text-emerald-400'
                  }`}>
                    {selectedVillage.gapScore >= 75 ? '🔴 CRITICAL' : '🟢 OPTIMIZED'}
                  </span>
                </div>
                <p className="text-xs text-slate-400">{selectedVillage.district}, {selectedVillage.state}</p>
              </div>

              <div className="flex items-center gap-2">
                <div className="text-right">
                  <div className="text-lg font-black text-emerald-400 leading-tight">{selectedVillage.gapScore}</div>
                  <div className="text-[9px] uppercase text-slate-400">Gap Score</div>
                </div>
                <button
                  type="button"
                  onClick={() => setSelectedVillage(null)}
                  className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 ml-2"
                  aria-label="Close sheet"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>

            {/* Quick Metrics */}
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="p-2.5 rounded-xl bg-slate-800/80 border border-slate-700/50">
                <div className="text-[10px] text-slate-400">Road Connectivity</div>
                <div className="text-sm font-bold text-amber-400">{selectedVillage.roadConnectivityIndex || 24}%</div>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-800/80 border border-slate-700/50">
                <div className="text-[10px] text-slate-400">Piped Water Access</div>
                <div className="text-sm font-bold text-cyan-400">{selectedVillage.waterSanitationIndex || 31}%</div>
              </div>
            </div>

            {/* Action Buttons */}
            <div className="grid grid-cols-2 gap-2 pt-1">
              <button
                type="button"
                onClick={() => navigate(`/ai-intelligence?village=${selectedVillage.id}`)}
                className="py-3 px-2 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-xl text-xs font-bold flex items-center justify-center gap-1.5 shadow-lg min-h-[48px] active:scale-95"
              >
                <Sparkles className="w-3.5 h-3.5" />
                <span>AI Triage</span>
              </button>

              <button
                type="button"
                onClick={() => navigate(`/projects?create=true&villageId=${selectedVillage.id}&villageName=${selectedVillage.villageName}&state=${selectedVillage.state}&district=${selectedVillage.district}`)}
                className="py-3 px-2 bg-slate-800 text-slate-200 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 border border-slate-700 min-h-[48px] active:scale-95"
              >
                <FolderPlus className="w-3.5 h-3.5 text-emerald-400" />
                <span>New Project</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default GISExplorerPage;
