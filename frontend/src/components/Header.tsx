import React from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { TRANSLATION_KEYS, I18N_NAMESPACES } from '@/i18n/keys';

function Header() {
    const { t, i18n } = useTranslation(I18N_NAMESPACES.COMMON);

    const changeLanguage = (e: React.ChangeEvent<HTMLSelectElement>) => {
        i18n.changeLanguage(e.target.value);
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark px-4">
            <NavLink className="navbar-brand" to="/">
                Humpback Studio
            </NavLink>
    ß
            <button
                className="navbar-toggler"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#navbarNav"
                aria-controls="navbarNav"
                aria-expanded="false"
                aria-label="Toggle navigation"
            >
                <span className="navbar-toggler-icon" />
            </button>

            <div className="collapse navbar-collapse" id="navbarNav">
                <ul className="navbar-nav ms-auto">
                <li className="nav-item">
                    <NavLink className="nav-link" to="/">
                    {t(TRANSLATION_KEYS.COMMON.NAV.HOME)}
                    </NavLink>
                </li>
                <li className="nav-item">
                    <NavLink className="nav-link" to="/gear">
                    {t(TRANSLATION_KEYS.COMMON.NAV.GEAR)}
                    </NavLink>
                </li>
                <li className="nav-item">
                    <NavLink className="nav-link" to="/gallery">
                    {t(TRANSLATION_KEYS.COMMON.NAV.GALLERY)}
                    </NavLink>
                </li>
                <li className="nav-item">
                    <NavLink className="nav-link" to="/booking">
                    {t(TRANSLATION_KEYS.COMMON.NAV.BOOKING)}
                    </NavLink>
                </li>
                <li className="nav-item">
                    <NavLink className="nav-link" to="/contact">
                    {t(TRANSLATION_KEYS.COMMON.NAV.CONTACT)}
                    </NavLink>
                </li>
                </ul>

                <div className="ms-3 text-white d-flex align-items-center">
                    <i className="bi bi-globe me-2" title="Select Language" />
                    <select
                        id="lang-select"
                        value={i18n.language}
                        onChange={changeLanguage}
                        className="form-select form-select-sm d-inline-block w-auto"
                    >
                        <option value="en">🇬🇧 English</option>
                        <option value="pt-BR">🇧🇷 Português</option>
                    </select>
                </div>
            </div>
        </nav>
    );
}

export default Header;