import React from "react";
import { Container } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";
import GearIntroPanel from "@/components/GearIntroPanel";

const Gear: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.GEAR);

  return (
    <Container className="my-5">
      <h2 className="mb-4">{t(TRANSLATION_KEYS.GEAR.TITLE)}</h2>

      <GearIntroPanel />
      {/* future gear list or images */}
    </Container>
  );
};

export default Gear;
