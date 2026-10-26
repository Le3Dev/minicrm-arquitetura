import type { StatusCliente } from '../tipos';

export function BadgeStatus({ status }: { status: StatusCliente }) {
  return <span className={`mfc-badge mfc-badge--${status.toLowerCase()}`}>{status}</span>;
}
