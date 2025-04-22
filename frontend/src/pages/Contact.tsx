import React from "react";
import { Container, Row, Col, Form, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

const Contact: React.FC = () => {
  const { i18n } = useTranslation();
  const { t } = useTranslation(I18N_NAMESPACES.CONTACT);

  const lang = i18n.language.toLowerCase();
  const suffix = lang === "pt-br" ? "PT_BR" : "EN";

  const addressLine = import.meta.env[
    `VITE_APPLICATION_CONTACT_ADDRESSLINE_${suffix}`
  ];
  const city = import.meta.env[`VITE_APPLICATION_CONTACT_CITY_${suffix}`];
  const country = import.meta.env[`VITE_APPLICATION_CONTACT_COUNTRY_${suffix}`];
  const phone = import.meta.env[`VITE_APPLICATION_CONTACT_PHONE_${suffix}`];
  const contact = import.meta.env[`VITE_APPLICATION_CONTACT_PERSON_${suffix}`];

  return (
    <Container className="my-5">
      <Row>
        {/* Left Side: Info */}
        <Col md={6} className="mb-4">
          <h2>{t(TRANSLATION_KEYS.CONTACT.TITLE)}</h2>
          <p>{t(TRANSLATION_KEYS.CONTACT.INTRO.P1)}</p>
          <p>{t(TRANSLATION_KEYS.CONTACT.INTRO.P2)}</p>
          <p>{t(TRANSLATION_KEYS.CONTACT.INTRO.P3)}</p>
          <p className="text-success fw-bold">
            {t(TRANSLATION_KEYS.CONTACT.INTRO.SAFE)}
          </p>
          <p>{t(TRANSLATION_KEYS.CONTACT.VISIT.TEXT)}</p>

          <p className="d-flex align-items-start">
            <i className="bi bi-geo-alt me-2 mt-1" />
            <span>
              <strong>{t(TRANSLATION_KEYS.CONTACT.VISIT.STUDIO)}</strong>
              <br />
              {addressLine}
              <br />
              {city}, {country}
            </span>
          </p>

          <p className="d-flex align-items-center">
            <i className="bi bi-telephone me-2" />
            <strong>{phone}</strong>
          </p>

          <p className="d-flex align-items-center">
            <i className="bi bi-person-circle me-2" />
            <strong>{contact}</strong>
          </p>
        </Col>

        {/* Right Side: Form */}
        <Col md={6}>
          <div className="alert alert-info text-center mb-4">
            {t(TRANSLATION_KEYS.CONTACT.FORM.NOT_ACCEPTING)}
          </div>
          <Form>
            <Form.Group className="mb-3" controlId="formName">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.NAME)}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formEmail">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.EMAIL)}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formPhone">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.PHONE)}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formMessage">
              <Form.Label>
                {t(TRANSLATION_KEYS.CONTACT.FORM.MESSAGE)}
              </Form.Label>
              <Form.Control
                as="textarea"
                rows={8}
                placeholder={t(TRANSLATION_KEYS.CONTACT.FORM.PLACEHOLDER)}
                style={{ minHeight: "200px" }}
              />
            </Form.Group>

            <Button disabled>{t(TRANSLATION_KEYS.CONTACT.FORM.SUBMIT)}</Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default Contact;
