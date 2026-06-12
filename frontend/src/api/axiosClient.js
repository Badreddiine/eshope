import axios from 'axios';

// =====================================================================
//  Instance Axios partagée.
//  - baseURL vide : on s'appuie sur le proxy Vite (dev) ou Nginx (prod)
//    qui relaie /api et /auth vers le backend Spring Boot.
//  - Le backend étant sécurisé par JWT, on injecte automatiquement le
//    token (stocké dans localStorage après login) dans l'en-tête
//    Authorization. Sur 401, on purge le token et on renvoie vers /login.
// =====================================================================

export const TOKEN_KEY = 'eshop_token';

const axiosClient = axios.create({
  baseURL: '',
  headers: { 'Content-Type': 'application/json' },
});

// --- Requête : ajoute le Bearer token s'il existe ---
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// --- Réponse : gestion centralisée du 401 (session expirée) ---
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem(TOKEN_KEY);
      if (window.location.pathname !== '/login') {
        window.location.assign('/login');
      }
    }
    return Promise.reject(error);
  }
);

/**
 * Extrait un message d'erreur lisible (message backend ou statut HTTP).
 * @param {unknown} error erreur Axios
 * @returns {string}
 */
export function messageErreur(error) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Erreur inconnue'
  );
}

export default axiosClient;
