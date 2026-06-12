import { Component } from 'react';

/**
 * Error boundary React : capture les erreurs de rendu d'une sous-arborescence
 * et affiche un repli, plutôt que de faire planter toute l'application.
 */
export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { error: null };
  }

  static getDerivedStateFromError(error) {
    return { error };
  }

  componentDidCatch(error, info) {
    // eslint-disable-next-line no-console
    console.error('ErrorBoundary a capturé une erreur :', error, info);
  }

  handleReset = () => this.setState({ error: null });

  render() {
    if (this.state.error) {
      return (
        <div className="m-4 rounded-lg border border-red-300 bg-red-50 p-4 text-red-700">
          <h2 className="font-semibold">Une erreur est survenue dans l'interface.</h2>
          <p className="mt-1 text-sm">{this.state.error.message}</p>
          <button
            onClick={this.handleReset}
            className="mt-3 rounded bg-red-600 px-3 py-1 text-sm font-medium text-white hover:bg-red-700"
          >
            Réessayer
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
