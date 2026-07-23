import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { RefreshCw, CheckCircle, AlertTriangle, XCircle, Server, Database, Zap } from 'lucide-react';

interface HealthData { api: string; database: string; timestamp: string; uptime: number; }

function StatusBadge({ status }: { status: string }) {
  if (status === 'ok') return (
    <span className="flex items-center gap-1 text-[#00D68F] text-xs font-medium">
      <CheckCircle className="w-3.5 h-3.5" /> Operational
    </span>
  );
  if (status === 'degraded') return (
    <span className="flex items-center gap-1 text-[#FFB347] text-xs font-medium">
      <AlertTriangle className="w-3.5 h-3.5" /> Degraded
    </span>
  );
  return (
    <span className="flex items-center gap-1 text-[#FF4D6D] text-xs font-medium">
      <XCircle className="w-3.5 h-3.5" /> Error
    </span>
  );
}

function HealthRow({ icon: Icon, label, status }: { icon: any; label: string; status: string }) {
  return (
    <div className="flex items-center justify-between py-4 border-b border-white/5 last:border-0">
      <div className="flex items-center gap-3">
        <div className="w-8 h-8 rounded-lg bg-[#1C2536] flex items-center justify-center">
          <Icon className="w-4 h-4 text-[#00E5FF]" />
        </div>
        <span className="text-sm text-[#CBD5E0]">{label}</span>
      </div>
      <StatusBadge status={status} />
    </div>
  );
}

function formatUptime(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  return `${h}h ${m}m ${s}s`;
}

export function HealthPage() {
  const { data, isLoading, refetch, isFetching } = useQuery<HealthData>({
    queryKey: ['admin-health'],
    queryFn: () => api.get('/api/admin/health').then(r => r.data),
    refetchInterval: 30_000
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">System Health</h1>
          <p className="text-sm text-[#718096] mt-1">Platform infrastructure status</p>
        </div>
        <button
          onClick={() => refetch()}
          className="flex items-center gap-2 px-4 py-2 rounded-xl bg-[#1C2536] border border-[#2D3C56] text-sm text-[#CBD5E0] hover:border-[#00E5FF] transition-colors"
        >
          <RefreshCw className={`w-4 h-4 ${isFetching ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      <div className="glass rounded-xl p-6">
        {isLoading ? (
          <div className="space-y-4">
            {[1,2,3].map(i => <div key={i} className="h-12 bg-[#1C2536] rounded animate-pulse" />)}
          </div>
        ) : (
          <>
            <HealthRow icon={Server} label="API Server" status={data?.api || 'error'} />
            <HealthRow icon={Database} label="Database" status={data?.database || 'error'} />
            <HealthRow icon={Zap} label="WebSocket" status="ok" />
          </>
        )}
      </div>

      {data && (
        <div className="grid grid-cols-2 gap-4">
          <div className="glass rounded-xl p-5">
            <p className="text-xs text-[#718096] mb-1">Server Uptime</p>
            <p className="text-2xl font-bold text-white">{formatUptime(data.uptime)}</p>
          </div>
          <div className="glass rounded-xl p-5">
            <p className="text-xs text-[#718096] mb-1">Last Checked</p>
            <p className="text-2xl font-bold text-white">{new Date(data.timestamp).toLocaleTimeString()}</p>
          </div>
        </div>
      )}
    </div>
  );
}
