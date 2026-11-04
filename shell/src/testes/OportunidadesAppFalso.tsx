/** Substitui o remote mfOportunidades/OportunidadesApp nos testes. */
export default function OportunidadesAppFalso({ apiUrl, apiKey, usuarioId }: { apiUrl: string; apiKey?: string; usuarioId?: string }) {
  return <div data-testid="mf-oportunidades">{`oportunidades api=${apiUrl} key=${apiKey ?? ''} usuario=${usuarioId}`}</div>;
}
