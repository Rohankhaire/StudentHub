import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user session exists in storage on app start
    const email = localStorage.getItem('email');
    const roles = localStorage.getItem('roles');
    const userId = localStorage.getItem('userId');
    const fullName = localStorage.getItem('fullName');
    const token = localStorage.getItem('accessToken');

    if (token && email && roles) {
      setUser({
        email,
        fullName,
        userId: parseInt(userId),
        roles: JSON.parse(roles),
      });
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await api.post('/api/v1/auth/login', { email, password });
      const { accessToken, refreshToken, email: resEmail, roles, userId, fullName } = response.data;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('email', resEmail);
      localStorage.setItem('roles', JSON.stringify(roles));
      localStorage.setItem('userId', userId.toString());
      localStorage.setItem('fullName', fullName);

      const loggedUser = { email: resEmail, roles, userId, fullName };
      setUser(loggedUser);
      setLoading(false);
      return loggedUser;
    } catch (error) {
      setLoading(false);
      throw error;
    }
  };

  const register = async (email, password, fullName, phone) => {
    setLoading(true);
    try {
      const response = await api.post('/api/v1/auth/register', { email, password, fullName, phone });
      const { accessToken, refreshToken, email: resEmail, roles, userId, fullName: resName } = response.data;

      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('email', resEmail);
      localStorage.setItem('roles', JSON.stringify(roles));
      localStorage.setItem('userId', userId.toString());
      localStorage.setItem('fullName', resName);

      const loggedUser = { email: resEmail, roles, userId, fullName: resName };
      setUser(loggedUser);
      setLoading(false);
      return loggedUser;
    } catch (error) {
      setLoading(false);
      throw error;
    }
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      try {
        await api.post('/api/v1/auth/logout', { refreshToken });
      } catch (err) {
        console.error('Logout error on backend:', err);
      }
    }
    localStorage.clear();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, register, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
