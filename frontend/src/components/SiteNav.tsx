import { NavLink } from 'react-router'

export default function SiteNav() {
  return (
    <nav className="site-nav" aria-label="Pages">
      <NavLink to="/" end>
        Booking
      </NavLink>
      <NavLink to="/admin">Admin</NavLink>
    </nav>
  )
}
