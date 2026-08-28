import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, Polygon, CircleMarker, useMap } from 'react-leaflet';
import L from 'leaflet';
import { 
  Building2, 
  HeartPulse, 
  GraduationCap, 
  Droplet, 
  Zap, 
  Radio, 
  MapPin, 
  Layers, 
  AlertTriangle,
  ArrowUpRight,
  TrendingDown,
  Navigation
} from 'lucide-react';

// Custom Map center animator
function ChangeView({ center, zoom }) {
  const map = useMap();
  useEffect(() => {
    if (center) {
      map.setView(center, zoom);
    }
  }, [center, zoom, map]);
  return null;
}

export const GISMapView = ({ 
  villages = [], 
  infrastructure = [], 
  gapZones = [], 
  projects = [],
  onSelectVillage,
  selectedVillageId,
  height = "600px" 
}) => {
  const [activeLayers, setActiveLayers] = useState({
    villages: true,
    infrastructure: true,
    gapZones: true,
    projectBoundaries: true
  });

  const [mapCenter, setMapCenter] = useState([20.5937, 78.9629]);
  const [mapZoom, setMapZoom] = useState(5);

  const getPriorityColor = (gapScore) => {
    if (gapScore >= 75) return '#ef4444'; // Red (Critical)
    if (gapScore >= 55) return '#f97316'; // Orange (High)
    if (gapScore >= 35) return '#eab308'; // Yellow (Medium)
    return '#10b981'; // Green (Low)
  };

  const getInfraIcon = (type) => {
    // Return HTML custom marker for Leaflet
    let color = '#3b82f6';
    let label = 'I';
    if (type.includes('HEALTH')) { color = '#ec4899'; label = '+'; }
    else if (type.includes('SCHOOL')) { color = '#8b5cf6'; label = 'S'; }
    else if (type.includes('WATER')) { color = '#06b6d4'; label = 'W'; }
    else if (type.includes('ROAD')) { color = '#eab308'; label = 'R'; }
    else if (type.includes('POWER') || type.includes('SOLAR')) { color = '#f59e0b'; label = 'E'; }

    return L.divIcon({
      className: 'custom-infra-pin',
      html: `<div style="background-color: ${color}; width: 22px; height: 22px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold; font-size: 11px; border: 2px solid #0f172a; box-shadow: 0 2px 8px rgba(0,0,0,0.5);">${label}</div>`,
      iconSize: [22, 22],
      iconAnchor: [11, 11]
    });
  };

  return (
    <div className="relative rounded-2xl overflow-hidden border border-slate-800 shadow-2xl bg-slate-950" style={{ height }}>
      {/* Map Layer Toggle Toolbar */}
      <div className="absolute top-4 right-4 z-[1000] bg-slate-900/90 backdrop-blur-md border border-slate-700/80 rounded-xl p-2.5 shadow-xl flex flex-wrap gap-2 text-xs">
        <button
          onClick={() => setActiveLayers(p => ({ ...p, villages: !p.villages }))}
          className={`px-2.5 py-1 rounded-lg border font-medium transition ${
            activeLayers.villages 
              ? 'bg-emerald-500/20 border-emerald-500/50 text-emerald-300' 
              : 'bg-slate-800 border-slate-700 text-slate-400'
          }`}
        >
          📍 Villages ({villages.length})
        </button>

        <button
          onClick={() => setActiveLayers(p => ({ ...p, infrastructure: !p.infrastructure }))}
          className={`px-2.5 py-1 rounded-lg border font-medium transition ${
            activeLayers.infrastructure 
              ? 'bg-blue-500/20 border-blue-500/50 text-blue-300' 
              : 'bg-slate-800 border-slate-700 text-slate-400'
          }`}
        >
          🏥 Infrastructure ({infrastructure.length})
        </button>

        <button
          onClick={() => setActiveLayers(p => ({ ...p, gapZones: !p.gapZones }))}
          className={`px-2.5 py-1 rounded-lg border font-medium transition ${
            activeLayers.gapZones 
              ? 'bg-rose-500/20 border-rose-500/50 text-rose-300' 
              : 'bg-slate-800 border-slate-700 text-slate-400'
          }`}
        >
          🔥 Deficit Zones
        </button>

        <button
          onClick={() => setActiveLayers(p => ({ ...p, projectBoundaries: !p.projectBoundaries }))}
          className={`px-2.5 py-1 rounded-lg border font-medium transition ${
            activeLayers.projectBoundaries 
              ? 'bg-purple-500/20 border-purple-500/50 text-purple-300' 
              : 'bg-slate-800 border-slate-700 text-slate-400'
          }`}
        >
          📐 Project Areas ({projects.length})
        </button>
      </div>

      {/* Legend */}
      <div className="absolute bottom-4 left-4 z-[1000] bg-slate-900/90 backdrop-blur-md border border-slate-700/80 rounded-xl p-3 shadow-xl text-xs space-y-1.5 hidden md:block">
        <div className="font-semibold text-slate-300 mb-1 flex items-center gap-1.5">
          <Layers className="w-3.5 h-3.5 text-emerald-400" />
          <span>Gap Severity Index (0-100)</span>
        </div>
        <div className="flex items-center gap-2 text-[11px] text-slate-300">
          <span className="w-3 h-3 rounded-full bg-rose-500"></span>
          <span>Critical Gap (&ge; 75)</span>
        </div>
        <div className="flex items-center gap-2 text-[11px] text-slate-300">
          <span className="w-3 h-3 rounded-full bg-orange-500"></span>
          <span>High Deficit (55 - 74)</span>
        </div>
        <div className="flex items-center gap-2 text-[11px] text-slate-300">
          <span className="w-3 h-3 rounded-full bg-yellow-500"></span>
          <span>Medium Deficit (35 - 54)</span>
        </div>
        <div className="flex items-center gap-2 text-[11px] text-slate-300">
          <span className="w-3 h-3 rounded-full bg-emerald-500"></span>
          <span>Optimized (&lt; 35)</span>
        </div>
      </div>

      {/* Leaflet Map */}
      <MapContainer
        center={mapCenter}
        zoom={mapZoom}
        scrollWheelZoom={true}
        className="w-full h-full"
      >
        <ChangeView center={mapCenter} zoom={mapZoom} />
        {/* Dark Mode Tiles */}
        <TileLayer
          attribution='&copy; <a href="https://carto.com/">CARTO</a> &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />

        {/* 1. Gap Deficit Polygons */}
        {activeLayers.gapZones && gapZones.map((zone, idx) => {
          if (!zone.geometry || zone.geometry.type !== 'Polygon') return null;
          const coords = zone.geometry.coordinates[0].map(c => [c[1], c[0]]);
          const isCrit = zone.properties?.severity === 'CRITICAL_DEFICIT';
          return (
            <Polygon
              key={zone.id || idx}
              positions={coords}
              pathOptions={{
                color: isCrit ? '#ef4444' : '#f97316',
                fillColor: isCrit ? '#ef4444' : '#f97316',
                fillOpacity: 0.18,
                weight: 2,
                dashArray: '4, 4'
              }}
            >
              <Popup>
                <div className="p-1 space-y-1">
                  <div className="font-bold text-rose-400 text-xs">{zone.properties?.severity}</div>
                  <div className="text-sm font-semibold">{zone.properties?.centerVillage}, {zone.properties?.district}</div>
                  <div className="text-xs text-slate-300">Composite Gap: <span className="font-bold">{zone.properties?.gapScore}</span></div>
                </div>
              </Popup>
            </Polygon>
          );
        })}

        {/* 2. Project Boundary Polygons */}
        {activeLayers.projectBoundaries && projects.map((p, idx) => {
          if (!p.latitude || !p.longitude) return null;
          const lat = p.latitude;
          const lng = p.longitude;
          const delta = 0.035;
          const coords = [
            [lat - delta, lng - delta],
            [lat - delta, lng + delta],
            [lat + (delta * 0.8), lng + (delta * 1.2)],
            [lat + delta, lng - delta],
            [lat - delta, lng - delta]
          ];
          return (
            <Polygon
              key={`proj-${p.id || idx}`}
              positions={coords}
              pathOptions={{
                color: '#10b981',
                fillColor: '#10b981',
                fillOpacity: 0.15,
                weight: 2
              }}
            >
              <Popup>
                <div className="p-1 space-y-1 text-xs">
                  <div className="font-mono text-emerald-400 text-[10px]">{p.projectCode}</div>
                  <div className="font-bold text-slate-100">{p.projectName}</div>
                  <div className="text-slate-300">Status: <span className="font-semibold text-emerald-300">{p.status}</span></div>
                  {p.gapReductionPct > 0 && (
                    <div className="text-emerald-400 font-bold">
                      &darr; {p.gapReductionPct}% Deficit Drop
                    </div>
                  )}
                </div>
              </Popup>
            </Polygon>
          );
        })}

        {/* 3. Infrastructure Markers */}
        {activeLayers.infrastructure && infrastructure.map((inf) => {
          if (!inf.latitude || !inf.longitude) return null;
          return (
            <Marker
              key={`infra-${inf.id}`}
              position={[inf.latitude, inf.longitude]}
              icon={getInfraIcon(inf.infraType || '')}
            >
              <Popup>
                <div className="p-1 space-y-1 text-xs">
                  <div className="font-bold text-slate-200">{inf.name}</div>
                  <div className="text-slate-400">Type: <span className="text-slate-200">{inf.infraType}</span></div>
                  <div className="text-slate-400">Status: <span className="text-emerald-400 font-semibold">{inf.status}</span></div>
                  <div className="text-slate-400">Condition Score: <span className="text-amber-400 font-bold">{inf.conditionScore}/10</span></div>
                  {inf.schemeName && <div className="text-[10px] text-slate-400 font-mono">{inf.schemeName}</div>}
                </div>
              </Popup>
            </Marker>
          );
        })}

        {/* 4. Villages Markers (CircleMarkers with live color gradient) */}
        {activeLayers.villages && villages.map((v) => {
          if (!v.latitude || !v.longitude) return null;
          const color = getPriorityColor(v.gapScore || 50);
          const isSelected = selectedVillageId === v.id;

          return (
            <CircleMarker
              key={`village-${v.id}`}
              center={[v.latitude, v.longitude]}
              radius={isSelected ? 10 : 7}
              pathOptions={{
                color: isSelected ? '#ffffff' : color,
                fillColor: color,
                fillOpacity: 0.85,
                weight: isSelected ? 3 : 2
              }}
              eventHandlers={{
                click: () => {
                  if (onSelectVillage) onSelectVillage(v);
                }
              }}
            >
              <Popup>
                <div className="p-1 space-y-2 text-xs min-w-[200px]">
                  <div className="flex items-center justify-between">
                    <span className="font-bold text-sm text-slate-100">{v.villageName}</span>
                    <span 
                      className="px-2 py-0.5 rounded text-[10px] font-extrabold text-white" 
                      style={{ backgroundColor: color }}
                    >
                      {v.gapScore}
                    </span>
                  </div>
                  <div className="text-slate-400 text-[11px]">
                    {v.block}, {v.district}, {v.state}
                  </div>
                  <div className="text-[11px] text-slate-300">
                    Population: <span className="font-semibold text-white">{v.population?.toLocaleString() || 'N/A'}</span>
                  </div>
                  <div className="grid grid-cols-2 gap-1 pt-1 text-[10px] border-t border-slate-700">
                    <div>Road Index: <span className="font-bold text-amber-300">{v.roadConnectivityIndex || 'N/A'}%</span></div>
                    <div>Health: <span className="font-bold text-pink-300">{v.healthAccessIndex || 'N/A'}%</span></div>
                    <div>Water: <span className="font-bold text-cyan-300">{v.waterSanitationIndex || 'N/A'}%</span></div>
                    <div>Power: <span className="font-bold text-yellow-300">{v.powerReliabilityIndex || 'N/A'}%</span></div>
                  </div>

                  {onSelectVillage && (
                    <button
                      onClick={() => onSelectVillage(v)}
                      className="w-full mt-2 py-1 bg-emerald-600 hover:bg-emerald-500 text-white rounded text-[11px] font-bold transition flex items-center justify-center gap-1"
                    >
                      <span>Analyze Village Gap</span>
                      <ArrowUpRight className="w-3 h-3" />
                    </button>
                  )}
                </div>
              </Popup>
            </CircleMarker>
          );
        })}
      </MapContainer>
    </div>
  );
};

export default GISMapView;
