import { Container, Row, Col, Card } from "react-bootstrap";
import BookingsCalendar from "@/components/admin/dashboard/BookingsCalendar";

const mockDashboard = {
  today: 2,
  month: 18,
  year: 124,
  total: 342,
  byType: {
    REHARSAL: 120,
    RECORDING: 90,
    MIXING: 60,
    MASTERING: 40,
    VIDEO_PRODUCTION: 32,
  },
};

const mockBookingsByDay: Record<string, number> = {
  "2025-12-01": 2,
  "2025-12-03": 1,
  "2025-12-07": 4,
  "2025-12-14": 3,
  "2025-12-21": 1,
};

export default function DashboardPage() {
  return (
    <Container className="admin-dashboard">
      <h1 className="mb-4">
        Dashboard
      </h1>

      {/* KPIs */}
      <Row className="g-3 mb-4">
        <Col md={3}>
          <StatCard title="Today" value={mockDashboard.today} />
        </Col>
        <Col md={3}>
          <StatCard title="This Month" value={mockDashboard.month} />
        </Col>
        <Col md={3}>
          <StatCard title="This Year" value={mockDashboard.year} />
        </Col>
        <Col md={3}>
          <StatCard title="Total" value={mockDashboard.total} />
        </Col>
      </Row>

      {/* Aggregations */}
      <Row className="g-4">
        <Col md={6}>
          <Card className="admin-surface">
            <Card.Body>
              <Card.Title className="mb-3">Bookings by type</Card.Title>

              <ul className="list-unstyled mb-0">
                {Object.entries(mockDashboard.byType).map(([type, count]) => (
                  <li
                    key={type}
                    className="d-flex justify-content-between py-1"
                  >
                    <span>{type}</span>
                    <strong>{count}</strong>
                  </li>
                ))}
              </ul>
            </Card.Body>
          </Card>
        </Col>

        <Col md={6}>
          <Card className="admin-surface">
            <Card.Body>
              <Card.Title className="mb-3">Bookings per day</Card.Title>

              <BookingsCalendar
                year={2025}
                month={11}
                bookings={mockBookingsByDay}
              />
            </Card.Body>
          </Card>
        </Col>
      </Row>
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
