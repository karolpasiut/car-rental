import { useEffect, useState } from 'react'
import { listReservations } from '../api'
import SiteNav from '../components/SiteNav'
import { TYPE_LABELS } from '../carTypes'
import { plusDaysIso, todayIso } from '../dates'
import type { Reservation } from '../types'
import '../App.css'

export default function AdminPage() {
  const [startDate, setStartDate] = useState(todayIso())
  const [endDate, setEndDate] = useState(plusDaysIso(3))
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const list = await listReservations(startDate, endDate)
        if (cancelled) return
        setReservations(list)
        setError(null)
      } catch (err) {
        if (cancelled) return
        setReservations([])
        setError(err instanceof Error ? err.message : 'Failed to load reservations')
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [startDate, endDate])

  return (
    <div className="page">
      <SiteNav />
      <header className="hero">
        <p className="brand">Admin</p>
        <h1>Active reservations</h1>
        <p className="lede">Reservations that overlap the selected dates.</p>
      </header>

      <section className="panel admin-panel" aria-labelledby="admin-range-title">
        <h2 id="admin-range-title">Date range</h2>
        <div className="row">
          <label>
            From
            <input
              type="date"
              value={startDate}
              onChange={(event) => {
                setStartDate(event.target.value)
                setLoading(true)
                setError(null)
              }}
              required
            />
          </label>
          <label>
            To
            <input
              type="date"
              value={endDate}
              onChange={(event) => {
                setEndDate(event.target.value)
                setLoading(true)
                setError(null)
              }}
              required
            />
          </label>
        </div>

        {error && <p className="warn">{error}</p>}
        {loading && reservations.length === 0 && !error && (
          <p className="muted">Loading reservations…</p>
        )}
        {!loading && !error && reservations.length === 0 && (
          <p className="muted">No active reservations in this date range.</p>
        )}

        {reservations.length > 0 && (
          <ul className="admin-list">
            {reservations.map((reservation) => (
              <li key={reservation.id}>
                <p className="admin-id">{reservation.id}</p>
                <p>
                  {reservation.startDate} → {reservation.endDate} · {reservation.cars.length}{' '}
                  {reservation.cars.length === 1 ? 'car' : 'cars'}
                </p>
                <ul>
                  {reservation.cars.map((car) => (
                    <li key={car.id}>
                      {TYPE_LABELS[car.type]} — {car.brand} {car.model}
                    </li>
                  ))}
                </ul>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
