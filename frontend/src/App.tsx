import { useCallback, useState } from 'react'
import BookingPanel from './components/BookingPanel'
import FleetPanel from './components/FleetPanel'
import Hero from './components/Hero'
import ReservationLookup from './components/ReservationLookup'
import SiteNav from './components/SiteNav'
import StatusToast from './components/StatusToast'
import { plusDaysIso, todayIso } from './dates'
import { useReservation } from './hooks/useReservation'
import type { Notice } from './notice'
import type { CarType, Reservation } from './types'
import './App.css'

export default function App() {
  const [startDate, setStartDate] = useState(todayIso())
  const [endDate, setEndDate] = useState(plusDaysIso(3))
  const [availabilityByType, setAvailabilityByType] = useState<
    Partial<Record<CarType, number>>
  >({})
  const [refreshKey, setRefreshKey] = useState(0)
  const [notice, setNotice] = useState<Notice | null>(null)

  const refresh = useCallback(() => {
    setRefreshKey((key) => key + 1)
  }, [])

  const handleNotice = useCallback((next: Notice | null) => {
    setNotice(next)
  }, [])

  const handleFleetLoaded = useCallback((availability: Partial<Record<CarType, number>>) => {
    setAvailabilityByType(availability)
    setNotice((current) => (current?.tone === 'error' ? null : current))
  }, [])

  const {
    lookupId,
    setLookupId,
    reservation,
    loading,
    lookup,
    cancel,
    select,
  } = useReservation({
    onNotice: handleNotice,
    onChanged: refresh,
  })

  const handleBooked = useCallback(
    (created: Reservation) => {
      select(created)
      refresh()
    },
    [refresh, select],
  )

  return (
    <div className="page">
      <SiteNav />
      <Hero />
      <main className="layout">
        <BookingPanel
          startDate={startDate}
          endDate={endDate}
          availabilityByType={availabilityByType}
          onStartDateChange={setStartDate}
          onEndDateChange={setEndDate}
          onBooked={handleBooked}
          onRefresh={refresh}
          onNotice={handleNotice}
        />
        <FleetPanel
          startDate={startDate}
          endDate={endDate}
          refreshKey={refreshKey}
          onLoaded={handleFleetLoaded}
          onNotice={handleNotice}
        />
        <ReservationLookup
          lookupId={lookupId}
          reservation={reservation}
          loading={loading}
          onLookupIdChange={setLookupId}
          onLookup={lookup}
          onCancel={cancel}
        />
      </main>
      <StatusToast notice={notice} />
    </div>
  )
}
