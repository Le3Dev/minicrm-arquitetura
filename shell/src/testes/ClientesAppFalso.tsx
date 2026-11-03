/** Substitui o remote mfClientes/ClientesApp nos testes. */
export default function ClientesAppFalso({ apiUrl, apiKey, usuarioId }: { apiUrl: string; apiKey?: string; usuarioId?: string }) {
  return <div data-testid="mf-clientes">{`clientes api=${apiUrl} key=${apiKey ?? ''} usuario=${usuarioId}`}</div>;
}
