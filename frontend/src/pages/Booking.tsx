import React, { useState } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import { Container, Row, Col, ListGroup } from "react-bootstrap";
import BookingForm from "@/components/BookingForm";
import { formatTime } from "@/utils/datetimeUtils";
import { getCalendarLocale } from "@/utils/langUtils";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

// Fake booking data
// const today = new Date();
// const yyyy = today.getFullYear();

const Booking: React.FC = () => {
  const { i18n } = useTranslation();
  const { t } = useTranslation(I18N_NAMESPACES.BOOKING);

  const [selectedDate, setSelectedDate] = useState<Date | null>(new Date());
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const formattedDate = selectedDate?.toLocaleDateString("en-CA") || "";
  const mockAvailability: Record<string, string[]> = {};
  const availableTimes: string[] = mockAvailability[formattedDate] || [];

  return (
    <Container className="my-5">
      <div className="alert alert-info text-center mb-4">
        {t(TRANSLATION_KEYS.BOOKING.LABELS.NOT_ACCEPTING)}
      </div>
      <Row>
        <Col md={6}>
          <h3 className="mb-3">
            {t(TRANSLATION_KEYS.BOOKING.LABELS.SELECT_DATE)}
          </h3>
          <Calendar
            onChange={(value) => {
              if (value instanceof Date) {
                setSelectedDate(value);
                setSelectedTime(null);
              }
            }}
            value={selectedDate}
            locale={getCalendarLocale(i18n.language)}
          />

          <h5 className="mt-4">
            {t(TRANSLATION_KEYS.BOOKING.LABELS.AVAILABLE_TIMES)}
          </h5>
          {availableTimes.length > 0 ? (
            <ListGroup>
              {availableTimes.map((time, index) => (
                <ListGroup.Item
                  key={index}
                  active={selectedTime === time}
                  action
                  onClick={() => setSelectedTime(time)}
                >
                  {formatTime(time)}
                </ListGroup.Item>
              ))}
            </ListGroup>
          ) : (
            <p className="text-muted">
              <span className="text-danger me-2">
                <i className="bi bi-exclamation-triangle-fill"></i>
              </span>
              {t(TRANSLATION_KEYS.BOOKING.WARNING_MESSAGES.NO_AVAILABLE_TIMES)}
            </p>
          )}
        </Col>

        <Col md={6}>
          <BookingForm
            selectedDate={selectedDate}
            selectedTime={selectedTime}
          />
        </Col>
      </Row>
    </Container>
  );
};

export default Booking;
