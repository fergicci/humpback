import React, { useState } from "react";
import { Container, Row, Col, Form, Button, Alert } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";
import { sendContactMessage } from "@/services/contactService";

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

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    telephone: "",
    message: "",
  });

  const [errors, setErrors] = useState<string[]>([]);
  const [success, setSuccess] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors([]);
    setSuccess(false);
    setSubmitting(true);

    try {
      await sendContactMessage(formData);
      setSuccess(true);
      setFormData({ name: "", email: "", telephone: "", message: "" });
    } catch (err: any) {
      if (err?.details?.length) {
        setErrors(err.details);
      } else {
        setErrors([t(TRANSLATION_KEYS.CONTACT.FORM.GENERIC_ERROR)]);
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container className="my-5">
      <Row>
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
        <Col md={6}>
          <Form onSubmit={handleSubmit}>
            {success && (
              <Alert
                variant="success"
                dismissible
                onClose={() => setSuccess(false)}
              >
                {t(TRANSLATION_KEYS.CONTACT.FORM.SUCCESS)}
              </Alert>
            )}

            {errors.length > 0 && (
              <Alert variant="danger" dismissible onClose={() => setErrors([])}>
                <ul className="mb-0">
                  {errors.map((e, idx) => (
                    <li key={idx}>{e}</li>
                  ))}
                </ul>
              </Alert>
            )}
            <Form.Group className="mb-3" controlId="formName">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.NAME)}</Form.Label>
              <Form.Control
                name="name"
                value={formData.name}
                onChange={handleChange}
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formEmail">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.EMAIL)}</Form.Label>
              <Form.Control
                name="email"
                value={formData.email}
                onChange={handleChange}
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formPhone">
              <Form.Label>{t(TRANSLATION_KEYS.CONTACT.FORM.PHONE)}</Form.Label>
              <Form.Control
                name="telephone"
                value={formData.telephone}
                onChange={handleChange}
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formMessage">
              <Form.Label>
                {t(TRANSLATION_KEYS.CONTACT.FORM.MESSAGE)}
              </Form.Label>
              <Form.Control
                as="textarea"
                rows={8}
                name="message"
                value={formData.message}
                onChange={handleChange}
                placeholder={t(TRANSLATION_KEYS.CONTACT.FORM.PLACEHOLDER)}
                style={{ minHeight: "200px" }}
              />
            </Form.Group>
            <Button type="submit" disabled={submitting}>
              {t(TRANSLATION_KEYS.CONTACT.FORM.SUBMIT)}
            </Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default Contact;
