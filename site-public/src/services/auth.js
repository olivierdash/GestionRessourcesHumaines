import { requete } from './api'

const BASE_URL = '/public'

export const authApi = {
  login: (credentials) =>
    requete(`${BASE_URL}/auth/login`, {
      method: 'POST',
      body: JSON.stringify(credentials),
    }),

  register: (payload) =>
    requete(`${BASE_URL}/auth/register`, {
      method: 'POST',
      body: JSON.stringify(payload),
    }),
}