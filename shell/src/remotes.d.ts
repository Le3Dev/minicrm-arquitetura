interface PropsMf {
  apiUrl: string;
  apiKey?: string;
  usuarioId?: string;
  usuarios?: { id: string; nome: string }[];
}

declare module 'mfClientes/ClientesApp' {
  const C: React.ComponentType<PropsMf>;
  export default C;
}

declare module 'mfOportunidades/OportunidadesApp' {
  const C: React.ComponentType<PropsMf>;
  export default C;
}
