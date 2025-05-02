import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import { I18N_NAMESPACES } from './keys';

import commonEN from './en/common.json';
import commonPT from './pt/common.json';
import contactEN from './en/contact.json';
import contactPT from './pt/contact.json';
import bookingEN from './en/booking.json';
import bookingPT from './pt/booking.json';
import homeEN from './en/home.json';
import homePT from './pt/home.json';
import gearEN from './en/gear.json';
import gearPT from './pt/gear.json';
import galleryEN from './en/gallery.json';
import galleryPT from './pt/gallery.json';

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    detection: {
        order: ['querystring', 'localStorage', 'navigator'],
        caches: ['localStorage'],
    },
    fallbackLng: 'en',
    debug: false,
    ns: [
        I18N_NAMESPACES.COMMON,
        I18N_NAMESPACES.HOME,
        I18N_NAMESPACES.GEAR,
        I18N_NAMESPACES.GALLERY,
        I18N_NAMESPACES.BOOKING,
        I18N_NAMESPACES.CONTACT,
    ],
    defaultNS: I18N_NAMESPACES.COMMON,
    resources: {
      en: {
        common: commonEN,
        home: homeEN,
        gear: gearEN,
        gallery: galleryEN,
        booking: bookingEN,
        contact: contactEN,
      },
      pt: {
        common: commonPT,
        home: homePT,
        gear: gearPT,
        gallery: galleryPT,
        booking: bookingPT,
        contact: contactPT,
      },
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
