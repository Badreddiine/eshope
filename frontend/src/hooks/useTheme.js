import { useEffect, useState } from 'react';

const STORAGE_KEY = 'eshop-theme';

/** Lit le thème initial : préférence stockée, sinon réglage système. */
function themeInitial() {
  const stored = localStorage.getItem(STORAGE_KEY);
  if (stored === 'light' || stored === 'dark') return stored;
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

/**
 * Gère le thème clair/sombre : applique la classe `dark` sur <html>,
 * persiste le choix dans localStorage et expose une bascule.
 *
 * @returns {{theme: 'light'|'dark', isDark: boolean, toggle: ()=>void}}
 */
export default function useTheme() {
  const [theme, setTheme] = useState(themeInitial);

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle('dark', theme === 'dark');
    localStorage.setItem(STORAGE_KEY, theme);
  }, [theme]);

  const toggle = () => setTheme((t) => (t === 'dark' ? 'light' : 'dark'));

  return { theme, isDark: theme === 'dark', toggle };
}
