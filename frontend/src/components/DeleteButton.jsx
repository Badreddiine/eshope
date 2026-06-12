import { useMutation, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { deleteLigne } from '../api/ligneCommandeApi';
import { messageErreur } from '../api/axiosClient';

/**
 * Bouton de suppression d'une ligne, avec confirmation et invalidation
 * automatique du cache React Query après succès.
 *
 * @param {{id:number}} props
 */
export default function DeleteButton({ id }) {
  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: () => deleteLigne(id),
    onSuccess: () => {
      toast.success(`Ligne ${id} supprimée (procédure deleteligne).`);
      queryClient.invalidateQueries({ queryKey: ['lignes'] });
    },
    onError: (error) => toast.error(`Suppression impossible : ${messageErreur(error)}`),
  });

  const handleClick = () => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette ligne ?')) {
      mutation.mutate();
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={mutation.isPending}
      className="rounded bg-red-100 px-2 py-1 text-xs font-medium text-red-700 hover:bg-red-200 disabled:opacity-50"
    >
      {mutation.isPending ? '…' : 'Supprimer'}
    </button>
  );
}
