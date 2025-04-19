export const TRANSLATION_KEYS = {
  COMMON: {
    NAV: {
      HOME: "nav.home",
      GEAR: "nav.gear",
      GALLERY: "nav.gallery",
      BOOKING: "nav.booking",
      CONTACT: "nav.contact",
    },
    LANGUAGE: "language",
  },
  HOME: {
    WELCOME: "welcome",
    INTRO: "intro",
    CTA: "call_to_action",
  },
  BOOKING: {
    LABELS: {
      SELECT_DATE: "labels.select_date",
      AVAILABLE_TIMES: "labels.available_times",
      BOOKING_FORM: "labels.booking_form",
    },
    FORM: {
      NAME: "form.name",
      EMAIL: "form.email",
      PHONE: "form.phone",
      SELECTED_DATE: "form.selected_date",
      SELECTED_TIME: "form.selected_time",
      DURATION: "form.duration",
      SUBMIT: "form.submit",
    },
    WARNING_MESSAGES: {
      NO_AVAILABLE_TIMES: 'warning_messages.no_available_times',
    },
  },
};

export const I18N_NAMESPACES = {
  COMMON: "common",
  HOME: "home",
  GEAR: "gear",
  GALLERY: "gallery",
  BOOKING: "booking",
  SHOPPING: "shopping",
  CONTACT: "contact",
} as const;
