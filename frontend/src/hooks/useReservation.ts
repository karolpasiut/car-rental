import { useCallback, useState } from 'react'
import type { FormEvent } from 'react'
import { cancelReservation, getReservation } from '../api'
import { errorNotice } from '../notice'
import type { Notice } from '../notice'
import type { Reservation } from '../types'

type UseReservationArgs = {
  onNotice: (notice: Notice | null) => void
  onChanged: () => void
}

export function useReservation({ onNotice, onChanged }: UseReservationArgs) {
  const [lookupId, setLookupId] = useState('')
  const [reservation, setReservation] = useState<Reservation | null>(null)
  const [loading, setLoading] = useState(false)

  const select = useCallback((next: Reservation) => {
    setLookupId(next.id)
    setReservation(next)
  }, [])

  const lookup = useCallback(
    async (event: FormEvent) => {
      event.preventDefault()
      const id = lookupId.trim()
      if (!id) return
      setLoading(true)
      onNotice(null)
      try {
        setReservation(await getReservation(id))
      } catch {
        setReservation(null)
        onNotice({ tone: 'error', text: 'Reservation number not found' })
      } finally {
        setLoading(false)
      }
    },
    [lookupId, onNotice],
  )

  const cancel = useCallback(
    async (id: string) => {
      setLoading(true)
      onNotice(null)
      try {
        const cancelledReservation = await cancelReservation(id)
        setReservation(cancelledReservation)
        onNotice({ tone: 'info', text: `Cancelled reservation ${id}` })
        onChanged()
      } catch (err) {
        onNotice(errorNotice(err, 'Failed to cancel reservation'))
      } finally {
        setLoading(false)
      }
    },
    [onChanged, onNotice],
  )

  return {
    lookupId,
    setLookupId,
    reservation,
    loading,
    lookup,
    cancel,
    select,
  }
}
