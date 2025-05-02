export const getCurrentLang = (): string => {
  return navigator.language || "en";
};

export const getBaseLang = (lang?: string): string => {
  return (lang || navigator.language || "en").split("-")[0];
};

export const getCalendarLocale = (lang: string): string => {
  switch (lang.toLowerCase()) {
    case "pt":
    case "pt-br":
      return "pt";
    case "en":
    default:
      return "en-US";
  }
};
