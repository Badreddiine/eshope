import axiosClient from './axiosClient';

// =====================================================================
//  Santé des 3 instances Oracle (ping individuel des datasources).
// =====================================================================

/**
 * État de chaque site distribué.
 * @returns {Promise<{global:string, site1:string, site2:string, global_status:string}>}
 */
export async function getSitesHealth() {
  const { data } = await axiosClient.get('/api/health/sites', {
    // Le endpoint renvoie 503 si un site est DOWN : on traite quand même le corps.
    validateStatus: (status) => status === 200 || status === 503,
  });
  return data;
}
