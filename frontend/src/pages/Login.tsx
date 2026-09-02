import React, { useState } from "react";
import { Container, Row, Col, Form, Button, Alert } from "react-bootstrap";
import { useLocation, useNavigate, Link} from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";
import {
  changePasswordWithCurrent,
  requestForgotPassword,
  resetForgotPassword,
} from "@/services/authService";

const USERNAME_PATTERN = "^[a-z0-9][a-z0-9._-]{2,31}$";
const PASSWORD_PATTERN = "^(?=\\S+$)(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,64}$";
const ERR_PASSWORD_CHANGE_REQUIRED = "Password change required";

const Login: React.FC = () => {
  const { signin, completeTwoFactorSignin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation() as any;
  const from = location.state?.from?.pathname || "/admin";

  const remembered = localStorage.getItem("hb_remember") === "1";

  const [formData, setFormData] = useState({
    username: "",
    password: "",
    twoFactorCode: "",
    forgotPasswordNew: "",
    forgotPasswordConfirm: "",
    rememberMe: remembered,
  });
  const [loginChallengeToken, setLoginChallengeToken] = useState<string | null>(null);
  const [forgotPasswordChallengeToken, setForgotPasswordChallengeToken] = useState<string | null>(null);
  const [stage, setStage] = useState<"credentials" | "two-factor" | "forgot-password" | "change-password">("credentials");

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [wasValidated, setWasValidated] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const nextValue = e.target.name === "username"
      ? e.target.value.toLowerCase()
      : e.target.value;
    setFormData({ ...formData, [e.target.name]: nextValue });
  };

  const handleCheckbox = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.checked });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);
    setWasValidated(true);
    let keepCurrentPassword = false;

    try {
      if (formData.rememberMe) localStorage.setItem("hb_remember", "1");
      else localStorage.removeItem("hb_remember");

      const result = await signin(
        formData.username.trim(),
        formData.password,
        formData.rememberMe
      );

      if (result.status === "two_factor_required") {
        setLoginChallengeToken(result.challengeToken);
        setStage("two-factor");
        return;
      }

      navigate(from, { replace: true });
    } catch (err: any) {
      if (err?.message === ERR_PASSWORD_CHANGE_REQUIRED) {
        keepCurrentPassword = true;
        setStage("change-password");
        setErrors({
          general: "Your password will expire soon. Please update it now to continue.",
        });
        return;
      }

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
      if (!keepCurrentPassword) {
        setFormData((s) => ({ ...s, password: "" }));
      }
      setSubmitting(false);
    }
  };

  const handleTwoFactorSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);

    try {
      if (!loginChallengeToken) throw new Error("2FA challenge expired. Please sign in again.");
      await completeTwoFactorSignin(
        loginChallengeToken,
        formData.twoFactorCode.trim(),
        formData.rememberMe
      );
      navigate(from, { replace: true });
    } catch (err: any) {
      setErrors({ general: err?.message ?? "2FA verification failed" });
    } finally {
      setSubmitting(false);
      setFormData((s) => ({ ...s, twoFactorCode: "" }));
    }
  };

  const handleForgotPasswordRequest = async () => {
    setErrors({});
    setSubmitting(true);
    try {
      const response = await requestForgotPassword(formData.username.trim());
      if (!response.challengeToken) {
        throw new Error("Could not start forgot password flow");
      }
      setForgotPasswordChallengeToken(response.challengeToken);
      setFormData((s) => ({ ...s, password: "", twoFactorCode: "" }));
      setStage("forgot-password");
    } catch (err: any) {
      setErrors({ general: err?.message ?? "Could not request password reset" });
    } finally {
      setSubmitting(false);
    }
  };

  const handleForgotPasswordReset = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);

    try {
      if (!forgotPasswordChallengeToken) {
        throw new Error("Reset challenge expired. Please request again.");
      }
      if (formData.forgotPasswordNew !== formData.forgotPasswordConfirm) {
        throw new Error("New password and confirmation do not match");
      }

      await resetForgotPassword(
        forgotPasswordChallengeToken,
        formData.twoFactorCode.trim(),
        formData.forgotPasswordNew
      );

      setStage("credentials");
      setForgotPasswordChallengeToken(null);
      setFormData((s) => ({
        ...s,
        password: "",
        twoFactorCode: "",
        forgotPasswordNew: "",
        forgotPasswordConfirm: "",
      }));
      setErrors({ general: "Password updated. You can sign in now." });
    } catch (err: any) {
      setErrors({ general: err?.message ?? "Could not reset password" });
    } finally {
      setSubmitting(false);
    }
  };

  const handleChangePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});
    setSubmitting(true);

    try {
      if (formData.forgotPasswordNew !== formData.forgotPasswordConfirm) {
        throw new Error("New password and confirmation do not match");
      }

      await changePasswordWithCurrent(
        formData.username.trim(),
        formData.password,
        formData.forgotPasswordNew
      );

      setStage("credentials");
      setFormData((s) => ({
        ...s,
        password: "",
        forgotPasswordNew: "",
        forgotPasswordConfirm: "",
      }));
      setErrors({ general: "Password updated. Please sign in again." });
    } catch (err: any) {
      setErrors({ general: err?.message ?? "Could not update password" });
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
              <h5 className="card-title mb-1 fw-semibold">Sign in</h5>
              <p className="text-muted mb-3">
                {stage === "credentials"
                  ? "Use your Humpback Studio admin account."
                  : stage === "two-factor"
                  ? "Enter the 6-digit code from your authenticator app."
                  : stage === "forgot-password"
                  ? "Reset your password using your authenticator code."
                  : "Your password must be updated before you can continue."}
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

              {stage === "credentials" ? (
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
                      pattern={USERNAME_PATTERN}
                      minLength={3}
                      maxLength={32}
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
                      onClick={(e) => {
                        e.preventDefault();
                        handleForgotPasswordRequest();
                      }}
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
              ) : stage === "two-factor" ? (
                <Form onSubmit={handleTwoFactorSubmit} noValidate>
                  <Form.Group className="mb-3" controlId="formTwoFactorCode">
                    <Form.Label>Authenticator code</Form.Label>
                    <Form.Control
                      name="twoFactorCode"
                      value={formData.twoFactorCode}
                      onChange={handleChange}
                      isInvalid={!!errors.code}
                      autoComplete="one-time-code"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={6}
                      autoFocus
                    />
                    <Form.Text className="text-muted">
                      Enter the 6-digit code from your app.
                    </Form.Text>
                  </Form.Group>

                  <div className="d-flex gap-2">
                    <Button
                      type="button"
                      variant="outline-secondary"
                      className="w-50"
                      onClick={() => {
                        setStage("credentials");
                        setLoginChallengeToken(null);
                        setFormData((s) => ({ ...s, twoFactorCode: "", password: "" }));
                      }}
                      disabled={submitting}
                    >
                      Back
                    </Button>
                    <Button type="submit" disabled={submitting} className="w-50">
                      {submitting ? "Verifying..." : "Verify code"}
                    </Button>
                  </div>
                </Form>
              ) : stage === "forgot-password" ? (
                <Form onSubmit={handleForgotPasswordReset} noValidate>
                  <Form.Group className="mb-3" controlId="formForgotUsername">
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                      name="username"
                      value={formData.username}
                      onChange={handleChange}
                      disabled
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formForgotTwoFactorCode">
                    <Form.Label>Authenticator code</Form.Label>
                    <Form.Control
                      name="twoFactorCode"
                      value={formData.twoFactorCode}
                      onChange={handleChange}
                      autoComplete="one-time-code"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      maxLength={6}
                      autoFocus
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formForgotNewPassword">
                    <Form.Label>New password</Form.Label>
                    <Form.Control
                      type="password"
                      name="forgotPasswordNew"
                      value={formData.forgotPasswordNew}
                      onChange={handleChange}
                      autoComplete="new-password"
                      pattern={PASSWORD_PATTERN}
                      minLength={12}
                      maxLength={64}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formForgotConfirmPassword">
                    <Form.Label>Confirm password</Form.Label>
                    <Form.Control
                      type="password"
                      name="forgotPasswordConfirm"
                      value={formData.forgotPasswordConfirm}
                      onChange={handleChange}
                      autoComplete="new-password"
                      pattern={PASSWORD_PATTERN}
                      minLength={12}
                      maxLength={64}
                    />
                    <Form.Text className="text-muted">
                      12-64 chars, including uppercase, lowercase, number, special character, and no spaces.
                    </Form.Text>
                  </Form.Group>

                  <div className="d-flex gap-2">
                    <Button
                      type="button"
                      variant="outline-secondary"
                      className="w-50"
                      onClick={() => {
                        setStage("credentials");
                        setForgotPasswordChallengeToken(null);
                        setFormData((s) => ({
                          ...s,
                          twoFactorCode: "",
                          forgotPasswordNew: "",
                          forgotPasswordConfirm: "",
                        }));
                      }}
                      disabled={submitting}
                    >
                      Cancel
                    </Button>
                    <Button type="submit" disabled={submitting} className="w-50">
                      {submitting ? "Updating..." : "Update password"}
                    </Button>
                  </div>
                </Form>
              ) : (
                <Form onSubmit={handleChangePasswordSubmit} noValidate>
                  <Form.Group className="mb-3" controlId="formChangeUsername">
                    <Form.Label>Username</Form.Label>
                    <Form.Control
                      name="username"
                      value={formData.username}
                      onChange={handleChange}
                      disabled
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formChangeCurrentPassword">
                    <Form.Label>Current password</Form.Label>
                    <Form.Control
                      type="password"
                      name="password"
                      value={formData.password}
                      onChange={handleChange}
                      autoComplete="current-password"
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formChangeNewPassword">
                    <Form.Label>New password</Form.Label>
                    <Form.Control
                      type="password"
                      name="forgotPasswordNew"
                      value={formData.forgotPasswordNew}
                      onChange={handleChange}
                      autoComplete="new-password"
                      pattern={PASSWORD_PATTERN}
                      minLength={12}
                      maxLength={64}
                    />
                  </Form.Group>

                  <Form.Group className="mb-3" controlId="formChangeConfirmPassword">
                    <Form.Label>Confirm new password</Form.Label>
                    <Form.Control
                      type="password"
                      name="forgotPasswordConfirm"
                      value={formData.forgotPasswordConfirm}
                      onChange={handleChange}
                      autoComplete="new-password"
                      pattern={PASSWORD_PATTERN}
                      minLength={12}
                      maxLength={64}
                    />
                    <Form.Text className="text-muted">
                      12-64 chars, including uppercase, lowercase, number, special character, and no spaces.
                    </Form.Text>
                  </Form.Group>

                  <Button type="submit" disabled={submitting} className="w-100">
                    {submitting ? "Updating..." : "Update password"}
                  </Button>
                </Form>
              )}
            </div>
          </div>
        </Col>
      </Row>
    </Container>
  );
};

export default Login;
