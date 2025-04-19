import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import { I18N_NAMESPACES } from './keys';

import commonEN from './en/common.json';
import commonPT from './pt-br/common.json';
import contactEN from './en/contact.json';
import contactPT from './pt-BR/contact.json';
import bookingEN from './en/booking.json';
import bookingPT from './pt-BR/booking.json';

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
        I18N_NAMESPACES.CONTACT,
        I18N_NAMESPACES.BOOKING,
    ],
    defaultNS: I18N_NAMESPACES.COMMON,
    resources: {
      en: {
        common: commonEN,
        booking: bookingEN,
        contact: contactEN,
      },
      'pt-br': {
        common: commonPT,
        booking: bookingPT,
        contact: contactPT,
      },
      'pt-BR': {
        common: commonPT,
        booking: bookingPT,
        contact: contactPT,
      },
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
