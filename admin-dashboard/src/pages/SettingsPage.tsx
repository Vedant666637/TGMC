import { useAuthStore } from '../store/authStore';
import { Shield, Lock, Bell, Database } from 'lucide-react';

export function SettingsPage() {
  const { email } = useAuthStore();
  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-2xl font-bold text-white">Settings</h1>
        <p className="text-sm text-[#718096] mt-1">Admin account preferences</p>
      </div>

      {/* Account */}
      <div className="glass rounded-xl p-6 space-y-4">
        <div className="flex items-center gap-2 mb-2">
          <Shield className="w-4 h-4 text-[#00E5FF]" />
          <h2 className="text-sm font-semibold text-white">Account</h2>
        </div>
        <div className="flex items-center justify-between py-3 border-b border-white/5">
          <div>
            <p className="text-sm text-[#CBD5E0]">Email</p>
            <p className="text-xs text-[#718096]">{email}</p>
          </div>
          <button className="text-xs text-[#00E5FF] hover:underline">Change</button>
        </div>
        <div className="flex items-center justify-between py-3">
          <div className="flex items-center gap-2">
            <Lock className="w-4 h-4 text-[#718096]" />
            <div>
              <p className="text-sm text-[#CBD5E0]">Multi-Factor Authentication</p>
              <p className="text-xs text-[#718096]">Strongly recommended for admin accounts</p>
            </div>
          </div>
          <span className="text-xs px-2 py-0.5 rounded-full bg-[#FF4D6D]/10 text-[#FF4D6D]">Not Configured</span>
        </div>
      </div>

      {/* Data Retention */}
      <div className="glass rounded-xl p-6 space-y-4">
        <div className="flex items-center gap-2 mb-2">
          <Database className="w-4 h-4 text-[#00E5FF]" />
          <h2 className="text-sm font-semibold text-white">Platform Data Retention</h2>
        </div>
        {[
          { label: 'Location history', value: '90 days' },
          { label: 'Audio recordings', value: '30 days' },
          { label: 'Camera snapshots', value: '30 days' },
          { label: 'Alert logs', value: '180 days' },
        ].map(({ label, value }) => (
          <div key={label} className="flex items-center justify-between py-2 border-b border-white/5 last:border-0">
            <span className="text-sm text-[#CBD5E0]">{label}</span>
            <span className="text-sm font-medium text-[#00E5FF]">{value}</span>
          </div>
        ))}
        <p className="text-xs text-[#718096]">Retention windows are configured at the platform level per PRD §6.5.</p>
      </div>

      {/* Version */}
      <div className="glass rounded-xl p-4 flex items-center justify-between">
        <span className="text-sm text-[#718096]">TGM-C Admin Dashboard</span>
        <span className="text-sm font-medium text-white">v1.0.0</span>
      </div>
    </div>
  );
}
