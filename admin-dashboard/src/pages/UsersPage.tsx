import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { Search, Smartphone, Calendar } from 'lucide-react';

interface User {
  id: string;
  email: string;
  displayName: string | null;
  subscriptionPlan: string;
  subscriptionStatus: string;
  createdAt: string;
  devices: {
    id: string;
    childName: string;
    deviceModel: string;
    isOnline: boolean;
  }[];
  _count: { devices: number };
}

export function UsersPage() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const { data: users = [], isLoading } = useQuery<User[]>({
    queryKey: ['admin-users', search, page],
    queryFn: () => api.get('/api/admin/users', { params: { search, page } }).then(r => r.data),
    refetchInterval: 60_000
  });

  const planColor = (plan: string) => ({
    free: 'text-[#718096] bg-[#718096]/10',
    monthly: 'text-[#00E5FF] bg-[#00E5FF]/10',
    annual: 'text-[#00D68F] bg-[#00D68F]/10',
  }[plan] || 'text-[#718096] bg-[#718096]/10');

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Users</h1>
          <p className="text-sm text-[#718096] mt-1">Registered parent accounts</p>
        </div>
      </div>

      {/* Search */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#4A5568]" />
        <input
          type="text"
          placeholder="Search by email…"
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(0); }}
          className="w-full pl-9 pr-4 py-2.5 rounded-xl bg-[#1C2536] border border-[#2D3C56] text-white text-sm placeholder-[#4A5568] focus:outline-none focus:border-[#00E5FF] transition-colors"
        />
      </div>

      {/* Table */}
      <div className="glass rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-white/5">
              <th className="px-4 py-3 text-left text-xs font-medium text-[#718096]">Email</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-[#718096]">Plan</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-[#718096]">Devices</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-[#718096]">Joined</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <tr key={i} className="border-b border-white/5">
                  {Array.from({ length: 4 }).map((_, j) => (
                    <td key={j} className="px-4 py-3">
                      <div className="h-4 bg-[#1C2536] rounded animate-pulse w-24" />
                    </td>
                  ))}
                </tr>
              ))
            ) : users.length === 0 ? (
              <tr><td colSpan={4} className="px-4 py-8 text-center text-[#718096]">No users found</td></tr>
            ) : (
              users.map(user => (
                <tr key={user.id} className="border-b border-white/5 hover:bg-white/2 transition-colors">
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <div className="w-7 h-7 rounded-full bg-[#1C2536] flex items-center justify-center text-xs font-bold text-[#00E5FF]">
                        {user.email[0].toUpperCase()}
                      </div>
                      <div>
                        <p className="text-white font-medium">{user.email}</p>
                        {user.displayName && <p className="text-xs text-[#718096]">{user.displayName}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`px-2 py-0.5 rounded-full text-xs font-medium capitalize ${planColor(user.subscriptionPlan)}`}>
                      {user.subscriptionPlan}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-col gap-1">
                      <span className="flex items-center gap-1 text-[#CBD5E0]">
                        <Smartphone className="w-3.5 h-3.5 text-[#4A5568]" />
                        {user._count.devices}
                      </span>
                      {user.devices.map(device => (
                        <span key={device.id} className="text-xs text-[#718096] flex items-center gap-1">
                          <div className={`w-1.5 h-1.5 rounded-full ${device.isOnline ? 'bg-[#00D68F]' : 'bg-[#718096]'}`} />
                          {device.childName} ({device.deviceModel})
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="flex items-center gap-1 text-[#718096] text-xs">
                      <Calendar className="w-3 h-3" />
                      {new Date(user.createdAt).toLocaleDateString()}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Pagination */}
        <div className="flex items-center justify-between px-4 py-3 border-t border-white/5">
          <span className="text-xs text-[#718096]">Page {page + 1}</span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="px-3 py-1 rounded-lg text-xs bg-[#1C2536] text-[#CBD5E0] disabled:opacity-40 hover:bg-[#2D3C56] transition-colors"
            >Previous</button>
            <button
              onClick={() => setPage(p => p + 1)}
              disabled={users.length < 20}
              className="px-3 py-1 rounded-lg text-xs bg-[#1C2536] text-[#CBD5E0] disabled:opacity-40 hover:bg-[#2D3C56] transition-colors"
            >Next</button>
          </div>
        </div>
      </div>
    </div>
  );
}
