import {
  Routes,
  Route,
  NavLink,
  Navigate,
  useNavigate,
  useLocation,
} from 'react-router-dom';
import { motion } from 'framer-motion';
import Dashboard from './pages/Dashboard';
import GestionPage from './pages/GestionPage';
import ProfilePage from './pages/ProfilePage';
import Login from './pages/Login';
import RegisterPage from './pages/RegisterPage';
import ErrorBoundary from './components/ErrorBoundary';
import ThemeToggle from './components/ThemeToggle';
import { TOKEN_KEY } from './api/axiosClient';

/**
 * Garde de route : renvoie vers /login si aucun JWT n'est présent.
 */
function RequireAuth({ children }) {
  const token = localStorage.getItem(TOKEN_KEY);
  return token ? children : <Navigate to="/login" replace />;
}

/**
 * En-tête de navigation entre le tableau de bord et la gestion + déconnexion.
 */
function NavBar() {
  const navigate = useNavigate();
  const linkClass = ({ isActive }) =>
    `rounded px-3 py-1.5 text-sm font-medium ${
      isActive
        ? 'bg-slate-800 text-white dark:bg-slate-100 dark:text-slate-900'
        : 'text-slate-600 hover:bg-slate-200 dark:text-slate-300 dark:hover:bg-slate-700'
    }`;

  const logout = () => {
    localStorage.removeItem(TOKEN_KEY);
    navigate('/login');
  };

  return (
    <header className="border-b border-slate-200 bg-white dark:border-slate-700 dark:bg-slate-800">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        <span className="text-lg font-bold text-slate-800 dark:text-slate-100">
          EShop · Oracle 3 sites
        </span>
        <nav className="flex items-center gap-2">
          <NavLink to="/" end className={linkClass}>
            Tableau de bord
          </NavLink>
          <NavLink to="/gestion" className={linkClass}>
            Gestion
          </NavLink>
          <NavLink to="/profile" className={linkClass}>
            Profil
          </NavLink>
          <button
            onClick={logout}
            className="rounded px-3 py-1.5 text-sm font-medium text-slate-500 hover:bg-slate-200 dark:text-slate-400 dark:hover:bg-slate-700"
          >
            Déconnexion
          </button>
          <ThemeToggle />
        </nav>
      </div>
    </header>
  );
}

/**
 * Coquille applicative : layout authentifié avec NavBar + contenu routé.
 */
function AuthenticatedLayout({ children }) {
  const location = useLocation();
  return (
    <div className="min-h-screen">
      <NavBar />
      <main className="mx-auto max-w-6xl px-4 py-6">
        {/* Transition de page : fondu + léger glissement à chaque navigation. */}
        <motion.div
          key={location.pathname}
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.25, ease: 'easeOut' }}
        >
          <ErrorBoundary>{children}</ErrorBoundary>
        </motion.div>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/"
        element={
          <RequireAuth>
            <AuthenticatedLayout>
              <Dashboard />
            </AuthenticatedLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/gestion"
        element={
          <RequireAuth>
            <AuthenticatedLayout>
              <GestionPage />
            </AuthenticatedLayout>
          </RequireAuth>
        }
      />
      <Route
        path="/profile"
        element={
          <RequireAuth>
            <AuthenticatedLayout>
              <ProfilePage />
            </AuthenticatedLayout>
          </RequireAuth>
        }
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
