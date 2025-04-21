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
    WELCOME: {
      MESSAGE: "welcome.message",
      GREETINGS: "welcome.greetings",
    },
    NEWS_PANEL: {
      TITLE: "news_panel.title",
      EMPTY: "news_panel.empty",
    },
  },
  GEAR: {
    INTRO: {
      TITLE: "intro.title",
      P1: "intro.p1",
      P2: "intro.p2",
      P3: "intro.p3",
    },
  },
  GALLERY: {
    TITLE: "title",
    PHOTOS: {
      TITLE: "photos.title",
      PLACEHOLDER: "photos.placeholder",
    },
    VIDEOS: {
      TITLE: "videos.title",
      PLACEHOLDER: "videos.placeholder",
    },
  },
  BOOKING: {
    LABELS: {
      SELECT_DATE: "labels.select_date",
      AVAILABLE_TIMES: "labels.available_times",
      BOOKING_FORM: "labels.booking_form",
      NOT_ACCEPTING: "labels.not_accepting",
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
      NO_AVAILABLE_TIMES: "warning_messages.no_available_times",
    },
  },
  CONTACT: {
    TITLE: "title",
    INTRO: {
      P1: "intro.p1",
      P2: "intro.p2",
      P3: "intro.p3",
      SAFE: "intro.safe",
    },
    VISIT: {
      TEXT: "visit.text",
      STUDIO: "visit.studio",
    },
    FORM: {
      NAME: "form.name",
      EMAIL: "form.email",
      PHONE: "form.phone",
      MESSAGE: "form.message",
      PLACEHOLDER: "form.placeholder",
      SUBMIT: "form.submit",
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
