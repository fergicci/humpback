import { useMemo, useState } from "react";
import { Container, Row, Col, Form, Button, Alert } from "react-bootstrap";
import { Link } from "react-router-dom";

import { registerUser, type RegisterRequest } from "@/services/authService";
import type { ApiError } from "@/services/api";

type RegisterFormState = RegisterRequest & {
  confirmPassword: string;
};

const USERNAME_PATTERN = "^[a-z0-9][a-z0-9._-]{2,31}$";
const PASSWORD_PATTERN = "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,64}$";

export default function Register() {
  const lang = useMemo(() => navigator.language || "en", []);

  const [formData, setFormData] = useState<RegisterFormState>({
    username: "",
    fullname: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [wasValidated, setWasValidated] = useState(false);
  const [ok, setOk] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    const nextValue = name === "username" ? value.toLowerCase() : value;

    setFormData((s) => ({ ...s, [name]: nextValue }));

    setErrors((prev) => {
      if (!prev[name]) return prev;
      const next = { ...prev };
      delete next[name];
      return next;
    });
  };

  function mapApiErrors(err: any): Record<string, string> {
    const msgs: Record<string, string> = {
      general: err?.message ?? "Registration failed",
    };

    if (Array.isArray(err?.details)) {
      for (const entry of err.details) {
        if (typeof entry !== "string") continue;
        const idx = entry.indexOf(":");
        if (idx <= 0) continue;

        const field = entry.slice(0, idx).trim();
        const message = entry.slice(idx + 1).trim();

        const key = field.toLowerCase();

        if (key === "fullname" || key === "full_name") msgs.fullname = message;
        else if (key === "confirmpassword" || key === "confirm_password")
          msgs.confirmPassword = message;
        else msgs[field] = message; // username, email, password...
      }
    }

    return msgs;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setWasValidated(true);
    setErrors({});
    setOk(false);

    if (formData.password !== formData.confirmPassword) {
      setErrors({ confirmPassword: "Passwords do not match." });
      return;
    }

    setSubmitting(true);

    try {
      await registerUser(
        {
          username: formData.username.trim(),
          fullname: formData.fullname.trim(),
          email: formData.email.trim(),
          password: formData.password,
        },
        lang
      );

      setOk(true);
      setFormData({
        username: "",
        fullname: "",
        email: "",
        password: "",
        confirmPassword: "",
      });
      setWasValidated(false);
    } catch (err: any) {
      setErrors(mapApiErrors(err as ApiError));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col sm={10} md={7} lg={5}>
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h5 className="card-title mb-1 fw-semibold">Register</h5>
              <p className="text-muted mb-3">
                Create your Humpback Studio admin account.
              </p>

              {ok && (
                <Alert variant="success" dismissible onClose={() => setOk(false)}>
                  Account created. You can now sign in.
                </Alert>
              )}

              {errors.general && (
                <Alert variant="danger" dismissible onClose={() => setErrors({})}>
                  {errors.general}
                </Alert>
              )}

              <Form onSubmit={handleSubmit} noValidate>
                <Form.Group className="mb-3" controlId="registerUsername">
                  <Form.Label>Username</Form.Label>
                  <Form.Control
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    isInvalid={!!errors.username}
                    isValid={wasValidated && !errors.username && formData.username.length > 0}
                    autoComplete="username"
                    pattern={USERNAME_PATTERN}
                    minLength={3}
                    maxLength={32}
                    inputMode="text"
                    autoFocus
                  />
                  <Form.Text className="text-muted">
                    3-32 chars: lowercase letters, numbers, dot, underscore, hyphen.
                  </Form.Text>
                  <Form.Control.Feedback type="invalid">
                    {errors.username}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="registerFullName">
                  <Form.Label>Full name</Form.Label>
                  <Form.Control
                    name="fullname"
                    value={formData.fullname}
                    onChange={handleChange}
                    isInvalid={!!errors.fullname}
                    isValid={wasValidated && !errors.fullname && formData.fullname.length > 0}
                    autoComplete="name"
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.fullname}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="registerEmail">
                  <Form.Label>Email</Form.Label>
                  <Form.Control
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    isInvalid={!!errors.email}
                    isValid={wasValidated && !errors.email && formData.email.length > 0}
                    autoComplete="email"
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.email}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="registerPassword">
                  <Form.Label>Password</Form.Label>
                  <Form.Control
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    isInvalid={!!errors.password}
                    isValid={wasValidated && !errors.password && formData.password.length > 0}
                    autoComplete="new-password"
                    pattern={PASSWORD_PATTERN}
                    minLength={12}
                    maxLength={64}
                  />
                  <Form.Text className="text-muted">
                    12-64 chars, including uppercase, lowercase, number, special character, and no spaces.
                  </Form.Text>
                  <Form.Control.Feedback type="invalid">
                    {errors.password}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="registerConfirmPassword">
                  <Form.Label>Confirm password</Form.Label>
                  <Form.Control
                    type="password"
                    name="confirmPassword"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    isInvalid={!!errors.confirmPassword}
                    isValid={
                      wasValidated &&
                      !errors.confirmPassword &&
                      formData.confirmPassword.length > 0
                    }
                    autoComplete="new-password"
                    pattern={PASSWORD_PATTERN}
                    minLength={12}
                    maxLength={64}
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.confirmPassword}
                  </Form.Control.Feedback>
                </Form.Group>

                <Button type="submit" disabled={submitting} className="w-100">
                  {submitting ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                      Registering…
                    </>
                  ) : (
                    "Register"
                  )}
                </Button>

                <div className="text-center mt-3 small">
                  Already have an account?
                  <Link to="/login" className="ms-1">
                    Sign in
                  </Link>
                </div>
              </Form>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
}
