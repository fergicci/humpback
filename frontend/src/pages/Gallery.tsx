import React from "react";
import { Container, Row, Col, Accordion } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

const Gallery: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.GALLERY);

  return (
    <Container className="my-5">
      <h2 className="mb-4">{t(TRANSLATION_KEYS.GALLERY.TITLE)}</h2>

      <Row className="mb-4">
        <Col>
          <Accordion defaultActiveKey="0">
            <Accordion.Item eventKey="0">
              <Accordion.Header>{t(TRANSLATION_KEYS.GALLERY.PHOTOS.TITLE)}</Accordion.Header>
              <Accordion.Body className="text-muted">
                {t(TRANSLATION_KEYS.GALLERY.PHOTOS.PLACEHOLDER)}
              </Accordion.Body>
            </Accordion.Item>
          </Accordion>
        </Col>
      </Row>

      <Row>
        <Col>
          <Accordion defaultActiveKey="0">
            <Accordion.Item eventKey="0">
              <Accordion.Header>{t(TRANSLATION_KEYS.GALLERY.VIDEOS.TITLE)}</Accordion.Header>
              <Accordion.Body className="text-muted">
                {t(TRANSLATION_KEYS.GALLERY.VIDEOS.PLACEHOLDER)}
              </Accordion.Body>
            </Accordion.Item>
          </Accordion>
        </Col>
      </Row>
    </Container>
  );
};

export default Gallery;