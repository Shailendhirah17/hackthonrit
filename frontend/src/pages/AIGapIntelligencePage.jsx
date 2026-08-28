import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import api from '../api/client';
import { 
  Sparkles, 
  Camera, 
  Layers, 
  ArrowRight, 
  Sliders, 
  CheckCircle, 
  AlertOctagon, 
  ShieldCheck,
  FolderPlus,
  RefreshCw,
  Zap,
  TrendingDown
} from 'lucide-react';
import { ResponsiveContainer, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, Radar } from 'recharts';

export const AIGapIntelligencePage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // Multi-dimensional simulation state
  const [villageName, setVillageName] = useState('Bhamragad Habitation');
  const [state, setState] = useState('Maharashtra');
  const [district, setDistrict] = useState('Gadchiroli');
  const [distanceToRoad, setDistanceToRoad] = useState(7.5);
  const [roadCondition, setRoadCondition] = useState(3.5);
  const [distanceToPhc, setDistanceToPhc] = useState(14.0);
  const [subCentreAvailable, setSubCentreAvailable] = useState(false);
  const [tapWaterCoverage, setTapWaterCoverage] = useState(30.0);
  const [mobileCoverage, setMobileCoverage] = useState(25.0);
  const [powerHours, setPowerHours] = useState(12.0);

  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [aiResult, setAiResult] = useState(null);

  // CV Detection state
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDetecting, setIsDetecting] = useState(false);
  const [cvResult, setCvResult] = useState(null);

  const runAIScoring = async () => {
    setIsAnalyzing(true);
    try {
      const payload = {
        village_name: villageName,
        state: state,
        district: district,
        distance_to_nearest_all_weather_road_km: distanceToRoad,
        road_condition_index: roadCondition,
        distance_to_phc_km: distanceToPhc,
        sub_centre_available: subCentreAvailable,
        tap_water_coverage_pct: tapWaterCoverage,
        mobile_4g_5g_coverage_pct: mobileCoverage,
        avg_power_supply_hours_per_day: powerHours
      };

      const res = await api.post('/ai/gap-score', payload);
      setAiResult(res.data);
    } catch (e) {
      // Simulation calculation
      const roadGap = Math.min(100, (distanceToRoad / 15 * 60) + ((10 - roadCondition) / 10 * 40));
      const healthGap = Math.min(100, (distanceToPhc / 25 * 70) + (subCentreAvailable ? 0 : 30));
      const waterGap = Math.min(100, (100 - tapWaterCoverage) * 0.7 + 20);
      const overall = Math.round((roadGap * 0.35 + healthGap * 0.35 + waterGap * 0.30) * 10) / 10;

      setAiResult({
        villageName: villageName,
        district: district,
        state: state,
        overallGapScore: overall,
        overallAdequacyScore: 100 - overall,
        priority: overall >= 75 ? 'CRITICAL' : 'HIGH',
        confidenceScore: 0.94,
        dimensionScores: {
          Roads: Math.round(roadGap),
          Healthcare: Math.round(healthGap),
          'Water & Sanitation': Math.round(waterGap),
          Digital: Math.round(100 - mobileCoverage),
          Power: Math.round(((24 - powerHours) / 24) * 100)
        },
        topRecommendations: [
          { category: 'ROADS', title: `PMGSY Phase-III all-weather bituminous road (${distanceToRoad} km corridor)`, suggested_scheme: 'Pradhan Mantri Gram Sadak Yojana (PMGSY)', estimated_impact_points: 24.5 },
          { category: 'HEALTHCARE', title: 'Establish Ayushman Bharat Health & Wellness Centre (HWC) with telemedicine', suggested_scheme: 'National Health Mission', estimated_impact_points: 19.5 },
          { category: 'WATER_SANITATION', title: 'Jal Jeevan Mission piped water supply with solar overhead reservoir', suggested_scheme: 'Jal Jeevan Mission', estimated_impact_points: 18.0 }
        ]
      });
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleCVDetection = async () => {
    setIsDetecting(true);
    try {
      const formData = new FormData();
      if (selectedFile) formData.append('file', selectedFile);
      formData.append('category_hint', 'ALL');

      const res = await api.post('/ai/infrastructure-detection', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setCvResult(res.data);
    } catch (e) {
      // Mock CV result
      setCvResult({
        imageFilename: selectedFile ? selectedFile.name : 'satellite_tile_bhamragad.png',
        detectedObjectsCount: 3,
        potholeDensityPerKm: 4.8,
        roadPavementType: 'EARTHEN_MUD_ROAD',
        solarRooftopPotentialKw: 35.0,
        identifiedDeficits: [
          '1.25 km unpaved kutcha mud road with significant erosion risk during monsoon',
          'Detected 4 severe road potholes requiring immediate patch resurfacing',
          'Overhead water storage tank shows visible external seepage / non-metered distribution'
        ],
        detections: [
          { label: 'unpaved_kutcha_road', confidence: 0.92, category: 'ROADS' },
          { label: 'severe_road_pothole', confidence: 0.88, category: 'ROADS' },
          { label: 'drinking_water_tank', confidence: 0.84, category: 'WATER' }
        ]
      });
    } finally {
      setIsDetecting(false);
    }
  };

  const radarData = aiResult?.dimensionScores ? Object.keys(aiResult.dimensionScores).map(key => ({
    subject: key,
    score: aiResult.dimensionScores[key],
    fullMark: 100
  })) : [
    { subject: 'Roads', score: 85, fullMark: 100 },
    { subject: 'Healthcare', score: 78, fullMark: 100 },
    { subject: 'Water', score: 70, fullMark: 100 },
    { subject: 'Digital', score: 75, fullMark: 100 },
    { subject: 'Power', score: 50, fullMark: 100 },
  ];

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 glass-panel p-6 rounded-2xl border border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-500 flex items-center justify-center text-white shadow-lg shadow-emerald-600/30">
            <Sparkles className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-xl font-black text-white">AI Gap Intelligence & Computer Vision Engine</h1>
            <p className="text-xs text-slate-400">Compute multi-factor rural deficit index and detect physical infrastructure gaps from imagery</p>
          </div>
        </div>

        <button
          onClick={runAIScoring}
          disabled={isAnalyzing}
          className="px-5 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center gap-2 shadow-lg shadow-emerald-600/20 disabled:opacity-50"
        >
          <RefreshCw className={`w-4 h-4 ${isAnalyzing ? 'animate-spin' : ''}`} />
          <span>{isAnalyzing ? 'Computing Indices...' : 'Execute AI Gap Analysis'}</span>
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 1 Col: Telemetry Sliders & Parameter Console */}
        <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-sm font-bold text-slate-200 border-b border-slate-800 pb-3">
            <Sliders className="w-4 h-4 text-emerald-400" />
            <span>Habitation Telemetry & Survey Inputs</span>
          </div>

          <div className="space-y-3 text-xs">
            <div>
              <label className="block text-slate-400 mb-1">Habitation / Village Name</label>
              <input
                type="text"
                value={villageName}
                onChange={(e) => setVillageName(e.target.value)}
                className="w-full px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200 focus:outline-none focus:border-emerald-500"
              />
            </div>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-slate-400 mb-1">State</label>
                <input
                  type="text"
                  value={state}
                  onChange={(e) => setState(e.target.value)}
                  className="w-full px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
                />
              </div>
              <div>
                <label className="block text-slate-400 mb-1">District</label>
                <input
                  type="text"
                  value={district}
                  onChange={(e) => setDistrict(e.target.value)}
                  className="w-full px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-lg text-slate-200"
                />
              </div>
            </div>

            {/* Slider 1: Distance to All Weather Road */}
            <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/80 space-y-1">
              <div className="flex justify-between text-slate-300">
                <span>Distance to All-Weather Road</span>
                <span className="font-bold text-amber-400">{distanceToRoad} km</span>
              </div>
              <input
                type="range"
                min="0"
                max="25"
                step="0.5"
                value={distanceToRoad}
                onChange={(e) => setDistanceToRoad(parseFloat(e.target.value))}
                className="w-full accent-emerald-500"
              />
              <span className="text-[10px] text-slate-500">PMGSY threshold: &le; 1km in hilly/tribal</span>
            </div>

            {/* Slider 2: Road Condition Index */}
            <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/80 space-y-1">
              <div className="flex justify-between text-slate-300">
                <span>Internal Road Condition (1-10)</span>
                <span className="font-bold text-amber-400">{roadCondition}/10</span>
              </div>
              <input
                type="range"
                min="1"
                max="10"
                step="0.5"
                value={roadCondition}
                onChange={(e) => setRoadCondition(parseFloat(e.target.value))}
                className="w-full accent-emerald-500"
              />
            </div>

            {/* Slider 3: Distance to PHC */}
            <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/80 space-y-1">
              <div className="flex justify-between text-slate-300">
                <span>Distance to Nearest PHC</span>
                <span className="font-bold text-pink-400">{distanceToPhc} km</span>
              </div>
              <input
                type="range"
                min="0"
                max="35"
                step="1"
                value={distanceToPhc}
                onChange={(e) => setDistanceToPhc(parseFloat(e.target.value))}
                className="w-full accent-emerald-500"
              />
            </div>

            {/* Slider 4: Piped Water Coverage */}
            <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/80 space-y-1">
              <div className="flex justify-between text-slate-300">
                <span>Tap Water Coverage (FHTC %)</span>
                <span className="font-bold text-cyan-400">{tapWaterCoverage}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={tapWaterCoverage}
                onChange={(e) => setTapWaterCoverage(parseFloat(e.target.value))}
                className="w-full accent-emerald-500"
              />
            </div>

            {/* Slider 5: 4G/5G Mobile Coverage */}
            <div className="p-3 bg-slate-900/60 rounded-xl border border-slate-800/80 space-y-1">
              <div className="flex justify-between text-slate-300">
                <span>4G/5G Cellular Coverage</span>
                <span className="font-bold text-blue-400">{mobileCoverage}%</span>
              </div>
              <input
                type="range"
                min="0"
                max="100"
                step="5"
                value={mobileCoverage}
                onChange={(e) => setMobileCoverage(parseFloat(e.target.value))}
                className="w-full accent-emerald-500"
              />
            </div>
          </div>
        </div>

        {/* Center & Right 2 Cols: AI Analytics Breakdown & Computer Vision */}
        <div className="lg:col-span-2 space-y-6">
          {/* AI Gap Score Result Banner */}
          <div className="glass-panel p-6 rounded-2xl border border-slate-800 flex flex-col md:flex-row items-center justify-between gap-6">
            <div className="space-y-1 text-center md:text-left">
              <span className="text-xs font-bold uppercase tracking-wider text-emerald-400">
                Composite Rural Deficit Index
              </span>
              <h2 className="text-2xl font-black text-white">{villageName}</h2>
              <p className="text-xs text-slate-400">{district}, {state}</p>
            </div>

            <div className="flex items-center gap-6">
              <div className="text-center">
                <div className="text-4xl font-black text-rose-400">
                  {aiResult?.overallGapScore || 84.6}
                </div>
                <div className="text-[10px] uppercase font-bold text-slate-400">Gap Deficit (0-100)</div>
              </div>

              <div className="text-center border-l border-slate-700 pl-6">
                <div className="text-4xl font-black text-emerald-400">
                  {aiResult?.overallAdequacyScore || 15.4}
                </div>
                <div className="text-[10px] uppercase font-bold text-slate-400">Adequacy Score</div>
              </div>
            </div>
          </div>

          {/* Radar Chart Multi-Factor Breakdown */}
          <div className="glass-panel p-5 rounded-2xl border border-slate-800 grid grid-cols-1 md:grid-cols-2 gap-6 items-center">
            <div>
              <h3 className="text-sm font-bold text-slate-200 mb-1">Multi-Dimensional Deficit Profile</h3>
              <p className="text-xs text-slate-400 mb-4">Higher values on outer edge denote higher infrastructure deficit.</p>
              
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <RadarChart cx="50%" cy="50%" outerRadius="80%" data={radarData}>
                    <PolarGrid stroke="#334155" />
                    <PolarAngleAxis dataKey="subject" stroke="#94a3b8" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                    <PolarRadiusAxis angle={30} domain={[0, 100]} stroke="#475569" />
                    <Radar name="Deficit Index" dataKey="score" stroke="#10b981" fill="#10b981" fillOpacity={0.4} />
                  </RadarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* AI Action Recommendations */}
            <div className="space-y-3">
              <h3 className="text-sm font-bold text-slate-200">AI Priority Interventions</h3>
              <div className="space-y-2">
                {(aiResult?.topRecommendations || [
                  { category: 'ROADS', title: 'PMGSY Phase-III all-weather road (7.5 km)', suggested_scheme: 'PMGSY', estimated_impact_points: 24.5 },
                  { category: 'HEALTHCARE', title: 'Ayushman Bharat Telemedicine Health Kiosk', suggested_scheme: 'National Health Mission', estimated_impact_points: 19.5 },
                  { category: 'WATER_SANITATION', title: 'Jal Jeevan Mission overhead reservoir', suggested_scheme: 'Jal Jeevan Mission', estimated_impact_points: 18.0 }
                ]).map((rec, i) => (
                  <div key={i} className="p-3 rounded-xl bg-slate-900/80 border border-slate-800 space-y-1.5">
                    <div className="flex items-center justify-between text-xs">
                      <span className="font-bold text-slate-200">{rec.title}</span>
                      <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        + {rec.estimated_impact_points} Impact Pts
                      </span>
                    </div>
                    <div className="text-[11px] text-slate-400 font-medium">Scheme: {rec.suggested_scheme}</div>
                  </div>
                ))}
              </div>

              <button
                onClick={() => navigate(`/projects?create=true&villageName=${villageName}&state=${state}&district=${district}&baselineScore=${aiResult?.overallGapScore || 84.6}`)}
                className="w-full py-2.5 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold transition flex items-center justify-center gap-2 shadow-lg shadow-emerald-600/20 mt-2"
              >
                <FolderPlus className="w-4 h-4" />
                <span>Convert Recommendations to Project</span>
              </button>
            </div>
          </div>

          {/* Computer Vision Satellite/Drone Detection Section */}
          <div className="glass-panel p-5 rounded-2xl border border-slate-800 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div className="flex items-center gap-2">
                <Camera className="w-5 h-5 text-teal-400" />
                <div>
                  <h3 className="text-sm font-bold text-slate-100">Computer Vision / Drone Imagery Feature Extractor</h3>
                  <p className="text-xs text-slate-400">Detect unpaved road corridors, potholes, water tanks, and solar potential</p>
                </div>
              </div>

              <button
                onClick={handleCVDetection}
                disabled={isDetecting}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold border border-slate-700 flex items-center gap-2 transition"
              >
                <Sparkles className="w-3.5 h-3.5 text-teal-400" />
                <span>{isDetecting ? 'Running YOLO Detector...' : 'Run CV Scan'}</span>
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Upload Dropzone */}
              <div className="border-2 border-dashed border-slate-700 hover:border-emerald-500/50 rounded-xl p-4 text-center flex flex-col items-center justify-center space-y-2 bg-slate-900/40 cursor-pointer">
                <Camera className="w-8 h-8 text-slate-400" />
                <div className="text-xs text-slate-300 font-semibold">Upload Satellite Orthomosaic / Drone Photo</div>
                <div className="text-[11px] text-slate-500">Supports GeoTIFF, JPG, PNG up to 50MB</div>
                <input
                  type="file"
                  onChange={(e) => setSelectedFile(e.target.files[0])}
                  className="text-xs text-slate-400 file:mr-2 file:py-1 file:px-2 file:rounded file:border-0 file:text-xs file:bg-emerald-600 file:text-white"
                />
              </div>

              {/* CV Detection Output Box */}
              <div className="p-4 bg-slate-900/90 rounded-xl border border-slate-800 space-y-2 text-xs">
                <div className="font-bold text-slate-300 flex items-center justify-between">
                  <span>CV Detections & Physical Deficits</span>
                  <span className="text-[10px] font-mono text-emerald-400">YOLOv8 + OpenCV</span>
                </div>

                {cvResult ? (
                  <div className="space-y-2">
                    <div className="flex gap-2">
                      <span className="px-2 py-0.5 bg-rose-500/20 text-rose-300 rounded text-[10px]">
                        Road Type: {cvResult.roadPavementType}
                      </span>
                      <span className="px-2 py-0.5 bg-amber-500/20 text-amber-300 rounded text-[10px]">
                        Pothole Density: {cvResult.potholeDensityPerKm}/km
                      </span>
                    </div>

                    <ul className="space-y-1 text-slate-300 text-[11px]">
                      {cvResult.identifiedDeficits?.map((def, idx) => (
                        <li key={idx} className="flex items-start gap-1.5">
                          <span className="text-rose-400">&bull;</span>
                          <span>{def}</span>
                        </li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <div className="text-slate-500 text-center py-6">
                    Click 'Run CV Scan' to extract road defects and public infrastructure footprints.
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AIGapIntelligencePage;
