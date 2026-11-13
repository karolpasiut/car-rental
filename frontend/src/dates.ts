function formatLocalIso(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function todayIso() {
  return formatLocalIso(new Date())
}

export function plusDaysIso(days: number) {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return formatLocalIso(date)
}
