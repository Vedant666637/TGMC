import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { HardDrive, Video, Image, MessageSquare, ShoppingBag } from 'lucide-react';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts';

interface StorageItem {
  name: string;
  size: number;
  color: string;
}

const iconMap: Record<string, any> = {
  'Camera Snapshots': Image,
  'Audio Recordings': MessageSquare,
  'Location History': HardDrive,
  'Content (Videos)': Video,
  'Store Assets': ShoppingBag
};

export function StoragePage() {
  const { data: storageData = [], isLoading } = useQuery<StorageItem[]>({
    queryKey: ['admin-storage'],
    queryFn: () => api.get('/api/admin/storage').then(r => r.data),
    refetchInterval: 60_000
  });

  const totalGB = storageData.reduce((sum, s) => sum + s.size, 0);
  const usedPercent = Math.round((totalGB / 50) * 100);
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Storage</h1>
        <p className="text-sm text-[#718096] mt-1">Infrastructure usage by module</p>
      </div>

      {/* Summary */}
      <div className="glass rounded-xl p-6 flex items-center gap-8">
        <div className="flex-1">
          <p className="text-xs text-[#718096] mb-1">Total Used</p>
          <p className="text-4xl font-black text-white">{totalGB.toFixed(1)} <span className="text-lg font-medium text-[#718096]">GB</span></p>
          <div className="mt-3 h-2 bg-[#1C2536] rounded-full overflow-hidden">
            <div
              className="h-full rounded-full bg-gradient-to-r from-[#00E5FF] to-[#6C63FF] transition-all"
              style={{ width: `${usedPercent}%` }}
            />
          </div>
          <p className="text-xs text-[#718096] mt-1">{usedPercent}% of 50 GB plan limit used</p>
        </div>
        <ResponsiveContainer width={180} height={160}>
          <PieChart>
            <Pie data={storageData} dataKey="size" nameKey="name" cx="50%" cy="50%" outerRadius={70} innerRadius={45} strokeWidth={0}>
              {storageData.map((entry, i) => (
                <Cell key={i} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{ background: '#1C2536', border: '1px solid #2D3C56', borderRadius: 8, fontSize: 12 }}
              formatter={(v: number) => [`${v} GB`]}
            />
          </PieChart>
        </ResponsiveContainer>
      </div>

      {/* Module breakdown */}
      <div className="glass rounded-xl overflow-hidden">
        <div className="px-4 py-3 border-b border-white/5">
          <h2 className="text-sm font-semibold text-white">Breakdown by Module</h2>
        </div>
        {isLoading ? (
          <div className="p-4 flex justify-center">
             <p className="text-sm text-[#718096]">Loading storage data...</p>
          </div>
        ) : storageData.map(({ name, size, color }) => {
          const Icon = iconMap[name] || HardDrive;
          return (
            <div key={name} className="flex items-center gap-4 px-4 py-4 border-b border-white/5 last:border-0">
              <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ backgroundColor: `${color}20` }}>
                <Icon className="w-4 h-4" style={{ color }} />
              </div>
              <div className="flex-1">
                <p className="text-sm text-[#CBD5E0]">{name}</p>
                <div className="mt-1 h-1.5 bg-[#1C2536] rounded-full overflow-hidden">
                  <div className="h-full rounded-full" style={{ width: `${(size / Math.max(totalGB, 1)) * 100}%`, backgroundColor: color }} />
                </div>
              </div>
              <span className="text-sm font-medium text-white w-16 text-right">{size} GB</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
