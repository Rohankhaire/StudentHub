import React, { useState } from 'react';
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { 
  LayoutDashboard, Users, BookOpen, GraduationCap, Building, 
  Megaphone, LogOut, Sun, Moon, Menu, X, FileText, CheckSquare, BarChart3, Settings
} from 'lucide-react';
import toast from 'react-hot-toast';

const DashboardLayout = () => {
  const { user, logout } = useAuth();
  const { darkMode, toggleTheme } = useTheme();
  const location = useLocation();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await logout();
      toast.success('Logged out successfully');
      navigate('/login');
    } catch (err) {
      toast.error('Logout error');
    }
  };

  const getLinks = () => {
    if (!user) return [];
    
    const adminLinks = [
      { path: '/admin', name: 'Dashboard', icon: LayoutDashboard },
      { path: '/admin/departments', name: 'Departments', icon: Building },
      { path: '/admin/courses', name: 'Courses', icon: BookOpen },
      { path: '/admin/faculty', name: 'Faculty Members', icon: Users },
      { path: '/admin/students', name: 'Students Registry', icon: GraduationCap },
      { path: '/admin/announcements', name: 'Announcements', icon: Megaphone },
    ];

    const facultyLinks = [
      { path: '/faculty', name: 'Overview', icon: LayoutDashboard },
      { path: '/faculty/attendance', name: 'Attendance Sheets', icon: CheckSquare },
      { path: '/faculty/marks', name: 'Gradebook', icon: BarChart3 },
      { path: '/faculty/notes', name: 'Upload notes', icon: FileText },
    ];

    const studentLinks = [
      { path: '/student', name: 'My Profile', icon: Settings },
      { path: '/student/courses', name: 'My Courses', icon: BookOpen },
      { path: '/student/attendance', name: 'Attendance Log', icon: CheckSquare },
      { path: '/student/marks', name: 'My Grades', icon: BarChart3 },
      { path: '/student/notes', name: 'Study materials', icon: FileText },
    ];

    if (user.roles.includes('ROLE_ADMIN')) return adminLinks;
    if (user.roles.includes('ROLE_FACULTY')) return facultyLinks;
    return studentLinks;
  };

  const links = getLinks();

  return (
    <div className="flex h-screen overflow-hidden bg-slate-50 dark:bg-slate-950 font-sans transition-colors duration-200">
      
      {/* Sidebar Overlay for Mobile */}
      {sidebarOpen && (
        <div 
          onClick={() => setSidebarOpen(false)}
          className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-sm lg:hidden transition-all duration-300"
        />
      )}

      {/* Sidebar Container */}
      <aside className={`fixed inset-y-0 left-0 z-50 flex w-72 flex-col border-r border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900 transition-all duration-300 lg:static lg:translate-x-0 ${
        sidebarOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        {/* Sidebar Header */}
        <div className="flex h-16 items-center justify-between border-b border-slate-200 px-6 dark:border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white font-bold shadow-md shadow-brand-500/20">
              SH
            </div>
            <span className="text-lg font-bold tracking-wide text-slate-800 dark:text-white">StudentHub</span>
          </div>
          <button onClick={() => setSidebarOpen(false)} className="lg:hidden text-slate-400 hover:text-slate-600 dark:hover:text-slate-200">
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1 space-y-1.5 overflow-y-auto px-4 py-6">
          {links.map((link) => {
            const Icon = link.icon;
            const active = location.pathname === link.path;
            return (
              <Link
                key={link.path}
                to={link.path}
                onClick={() => setSidebarOpen(false)}
                className={`flex items-center gap-3.5 rounded-xl px-4 py-3 text-sm font-medium transition-all duration-200 ${
                  active 
                    ? 'bg-brand-50 text-brand-700 dark:bg-brand-950/40 dark:text-brand-400' 
                    : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800/40 dark:hover:text-slate-200'
                }`}
              >
                <Icon className={`h-5 w-5 ${active ? 'text-brand-600 dark:text-brand-400' : 'text-slate-400'}`} />
                {link.name}
              </Link>
            );
          })}
        </nav>

        {/* Sidebar Footer */}
        <div className="border-t border-slate-200 p-4 dark:border-slate-800">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3.5 rounded-xl px-4 py-3 text-sm font-semibold text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/20 transition-all"
          >
            <LogOut className="h-5 w-5 text-red-500" />
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main App Workspace */}
      <div className="flex flex-1 flex-col overflow-hidden">
        
        {/* Navbar */}
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-6 dark:border-slate-800 dark:bg-slate-900 transition-colors duration-200">
          <div className="flex items-center gap-4">
            <button onClick={() => setSidebarOpen(true)} className="lg:hidden text-slate-500 hover:text-slate-700 dark:hover:text-slate-300">
              <Menu className="h-5 w-5" />
            </button>
            <h1 className="hidden text-sm font-semibold text-slate-500 dark:text-slate-400 sm:block">
              Academic Dashboard Portal
            </h1>
          </div>

          <div className="flex items-center gap-4">
            {/* Theme Toggle Button */}
            <button 
              onClick={toggleTheme} 
              className="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-50 text-slate-500 hover:bg-slate-100 hover:text-slate-700 dark:bg-slate-800 dark:text-slate-400 dark:hover:bg-slate-700 dark:hover:text-slate-200 transition-all"
            >
              {darkMode ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
            </button>

            {/* Profile Detail */}
            <div className="flex items-center gap-3.5 border-l border-slate-200 pl-4 dark:border-slate-800">
              <div className="text-right">
                <div className="text-sm font-bold text-slate-800 dark:text-white">{user?.fullName}</div>
                <div className="text-xs text-slate-400 font-semibold">{user?.roles[0].replace('ROLE_', '')}</div>
              </div>
              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-brand-100 text-brand-700 font-bold dark:bg-brand-950/40 dark:text-brand-400">
                {user?.fullName.charAt(0)}
              </div>
            </div>
          </div>
        </header>

        {/* Content Outlet container */}
        <main className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
