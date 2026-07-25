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
    <div className="min-h-screen bg-[var(--clay-bg)] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex w-16 h-16 rounded-2xl clay-icon-box items-center justify-center mb-4 text-[var(--clay-primary)]">
            <Shield className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-bold text-[var(--clay-text-title)]">TGM-C Admin</h1>
          <p className="text-sm text-[var(--clay-text-body)] mt-1">Internal console — authorized staff only</p>
        </div>

        {/* Card */}
        <div className="clay-card p-8">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-xs font-medium text-[var(--clay-text-title)] mb-1.5">Email Address</label>
              <input
                id="admin-email"
                type="email"
                autoComplete="email"
                required
                value={email}
                onChange={e => setEmail(e.target.value)}
                className="w-full px-4 py-3 clay-input text-[var(--clay-text-title)] placeholder-[var(--clay-text-body)] focus:ring-1 focus:ring-[var(--clay-primary)]/30 transition-colors"
                placeholder="admin@tgmc.app"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-[var(--clay-text-title)] mb-1.5">Password</label>
              <div className="relative">
                <input
                  id="admin-password"
                  type={showPass ? 'text' : 'password'}
                  autoComplete="current-password"
                  required
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full px-4 py-3 pr-10 clay-input text-[var(--clay-text-title)] placeholder-[var(--clay-text-body)] focus:ring-1 focus:ring-[var(--clay-primary)]/30 transition-colors"
                  placeholder="••••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--clay-text-body)] hover:text-[var(--clay-primary)] transition-colors"
                >
                  {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="flex items-center gap-2 px-4 py-3 rounded-lg bg-[var(--clay-accent)]/10 text-[var(--clay-accent)] text-sm font-semibold">
                <AlertCircle className="w-4 h-4 flex-shrink-0" />
                {error}
              </div>
            )}

            <button
              id="admin-login-btn"
              type="submit"
              disabled={loading}
              className="w-full py-3 clay-btn font-bold text-sm disabled:opacity-60 disabled:cursor-not-allowed"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Signing In…
                </span>
              ) : 'Sign In'}
            </button>
          </form>
        </div>

        <p className="text-center text-xs text-[var(--clay-text-body)] mt-6 font-medium">
          This dashboard is for internal TGM-C staff only.
        </p>
      </div>
    </div>
  );
}
