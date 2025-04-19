import React, { useState, useEffect } from "react";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import { Container, Row, Col, ListGroup } from "react-bootstrap";
import BookingForm from "@/components/BookingForm";
import { formatTime } from "@/utils/datetimeUtils";

import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

// Locale
const getLocaleCode = (lang: string) => {
  switch (lang.toLowerCase()) {
    case "pt":
    case "pt-br":
    case "pt-BR":
      return "pt";
    case "en":
    default:
      return "en-US";
  }
};

// Fake booking data
const today = new Date();
const yyyy = today.getFullYear();

const mockAvailability = {
  [`${yyyy}-04-21`]: ["10:00", "14:00", "16:00"],
  [`${yyyy}-04-22`]: ["10:00", "16:00"],
  [`${yyyy}-04-23`]: [],
  [`${yyyy}-04-24`]: ["9:00", "12:00", "15:00"],
};

const Booking: React.FC = () => {
  const { i18n } = useTranslation();
  const { t } = useTranslation(I18N_NAMESPACES.BOOKING);
  

  const [selectedDate, setSelectedDate] = useState<Date | null>(new Date());
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const formattedDate = selectedDate?.toLocaleDateString("en-CA") || "";
  const availableTimes = mockAvailability[formattedDate] || [];

  return (
    <Container className="my-5">
      <Row>
        <Col md={6}>
          <h3 className="mb-3">
            {t(TRANSLATION_KEYS.BOOKING.LABELS.SELECT_DATE)}
          </h3>
          <Calendar
            onChange={(date) => {
              setSelectedDate(date);
              setSelectedTime(null);
            }}
            value={selectedDate}
            locale={getLocaleCode(i18n.language)}
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
