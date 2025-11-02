import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthProvider";

export function ProtectedRoute() {
  const { user, ready } = useAuth();
  const location = useLocation();

  if (!ready) return null; // or a spinner
  return user
    ? <Outlet />
    : <Navigate to="/login" replace state={{ from: location }} />;
}

export function RequireRoles({ roles }: { roles: string[] }) {
  const { user, ready } = useAuth();
  const location = useLocation();

  if (!ready) return null;
  const ok = user && user.roles?.some(r => roles.includes(r));
  return ok
    ? <Outlet />
    : <Navigate to="/login" replace state={{ from: location }} />;
}