import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import { I18N_NAMESPACES } from './keys';

import commonEN from './en/common.json';
import commonPT from './pt-br/common.json';


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
        I18N_NAMESPACES.COMMON
    ],
    defaultNS: I18N_NAMESPACES.COMMON,
    resources: {
      en: {
        common: commonEN,
      },
      'pt-br': {
        common: commonPT,
      },
      'pt-BR': {
        common: commonPT,
      },
    },
    interpolation: {
      escapeValue: false,
    },
  });

export default i18n;
