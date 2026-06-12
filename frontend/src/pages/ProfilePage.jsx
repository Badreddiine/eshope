import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { getProfile, updateProfile } from '../api/profileApi';
import { TOKEN_KEY, messageErreur } from '../api/axiosClient';
import Spinner from '../components/Spinner';

const VIDE = { currentPassword: '', newUsername: '', newPassword: '' };

/**
 * Page de profil : affiche le compte connecté et permet de modifier le nom
 * d'utilisateur et/ou le mot de passe (le mot de passe actuel est requis).
 */
export default function ProfilePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form, setForm] = useState(VIDE);

  const { data: profil, isPending, isError, error } = useQuery({
    queryKey: ['profile'],
    queryFn: getProfile,
  });

  const mutation = useMutation({
    mutationFn: () => {
      // On n'envoie que les champs renseignés (le backend ignore les vides).
      const payload = { currentPassword: form.currentPassword };
      if (form.newUsername.trim()) payload.newUsername = form.newUsername.trim();
      if (form.newPassword) payload.newPassword = form.newPassword;
      return updateProfile(payload);
    },
    onSuccess: (data) => {
      toast.success(data.message ?? 'Profil mis à jour.');
      setForm(VIDE);
      if (data.usernameChanged) {
        // Le JWT porte l'ancien nom : on force une reconnexion.
        localStorage.removeItem(TOKEN_KEY);
        navigate('/login');
        return;
      }
      queryClient.invalidateQueries({ queryKey: ['profile'] });
    },
    onError: (err) => toast.error(`Échec : ${messageErreur(err)}`),
  });

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.currentPassword) {
      toast.error('Le mot de passe actuel est requis.');
      return;
    }
    if (!form.newUsername.trim() && !form.newPassword) {
      toast.error('Renseigne un nouveau nom d’utilisateur ou un nouveau mot de passe.');
      return;
    }
    mutation.mutate();
  };

  if (isPending) return <Spinner label="Chargement du profil…" />;
  if (isError) {
    return (
      <div className="rounded border border-red-300 bg-red-50 p-3 text-sm text-red-700">
        Erreur de chargement : {messageErreur(error)}
      </div>
    );
  }

  const inputClass =
    'mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100';

  return (
    <div className="mx-auto grid max-w-2xl grid-cols-1 gap-6 md:grid-cols-2">
      {/* Carte profil (lecture) */}
      <motion.div
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.25 }}
        className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-700 dark:bg-slate-800"
      >
        <div className="mb-4 flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-800 text-lg font-bold text-white dark:bg-slate-100 dark:text-slate-900">
            {profil.username?.[0]?.toUpperCase() ?? '?'}
          </div>
          <div>
            <p className="text-lg font-semibold text-slate-800 dark:text-slate-100">{profil.username}</p>
            <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-700 dark:text-slate-300">
              {profil.role}
            </span>
          </div>
        </div>
        <dl className="space-y-1 text-sm">
          <div className="flex justify-between border-t border-slate-100 py-1 dark:border-slate-700">
            <dt className="text-slate-500 dark:text-slate-400">ID</dt>
            <dd className="font-mono text-slate-700 dark:text-slate-200">#{profil.id}</dd>
          </div>
          <div className="flex justify-between border-t border-slate-100 py-1 dark:border-slate-700">
            <dt className="text-slate-500 dark:text-slate-400">Utilisateur</dt>
            <dd className="text-slate-700 dark:text-slate-200">{profil.username}</dd>
          </div>
          <div className="flex justify-between border-t border-slate-100 py-1 dark:border-slate-700">
            <dt className="text-slate-500 dark:text-slate-400">Rôle</dt>
            <dd className="text-slate-700 dark:text-slate-200">{profil.role}</dd>
          </div>
        </dl>
      </motion.div>

      {/* Carte édition */}
      <motion.form
        initial={{ opacity: 0, y: 12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.25, delay: 0.06 }}
        onSubmit={handleSubmit}
        className="space-y-3 rounded-lg border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-700 dark:bg-slate-800"
      >
        <h3 className="font-semibold text-slate-700 dark:text-slate-200">Modifier mon profil</h3>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
            Nouveau nom d’utilisateur <span className="text-slate-400">(optionnel)</span>
          </label>
          <input
            name="newUsername"
            value={form.newUsername}
            onChange={handleChange}
            placeholder={profil.username}
            className={inputClass}
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
            Nouveau mot de passe <span className="text-slate-400">(optionnel)</span>
          </label>
          <input
            name="newPassword"
            type="password"
            value={form.newPassword}
            onChange={handleChange}
            className={inputClass}
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">
            Mot de passe actuel <span className="text-red-500">*</span>
          </label>
          <input
            name="currentPassword"
            type="password"
            required
            value={form.currentPassword}
            onChange={handleChange}
            className={inputClass}
          />
        </div>

        <button
          type="submit"
          disabled={mutation.isPending}
          className="w-full rounded bg-slate-800 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-white"
        >
          {mutation.isPending ? 'Enregistrement…' : 'Mettre à jour'}
        </button>
      </motion.form>
    </div>
  );
}
