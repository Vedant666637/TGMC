import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Users, Smartphone, Wifi, Bell, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

interface Stats {
  totalParents: number;
  totalDevices: number;
  onlineDevices: number;
  totalAlerts: number;
}

const mockTrendData = Array.from({ length: 7 }, (_, i) => ({
  day: ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'][i],
  parents: Math.floor(Math.random() * 50) + 100,
  devices: Math.floor(Math.random() * 80) + 150
}));

function KpiCard({ label, value, icon: Icon, accent, sub }: { label: string; value: number | string; icon: any; accent: string; sub?: string }) {
  return (
    <div className={`glass rounded-xl p-5 border-l-2`} style={{ borderLeftColor: accent }}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs text-[#718096] mb-1">{label}</p>
          <p className="text-3xl font-bold text-white">{typeof value === 'number' ? value.toLocaleString() : value}</p>
          {sub && <p className="text-xs text-[#718096] mt-1">{sub}</p>}
        </div>
        <div className="w-10 h-10 rounded-lg flex items-center justify-center" style={{ backgroundColor: `${accent}20` }}>
          <Icon className="w-5 h-5" style={{ color: accent }} />
        </div>
      </div>
    </div>
  );
}

export function DashboardPage() {
  const { data: stats, isLoading } = useQuery<Stats>({
    queryKey: ['admin-stats'],
    queryFn: () => api.get('/api/admin/stats').then(r => r.data),
    refetchInterval: 30_000
  });

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Dashboard</h1>
        <p className="text-sm text-[#718096] mt-1">Platform overview — live data</p>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-2 xl:grid-cols-4 gap-4">
        <KpiCard label="Total Parents" value={isLoading ? '—' : stats?.totalParents ?? 0} icon={Users} accent="#00E5FF" />
        <KpiCard label="Total Devices" value={isLoading ? '—' : stats?.totalDevices ?? 0} icon={Smartphone} accent="#6C63FF" />
        <KpiCard label="Online Now" value={isLoading ? '—' : stats?.onlineDevices ?? 0} icon={Wifi} accent="#00D68F" sub="Active child devices" />
        <KpiCard label="Total Alerts" value={isLoading ? '—' : stats?.totalAlerts ?? 0} icon={Bell} accent="#FFB347" />
      </div>

      {/* Trend Chart */}
      <div className="glass rounded-xl p-6">
        <div className="flex items-center gap-2 mb-6">
          <TrendingUp className="w-4 h-4 text-[#00E5FF]" />
          <h2 className="text-sm font-semibold text-white">Growth Trend (last 7 days)</h2>
        </div>
        <ResponsiveContainer width="100%" height={240}>
          <AreaChart data={mockTrendData}>
            <defs>
              <linearGradient id="parentGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#00E5FF" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#00E5FF" stopOpacity={0} />
              </linearGradient>
              <linearGradient id="deviceGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#6C63FF" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#6C63FF" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1C2536" />
            <XAxis dataKey="day" tick={{ fontSize: 12, fill: '#718096' }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fontSize: 12, fill: '#718096' }} axisLine={false} tickLine={false} />
            <Tooltip
              contentStyle={{ background: '#1C2536', border: '1px solid #2D3C56', borderRadius: 8, color: '#F7FAFC', fontSize: 12 }}
              cursor={{ stroke: '#00E5FF', strokeWidth: 1 }}
            />
            <Area type="monotone" dataKey="parents" name="Parents" stroke="#00E5FF" fill="url(#parentGrad)" strokeWidth={2} />
            <Area type="monotone" dataKey="devices" name="Devices" stroke="#6C63FF" fill="url(#deviceGrad)" strokeWidth={2} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
