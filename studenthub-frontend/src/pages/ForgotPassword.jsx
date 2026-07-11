import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Mail, AlertCircle, ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import toast from 'react-hot-toast';

const ForgotPassword = () => {
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [apiError, setApiError] = useState('');

  const { register, handleSubmit, formState: { errors } } = useForm();

  const onSubmit = async (data) => {
    setLoading(true);
    setApiError('');
    try {
      await api.post('/api/v1/auth/forgot-password', { email: data.email });
      setSuccess(true);
      toast.success('Reset email simulated. Check backend logs!');
    } catch (err) {
      setApiError(err.response?.data?.message || 'Something went wrong. Try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-slate-100 to-slate-200 p-4 dark:from-slate-900 dark:to-slate-950">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-xl border border-slate-100 dark:bg-slate-900 dark:border-slate-800 transition-all duration-300">
        <div className="mb-6">
          <Link to="/login" className="inline-flex items-center gap-2 text-sm text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200">
            <ArrowLeft className="h-4 w-4" /> Back to Sign In
          </Link>
        </div>

        <div className="mb-8">
          <h2 className="text-2xl font-extrabold tracking-tight text-slate-900 dark:text-white">Forgot Password?</h2>
          <p className="mt-2 text-sm text-slate-500 dark:text-slate-400">
            Enter your email address and we'll log a simulated link to change your password.
          </p>
        </div>

        {apiError && (
          <div className="mb-6 flex items-center gap-2 rounded-lg bg-red-50 p-4 text-sm text-red-800 dark:bg-red-950/30 dark:text-red-400">
            <AlertCircle className="h-5 w-5 flex-shrink-0" />
            <span>{apiError}</span>
          </div>
        )}

        {success ? (
          <div className="rounded-xl bg-green-50 p-6 text-green-800 dark:bg-green-950/30 dark:text-green-300">
            <h3 className="font-bold">Check backend console logs</h3>
            <p className="mt-2 text-sm">
              We have outputted the password reset link inside the backend Spring Boot log console. Copy that URL to reset your credentials.
            </p>
          </div>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-slate-700 dark:text-slate-300">Email Address</label>
              <div className="relative mt-1">
                <Mail className="absolute left-3 top-3 h-5 w-5 text-slate-400" />
                <input
                  type="email"
                  placeholder="you@university.edu"
                  className={`w-full rounded-xl border bg-slate-50 py-2.5 pl-10 pr-4 text-sm outline-none transition-all dark:bg-slate-800/50 ${
                    errors.email ? 'border-red-500 focus:ring-red-500' : 'border-slate-200 focus:border-brand-500 focus:ring-brand-500 dark:border-slate-700'
                  }`}
                  {...register('email', { 
                    required: 'Email is required',
                    pattern: { value: /^\S+@\S+$/i, message: 'Invalid email address' }
                  })}
                />
              </div>
              {errors.email && <span className="mt-1 text-xs text-red-500">{errors.email.message}</span>}
            </div>

            <button
              type="submit"
              disabled={loading}
              className="flex w-full items-center justify-center rounded-xl bg-brand-600 py-3 text-sm font-semibold text-white shadow-lg shadow-brand-500/20 hover:bg-brand-500 transition-all focus:outline-none focus:ring-2 focus:ring-brand-500 focus:ring-offset-2"
            >
              {loading ? (
                <div className="h-5 w-5 animate-spin rounded-full border-2 border-white border-t-transparent"></div>
              ) : (
                'Request Reset Link'
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
