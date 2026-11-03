export interface Config {
  /** URL do API Gateway (produção) ou do BFF (desenvolvimento). */
  apiUrl: string;
  /** API key do usage plan do API Gateway (header x-api-key). */
  apiKey?: string;
}

export const config: Config = {
  apiUrl: (import.meta.env.VITE_API_URL ?? 'http://localhost:3000').replace(/\/+$/, ''),
  apiKey: import.meta.env.VITE_API_KEY || undefined,
};
