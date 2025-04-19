import React from 'react';
import { NavLink } from 'react-router-dom';

function Header() {

    const getNavClass = ({ isActive }: { isActive: boolean }) =>
        isActive ? 'nav-link active' : 'nav-link';

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark px-4">
            <NavLink className="navbar-brand" to="/">
                HUMPBACK STUDIO
            </NavLink>
            <button
                className="navbar-toggler"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#navbarNav"
                aria-controls="navbarNav"
                aria-expanded="false"
                aria-label="Toggle navigation"
            >
                <span className="navbar-toggler-icon"></span>
            </button>

            <div className="collapse navbar-collapse" id="navbarNav">
                <ul className="navbar-nav ms-auto">
                    <li className="nav-item">
                        <NavLink
                            to="/"
                            className={getNavClass}
                        >
                            Home
                        </NavLink>
                    </li>
                    <li className="nav-item">
                        <NavLink
                            to="/gear"
                            className={getNavClass}
                        >
                            Gear
                        </NavLink>
                    </li>
                    <li className="nav-item">
                        <NavLink 
                            to="/gallery"
                            className={getNavClass}
                        >
                            Gallery
                        </NavLink>
                    </li>
                    <li className="nav-item">
                        <NavLink
                            to="/booking"
                            className={getNavClass}
                        >
                            Booking
                        </NavLink>
                    </li>
                    <li className="nav-item">
                        <NavLink 
                            to="/contact"
                            className={getNavClass}
                        >
                            Contact
                        </NavLink>
                    </li>
                </ul>
            </div>
        </nav>
  );
}

export default Header;