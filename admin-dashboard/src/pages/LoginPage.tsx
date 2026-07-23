import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import { api } from '../lib/api';
import { Shield, Eye, EyeOff, AlertCircle } from 'lucide-react';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login } = useAuthStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await api.post('/api/admin/login', { email, password });
      login(data.accessToken, data.email);
      navigate('/');
    } catch (err: any) {
      setError(err.response?.data?.error || 'Login failed. Check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#0A0E1A] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex w-16 h-16 rounded-2xl bg-gradient-to-br from-[#00E5FF] to-[#6C63FF] items-center justify-center mb-4">
            <Shield className="w-8 h-8 text-[#0A0E1A]" />
          </div>
          <h1 className="text-2xl font-bold text-white">TGM-C Admin</h1>
          <p className="text-sm text-[#718096] mt-1">Internal console — authorized staff only</p>
        </div>

        {/* Card */}
        <div className="glass rounded-2xl p-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-medium text-[#CBD5E0] mb-1.5">Email Address</label>
              <input
                id="admin-email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="w-full px-4 py-3 rounded-xl bg-[#0A0E1A] border border-[#2D3C56] text-white text-sm placeholder-[#4A5568] focus:outline-none focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF]/30 transition-colors"
                placeholder="admin@tgmc.app"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-[#CBD5E0] mb-1.5">Password</label>
              <div className="relative">
                <input
                  id="admin-password"
                  type={showPass ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full px-4 py-3 pr-10 rounded-xl bg-[#0A0E1A] border border-[#2D3C56] text-white text-sm placeholder-[#4A5568] focus:outline-none focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF]/30 transition-colors"
                  placeholder="••••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[#4A5568] hover:text-[#00E5FF] transition-colors"
                >
                  {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 px-4 py-3 rounded-lg bg-[#FF4D6D]/10 border border-[#FF4D6D]/20 text-[#FF4D6D] text-sm">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                {error}
              </div>
            )}

            <button
              id="admin-login-btn"
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-xl bg-[#00E5FF] text-[#0A0E1A] font-bold text-sm hover:bg-[#40EEFF] disabled:opacity-60 disabled:cursor-not-allowed transition-all duration-150"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-[#0A0E1A]/30 border-t-[#0A0E1A] rounded-full animate-spin" />
                  Signing In…
                </span>
              ) : 'Sign In'}
            </button>
          </form>
        </div>

        <p className="text-center text-xs text-[#4A5568] mt-6">
          This dashboard is for internal TGM-C staff only.
        </p>
      </div>
    </div>
  );
}
