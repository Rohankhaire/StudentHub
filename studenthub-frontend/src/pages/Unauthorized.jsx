import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const Unauthorized = () => {
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleBack = () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (user.roles.includes('ROLE_ADMIN')) {
      navigate('/admin');
    } else if (user.roles.includes('ROLE_FACULTY')) {
      navigate('/faculty');
    } else {
      navigate('/student');
    }
  };

  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-slate-50 p-4 text-center dark:bg-slate-950">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-red-100 text-red-600 dark:bg-red-950/30 dark:text-red-400">
        <ShieldAlert className="h-10 w-10" />
      </div>
      <h1 className="mt-6 text-3xl font-extrabold tracking-tight text-slate-900 dark:text-white font-sans">403 - Forbidden</h1>
      <p className="mt-2 max-w-sm text-slate-500 dark:text-slate-400">
        You do not have the credentials required to view this page. If you believe this is an error, please contact administration.
      </p>
      <button
        onClick={handleBack}
        className="mt-8 rounded-xl bg-brand-600 px-6 py-2.5 text-sm font-semibold text-white shadow-lg shadow-brand-500/20 hover:bg-brand-500 transition-all focus:outline-none"
      >
        Go to Dashboard
      </button>
    </div>
  );
};

export default Unauthorized;
