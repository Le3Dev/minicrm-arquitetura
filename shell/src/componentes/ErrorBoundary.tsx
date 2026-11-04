import { Component, type ErrorInfo, type ReactNode } from 'react';

interface Props {
  /** Nome do microfrontend, para a mensagem. */
  nome: string;
  children: ReactNode;
}

interface Estado {
  erro: Error | null;
}

/** Se um MF não carregar (rede, deploy fora do ar) ou quebrar, o resto do Shell continua funcionando. */
export class ErrorBoundary extends Component<Props, Estado> {
  state: Estado = { erro: null };

  static getDerivedStateFromError(erro: Error): Estado {
    return { erro };
  }

  componentDidCatch(erro: Error, info: ErrorInfo) {
    console.error(`[shell] ${this.props.nome} falhou`, erro, info.componentStack);
  }

  render() {
    if (!this.state.erro) return this.props.children;
    return (
      <div className="sh-erro" role="alert">
        <strong>Não foi possível carregar {this.props.nome}.</strong>
        <p>O restante do sistema continua disponível. Confira se o microfrontend está publicado e tente de novo.</p>
        <button type="button" className="sh-btn" onClick={() => this.setState({ erro: null })}>
          Tentar de novo
        </button>
      </div>
    );
  }
}
