import { useMemo } from "react";
import "./BookingsCalendar.scss";

type Props = {
  year: number;
  month: number;
  bookings: Record<string, number>;
};

export default function BookingsCalendar({
  year,
  month,
  bookings,
}: Props) {
  const days = useMemo(() => {
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    const startWeekday = firstDay.getDay();
    const totalDays = lastDay.getDate();

    const cells: (Date | null)[] = [];

    for (let i = 0; i < startWeekday; i++) {
      cells.push(null);
    }

    for (let d = 1; d <= totalDays; d++) {
      cells.push(new Date(year, month, d));
    }

    return cells;
  }, [year, month]);

  return (
    <div className="bookings-calendar">
      <div className="calendar-grid">
        {["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"].map((d) => (
          <div key={d} className="calendar-header">
            {d}
          </div>
        ))}

        {days.map((date, i) => {
          if (!date) {
            return <div key={i} className="calendar-cell empty" />;
          }

          const key = date.toISOString().slice(0, 10);
          const count = bookings[key] ?? 0;

          return (
            <div
              key={i}
              className={`calendar-cell ${count > 0 ? "has-bookings" : ""}`}
            >
              <div className="day-number">{date.getDate()}</div>
              {count > 0 && (
                <div className="booking-count">{count}</div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}