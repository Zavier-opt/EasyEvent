import { NavLink } from "react-router-dom"
import AuthContext from "../../context/auth-context"
import "./MainNavigation.css"

const MainNavigation = (props) => {
  <AuthContext.Consumer>
    {(context) => {
      return (
        <header className="main-navigation">
          <div>
            <h1 className="main-navigation__logo"> EasyEvent</h1>
          </div>
          <nav className="main-navigation__items">
            <ul>
              {!context.token && (
                <li>
                  <NavLink to="/auth">Authenticate</NavLink>
                </li>
              )}
              <li>
                <NavLink to="/events">Event</NavLink>
              </li>
              {!context.token && (
                <li>
                  <NavLink to="/bookings">Bookings</NavLink>
                </li>
              )}
            </ul>
          </nav>
        </header>)
    }}
  </AuthContext.Consumer>
}

export default MainNavigation