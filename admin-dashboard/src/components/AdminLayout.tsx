import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import {
  LayoutDashboard, Users, HardDrive, Activity,
  FileText, Settings, LogOut, Shield, ShoppingBag
} from 'lucide-react';
import clsx from 'clsx';

const navItems = [
  { to: '/',        icon: LayoutDashboard, label: 'Dashboard',  end: true },
  { to: '/users',   icon: Users,           label: 'Users'               },
  { to: '/storage', icon: HardDrive,       label: 'Storage'             },
  { to: '/health',  icon: Activity,        label: 'Health'              },
  { to: '/content', icon: FileText,        label: 'Content'             },
  { to: '/store',   icon: ShoppingBag,     label: 'Store'               },
  { to: '/settings',icon: Settings,        label: 'Settings'            },
];

export function AdminLayout() {
  const { email, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => { logout(); navigate('/login'); };

  return (
    <div className="flex h-screen overflow-hidden bg-[#0A0E1A]">
      {/* Sidebar */}
      <aside className="w-64 flex-shrink-0 flex flex-col border-r border-white/5 bg-[#0F1628]">
        {/* Logo */}
        <div className="flex items-center gap-3 px-6 py-5 border-b border-white/5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#00E5FF] to-[#6C63FF] flex items-center justify-center">
            <Shield className="w-4 h-4 text-[#0A0E1A]" />
          </div>
          <div>
            <p className="text-sm font-bold text-white">TGM-C</p>
            <p className="text-[10px] text-[#00E5FF]">Admin Console</p>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          {navItems.map(({ to, icon: Icon, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150',
                  isActive
                    ? 'bg-[#00E5FF]/10 text-[#00E5FF] border border-[#00E5FF]/20'
                    : 'text-[#CBD5E0] hover:bg-white/5 hover:text-white'
                )
              }
            >
              <Icon className="w-4 h-4 flex-shrink-0" />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Footer */}
        <div className="px-4 py-4 border-t border-white/5">
          <div className="flex items-center gap-2 mb-3">
            <div className="w-7 h-7 rounded-full bg-[#1C2536] flex items-center justify-center text-xs font-bold text-[#00E5FF]">
              {email?.[0]?.toUpperCase() || 'A'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-medium text-white truncate">{email}</p>
              <p className="text-[10px] text-[#718096]">Administrator</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-xs text-[#FF4D6D] hover:bg-[#FF4D6D]/10 transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto">
        <div className="p-6 animate-fade-in">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
