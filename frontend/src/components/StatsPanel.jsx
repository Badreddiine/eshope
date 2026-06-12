import { useQuery } from '@tanstack/react-query';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts';
import { getCommandesParClient, getCaParCategorie } from '../api/statsApi';
import { messageErreur } from '../api/axiosClient';
import Spinner from './Spinner';

const ANNEE = 2026;

/**
 * Deux cartes statistiques : commandes/client et CA/catégorie (BarChart).
 */
export default function StatsPanel() {
  const commandesQuery = useQuery({
    queryKey: ['stats', 'commandes-par-client', ANNEE],
    queryFn: () => getCommandesParClient(ANNEE),
  });

  const caQuery = useQuery({
    queryKey: ['stats', 'ca-par-categorie', ANNEE],
    queryFn: () => getCaParCategorie(ANNEE),
  });

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
      {/* Carte 1 — commandes par client */}
      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <h3 className="mb-2 font-semibold text-slate-700 dark:text-slate-200">
          Commandes par client ({ANNEE})
        </h3>
        {commandesQuery.isPending && <Spinner />}
        {commandesQuery.isError && (
          <p className="text-sm text-red-600">{messageErreur(commandesQuery.error)}</p>
        )}
        {commandesQuery.data && (
          <div className="max-h-56 overflow-auto">
            <table className="w-full text-left text-sm">
              <thead className="sticky top-0 bg-slate-50 text-xs uppercase text-slate-500 dark:bg-slate-700/50 dark:text-slate-400">
                <tr>
                  <th className="px-2 py-1">Client</th>
                  <th className="px-2 py-1 text-right">Nb commandes</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-700">
                {commandesQuery.data.map((row) => (
                  <tr key={row.idclient}>
                    <td className="px-2 py-1">#{row.idclient}</td>
                    <td className="px-2 py-1 text-right font-medium">{row.nbCommandes}</td>
                  </tr>
                ))}
                {commandesQuery.data.length === 0 && (
                  <tr>
                    <td colSpan={2} className="px-2 py-4 text-center text-slate-400">
                      Aucune donnée.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Carte 2 — CA par catégorie (BarChart) */}
      <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
        <h3 className="mb-2 font-semibold text-slate-700 dark:text-slate-200">
          Chiffre d'affaires par catégorie ({ANNEE})
        </h3>
        {caQuery.isPending && <Spinner />}
        {caQuery.isError && (
          <p className="text-sm text-red-600">{messageErreur(caQuery.error)}</p>
        )}
        {caQuery.data && (
          <ResponsiveContainer width="100%" height={220}>
            <BarChart
              // Une entrée par catégorie : libellé "Cat N" pour l'axe X et CA
              // numérique pour la barre. Number(...) || 0 évite une barre NaN
              // (invisible) si caTotal arrive en chaîne ou null.
              data={[...caQuery.data]
                .sort((a, b) => a.idcateg - b.idcateg)
                .map((d) => ({
                  categorie: `Cat ${d.idcateg}`,
                  ca: Number(d.caTotal) || 0,
                }))}
              margin={{ top: 5, right: 10, left: 0, bottom: 5 }}
            >
              <CartesianGrid strokeDasharray="3 3" stroke="#94a3b8" strokeOpacity={0.3} />
              <XAxis dataKey="categorie" tick={{ fontSize: 12, fill: '#94a3b8' }} stroke="#94a3b8" />
              <YAxis tick={{ fontSize: 12, fill: '#94a3b8' }} stroke="#94a3b8" />
              <Tooltip
                formatter={(value) =>
                  new Intl.NumberFormat('fr-FR', {
                    style: 'currency',
                    currency: 'MAD',
                    maximumFractionDigits: 0,
                  }).format(value)
                }
              />
              {/* minPointSize : une catégorie au CA faible reste visible à côté
                  d'une catégorie au CA très élevé (sinon barre quasi "vide"). */}
              <Bar
                dataKey="ca"
                fill="#2563eb"
                radius={[4, 4, 0, 0]}
                minPointSize={2}
                isAnimationActive={false}
              />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  );
}
