import React from "react";
import { useTranslation } from "react-i18next";
import { TRANSLATION_KEYS, I18N_NAMESPACES } from "@/i18n/keys";
import bgImage from "@/assets/404_bg.png";

const NotFound: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.COMMON);

  return (
    <div
      className="not-found-container"
      style={{
        backgroundImage: `url(${bgImage})`,
      }}
    >
      <div className="overlay-content">
        <h1>404</h1>
        <p>{t(TRANSLATION_KEYS.COMMON.NOT_FOUND.ERROR)}</p>
      </div>
    </div>
  );
};

export default NotFound;