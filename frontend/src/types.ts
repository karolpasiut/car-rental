export type CarType = 'SUV' | 'VAN' | 'SEDAN'

export interface Car {
  id: string
  type: CarType
  brand: string
  model: string
  photoUrl: string
}

export interface CarsResponse {
  cars: Car[]
  availabilityByType: Partial<Record<CarType, number>>
}

export interface Reservation {
  id: string
  startDate: string
  endDate: string
  cancelled: boolean
  cars: Car[]
}

export interface AvailabilityResponse {
  available: boolean
  requested: Partial<Record<CarType, number>>
  availableByType: Partial<Record<CarType, number>>
}

export interface BookingPayload {
  startDate: string
  endDate: string
  cars: { type: CarType; quantity: number }[]
}
