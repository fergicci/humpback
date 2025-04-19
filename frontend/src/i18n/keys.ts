export const TRANSLATION_KEYS = {
  COMMON: {
    NAV: {
      HOME: 'nav.home',
      GEAR: 'nav.gear',
      GALLERY: 'nav.gallery',
      BOOKING: 'nav.booking',
      CONTACT: 'nav.contact',
    },
    LANGUAGE: 'language',
  },
  HOME: {
    WELCOME: "welcome",
    INTRO: "intro",
    CTA: "call_to_action",
  },
  BOOKING: {
    TITLE: "title",
    DESCRIPTION: "description",
    BUTTON: "submit",
    SUCCESS_MESSAGE: "success",
  },
};

export const I18N_NAMESPACES = {
  COMMON: 'common',
  HOME: 'home',
  GEAR: 'gear',
  GALLERY: 'gallery',
  BOOKING: 'booking',
  SHOPPING: 'shopping',
  CONTACT: 'contact'
} as const;