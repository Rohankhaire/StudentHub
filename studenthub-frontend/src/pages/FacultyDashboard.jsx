import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { BookOpen, CheckSquare, BarChart3, FileText, Upload, Trash2, Calendar, Award } from 'lucide-react';
import toast from 'react-hot-toast';

const FacultyDashboard = () => {
  const [courses, setCourses] = useState([]);
  const [activeTab, setActiveTab] = useState('courses');
  const [loading, setLoading] = useState(true);

  // States for Attendance Recording
  const [selectedCourse, setSelectedCourse] = useState('');
  const [attendanceDate, setAttendanceDate] = useState(new Date().toISOString().split('T')[0]);
  const [students, setStudents] = useState([]);
  const [attendanceRecords, setAttendanceRecords] = useState({}); // studentId -> status (PRESENT, ABSENT, LATE)
  const [attendanceRemarks, setAttendanceRemarks] = useState({});

  // States for Grading Marks
  const [selectedGradingCourse, setSelectedGradingCourse] = useState('');
  const [gradingExamType, setGradingExamType] = useState('MID_TERM');
  const [marksObtained, setMarksObtained] = useState({});
  const [maxMarks, setMaxMarks] = useState('100');
  const [gradingRemarks, setGradingRemarks] = useState({});

  // States for Notes uploads
  const [selectedNotesCourse, setSelectedNotesCourse] = useState('');
  const [noteTitle, setNoteTitle] = useState('');
  const [noteFile, setNoteFile] = useState(null);
  const [notesList, setNotesList] = useState([]);

  useEffect(() => {
    fetchAssignedCourses();
  }, []);

  const fetchAssignedCourses = async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/v1/faculty/courses');
      setCourses(res.data);
      if (res.data.length > 0) {
        setSelectedCourse(res.data[0].id);
        setSelectedGradingCourse(res.data[0].id);
        setSelectedNotesCourse(res.data[0].id);
      }
    } catch (err) {
      toast.error('Failed to load courses.');
    } finally {
      setLoading(false);
    }
  };

  // Fetch student roster for a course
  const fetchStudentsForCourse = async (courseId) => {
    if (!courseId) return;
    try {
      // In the admin service, we can fetch students. Since faculty wants to grade course students,
      // we can fetch the course details or the students enrolled in this course.
      // Let's implement an endpoint or fetch from the admin student endpoint filtered or mock students.
      // Wait, let's fetch students. Since we can fetch all students or page, we can fetch page of students.
      // In a real database, course has @ManyToMany students. Let's look up if we have a student list in course.
      // Since CourseResponseDto does not return full student entities, we can fetch students from /api/v1/admin/students
      // but let's query all students from backend /api/v1/admin/students, or we can fetch a specific list.
      // Since it's faculty context, let's fetch students via `/api/v1/admin/students?size=100`.
      // Let's do that!
      const res = await api.get('/api/v1/admin/students?size=50');
      const studentList = res.data.content || [];
      setStudents(studentList);
      
      // Initialize states
      const initialRecords = {};
      const initialRemarks = {};
      const initialMarks = {};
      const initialGradRemarks = {};
      
      studentList.forEach(s => {
        initialRecords[s.id] = 'PRESENT';
        initialRemarks[s.id] = '';
        initialMarks[s.id] = '';
        initialGradRemarks[s.id] = '';
      });
      setAttendanceRecords(initialRecords);
      setAttendanceRemarks(initialRemarks);
      setMarksObtained(initialMarks);
      setGradingRemarks(initialGradRemarks);
    } catch (err) {
      toast.error('Failed to load student list.');
    }
  };

  useEffect(() => {
    if (activeTab === 'attendance' && selectedCourse) {
      fetchStudentsForCourse(selectedCourse);
    }
    if (activeTab === 'marks' && selectedGradingCourse) {
      fetchStudentsForCourse(selectedGradingCourse);
    }
    if (activeTab === 'notes' && selectedNotesCourse) {
      fetchNotesForCourse(selectedNotesCourse);
    }
  }, [activeTab, selectedCourse, selectedGradingCourse, selectedNotesCourse]);

  // Attendance Submission
  const handleSaveAttendance = async () => {
    try {
      const promises = students.map(student => {
        return api.post('/api/v1/faculty/attendance', {
          studentId: student.id,
          courseId: selectedCourse,
          date: attendanceDate,
          status: attendanceRecords[student.id] || 'PRESENT',
          remarks: attendanceRemarks[student.id] || '',
        });
      });
      await Promise.all(promises);
      toast.success('Attendance recorded successfully!');
    } catch (err) {
      toast.error('Failed to save attendance. Verify role authorizations.');
    }
  };

  // Grading Submission
  const handleSaveMarks = async () => {
    try {
      const promises = students
        .filter(student => marksObtained[student.id] !== '')
        .map(student => {
          return api.post('/api/v1/faculty/marks', {
            studentId: student.id,
            courseId: selectedGradingCourse,
            examType: gradingExamType,
            marksObtained: parseFloat(marksObtained[student.id]),
            maxMarks: parseFloat(maxMarks),
            gradingDate: new Date().toISOString().split('T')[0],
            remarks: gradingRemarks[student.id] || '',
          });
        });

      if (promises.length === 0) {
        toast.error('Please enter marks for at least one student.');
        return;
      }

      await Promise.all(promises);
      toast.success('Grades published successfully!');
    } catch (err) {
      toast.error('Failed to publish grades.');
    }
  };

  // Notes fetch
  const fetchNotesForCourse = async (courseId) => {
    if (!courseId) return;
    try {
      const res = await api.get(`/api/v1/faculty/courses/${courseId}/notes`);
      setNotesList(res.data);
    } catch (err) {
      toast.error('Failed to load course materials.');
    }
  };

  // Upload notes
  const handleUploadNote = async (e) => {
    e.preventDefault();
    if (!selectedNotesCourse || !noteTitle || !noteFile) {
      toast.error('All fields and file selection are required.');
      return;
    }

    const formData = new FormData();
    formData.append('courseId', selectedNotesCourse);
    formData.append('title', noteTitle);
    formData.append('file', noteFile);

    try {
      await api.post('/api/v1/faculty/notes', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      toast.success('Study note uploaded successfully!');
      setNoteTitle('');
      setNoteFile(null);
      // reset file input
      document.getElementById('noteFileInput').value = '';
      fetchNotesForCourse(selectedNotesCourse);
    } catch (err) {
      toast.error('Failed to upload material.');
    }
  };

  // Delete note
  const handleDeleteNote = async (noteId) => {
    if (!window.confirm('Delete note permanently?')) return;
    try {
      await api.delete(`/api/v1/faculty/notes/${noteId}`);
      toast.success('Note deleted.');
      fetchNotesForCourse(selectedNotesCourse);
    } catch (err) {
      toast.error('Failed to delete note.');
    }
  };

  if (loading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand-600 border-t-transparent"></div>
      </div>
    );
  }

  const tabs = [
    { id: 'courses', name: 'Assigned Courses', icon: BookOpen },
    { id: 'attendance', name: 'Take Attendance', icon: CheckSquare },
    { id: 'marks', name: 'Gradebook', icon: Award },
    { id: 'notes', name: 'Course Notes', icon: FileText },
  ];

  return (
    <div className="space-y-8 animate-slide-in">
      {/* Title */}
      <div>
        <h2 className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white font-sans">Faculty Portal</h2>
        <p className="mt-1 text-slate-500 dark:text-slate-400">Record attendance, publish marks, and upload study files.</p>
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
        
        {/* COURSES TAB */}
        {activeTab === 'courses' && (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {courses.length === 0 ? (
              <p className="text-slate-400">No courses assigned to your profile.</p>
            ) : (
              courses.map(course => (
                <div key={course.id} className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
                  <div className="flex items-center gap-2 text-brand-600 dark:text-brand-400">
                    <BookOpen className="h-5 w-5" />
                    <span className="text-xs font-bold uppercase tracking-wider">{course.code}</span>
                  </div>
                  <h3 className="mt-3 text-lg font-bold text-slate-900 dark:text-white">{course.name}</h3>
                  <p className="mt-2 text-sm text-slate-500 dark:text-slate-400 line-clamp-3">{course.description || 'No description provided.'}</p>
                  <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800 text-xs text-slate-400 font-semibold">
                    <span>Credits: {course.credits}</span>
                    <span>Dept: {course.department?.code}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* ATTENDANCE TAB */}
        {activeTab === 'attendance' && (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-6">
            <div className="flex flex-wrap gap-4 items-center justify-between">
              <div className="flex gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Select Course</label>
                  <select
                    value={selectedCourse}
                    onChange={(e) => setSelectedCourse(e.target.value)}
                    className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                  >
                    {courses.map(c => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Select Date</label>
                  <div className="relative">
                    <input
                      type="date"
                      value={attendanceDate}
                      onChange={(e) => setAttendanceDate(e.target.value)}
                      className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2 text-sm dark:border-slate-800 dark:bg-slate-800"
                    />
                  </div>
                </div>
              </div>
              <button
                onClick={handleSaveAttendance}
                className="rounded-xl bg-brand-600 px-6 py-2.5 text-sm font-semibold text-white shadow-md hover:bg-brand-500"
              >
                Submit Attendance Sheet
              </button>
            </div>

            <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
              <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800 text-left">
                <thead className="bg-slate-50 dark:bg-slate-800/50">
                  <tr className="text-xs font-bold uppercase text-slate-400">
                    <th className="px-6 py-3">Roll Number</th>
                    <th className="px-6 py-3">Name</th>
                    <th className="px-6 py-3">Status</th>
                    <th className="px-6 py-3">Remarks</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800 text-sm">
                  {students.map((student) => (
                    <tr key={student.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/20">
                      <td className="px-6 py-4 font-semibold text-slate-700 dark:text-slate-300">{student.rollNumber}</td>
                      <td className="px-6 py-4 font-bold text-slate-900 dark:text-white">{student.user.fullName}</td>
                      <td className="px-6 py-4">
                        <select
                          value={attendanceRecords[student.id] || 'PRESENT'}
                          onChange={(e) => setAttendanceRecords({ ...attendanceRecords, [student.id]: e.target.value })}
                          className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                        >
                          <option value="PRESENT">Present</option>
                          <option value="ABSENT">Absent</option>
                          <option value="LATE">Late</option>
                        </select>
                      </td>
                      <td className="px-6 py-4">
                        <input
                          type="text"
                          placeholder="Add comment..."
                          value={attendanceRemarks[student.id] || ''}
                          onChange={(e) => setAttendanceRemarks({ ...attendanceRemarks, [student.id]: e.target.value })}
                          className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* GRADEBOOK TAB */}
        {activeTab === 'marks' && (
          <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 space-y-6">
            <div className="flex flex-wrap gap-4 items-center justify-between">
              <div className="flex flex-wrap gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Select Course</label>
                  <select
                    value={selectedGradingCourse}
                    onChange={(e) => setSelectedGradingCourse(e.target.value)}
                    className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                  >
                    {courses.map(c => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Exam Type</label>
                  <select
                    value={gradingExamType}
                    onChange={(e) => setGradingExamType(e.target.value)}
                    className="rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                  >
                    <option value="MID_TERM">Mid Term</option>
                    <option value="FINAL">Final</option>
                    <option value="QUIZ">Quiz</option>
                    <option value="ASSIGNMENT">Assignment</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Max Marks</label>
                  <input
                    type="number"
                    value={maxMarks}
                    onChange={(e) => setMaxMarks(e.target.value)}
                    className="w-20 rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm dark:border-slate-800 dark:bg-slate-800 text-center"
                  />
                </div>
              </div>
              <button
                onClick={handleSaveMarks}
                className="rounded-xl bg-brand-600 px-6 py-2.5 text-sm font-semibold text-white shadow-md hover:bg-brand-500"
              >
                Publish Grades
              </button>
            </div>

            <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
              <table className="min-w-full divide-y divide-slate-200 dark:divide-slate-800 text-left">
                <thead className="bg-slate-50 dark:bg-slate-800/50">
                  <tr className="text-xs font-bold uppercase text-slate-400">
                    <th className="px-6 py-3">Roll Number</th>
                    <th className="px-6 py-3">Name</th>
                    <th className="px-6 py-3 w-40">Marks Obtained</th>
                    <th className="px-6 py-3">Remarks</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800 text-sm">
                  {students.map((student) => (
                    <tr key={student.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/20">
                      <td className="px-6 py-4 font-semibold text-slate-700 dark:text-slate-300">{student.rollNumber}</td>
                      <td className="px-6 py-4 font-bold text-slate-900 dark:text-white">{student.user.fullName}</td>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-2">
                          <input
                            type="number"
                            min="0"
                            max={maxMarks}
                            step="0.5"
                            placeholder="Score"
                            value={marksObtained[student.id] || ''}
                            onChange={(e) => setMarksObtained({ ...marksObtained, [student.id]: e.target.value })}
                            className="w-24 rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm dark:border-slate-800 dark:bg-slate-800 text-center"
                          />
                          <span className="text-slate-400">/ {maxMarks}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <input
                          type="text"
                          placeholder="Grade feedback..."
                          value={gradingRemarks[student.id] || ''}
                          onChange={(e) => setGradingRemarks({ ...gradingRemarks, [student.id]: e.target.value })}
                          className="w-full rounded-lg border border-slate-200 bg-slate-50 px-3 py-1.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* NOTES TAB */}
        {activeTab === 'notes' && (
          <div className="grid gap-8 lg:grid-cols-3">
            {/* Upload form */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 h-fit">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
                <Upload className="h-5 w-5 text-brand-600" /> Share Note / Slides
              </h3>
              <form onSubmit={handleUploadNote} className="space-y-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Select Course</label>
                  <select
                    value={selectedNotesCourse}
                    onChange={(e) => setSelectedNotesCourse(e.target.value)}
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                  >
                    {courses.map(c => <option key={c.id} value={c.id}>{c.code} - {c.name}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Title / Description</label>
                  <input
                    type="text"
                    placeholder="e.g. Lecture 1 - Basics of MVC"
                    value={noteTitle}
                    onChange={(e) => setNoteTitle(e.target.value)}
                    className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-2.5 text-sm dark:border-slate-800 dark:bg-slate-800"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate-400 mb-1">Attachment File</label>
                  <input
                    type="file"
                    id="noteFileInput"
                    onChange={(e) => setNoteFile(e.target.files[0])}
                    className="w-full text-sm text-slate-500 file:mr-4 file:py-2 file:px-4 file:rounded-xl file:border-0 file:text-xs file:font-semibold file:bg-brand-50 file:text-brand-700 hover:file:bg-brand-100 dark:file:bg-brand-950/40 dark:file:text-brand-400"
                  />
                </div>
                <button
                  type="submit"
                  className="w-full rounded-xl bg-brand-600 py-3 text-sm font-semibold text-white hover:bg-brand-500 shadow-md transition-all"
                >
                  Upload Study Material
                </button>
              </form>
            </div>

            {/* List of uploaded notes */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 dark:border-slate-800 dark:bg-slate-900 lg:col-span-2">
              <h3 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
                <FileText className="h-5 w-5 text-brand-600" /> Active Course Files
              </h3>
              <div className="space-y-4">
                {notesList.length === 0 ? (
                  <p className="text-sm text-slate-400">No notes shared for this course yet.</p>
                ) : (
                  notesList.map((note) => (
                    <div key={note.id} className="flex items-center justify-between border-b border-slate-100 pb-4 last:border-0 last:pb-0 dark:border-slate-800">
                      <div>
                        <h4 className="font-bold text-slate-800 dark:text-slate-200">{note.title}</h4>
                        <p className="text-xs text-slate-400 mt-1">
                          File: {note.fileName} | Shared: {new Date(note.uploadedAt).toLocaleDateString()}
                        </p>
                      </div>
                      <button
                        onClick={() => handleDeleteNote(note.id)}
                        className="rounded-lg p-2 text-red-500 hover:bg-red-50 dark:hover:bg-red-950/20 transition-all"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}

      </div>
    </div>
  );
};

export default FacultyDashboard;
