import type { FormEvent } from 'react'
import { TYPE_LABELS } from '../carTypes'
import type { Reservation } from '../types'

type ReservationLookupProps = {
  lookupId: string
  reservation: Reservation | null
  loading: boolean
  onLookupIdChange: (value: string) => void
  onLookup: (event: FormEvent) => void
  onCancel: (id: string) => void
}

export default function ReservationLookup({
  lookupId,
  reservation,
  loading,
  onLookupIdChange,
  onLookup,
  onCancel,
}: ReservationLookupProps) {
  return (
    <section className="panel" aria-labelledby="lookup-title">
      <h2 id="lookup-title">Details / cancellation</h2>
      <form className="row" onSubmit={onLookup}>
        <label className="grow">
          Reservation ID
          <input
            value={lookupId}
            onChange={(event) => onLookupIdChange(event.target.value)}
            placeholder="Reservation UUID"
          />
        </label>
        <button type="submit" disabled={loading}>
          Show
        </button>
      </form>
      {reservation && (
        <div className="reservation">
          <p>
            <strong>{reservation.id}</strong>
          </p>
          <p>
            {reservation.startDate} → {reservation.endDate}{' '}
            {reservation.cancelled ? '(cancelled)' : ''}
          </p>
          <ul>
            {reservation.cars.map((car) => (
              <li key={car.id}>
                {TYPE_LABELS[car.type]} — {car.brand} {car.model}
              </li>
            ))}
          </ul>
          {!reservation.cancelled && (
            <button
              type="button"
              className="danger"
              disabled={loading}
              onClick={() => onCancel(reservation.id)}
            >
              Cancel reservation
            </button>
          )}
        </div>
      )}
    </section>
  )
}
