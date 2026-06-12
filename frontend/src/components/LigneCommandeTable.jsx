import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, keepPreviousData } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { getLignes } from '../api/ligneCommandeApi';
import { messageErreur } from '../api/axiosClient';
import DeleteButton from './DeleteButton';
import Spinner from './Spinner';

const SEUIL_SITE1 = 100;

/**
 * Badge de site déduit de la quantité : Site1 (vert) si >= 100, sinon Site2 (bleu).
 * @param {{quantite:number}} props
 */
function SiteBadge({ quantite }) {
  const site1 = quantite >= SEUIL_SITE1;
  const classes = site1
    ? 'bg-green-100 text-green-700'
    : 'bg-blue-100 text-blue-700';
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${classes}`}>
      {site1 ? 'Site1 🔵' : 'Site2 🟢'}
    </span>
  );
}

/**
 * Tableau paginé des lignes de commande.
 * @param {{onEdit?: (ligne:object)=>void}} props
 */
export default function LigneCommandeTable({ onEdit }) {
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const size = 10;

  // "Modifier" : si un handler d'édition est fourni (page Gestion, formulaire
  // à côté), on l'appelle ; sinon (Dashboard) on navigue vers /gestion en
  // transmettant la ligne pour pré-remplir le formulaire.
  const handleEdit = (ligne) => {
    if (onEdit) {
      onEdit(ligne);
    } else {
      navigate('/gestion', { state: { editLigne: ligne } });
    }
  };

  const { data, isPending, isError, error, isFetching } = useQuery({
    queryKey: ['lignes', page, size],
    queryFn: () => getLignes(page, size),
    placeholderData: keepPreviousData,
  });

  if (isPending) return <Spinner label="Chargement des lignes…" />;
  if (isError) {
    return (
      <div className="rounded border border-red-300 bg-red-50 p-3 text-sm text-red-700">
        Erreur de chargement : {messageErreur(error)}
      </div>
    );
  }

  const lignes = data.content ?? [];
  const totalPages = data.totalPages ?? 1;

  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.25 }}
      className="overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-800"
    >
      <div className="flex items-center justify-between border-b border-slate-200 px-4 py-2 dark:border-slate-700">
        <h3 className="font-semibold text-slate-700 dark:text-slate-200">
          Lignes de commande
          {isFetching && <span className="ml-2 text-xs text-slate-400">(maj…)</span>}
        </h3>
        <span className="text-xs text-slate-400">{data.totalElements} au total</span>
      </div>

      <table className="w-full text-left text-sm">
        <thead className="bg-slate-50 text-xs uppercase text-slate-500 dark:bg-slate-700/50 dark:text-slate-400">
          <tr>
            <th className="px-4 py-2">ID</th>
            <th className="px-4 py-2">Produit</th>
            <th className="px-4 py-2">Quantité</th>
            <th className="px-4 py-2">Remise</th>
            <th className="px-4 py-2">Site</th>
            <th className="px-4 py-2 text-right">Actions</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
          {lignes.length === 0 && (
            <tr>
              <td colSpan={6} className="px-4 py-6 text-center text-slate-400">
                Aucune ligne.
              </td>
            </tr>
          )}
          {lignes.map((l) => (
            <tr key={l.idligneCommande} className="hover:bg-slate-50 dark:hover:bg-slate-700/40">
              <td className="px-4 py-2 font-mono text-slate-500 dark:text-slate-400">{l.idligneCommande}</td>
              <td className="px-4 py-2">#{l.idproduit}</td>
              <td className="px-4 py-2">{l.quantite}</td>
              <td className="px-4 py-2">
                {l.remise != null ? `${(l.remise * 100).toFixed(0)}%` : '—'}
              </td>
              <td className="px-4 py-2">
                <SiteBadge quantite={l.quantite} />
              </td>
              <td className="px-4 py-2">
                <div className="flex justify-end gap-2">
                  <button
                    onClick={() => handleEdit(l)}
                    className="rounded bg-blue-100 px-2 py-1 text-xs font-medium text-blue-700 hover:bg-blue-200 dark:bg-blue-950/50 dark:text-blue-300 dark:hover:bg-blue-900/60"
                  >
                    Modifier
                  </button>
                  <DeleteButton id={l.idligneCommande} />
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div className="flex items-center justify-between border-t border-slate-200 px-4 py-2 text-sm dark:border-slate-700">
        <button
          onClick={() => setPage((p) => Math.max(0, p - 1))}
          disabled={page === 0}
          className="rounded px-3 py-1 text-slate-600 hover:bg-slate-100 disabled:opacity-40 dark:text-slate-300 dark:hover:bg-slate-700"
        >
          ← Précédent
        </button>
        <span className="text-slate-500 dark:text-slate-400">
          Page {page + 1} / {Math.max(1, totalPages)}
        </span>
        <button
          onClick={() => setPage((p) => (p + 1 < totalPages ? p + 1 : p))}
          disabled={page + 1 >= totalPages}
          className="rounded px-3 py-1 text-slate-600 hover:bg-slate-100 disabled:opacity-40 dark:text-slate-300 dark:hover:bg-slate-700"
        >
          Suivant →
        </button>
      </div>
    </motion.div>
  );
}
