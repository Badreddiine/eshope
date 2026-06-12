import axiosClient from './axiosClient';

// =====================================================================
//  Appels REST des lignes de commande (CRUD).
//  Côté backend, les écritures passent par des procédures stockées
//  Oracle ; le routage Site1/Site2 est décidé par les triggers.
// =====================================================================

const BASE = '/api/lignes';

/**
 * Liste paginée des lignes (table maître du site global).
 * @param {number} page index de page (0-based)
 * @param {number} size taille de page
 * @returns {Promise<object>} page Spring { content, totalElements, totalPages, number, size }
 */
export async function getLignes(page = 0, size = 10) {
  const { data } = await axiosClient.get(BASE, {
    params: { page, size, sort: 'idligneCommande' },
  });
  return data;
}

/**
 * Crée une ligne (POST -> procédure ESHOP.insertligne).
 * Corps envoyé tel quel, dans l'ordre attendu par le backend :
 * { idligneCommande, idcommande, idproduit, quantite, remise }.
 * idligneCommande vaut null en création (généré par la séquence Oracle).
 * @param {{idligneCommande:?number, idcommande:number, idproduit:number, quantite:number, remise:number}} ligne
 */
export async function createLigne(ligne) {
  const { data } = await axiosClient.post(BASE, ligne);
  return data;
}

/**
 * Met à jour une ligne (PUT -> procédure updateligne).
 * @param {number} id identifiant de la ligne
 * @param {{idproduit:number, quantite:number, remise:number}} ligne
 */
export async function updateLigne(id, ligne) {
  const { data } = await axiosClient.put(`${BASE}/${id}`, ligne);
  return data;
}

/**
 * Supprime une ligne (DELETE -> procédure deleteligne).
 * @param {number} id identifiant de la ligne
 */
export async function deleteLigne(id) {
  await axiosClient.delete(`${BASE}/${id}`);
}
