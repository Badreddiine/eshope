import { useQuery } from '@tanstack/react-query';
import { getSitesHealth } from '../api/healthApi';
import Spinner from './Spinner';

/**
 * Badge coloré d'un site : vert si UP, rouge sinon.
 * @param {{label:string, status:string}} props
 */
function HealthBadge({ label, status }) {
  const up = status === 'UP';
  return (
    <div
      className={`flex items-center gap-2 rounded-lg border px-3 py-2 ${
        up
          ? 'border-green-200 bg-green-50 dark:border-green-900 dark:bg-green-950/40'
          : 'border-red-200 bg-red-50 dark:border-red-900 dark:bg-red-950/40'
      }`}
    >
      <span className={`h-2.5 w-2.5 rounded-full ${up ? 'bg-green-500' : 'bg-red-500'}`} />
      <span className="text-sm font-medium text-slate-700 dark:text-slate-200">{label}</span>
      <span className={`ml-auto text-xs font-semibold ${up ? 'text-green-700' : 'text-red-700'}`}>
        {status ?? '—'}
      </span>
    </div>
  );
}

/**
 * Santé des 3 instances Oracle, rafraîchie toutes les 30 secondes.
 */
export default function SiteHealth() {
  const { data, isPending, isError } = useQuery({
    queryKey: ['health'],
    queryFn: getSitesHealth,
    refetchInterval: 30000,
    refetchOnWindowFocus: true,
  });

  if (isPending) return <Spinner label="Vérification des sites…" />;

  if (isError) {
    return (
      <div className="rounded border border-red-300 bg-red-50 p-3 text-sm text-red-700">
        Impossible de joindre le health-check des sites.
      </div>
    );
  }

  const degrade = data.global_status !== 'UP';

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="font-semibold text-slate-700 dark:text-slate-200">Santé des sites Oracle</h3>
        <span
          className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
            degrade
              ? 'bg-amber-100 text-amber-700 dark:bg-amber-950/50 dark:text-amber-300'
              : 'bg-green-100 text-green-700 dark:bg-green-950/50 dark:text-green-300'
          }`}
        >
          {degrade ? 'DÉGRADÉ' : 'NOMINAL'}
        </span>
      </div>
      <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
        <HealthBadge label="Site1 (gros volumes)" status={data.site1} />
        <HealthBadge label="Site2 (petits volumes)" status={data.site2} />
        <HealthBadge label="Global (maître)" status={data.global} />
      </div>
    </div>
  );
}
