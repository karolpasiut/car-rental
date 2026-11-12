import { useEffect, useState } from 'react'
import { fetchCars } from '../api'
import { CAR_TYPES, TYPE_LABELS } from '../carTypes'
import { errorNotice } from '../notice'
import type { Notice } from '../notice'
import type { Car, CarType } from '../types'

type FleetPanelProps = {
  startDate: string
  endDate: string
  refreshKey: number
  onLoaded: (availabilityByType: Partial<Record<CarType, number>>) => void
  onNotice: (notice: Notice) => void
}

export default function FleetPanel({
  startDate,
  endDate,
  refreshKey,
  onLoaded,
  onNotice,
}: FleetPanelProps) {
  const [cars, setCars] = useState<Car[]>([])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const response = await fetchCars(startDate, endDate)
        if (cancelled) return
        setCars(response.cars)
        onLoaded(response.availabilityByType)
      } catch (err) {
        if (!cancelled) onNotice(errorNotice(err, 'Failed to load data'))
      }
    })()
    return () => {
      cancelled = true
    }
  }, [startDate, endDate, refreshKey, onLoaded, onNotice])

  return (
    <section className="panel" aria-labelledby="fleet-title">
      <h2 id="fleet-title">Available fleet</h2>
      <div className="fleet">
        {CAR_TYPES.map((type) => {
          const group = cars.filter((car) => car.type === type)
          return (
            <section key={type} className="fleet-category" aria-labelledby={`fleet-${type}`}>
              <h3 id={`fleet-${type}`}>{TYPE_LABELS[type]}</h3>
              {group.length === 0 ? (
                <p className="muted">None available in this date range.</p>
              ) : (
                <div className="fleet-cars">
                  {group.map((car) => (
                    <article key={car.id} className="car">
                      <img src={car.photoUrl} alt={`${car.brand} ${car.model}`} />
                      <h4>
                        {car.brand} {car.model}
                      </h4>
                    </article>
                  ))}
                </div>
              )}
            </section>
          )
        })}
      </div>
    </section>
  )
}
