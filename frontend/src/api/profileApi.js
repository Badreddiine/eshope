import axiosClient from './axiosClient';

// =====================================================================
//  Profil de l'utilisateur connecté (déduit du JWT côté backend).
// =====================================================================

const BASE = '/api/profile';

/**
 * Récupère le profil courant.
 * @returns {Promise<{id:number, username:string, role:string}>}
 */
export async function getProfile() {
  const { data } = await axiosClient.get(BASE);
  return data;
}

/**
 * Met à jour le profil (nom d'utilisateur et/ou mot de passe).
 * @param {{currentPassword:string, newUsername?:string, newPassword?:string}} payload
 */
export async function updateProfile(payload) {
  const { data } = await axiosClient.put(BASE, payload);
  return data;
}
