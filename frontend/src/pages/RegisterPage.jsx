import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { registerUser } from '../api/authApi';
import { messageErreur } from '../api/axiosClient';

/**
 * Page d'inscription : crée un compte via POST /auth/register puis renvoie
 * vers la page de connexion.
 */
export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '', role: 'USER' });
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => setForm((f) => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await registerUser(form);
      toast.success('Compte créé. Tu peux te connecter.');
      navigate('/login');
    } catch (error) {
      toast.error(`Inscription refusée : ${messageErreur(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const inputClass =
    'mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100';

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 dark:bg-slate-900">
      <motion.form
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, ease: 'easeOut' }}
        onSubmit={handleSubmit}
        className="w-80 space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow dark:border-slate-700 dark:bg-slate-800"
      >
        <h1 className="text-lg font-semibold text-slate-800 dark:text-slate-100">EShop — Créer un compte</h1>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Utilisateur</label>
          <input name="username" value={form.username} onChange={handleChange} className={inputClass} required />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Mot de passe</label>
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={handleChange}
            className={inputClass}
            required
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Rôle</label>
          <select name="role" value={form.role} onChange={handleChange} className={inputClass}>
            <option value="USER">USER</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded bg-slate-800 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-white"
        >
          {loading ? 'Création…' : 'Créer le compte'}
        </button>

        <p className="text-center text-xs text-slate-500 dark:text-slate-400">
          Déjà un compte ?{' '}
          <Link to="/login" className="font-medium text-blue-600 hover:underline dark:text-blue-400">
            Se connecter
          </Link>
        </p>
      </motion.form>
    </div>
  );
}
