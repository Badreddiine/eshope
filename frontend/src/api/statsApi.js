import axiosClient from './axiosClient';

// =====================================================================
//  Statistiques distribuées (agrégations UNION ALL via database links).
// =====================================================================

/**
 * Nombre de commandes par client pour une année.
 * @param {number} annee année de filtrage
 * @returns {Promise<Array<{idclient:number, nbCommandes:number}>>}
 */
export async function getCommandesParClient(annee = 2026) {
  const { data } = await axiosClient.get('/api/stats/commandes-par-client', {
    params: { annee },
  });
  return data;
}

/**
 * Chiffre d'affaires par catégorie pour une année (Site1 + Site2).
 * @param {number} annee année de filtrage
 * @returns {Promise<Array<{idcateg:number, caTotal:number}>>}
 */
export async function getCaParCategorie(annee = 2026) {
  const { data } = await axiosClient.get('/api/stats/ca-par-categorie', {
    params: { annee },
  });
  return data;
}
