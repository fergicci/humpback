export const TRANSLATION_KEYS = {
  COMMON: {
    NAV: {
      HOME: "nav.home",
      GEAR: "nav.gear",
      GALLERY: "nav.gallery",
      BOOKING: "nav.booking",
      WORKSHOP: "nav.workshop",
      SHOP: "nav.shop",
      CONTACT: "nav.contact",
    },
    LANGUAGE: "language",
    NOT_FOUND: {
      ERROR: "not_found.error",
    }
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
    HIGHLIGHT: {
      COMPOSITION: {
        TITLE: "highlight.composition.title",
        DESCRIPTION: "highlight.composition.description",
      },
      RECORDING: {
        TITLE: "highlight.recording.title",
        DESCRIPTION: "highlight.recording.description",
      },
      MIXING: {
        TITLE: "highlight.mixing.title",
        DESCRIPTION: "highlight.mixing.description",
      },
      MASTERING: {
        TITLE: "highlight.mastering.title",
        DESCRIPTION: "highlight.mastering.description",
      },
      WORKSHOPS: {
        TITLE: "highlight.workshop.title",
        DESCRIPTION: "highlight.workshop.description",
      },
      SHOP: {
        TITLE: "highlight.shop.title",
        DESCRIPTION: "highlight.shop.description",
      }
    }
  },
  GEAR: {
    TITLE: "title",
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
      TYPE: "form.type",
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
      NOT_ACCEPTING: "form.not_accepting",
      GENERIC_ERROR: "form.generic_error",
      SUCCESS: "form.success",
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
