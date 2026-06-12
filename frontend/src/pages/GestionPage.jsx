import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import ErrorBoundary from '../components/ErrorBoundary';
import LigneCommandeTable from '../components/LigneCommandeTable';
import LigneCommandeForm from '../components/LigneCommandeForm';

/**
 * Page de gestion : tableau des lignes (gauche) + formulaire create/edit
 * (droite) dans un split layout. Cliquer "Modifier" charge la ligne dans le
 * formulaire — y compris quand on arrive depuis le Dashboard (router state).
 */
export default function GestionPage() {
  const location = useLocation();
  const [editing, setEditing] = useState(location.state?.editLigne ?? null);

  // Si on navigue ici depuis le bouton "Modifier" du Dashboard, la ligne à
  // éditer est transmise via l'état de navigation : on pré-remplit le formulaire.
  useEffect(() => {
    if (location.state?.editLigne) {
      setEditing(location.state.editLigne);
    }
  }, [location.state]);

  return (
    <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <div className="lg:col-span-2">
        <ErrorBoundary>
          <LigneCommandeTable onEdit={setEditing} />
        </ErrorBoundary>
      </div>
      <div className="lg:col-span-1">
        <ErrorBoundary>
          <LigneCommandeForm editing={editing} onDone={() => setEditing(null)} />
        </ErrorBoundary>
      </div>
    </div>
  );
}
