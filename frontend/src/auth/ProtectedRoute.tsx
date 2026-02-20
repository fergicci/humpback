import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "./AuthProvider";

export function ProtectedRoute() {
  const { user, ready } = useAuth();
  const location = useLocation();

  if (!ready) return null;

  return user ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />;
}

export function RequireRoles() {
  return <Outlet />;
}