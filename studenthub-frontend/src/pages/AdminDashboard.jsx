import React, { useEffect, useState } from 'react';
import api, { API_BASE_URL } from '../services/api';
import { Building, Users, BookOpen, GraduationCap, FileDown, Megaphone, ArrowRight } from 'lucide-react';
import toast from 'react-hot-toast';

const AdminDashboard = () => {
  const [stats, setStats] = useState({
    totalStudents: 0,
    totalFaculty: 0,
    totalCourses: 0,
    totalDepartments: 0,
  });
  const [announcements, setAnnouncements] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const statsRes = await api.get('/api/v1/admin/stats');
        setStats(statsRes.data);

        // Fetch top recent announcements
        const annRes = await api.get('/api/v1/announcements?size=5');
        setAnnouncements(annRes.data.content || []);
      } catch (err) {
        toast.error('Failed to load dashboard metrics');
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, []);

  const handleExport = (reportType) => {
    // Directly fetch CSV as blob and download
    api({
      url: `/api/v1/reports/${reportType}/csv`,
      method: 'GET',
      responseType: 'blob', // Important for file download
    })
      .then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', `${reportType}_report.csv`);
        document.body.appendChild(link);
        link.click();
        link.remove();
        toast.success(`Exported ${reportType} CSV successfully!`);
      })
      .catch(() => {
        toast.error(`Failed to export ${reportType} report.`);
      });
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-600 border-t-transparent"></div>
      </div>
    );
  }

  const statCards = [
    { title: 'Total Departments', value: stats.totalDepartments, icon: Building, color: 'from-blue-500 to-cyan-500 bg-blue-500/10 text-blue-600 dark:text-blue-400' },
    { title: 'Enrolled Courses', value: stats.totalCourses, icon: BookOpen, color: 'from-purple-500 to-indigo-500 bg-purple-500/10 text-purple-600 dark:text-purple-400' },
    { title: 'Faculty Members', value: stats.totalFaculty, icon: Users, color: 'from-amber-500 to-orange-500 bg-amber-500/10 text-amber-600 dark:text-amber-400' },
    { title: 'Registered Students', value: stats.totalStudents, icon: GraduationCap, color: 'from-emerald-500 to-teal-500 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400' },
  ];

  return (
    <div className="space-y-8 animate-slide-in">
      {/* Welcome Header */}
      <div>
        <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">Admin Command Center</h2>
        <p className="mt-1 text-slate-500 dark:text-slate-400">View real-time campus statistics and export records.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {statCards.map((card, index) => {
          const Icon = card.icon;
          return (
            <div key={index} className="relative overflow-hidden rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-slate-500 dark:text-slate-400">{card.title}</p>
                  <p className="mt-2 text-3xl font-bold tracking-tight text-slate-900 dark:text-white">{card.value}</p>
                </div>
                <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${card.color.split(' ')[2]} ${card.color.split(' ')[3]}`}>
                  <Icon className="h-6 w-6" />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Reports and Announcements row */}
      <div className="grid gap-8 lg:grid-cols-3">
        {/* Export Reports Card */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 lg:col-span-1">
          <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
            <FileDown className="h-5 w-5 text-brand-600" /> System Exports
          </h3>
          <p className="text-sm text-slate-500 dark:text-slate-400 mb-6">
            Export complete databases tables as raw Excel/CSV formats for local archival or data synchronization.
          </p>
          <div className="space-y-3.5">
            <button
              onClick={() => handleExport('students')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-all dark:border-slate-800 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
            >
              <span>Students Registry CSV</span>
              <ArrowRight className="h-4 w-4" />
            </button>
            <button
              onClick={() => handleExport('faculty')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-all dark:border-slate-800 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
            >
              <span>Faculty Directory CSV</span>
              <ArrowRight className="h-4 w-4" />
            </button>
            <button
              onClick={() => handleExport('courses')}
              className="flex w-full items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-all dark:border-slate-800 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
            >
              <span>Courses Schedule CSV</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Announcements Stream Card */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 lg:col-span-2">
          <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
            <Megaphone className="h-5 w-5 text-brand-600" /> Recent Bulletins
          </h3>
          <div className="space-y-4">
            {announcements.length === 0 ? (
              <p className="text-sm text-slate-400">No active bulletins posted yet.</p>
            ) : (
              announcements.map((ann) => (
                <div key={ann.id} className="border-b border-slate-100 pb-4 last:border-0 last:pb-0 dark:border-slate-800">
                  <div className="flex items-center justify-between">
                    <span className="inline-flex items-center rounded-full bg-brand-50 px-2.5 py-0.5 text-xs font-semibold text-brand-700 dark:bg-brand-950/40 dark:text-brand-400">
                      {ann.targetAudience}
                    </span>
                    <span className="text-xs text-slate-400">
                      {new Date(ann.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                  <h4 className="mt-1 font-bold text-slate-800 dark:text-slate-200">{ann.title}</h4>
                  <p className="mt-1 text-sm text-slate-500 dark:text-slate-400 line-clamp-2">{ann.content}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
