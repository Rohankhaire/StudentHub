import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Lock, AlertCircle, CheckCircle } from 'lucide-react';
import api from '../services/api';
import toast from 'react-hot-toast';

const ResetPassword = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [apiError, setApiError] = useState('');

  const { register, handleSubmit, watch, formState: { errors } } = useForm();
  const passwordVal = watch('newPassword', '');

  const onSubmit = async (data) => {
    if (!token) {
      setApiError('Invalid reset session: token is missing.');
      return;
    }
    setLoading(true);
    setApiError('');
    try {
      await api.post('/api/v1/auth/reset-password', {
        token: token,
        newPassword: data.newPassword,
      });
      setSuccess(true);
      toast.success('Password updated successfully!');
    } catch (err) {
      setApiError(err.response?.data?.message || 'Token is invalid or expired. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 p-4 dark:from-slate-900 dark:to-slate-950">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl border border-slate-100 dark:bg-slate-900 dark:border-slate-800 transition-all duration-300">
        <div className="mb-8 text-center">
          <h2 className="text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">Reset Password</h2>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Set your new credentials.
          </p>
        </div>

        {apiError && (
          <div className="mb-6 flex items-center gap-2 rounded-lg bg-red-50 p-4 text-sm text-red-800 dark:bg-red-950/30 dark:text-red-400">
            <AlertCircle className="h-5 w-5 flex-shrink-0" />
            <span>{apiError}</span>
          </div>
        )}

        {success ? (
          <div className="text-center">
            <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-full bg-green-100 text-green-600 dark:bg-green-950/30 dark:text-green-400">
              <CheckCircle className="h-6 w-6" />
            </div>
            <h3 className="mt-4 text-lg font-bold text-slate-900 dark:text-white font-sans">Success</h3>
            <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
              Your password has been changed. You can now log in with your new password.
            </p>
            <button
              onClick={() => navigate('/login')}
              className="mt-6 w-full rounded-xl bg-brand-600 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-500/20 hover:bg-brand-500 transition-all"
            >
              Sign In
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300">New Password</label>
              <div className="relative mt-1">
                <Lock className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                <input
                  type="password"
                  placeholder="••••••••"
                  className={`w-full rounded-xl border bg-slate-50 py-2.5 pl-10 pr-4 text-sm outline-none transition-all dark:bg-slate-800/50 ${
                    errors.newPassword ? 'border-red-500 focus:ring-red-500' : 'border-slate-200 focus:border-brand-500 focus:ring-brand-500 dark:border-slate-700'
                  }`}
                  {...register('newPassword', { 
                    required: 'New password is required',
                    minLength: { value: 6, message: 'Password must be at least 6 characters' }
                  })}
                />
              </div>
              {errors.newPassword && <span className="mt-1 text-xs text-red-500">{errors.newPassword.message}</span>}
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300">Confirm Password</label>
              <div className="relative mt-1">
                <Lock className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                <input
                  type="password"
                  placeholder="••••••••"
                  className={`w-full rounded-xl border bg-slate-50 py-2.5 pl-10 pr-4 text-sm outline-none transition-all dark:bg-slate-800/50 ${
                    errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : 'border-slate-200 focus:border-brand-500 focus:ring-brand-500 dark:border-slate-700'
                  }`}
                  {...register('confirmPassword', { 
                    required: 'Please confirm your password',
                    validate: val => val === passwordVal || 'Passwords do not match'
                  })}
                />
              </div>
              {errors.confirmPassword && <span className="mt-1 text-xs text-red-500">{errors.confirmPassword.message}</span>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center rounded-xl bg-brand-600 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-500/20 hover:bg-brand-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2"
            >
              {loading ? (
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
              ) : (
                'Reset Password'
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ResetPassword;
