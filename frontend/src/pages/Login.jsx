import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import axiosClient, { TOKEN_KEY, messageErreur } from '../api/axiosClient';

/**
 * Page de connexion : authentifie via POST /auth/login et stocke le JWT dans
 * localStorage, puis redirige vers le tableau de bord. Comptes de démo :
 * admin/admin123 (ADMIN) ou user1/user123 (USER).
 */
export default function Login() {
  const navigate = useNavigate();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { data } = await axiosClient.post('/auth/login', { username, password });
      localStorage.setItem(TOKEN_KEY, data.token);
      toast.success('Connecté.');
      navigate('/');
    } catch (error) {
      toast.error(`Connexion refusée : ${messageErreur(error)}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 dark:bg-slate-900">
      <motion.form
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, ease: 'easeOut' }}
        onSubmit={handleSubmit}
        className="w-80 space-y-4 rounded-lg border border-slate-200 bg-white p-6 shadow dark:border-slate-700 dark:bg-slate-800"
      >
        <h1 className="text-lg font-semibold text-slate-800 dark:text-slate-100">EShop — Connexion</h1>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Utilisateur</label>
          <input
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
            required
          />
        </div>

        <div>
          <label className="block text-xs font-medium text-slate-500 dark:text-slate-400">Mot de passe</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="mt-1 w-full rounded border border-slate-300 px-2 py-1 text-sm dark:border-slate-600 dark:bg-slate-700 dark:text-slate-100"
            required
          />
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded bg-slate-800 py-2 text-sm font-medium text-white hover:bg-slate-900 disabled:opacity-50 dark:bg-slate-100 dark:text-slate-900 dark:hover:bg-white"
        >
          {loading ? 'Connexion…' : 'Se connecter'}
        </button>

        <p className="text-center text-xs text-slate-400">
          démo : admin / admin123
        </p>
        <p className="text-center text-xs text-slate-500 dark:text-slate-400">
          Pas de compte ?{' '}
          <Link to="/register" className="font-medium text-blue-600 hover:underline dark:text-blue-400">
            Créer un compte
          </Link>
        </p>
      </motion.form>
    </div>
  );
}
