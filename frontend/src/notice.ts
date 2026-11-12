export type Notice = { tone: 'error' | 'info'; text: string }

export function errorNotice(err: unknown, fallback: string): Notice {
  return {
    tone: 'error',
    text: err instanceof Error ? err.message : fallback,
  }
}
