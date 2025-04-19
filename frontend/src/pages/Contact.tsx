import React from "react";
import { Container, Row, Col, Form, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES } from "@/i18n/keys";

const Contact: React.FC = () => {
  const { t } = useTranslation(I18N_NAMESPACES.CONTACT);

  return (
    <Container className="my-5">
      <Row>
        {/* Left Side: Info */}
        <Col md={6} className="mb-4">
          <h2>{t("title")}</h2>
          <p>{t("intro.p1")}</p>
          <p>{t("intro.p2")}</p>
          <p>{t("intro.p3")}</p>
          <p className="text-success fw-bold">{t("intro.safe")}</p>
          <p>{t("visit.text")}</p>

          <p className="d-flex align-items-start">
            <i className="bi bi-geo-alt me-2 mt-1" />
            <span>
              <strong>{t("visit.studio")}</strong>
              <br />
              {t("visit.addressLine")}
              <br />
              {t("visit.city")}, {t("visit.country")}
            </span>
          </p>

          <p className="d-flex align-items-center">
            <i className="bi bi-telephone me-2" />
            <strong>{t("visit.phone")}</strong>
          </p>

          <p className="d-flex align-items-center">
            <i className="bi bi-person-circle me-2" />
            <strong>{t("visit.contact")}</strong>
          </p>
        </Col>

        {/* Right Side: Form */}
        <Col md={6}>
          <Form>
            <Form.Group className="mb-3" controlId="formName">
              <Form.Label>{t("form.name")}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formEmail">
              <Form.Label>{t("form.email")}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formPhone">
              <Form.Label>{t("form.phone")}</Form.Label>
              <Form.Control />
            </Form.Group>

            <Form.Group className="mb-3" controlId="formMessage">
              <Form.Label>{t("form.message")}</Form.Label>
              <Form.Control
                as="textarea"
                rows={8}
                placeholder={t("form.placeholder")}
                style={{ minHeight: "200px" }}
              />
            </Form.Group>

            <Button>{t("form.submit")}</Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default Contact;
