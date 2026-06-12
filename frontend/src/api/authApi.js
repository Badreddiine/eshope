import axiosClient from './axiosClient';

// =====================================================================
//  Authentification : login + création de compte.
// =====================================================================

/**
 * Authentifie un utilisateur.
 * @param {{username:string, password:string}} credentials
 * @returns {Promise<{token:string, expiresIn:number}>}
 */
export async function login(credentials) {
  const { data } = await axiosClient.post('/auth/login', credentials);
  return data;
}

/**
 * Crée un nouveau compte (POST /auth/register, public).
 * @param {{username:string, password:string, role:'USER'|'ADMIN'}} payload
 */
export async function registerUser(payload) {
  const { data } = await axiosClient.post('/auth/register', payload);
  return data;
}
