import React from "react";
import { Container, Row, Col, Image } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

import consoleImg from "@/assets/console_01.jpg";

const Gear: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.GEAR);

  return (
    <Container className="my-5">
      <h2 className="mb-4">{t(TRANSLATION_KEYS.GEAR.INTRO.TITLE)}</h2>
      <Row>
        <Col md={6}>
          <p>{t(TRANSLATION_KEYS.GEAR.INTRO.P1)}</p>
          <p>{t(TRANSLATION_KEYS.GEAR.INTRO.P2)}</p>
          <p>{t(TRANSLATION_KEYS.GEAR.INTRO.P3)}</p>
        </Col>
        <Col md={6}>
          <Image src={consoleImg} alt="Audient ASP8024" fluid rounded />
        </Col>
      </Row>
    </Container>
  );
};

export default Gear;
