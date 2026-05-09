import client from './client';

export const login = (username, password) =>
  client.post('/auth/login', { username, password });

export const register = (username, password, fullName) =>
  client.post('/auth/register', { username, password, fullName });
