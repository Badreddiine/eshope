import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import ErrorBoundary from '../components/ErrorBoundary';
import SiteHealth from '../components/SiteHealth';
import StatsPanel from '../components/StatsPanel';
import LigneCommandeTable from '../components/LigneCommandeTable';
import Spinner from '../components/Spinner';
import { getLignes } from '../api/ligneCommandeApi';
import { messageErreur } from '../api/axiosClient';

// On récupère une page large pour couvrir TOUTES les lignes : les compteurs
// par site sont alors le total réel (toutes pages confondues), pas une page.
const TAILLE_TOTALE = 1000;

/** Cartes de répartition Site1 / Site2 calculées depuis les quantités. */
function SiteCounters() {
  const { data: lignes, isPending, isError, error } = useQuery({
    queryKey: ['lignes', 'resume', TAILLE_TOTALE],
    queryFn: () => getLignes(0, TAILLE_TOTALE),
  });

  if (isPending) return <Spinner label="Calcul de la répartition…" />;
  if (isError) {
    return (
      <div className="rounded border border-red-300 bg-red-50 p-3 text-sm text-red-700">
        Erreur de chargement : {messageErreur(error)}
      </div>
    );
  }

  // Champ "quantite" renvoyé par GET /api/lignes (entité LigneCommande).
  const site1Count = lignes?.content?.filter((l) => l.quantite >= 100).length ?? 0;
  const site2Count = lignes?.content?.filter((l) => l.quantite < 100).length ?? 0;
  const total = lignes?.totalElements ?? site1Count + site2Count;

  return (
    <div className="grid grid-cols-2 gap-4">
      <motion.div
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.25 }}
        className="rounded-lg border border-green-200 bg-green-50 p-4 shadow-sm dark:border-green-900 dark:bg-green-950/40"
      >
        <p className="text-xs font-medium uppercase text-green-700 dark:text-green-400">Site1 🔵 (Quantité ≥ 100)</p>
        <p className="mt-1 text-2xl font-bold text-green-800 dark:text-green-300">{site1Count}</p>
        <p className="text-xs text-green-600 dark:text-green-500">lignes gros volumes · {total} au total</p>
      </motion.div>
      <motion.div
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.25, delay: 0.08 }}
        className="rounded-lg border border-blue-200 bg-blue-50 p-4 shadow-sm dark:border-blue-900 dark:bg-blue-950/40"
      >
        <p className="text-xs font-medium uppercase text-blue-700 dark:text-blue-400">Site2 🟢 (Quantité &lt; 100)</p>
        <p className="mt-1 text-2xl font-bold text-blue-800 dark:text-blue-300">{site2Count}</p>
        <p className="text-xs text-blue-600 dark:text-blue-500">lignes petits volumes · {total} au total</p>
      </motion.div>
    </div>
  );
}

/**
 * Tableau de bord : santé des sites + statistiques en haut, lignes en bas.
 */
export default function Dashboard() {
  return (
    <div className="space-y-6">
      <ErrorBoundary>
        <SiteHealth />
      </ErrorBoundary>

      <ErrorBoundary>
        <StatsPanel />
      </ErrorBoundary>

      <ErrorBoundary>
        <SiteCounters />
      </ErrorBoundary>

      <ErrorBoundary>
        <LigneCommandeTable />
      </ErrorBoundary>
    </div>
  );
}
