import { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { createLigne, updateLigne } from '../api/ligneCommandeApi';
import { messageErreur } from '../api/axiosClient';

const VIDE = { idcommande: '', idproduit: '', quantite: '', remise: '' };
const SEUIL_SITE1 = 100;

/**
 * Formulaire de création / édition d'une ligne de commande.
 * En édition, l'idcommande est figé (l'API updateligne ne le modifie pas).
 *
 * @param {{editing: object|null, onDone: ()=>void}} props
 */
export default function LigneCommandeForm({ editing, onDone }) {
  const queryClient = useQueryClient();
  const [form, setForm] = useState(VIDE);

  const isEdit = Boolean(editing);

  useEffect(() => {
    if (editing) {
      setForm({
        idcommande: editing.idcommande ?? '',
        idproduit: editing.idproduit ?? '',
        quantite: editing.quantite ?? '',
        remise: editing.remise ?? '',
      });
    } else {
      setForm(VIDE);
    }
  }, [editing]);

  const mutation = useMutation({
    mutationFn: (payload) =>
      isEdit ? updateLigne(editing.idligneCommande, payload) : createLigne(payload),
    onSuccess: (_data, payload) => {
      if (isEdit) {
        // La procédure UPDATELIGNE migre la ligne entre fragments si la
        // quantité franchit le seuil 100. On le signale à l'utilisateur.
        const ancienne = Number(editing.quantite);
        const nouvelle = payload.quantite;
        if (ancienne >= SEUIL_SITE1 && nouvelle < SEUIL_SITE1) {
          toast('Ligne déplacée Site1→Site2', { icon: '↘️' });
        } else if (ancienne < SEUIL_SITE1 && nouvelle >= SEUIL_SITE1) {
          toast('Ligne déplacée Site2→Site1', { icon: '↗️' });
        } else {
          toast.success('Ligne mise à jour (procédure updateligne).');
        }
      } else {
        toast.success('Ligne créée et routée par Oracle (insertligne).');
      }
      queryClient.invalidateQueries({ queryKey: ['lignes'] });
      setForm(VIDE);
      onDone?.();
    },
    onError: (error) => toast.error(`Échec : ${messageErreur(error)}`),
  });

  const handleChange = (e) =>
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = (e) => {
    e.preventDefault();
    const remise = form.remise === '' ? 0 : Number(form.remise);
    const base = {
      idproduit: Number(form.idproduit),
      quantite: Number(form.quantite),
      remise,
    };
    // En création : { idligneCommande, idcommande, idproduit, quantite, remise }.
    // idligneCommande est laissé à null (généré par Oracle via la séquence ESHOP).
    const payload = isEdit
      ? base
      : {
          idligneCommande: null,
          idcommande: Number(form.idcommande),
          ...base,
        };
    mutation.mutate(payload);
  };

  return (
    <motion.form
      key={isEdit ? `edit-${editing.idligneCommande}` : 'create'}
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.2 }}
      onSubmit={handleSubmit}
      className="space-y-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800"
    >
      <h3 className="font-semibold text-slate-700 dark:text-slate-200">
        {isEdit ? `Éditer la ligne #${editing.idligneCommande}` : 'Nouvelle ligne'}
      </h3>

      <div>
        <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
          ID Ligne <span className="text-slate-400">(généré par Oracle)</span>
        </label>
        <input
          name="idligneCommande"
          type="number"
          disabled
          value={isEdit ? editing.idligneCommande : ''}
          placeholder="auto"
          className="mt-1 w-full rounded border border-slate-300 bg-slate-100 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-900 dark:text-slate-400"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">ID Commande</label>
        <input
          name="idcommande"
          type="number"
          required
          disabled={isEdit}
          value={form.idcommande}
          onChange={handleChange}
          className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm disabled:bg-slate-100 dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100 dark:disabled:bg-slate-900 dark:disabled:text-slate-400"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">ID Produit</label>
        <input
          name="idproduit"
          type="number"
          required
          value={form.idproduit}
          onChange={handleChange}
          className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
        />
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
          Quantité <span className="text-slate-400">(≥100 → Site1, &lt;100 → Site2)</span>
        </label>
        <input
          name="quantite"
          type="number"
          min="1"
          required
          value={form.quantite}
          onChange={handleChange}
          className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
        />
        {form.quantite !== '' && (
          Number(form.quantite) >= SEUIL_SITE1 ? (
            <p className="mt-1 text-xs font-medium text-green-600">→ sera routé vers Site1</p>
          ) : (
            <p className="mt-1 text-xs font-medium text-blue-600">→ sera routé vers Site2</p>
          )
        )}
      </div>

      <div>
        <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
          Remise (0 à 1)
        </label>
        <input
          name="remise"
          type="number"
          step="0.01"
          min="0"
          max="1"
          value={form.remise}
          onChange={handleChange}
          className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
        />
      </div>

      <div className="flex gap-2 pt-1">
        <button
          type="submit"
          disabled={mutation.isPending}
          className="rounded bg-slate-800 px-3 py-1.5 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-white"
        >
          {mutation.isPending ? 'Envoi…' : isEdit ? 'Mettre à jour' : 'Créer'}
        </button>
        {isEdit && (
          <button
            type="button"
            onClick={() => onDone?.()}
            className="rounded border border-slate-300 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-700"
          >
            Annuler
          </button>
        )}
      </div>
    </motion.form>
  );
}
