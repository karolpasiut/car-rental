import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { bookReservation, checkAvailability } from '../api'
import { CAR_TYPES, TYPE_LABELS } from '../carTypes'
import { errorNotice } from '../notice'
import type { Notice } from '../notice'
import type { AvailabilityResponse, CarType, Reservation } from '../types'

type BookingPanelProps = {
  startDate: string
  endDate: string
  availabilityByType: Partial<Record<CarType, number>>
  onStartDateChange: (value: string) => void
  onEndDateChange: (value: string) => void
  onBooked: (reservation: Reservation) => void
  onRefresh: () => void
  onNotice: (notice: Notice | null) => void
}

export default function BookingPanel({
  startDate,
  endDate,
  availabilityByType,
  onStartDateChange,
  onEndDateChange,
  onBooked,
  onRefresh,
  onNotice,
}: BookingPanelProps) {
  const [quantities, setQuantities] = useState<Record<CarType, number>>({
    SUV: 1,
    VAN: 0,
    SEDAN: 0,
  })
  const [availability, setAvailability] = useState<AvailabilityResponse | null>(null)
  const [loading, setLoading] = useState(false)

  const payload = useMemo(
    () => ({
      startDate,
      endDate,
      cars: CAR_TYPES.filter((type) => quantities[type] > 0).map((type) => ({
        type,
        quantity: quantities[type],
      })),
    }),
    [startDate, endDate, quantities],
  )

  async function onCheck() {
    if (payload.cars.length === 0) {
      onNotice({ tone: 'error', text: 'Select at least one car' })
      return
    }
    setLoading(true)
    onNotice(null)
    try {
      const result = await checkAvailability(payload)
      setAvailability(result)
      onNotice({
        tone: 'info',
        text: result.available
          ? 'The selected cars are available for the entire date range.'
          : 'Not fully available for the selected date range.',
      })
      onRefresh()
    } catch (err) {
      onNotice(errorNotice(err, 'Availability check failed'))
    } finally {
      setLoading(false)
    }
  }

  async function onBook(event: FormEvent) {
    event.preventDefault()
    if (payload.cars.length === 0) {
      onNotice({ tone: 'error', text: 'Select at least one car' })
      return
    }
    setLoading(true)
    onNotice(null)
    try {
      const reservation = await bookReservation(payload)
      setAvailability(null)
      onNotice({ tone: 'info', text: `Created reservation ${reservation.id}` })
      onBooked(reservation)
    } catch (err) {
      onNotice(errorNotice(err, 'Booking failed'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="panel" aria-labelledby="booking-title">
      <h2 id="booking-title">Booking</h2>
      <form className="stack" onSubmit={onBook}>
        <div className="row">
          <label>
            From
            <input
              type="date"
              value={startDate}
              onChange={(event) => onStartDateChange(event.target.value)}
              required
            />
          </label>
          <label>
            To
            <input
              type="date"
              value={endDate}
              onChange={(event) => onEndDateChange(event.target.value)}
              required
            />
          </label>
        </div>

        <div className="quantities">
          {CAR_TYPES.map((type) => (
            <label key={type}>
              {TYPE_LABELS[type]}
              <span className="hint">available: {availabilityByType[type] ?? 0}</span>
              <input
                type="number"
                min={0}
                max={availabilityByType[type] ?? 99}
                value={quantities[type]}
                onChange={(event) =>
                  setQuantities((current) => ({
                    ...current,
                    [type]: Number(event.target.value),
                  }))
                }
              />
            </label>
          ))}
        </div>

        <div className="actions">
          <button type="button" className="secondary" disabled={loading} onClick={onCheck}>
            Check availability
          </button>
          <button type="submit" disabled={loading}>
            Book
          </button>
        </div>
      </form>

      {availability && (
        <p className={availability.available ? 'ok' : 'warn'}>
          {availability.available ? 'OK' : 'Unavailable'} — requested:{' '}
          {CAR_TYPES.filter((type) => (availability.requested[type] ?? 0) > 0)
            .map((type) => `${TYPE_LABELS[type]}×${availability.requested[type]}`)
            .join(', ')}
        </p>
      )}
    </section>
  )
}
