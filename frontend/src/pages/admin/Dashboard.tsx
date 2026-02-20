import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Card, Col, Container, Row, Spinner } from "react-bootstrap";
import BookingsCalendar from "@/components/admin/dashboard/BookingsCalendar";
import { getDashboard, type DashboardData } from "@/services/dashboardService";
import type { ApiError } from "@/services/api";
import { useAutoRefresh } from "@/utils/useAutoRefresh";

const now = new Date();

const EMPTY_DASHBOARD: DashboardData = {
  generatedAt: "",
  calendarYear: now.getUTCFullYear(),
  calendarMonth: now.getUTCMonth() + 1,
  counters: {
    today: 0,
    month: 0,
    year: 0,
    total: 0,
  },
  unreadContacts: 0,
  byType: {},
  bookingsByDay: {},
};

function toBookingTypeLabel(value: string): string {
  return value
    .trim()
    .replace(/[_-]+/g, " ")
    .replace(/\s+/g, " ")
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<DashboardData>(EMPTY_DASHBOARD);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<ApiError | null>(null);
  const refreshTick = useAutoRefresh(60_000);

  const byTypeEntries = useMemo(
    () => Object.entries(dashboard.byType ?? {}).sort((a, b) => b[1] - a[1]),
    [dashboard.byType]
  );

  async function loadDashboard() {
    setLoading(true);
    setError(null);

    try {
      const data = await getDashboard();
      setDashboard(data);
    } catch (e) {
      setError(e as ApiError);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    (async () => {
      try {
        const data = await getDashboard();
        if (!cancelled) {
          setDashboard(data);
          setError(null);
        }
      } catch (e) {
        if (!cancelled) {
          setError(e as ApiError);
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [refreshTick]);

  return (
    <Container className="admin-dashboard">
      <h1 className="mb-4">
        Dashboard
      </h1>
      {error && (
        <Alert variant="danger" className="d-flex justify-content-between align-items-center">
          <span>{`Error ${error.code || 500}: ${error.message}`}</span>
          <Button size="sm" variant="outline-danger" onClick={loadDashboard}>
            Retry
          </Button>
        </Alert>
      )}

      {loading && (
        <div className="d-flex justify-content-center my-5">
          <Spinner animation="border" role="status" />
        </div>
      )}

      {!loading && (
        <>
          <Row className="g-3 mb-4">
            <Col md={3}>
              <StatCard title="Today" value={dashboard.counters?.today ?? 0} />
            </Col>
            <Col md={3}>
              <StatCard title="This Month" value={dashboard.counters?.month ?? 0} />
            </Col>
            <Col md={3}>
              <StatCard title="This Year" value={dashboard.counters?.year ?? 0} />
            </Col>
            <Col md={3}>
              <StatCard title="Total" value={dashboard.counters?.total ?? 0} />
            </Col>
            <Col md={3}>
              <StatCard title="Unread Contacts" value={dashboard.unreadContacts ?? 0} />
            </Col>
          </Row>

          <Row className="g-4">
            <Col md={6}>
              <Card className="admin-surface">
                <Card.Body>
                  <Card.Title className="mb-3">Bookings by type</Card.Title>

                  {byTypeEntries.length > 0 ? (
                    <ul className="list-unstyled mb-0">
                      {byTypeEntries.map(([type, count]) => (
                        <li
                          key={type}
                          className="d-flex justify-content-between py-1"
                        >
                          <span>{toBookingTypeLabel(type)}</span>
                          <strong>{count}</strong>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <div className="text-muted">No booking data yet.</div>
                  )}
                </Card.Body>
              </Card>
            </Col>

            <Col md={6}>
              <Card className="admin-surface">
                <Card.Body>
                  <Card.Title className="mb-3">Bookings per day</Card.Title>

                  <BookingsCalendar
                    year={dashboard.calendarYear || now.getUTCFullYear()}
                    month={(dashboard.calendarMonth || now.getUTCMonth() + 1) - 1}
                    bookings={dashboard.bookingsByDay ?? {}}
                  />
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </>
      )}
    </Container>
  );
}

/* Small internal component */
function StatCard({ title, value }: { title: string; value: number }) {
  return (
    <Card className="admin-surface stat-card">
      <Card.Body>
        <div className="stat-title">{title}</div>
        <div className="stat-value">{value}</div>
      </Card.Body>
    </Card>
  );
}
