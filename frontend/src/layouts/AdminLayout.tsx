import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { Container, Row, Col, Nav } from "react-bootstrap";
import { useAuth } from "@/auth/AuthProvider";

export default function AdminLayout() {
  const { user, signout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    signout();
    navigate("/login", { replace: true });
  };

  return (
    <Container fluid className="admin-wrapper">
      <Row className="min-vh-100 g-0">
        {/* Sidebar */}
        <Col className="admin-sidebar p-0" xs={12} md={3} lg={2}>
          <Nav className="flex-column admin-nav h-100">
            <div className="admin-nav-list">
              <NavLink
                to="/admin/dashboard"
                className="nav-link"
                title="Dashboard"
              >
                <span className="nav-icon">
                  <i className="bi bi-speedometer2" />
                </span>
              </NavLink>

              <NavLink
                to="/admin/contacts"
                className="nav-link"
                title="Contacts"
              >
                <span className="nav-icon">
                  <i className="bi bi-envelope-fill" />
                </span>
              </NavLink>

              <NavLink
                to="/admin/bookings"
                className="nav-link"
                title="Bookings"
              >
                <span className="nav-icon">
                  <i className="bi bi-calendar-event" />
                </span>
              </NavLink>

              <NavLink to="/admin/users" className="nav-link" title="Users">
                <span className="nav-icon">
                  <i className="bi bi-people-fill" />
                </span>
              </NavLink>

              {/* Logout */}
              {user && (
                <button
                  type="button"
                  className="nav-link btn btn-link p-0"
                  title="Sign out"
                  aria-label="Sign out"
                  onClick={handleLogout}
                >
                  <span className="nav-icon">
                    <i className="bi bi-box-arrow-left" />
                  </span>
                </button>
              )}
            </div>
          </Nav>
        </Col>

        {/* Content */}
        <Col className="admin-content">
          <div className="admin-surface">
            <Outlet />
          </div>
        </Col>
      </Row>
    </Container>
  );
}
