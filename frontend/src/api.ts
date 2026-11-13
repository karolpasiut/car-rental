import type {
  AvailabilityResponse,
  BookingPayload,
  CarsResponse,
  Reservation,
} from './types'

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    ...init,
  })

  if (!response.ok) {
    let message = `HTTP error ${response.status}`
    try {
      const body = (await response.json()) as { error?: string }
      if (body.error) message = body.error
    } catch {
      // ignore parse errors
    }
    throw new Error(message)
  }

  return (await response.json()) as T
}

export function fetchCars(startDate?: string, endDate?: string) {
  const params = new URLSearchParams()
  if (startDate) params.set('startDate', startDate)
  if (endDate) params.set('endDate', endDate)
  const query = params.toString()
  return request<CarsResponse>(`/cars${query ? `?${query}` : ''}`)
}

export function checkAvailability(payload: BookingPayload) {
  return request<AvailabilityResponse>('/checkAvailability', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function bookReservation(payload: BookingPayload) {
  return request<Reservation>('/bookReservation', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function getReservation(id: string) {
  return request<Reservation>(`/reservation/${id}`)
}

export function cancelReservation(id: string) {
  return request<Reservation>(`/cancelReservation/${id}`, { method: 'POST' })
}

export function listReservations(startDate: string, endDate: string) {
  return request<Reservation[]>(`/reservations/${startDate}/${endDate}`)
}
