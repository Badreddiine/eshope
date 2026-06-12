/**
 * Indicateur de chargement minimaliste.
 * @param {{label?: string}} props
 */
export default function Spinner({ label = 'Chargement…' }) {
  return (
    <div className="flex items-center gap-2 p-4 text-slate-500 dark:text-slate-400">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-slate-300 border-t-slate-600 dark:border-slate-600 dark:border-t-slate-300" />
      <span className="text-sm">{label}</span>
    </div>
  );
}
