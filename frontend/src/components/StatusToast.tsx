import type { Notice } from '../notice'

export default function StatusToast({ notice }: { notice: Notice | null }) {
  if (!notice) return null

  return (
    <div className={`toast ${notice.tone === 'error' ? 'error' : 'info'}`} role="status">
      {notice.text}
    </div>
  )
}
