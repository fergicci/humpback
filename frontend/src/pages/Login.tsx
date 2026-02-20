import React, { useState } from "react";
import { Container, Row, Col, Form, Button, Alert } from "react-bootstrap";
import { useLocation, useNavigate, Link} from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";

const Login: React.FC = () => {
  const { signin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as any;
  const from = location.state?.from?.pathname || "/admin";

  const remembered = localStorage.getItem("hb_remember") === "1";

  const [formData, setFormData] = useState({
    username: "",
    password: "",
    rememberMe: remembered,
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [wasValidated, setWasValidated] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleCheckbox = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.checked });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);
    setWasValidated(true);

    try {
      if (formData.rememberMe) localStorage.setItem("hb_remember", "1");
      else localStorage.removeItem("hb_remember");

      await signin(
        formData.username.trim(),
        formData.password,
        formData.rememberMe
      );
      navigate(from, { replace: true });
    } catch (err: any) {
      const msgs: Record<string, string> = {
        general: err?.message ?? "Login failed",
      };

      if (err?.details?.length) {
        err.details.forEach((entry: string) => {
          const [field, message] = entry.split(":").map((s) => s.trim());
          msgs[field] = message;
        });
      }
      console.log(msgs);
      setErrors(msgs);
    } finally {
      setFormData((s) => ({ ...s, password: "" }));
      setSubmitting(false);
    }
  };

  return (
    <Container className="py-5">
      <Row className="justify-content-center">
        <Col sm={10} md={7} lg={5}>
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h5 className="card-title mb-1 fw-semibold">Sign in</h5>
              <p className="text-muted mb-3">
                Use your Humpback Studio admin account.
              </p>

              {errors.general && (
                <Alert
                  variant="danger"
                  dismissible
                  onClose={() => setErrors({})}
                >
                  {errors.general}
                </Alert>
              )}

              <Form onSubmit={handleSubmit} noValidate>
                <Form.Group className="mb-3" controlId="formUsername">
                  <Form.Label>Username</Form.Label>
                  <Form.Control
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    isInvalid={!!errors.username}
                    isValid={
                      wasValidated &&
                      !errors.username &&
                      formData.username.length > 0
                    }
                    autoComplete="username"
                    autoFocus
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.username}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3" controlId="formPassword">
                  <Form.Label>Password</Form.Label>
                  <Form.Control
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    isInvalid={!!errors.password}
                    isValid={
                      wasValidated &&
                      !errors.password &&
                      formData.password.length > 0
                    }
                    autoComplete="current-password"
                  />
                  <Form.Control.Feedback type="invalid">
                    {errors.password}
                  </Form.Control.Feedback>
                </Form.Group>

                <Form.Group className="mb-3 d-flex align-items-center justify-content-between">
                  <Form.Check
                    id="rememberMe"
                    name="rememberMe"
                    checked={formData.rememberMe}
                    onChange={handleCheckbox}
                    label="Remember me"
                  />
                  <a
                    href="#"
                    onClick={(e) => e.preventDefault()}
                    className="small"
                  >
                    Forgot password?
                  </a>
                </Form.Group>

                <Button type="submit" disabled={submitting} className="w-100">
                  {submitting ? (
                    <>
                      <span
                        className="spinner-border spinner-border-sm me-2"
                        role="status"
                        aria-hidden="true"
                      />
                      Signing in…
                    </>
                  ) : (
                    "Sign in"
                  )}
                </Button>

                <div className="text-center mt-3 small">
                  Don’t have an account?
                  <Link to="/register" className="ms-1">
                    Register
                  </Link>
                </div>
              </Form>
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default Login;
