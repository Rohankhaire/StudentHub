import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import { Toaster } from 'react-hot-toast';
import ProtectedRoute from './routes/ProtectedRoute';

// Pages
import Login from './pages/Login';
import ForgotPassword from './pages/ForgotPassword';
import ResetPassword from './pages/ResetPassword';
import Unauthorized from './pages/Unauthorized';
import AdminDashboard from './pages/AdminDashboard';
import FacultyDashboard from './pages/FacultyDashboard';
import StudentDashboard from './pages/StudentDashboard';

// Layout
import DashboardLayout from './layouts/DashboardLayout';

const AppRoutes = () => {
  const { user } = useAuth();

  return (
    <Routes>
      {/* Public Guest Routes */}
      <Route path="/login" element={<Login />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* Role-Protected Admin, Faculty, and Student Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<DashboardLayout />}>
          
          {/* Admin Command Center */}
          <Route element={<ProtectedRoute allowedRoles={['ROLE_ADMIN']} />}>
            <Route path="/admin" element={<AdminDashboard />} />
            {/* Standard admin routing placeholders */}
            <Route path="/admin/departments" element={<AdminDashboard />} />
            <Route path="/admin/courses" element={<AdminDashboard />} />
            <Route path="/admin/faculty" element={<AdminDashboard />} />
            <Route path="/admin/students" element={<AdminDashboard />} />
            <Route path="/admin/announcements" element={<AdminDashboard />} />
          </Route>

          {/* Faculty Portal */}
          <Route element={<ProtectedRoute allowedRoles={['ROLE_FACULTY']} />}>
            <Route path="/faculty" element={<FacultyDashboard />} />
            <Route path="/faculty/attendance" element={<FacultyDashboard />} />
            <Route path="/faculty/marks" element={<FacultyDashboard />} />
            <Route path="/faculty/notes" element={<FacultyDashboard />} />
          </Route>

          {/* Student Hub Portal */}
          <Route element={<ProtectedRoute allowedRoles={['ROLE_STUDENT']} />}>
            <Route path="/student" element={<StudentDashboard />} />
            <Route path="/student/courses" element={<StudentDashboard />} />
            <Route path="/student/attendance" element={<StudentDashboard />} />
            <Route path="/student/marks" element={<StudentDashboard />} />
            <Route path="/student/notes" element={<StudentDashboard />} />
          </Route>

        </Route>
      </Route>

      {/* Default Redirect Fallback */}
      <Route 
        path="*" 
        element={
          user ? (
            user.roles.includes('ROLE_ADMIN') ? (
              <Navigate to="/admin" replace />
            ) : user.roles.includes('ROLE_FACULTY') ? (
              <Navigate to="/faculty" replace />
            ) : (
              <Navigate to="/student" replace />
            )
          ) : (
            <Navigate to="/login" replace />
          )
        } 
      />
    </Routes>
  );
};

function App() {
  return (
    <Router>
      <ThemeProvider>
        <AuthProvider>
          <AppRoutes />
          <Toaster 
            position="top-right" 
            toastOptions={{
              className: 'dark:bg-slate-800 dark:text-white',
              duration: 4000,
            }} 
          />
        </AuthProvider>
      </ThemeProvider>
    </Router>
  );
}

export default App;
