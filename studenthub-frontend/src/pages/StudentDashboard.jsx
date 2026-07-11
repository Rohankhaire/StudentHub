import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Settings, CheckSquare, BarChart3, FileText, Download, User as UserIcon, Calendar, Award } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import toast from 'react-hot-toast';

const StudentDashboard = () => {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [stats, setStats] = useState({ attendanceRate: 0, averageMarks: 0, enrolledCoursesCount: 0 });
  const [attendance, setAttendance] = useState([]);
  const [marks, setMarks] = useState([]);
  const [notes, setNotes] = useState([]);
  const [activeTab, setActiveTab] = useState('overview');
  const [loading, setLoading] = useState(true);

  // Profile Edit fields
  const [phone, setPhone] = useState('');
  const [dob, setDob] = useState('');
  const [password, setPassword] = useState('');
  const [profilePic, setProfilePic] = useState(null);

  useEffect(() => {
    fetchStudentData();
  }, []);

  const fetchStudentData = async () => {
    setLoading(true);
    try {
      // 1. Fetch Profile
      const profRes = await api.get('/api/v1/student/profile');
      setProfile(profRes.data);
      setPhone(profRes.data.user.phone || '');
      setDob(profRes.data.dateOfBirth || '');

      // 2. Fetch Stats
      const statsRes = await api.get('/api/v1/student/stats');
      setStats(statsRes.data);

      // 3. Fetch Attendance
      const attRes = await api.get('/api/v1/student/attendance');
      setAttendance(attRes.data);

      // 4. Fetch Marks
      const marksRes = await api.get('/api/v1/student/marks');
      setMarks(marksRes.data);

      // 5. Fetch Notes
      const notesRes = await api.get('/api/v1/student/notes');
      setNotes(notesRes.data);
    } catch (err) {
      toast.error('Failed to load portal records.');
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    if (phone) formData.append('phone', phone);
    if (dob) formData.append('dob', dob);
    if (password) formData.append('password', password);
    if (profilePic) formData.append('profilePic', profilePic);

    try {
      const res = await api.put('/api/v1/student/profile', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      setProfile(res.data);
      setPassword('');
      setProfilePic(null);
      document.getElementById('profilePicInput').value = '';
      toast.success('Profile updated successfully!');
    } catch (err) {
      toast.error('Failed to update profile settings.');
    }
  };

  const handleDownloadNote = async (note) => {
    try {
      const response = await api({
        url: `/api/v1/files/download/${note.filePath}`,
        method: 'GET',
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', note.fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success(`Downloaded: ${note.fileName}`);
    } catch (err) {
      toast.error('Failed to download note attachment.');
    }
  };

  if (loading || !profile) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-600 border-t-transparent"></div>
      </div>
    );
  }

  // Formatting mark values for chart visualization
  const chartData = marks.map((m, idx) => ({
    name: m.courseCode + ' - ' + m.examType.replace('_', ' '),
    percentage: Math.round((m.marksObtained / m.maxMarks) * 100),
  }));

  const statCards = [
    { title: 'Enrolled Courses', value: stats.enrolledCoursesCount, icon: BookOpen, color: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400' },
    { title: 'Attendance Rate', value: `${stats.attendanceRate}%`, icon: CheckSquare, color: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400' },
    { title: 'Average Score', value: `${stats.averageMarks}%`, icon: Award, color: 'bg-amber-500/10 text-amber-600 dark:text-amber-400' },
  ];

  const tabs = [
    { id: 'overview', name: 'Overview', icon: UserIcon },
    { id: 'grades', name: 'My Grades', icon: BarChart3 },
    { id: 'attendance', name: 'Attendance', icon: CheckSquare },
    { id: 'notes', name: 'Study Library', icon: FileText },
  ];

  return (
    <div className="space-y-8 animate-slide-in">
      {/* Title */}
      <div className="flex flex-wrap gap-4 items-center justify-between">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white">Student Dashboard</h2>
          <p className="mt-1 text-slate-500 dark:text-slate-400">Welcome, {profile.user.fullName} (Roll: {profile.rollNumber})</p>
        </div>
        {profile.profilePicturePath && (
          <img
            src={`http://localhost:8080/uploads/${profile.profilePicturePath}`}
            alt="Profile Avatar"
            className="h-16 w-16 rounded-full object-cover border-2 border-brand-500 shadow-md"
          />
        )}
      </div>

      {/* Stats Cards */}
      <div className="grid gap-6 sm:grid-cols-3">
        {statCards.map((card, index) => {
          const Icon = card.icon;
          return (
            <div key={index} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-semibold text-slate-500 dark:text-slate-400">{card.title}</p>
                  <p className="mt-2 text-3xl font-bold tracking-tight text-slate-900 dark:text-white">{card.value}</p>
                </div>
                <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${card.color}`}>
                  <Icon className="h-6 w-6" />
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 dark:border-slate-800 space-x-4">
        {tabs.map(tab => {
          const Icon = tab.icon;
          const active = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 border-b-2 py-4 px-1 text-sm font-semibold transition-all ${
                active 
                  ? 'border-brand-600 text-brand-600 dark:border-brand-400 dark:text-brand-400' 
                  : 'border-transparent text-slate-500 hover:border-slate-300 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
              }`}
            >
              <Icon className="h-4 w-4" />
              {tab.name}
            </button>
          );
        })}
      </div>

      {/* Tab Contents */}
      <div className="mt-6">
        
        {/* OVERVIEW TAB */}
        {activeTab === 'overview' && (
          <div className="grid gap-8 lg:grid-cols-3">
            {/* Details Card */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-4 h-fit">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white border-b border-slate-100 pb-3 dark:border-slate-800">
                Academic Profile
              </h3>
              <div className="space-y-3 text-sm">
                <div>
                  <span className="text-slate-400 block font-semibold">Department</span>
                  <span className="text-slate-800 dark:text-slate-200 font-bold">{profile.department?.name || 'Unassigned'}</span>
                </div>
                <div>
                  <span className="text-slate-400 block font-semibold">Enrollment Date</span>
                  <span className="text-slate-800 dark:text-slate-200 font-bold">{new Date(profile.enrollmentDate).toLocaleDateString()}</span>
                </div>
                <div>
                  <span className="text-slate-400 block font-semibold">Birthdate</span>
                  <span className="text-slate-800 dark:text-slate-200 font-bold">{profile.dateOfBirth ? new Date(profile.dateOfBirth).toLocaleDateString() : 'N/A'}</span>
                </div>
                <div>
                  <span className="text-slate-400 block font-semibold">Phone Number</span>
                  <span className="text-slate-800 dark:text-slate-200 font-bold">{profile.user.phone || 'N/A'}</span>
                </div>
              </div>
            </div>

            {/* Performance Chart Card */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 lg:col-span-2 space-y-4">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white">Performance Progress</h3>
              <div className="h-64">
                {chartData.length === 0 ? (
                  <div className="flex h-full items-center justify-center text-slate-400">
                    No academic records found to graph performance logs.
                  </div>
                ) : (
                  <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={chartData}>
                      <defs>
                        <linearGradient id="colorPercentage" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.4}/>
                          <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0}/>
                        </linearGradient>
                      </defs>
                      <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                      <XAxis dataKey="name" stroke="#94A3B8" fontSize={11} />
                      <YAxis domain={[0, 100]} stroke="#94A3B8" fontSize={11} />
                      <Tooltip />
                      <Area type="monotone" dataKey="percentage" stroke="#8b5cf6" strokeWidth={2.5} fillOpacity={1} fill="url(#colorPercentage)" />
                    </AreaChart>
                  </ResponsiveContainer>
                )}
              </div>
              
              {/* Profile Editor Card */}
              <div className="border-t border-slate-100 pt-6 dark:border-slate-800">
                <h4 className="text-md font-bold text-slate-900 dark:text-white mb-4">Edit Profile Settings</h4>
                <form onSubmit={handleUpdateProfile} className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Phone Number</label>
                    <input
                      type="text"
                      value={phone}
                      onChange={(e) => setPhone(e.target.value)}
                      className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm dark:border-slate-800 dark:bg-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Date of Birth</label>
                    <input
                      type="date"
                      value={dob}
                      onChange={(e) => setDob(e.target.value)}
                      className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm dark:border-slate-800 dark:bg-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">New Password (optional)</label>
                    <input
                      type="password"
                      placeholder="Enter new password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm dark:border-slate-800 dark:bg-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1">Profile Photo</label>
                    <input
                      type="file"
                      id="profilePicInput"
                      onChange={(e) => setProfilePic(e.target.files[0])}
                      className="w-full text-sm text-slate-500 file:mr-4 file:py-1.5 file:px-3 file:rounded-lg file:border-0 file:text-xs file:font-semibold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 dark:file:bg-brand-950/40 dark:file:text-brand-400"
                    />
                  </div>
                  <button
                    type="submit"
                    className="sm:col-span-2 rounded-xl bg-brand-600 py-2.5 text-sm font-semibold text-white hover:bg-brand-500 shadow-md transition-all"
                  >
                    Save Details
                  </button>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* GRADES TAB */}
        {activeTab === 'grades' && (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">Academic Performance Card</h3>
            <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
              <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800 text-left">
                <thead className="bg-slate-50 dark:bg-slate-800/50">
                  <tr className="text-xs font-bold uppercase text-slate-400">
                    <th className="px-6 py-3">Course Code</th>
                    <th className="px-6 py-3">Course Name</th>
                    <th className="px-6 py-3">Exam Type</th>
                    <th className="px-6 py-3">Marks Obtained</th>
                    <th className="px-6 py-3">Grade Date</th>
                    <th className="px-6 py-3">Remarks / Grader</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800 text-sm">
                  {marks.length === 0 ? (
                    <tr>
                      <td colSpan="6" className="px-6 py-8 text-center text-slate-400">No grades posted yet.</td>
                    </tr>
                  ) : (
                    marks.map((mark) => (
                      <tr key={mark.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/20">
                        <td className="px-6 py-4 font-semibold text-slate-700 dark:text-slate-300">{mark.courseCode}</td>
                        <td className="px-6 py-4 font-bold text-slate-900 dark:text-white">{mark.courseName}</td>
                        <td className="px-6 py-4 uppercase">{mark.examType.replace('_', ' ')}</td>
                        <td className="px-6 py-4 font-bold text-brand-600 dark:text-brand-400">
                          {mark.marksObtained} / {mark.maxMarks} ({Math.round((mark.marksObtained / mark.maxMarks) * 100)}%)
                        </td>
                        <td className="px-6 py-4">{new Date(mark.gradingDate).toLocaleDateString()}</td>
                        <td className="px-6 py-4 text-slate-500 dark:text-slate-400">
                          {mark.remarks || 'None'} | By: {mark.gradedByFullName}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ATTENDANCE TAB */}
        {activeTab === 'attendance' && (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">Attendance Logs</h3>
            <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
              <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800 text-left">
                <thead className="bg-slate-50 dark:bg-slate-800/50">
                  <tr className="text-xs font-bold uppercase text-slate-400">
                    <th className="px-6 py-3">Date</th>
                    <th className="px-6 py-3">Course Code</th>
                    <th className="px-6 py-3">Course Name</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3">Remarks / Recorder</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800 text-sm">
                  {attendance.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="px-6 py-8 text-center text-slate-400">No attendance registered yet.</td>
                    </tr>
                  ) : (
                    attendance.map((att) => (
                      <tr key={att.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/20">
                        <td className="px-6 py-4 font-semibold text-slate-700 dark:text-slate-300">
                          {new Date(att.date).toLocaleDateString()}
                        </td>
                        <td className="px-6 py-4">{att.courseCode}</td>
                        <td className="px-6 py-4 font-bold text-slate-900 dark:text-white">{att.courseName}</td>
                        <td className="px-6 py-4">
                          <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            att.status === 'PRESENT' 
                              ? 'bg-green-50 text-green-700 dark:bg-green-950/40 dark:text-green-400' 
                              : att.status === 'LATE'
                              ? 'bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-400'
                              : 'bg-red-50 text-red-700 dark:bg-red-950/40 dark:text-red-400'
                          }`}>
                            {att.status}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-slate-500 dark:text-slate-400">
                          {att.remarks || 'None'} | By: {att.createdByFullName}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* NOTES TAB */}
        {activeTab === 'notes' && (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-6">
            <h3 className="text-lg font-bold text-slate-900 dark:text-white">Course Study Materials</h3>
            <div className="space-y-4">
              {notes.length === 0 ? (
                <p className="text-sm text-slate-400">No study notes shared for your courses yet.</p>
              ) : (
                notes.map((note) => (
                  <div key={note.id} className="flex items-center justify-between border-b border-slate-100 pb-4 last:border-0 last:pb-0 dark:border-slate-800">
                    <div>
                      <span className="inline-flex items-center rounded-full bg-indigo-50 px-2 py-0.5 text-[10px] font-bold text-indigo-700 dark:bg-indigo-950/40 dark:text-indigo-400 mb-1">
                        {note.courseCode}
                      </span>
                      <h4 className="font-bold text-slate-800 dark:text-slate-200">{note.title}</h4>
                      <p className="text-xs text-slate-400 mt-1">
                        File: {note.fileName} | Posted by: {note.uploadedByFullName} | {new Date(note.uploadedAt).toLocaleDateString()}
                      </p>
                    </div>
                    <button
                      onClick={() => handleDownloadNote(note)}
                      className="flex items-center gap-2 rounded-xl bg-slate-50 border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 hover:bg-slate-100 transition-all dark:border-slate-800 dark:bg-slate-800 dark:text-slate-300 dark:hover:bg-slate-700"
                    >
                      <Download className="h-4 w-4" /> Download
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default StudentDashboard;
