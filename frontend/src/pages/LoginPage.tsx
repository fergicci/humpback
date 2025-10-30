import { useState } from "react";
import { useForm } from "react-hook-form";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/auth/AuthProvider";

type FormValues = {
  username: string;
  password: string;
  rememberMe: boolean;
};

export default function LoginPage() {
  const { signin } = useAuth();
  const location = useLocation() as any;
  const navigate = useNavigate();
  const from = location.state?.from?.pathname || "/admin/contacts";

  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    resetField,
    formState: { errors },
  } = useForm<FormValues>({
    defaultValues: { username: "", password: "", rememberMe: false },
    mode: "onTouched",
  });

  const onSubmit = async (data: FormValues) => {
    setErrorMsg(null);
    setSubmitting(true);
    try {
      if (data.rememberMe) localStorage.setItem("hb_remember", "1");
      else localStorage.removeItem("hb_remember");

      await signin(data.username.trim(), data.password, data.rememberMe);
      navigate(from, { replace: true });
    } catch (e: any) {
      setErrorMsg(e?.message ?? "Login failed");
    } finally {
      resetField("password");
      setSubmitting(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-sm-10 col-md-7 col-lg-5">
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h5 className="card-title mb-1 fw-semibold">Sign in</h5>
              <p className="text-muted mb-3">
                Use your Humpback Studio admin account.
              </p>

              {errorMsg && (
                <div className="alert alert-danger py-2" role="alert">
                  {errorMsg}
                </div>
              )}

              <form onSubmit={handleSubmit(onSubmit)} noValidate>
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">
                    Username
                  </label>
                  <input
                    id="username"
                    type="text"
                    className={`form-control ${
                      errors.username ? "is-invalid" : ""
                    }`}
                    autoComplete="username"
                    autoFocus
                    {...register("username", {
                      required: "Username is required",
                      minLength: { value: 3, message: "Minimum 3 characters" },
                    })}
                  />
                  {errors.username && (
                    <div className="invalid-feedback">
                      {errors.username.message}
                    </div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="password" className="form-label">
                    Password
                  </label>
                  <div className="input-group">
                    <input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      className={`form-control ${
                        errors.password ? "is-invalid" : ""
                      }`}
                      autoComplete="current-password"
                      {...register("password", {
                        required: "Password is required",
                        minLength: {
                          value: 6,
                          message: "Minimum 6 characters",
                        },
                      })}
                    />
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={() => setShowPassword((v) => !v)}
                      aria-label={
                        showPassword ? "Hide password" : "Show password"
                      }
                    >
                      {showPassword ? "Hide" : "Show"}
                    </button>
                    {errors.password && (
                      <div className="invalid-feedback d-block">
                        {errors.password.message}
                      </div>
                    )}
                  </div>
                </div>

                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="form-check">
                    <input
                      id="rememberMe"
                      type="checkbox"
                      className="form-check-input"
                      {...register("rememberMe")}
                    />
                    <label htmlFor="rememberMe" className="form-check-label">
                      Remember me
                    </label>
                  </div>
                  <a
                    href="#"
                    onClick={(e) => e.preventDefault()}
                    className="small"
                  >
                    Forgot password?
                  </a>
                </div>

                <button
                  type="submit"
                  className="btn btn-primary w-100"
                  disabled={submitting}
                >
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
                </button>

                <div className="text-center mt-3">
                  <a
                    href="#"
                    onClick={(e) => e.preventDefault()}
                    className="small"
                  >
                    Don’t have an account? Contact the admin
                  </a>
                </div>
              </form>
            </div>
          </div>

          <p className="text-center text-muted small mt-3 mb-0">
            By signing in you agree to our Terms & Privacy.
          </p>
        </div>
      </div>
    </div>
  );
}
