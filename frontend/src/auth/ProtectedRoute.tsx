import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "./AuthProvider"

export function ProtectedRoute() {
  const { user, ready } = useAuth();
  if (!ready) return null; // or a spinner
  return user ? <Outlet /> : <Navigate to="/login" replace />;
}

export function RequireRoles({ roles }: { roles: string[] }) {
  const { user, ready } = useAuth();
  if (!ready) return null;
  const ok = user && user.roles?.some(r => roles.includes(r));
  return ok ? <Outlet /> : <Navigate to="/" replace />;
}
