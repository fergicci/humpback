import React, { useState } from "react";
import { Form, Button } from "react-bootstrap";
import { useTranslation } from "react-i18next";
import { I18N_NAMESPACES, TRANSLATION_KEYS } from "@/i18n/keys";

interface BookingFormProps {
  selectedDate: Date | null;
  selectedTime: string | null;
}

const BookingForm: React.FC<BookingFormProps> = ({
  selectedDate,
  selectedTime,
}) => {
  const { t } = useTranslation(I18N_NAMESPACES.BOOKING);
  const [selectedDuration, setSelectedDuration] = useState<number>(2);

  return (
    <>
      <h3 className="mb-3">{t(TRANSLATION_KEYS.BOOKING.LABELS.BOOKING_FORM)}</h3>
      <Form>
        <Form.Group className="mb-3">
          <Form.Label>{t(TRANSLATION_KEYS.BOOKING.FORM.NAME)}</Form.Label>
          <Form.Control type="text" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>{t(TRANSLATION_KEYS.BOOKING.FORM.EMAIL)}</Form.Label>
          <Form.Control type="email" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>{t(TRANSLATION_KEYS.BOOKING.FORM.PHONE)}</Form.Label>
          <Form.Control type="tel" />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>
            {t(TRANSLATION_KEYS.BOOKING.FORM.SELECTED_DATE)}
          </Form.Label>
          <Form.Control
            type="text"
            readOnly
            value={selectedDate?.toLocaleDateString() || ""}
          />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>
            {t(TRANSLATION_KEYS.BOOKING.FORM.SELECTED_TIME)}
          </Form.Label>

          <Form.Control type="text" readOnly value={selectedTime || ""} />
        </Form.Group>

        <Form.Group className="mb-3">
          <Form.Label>{t(TRANSLATION_KEYS.BOOKING.FORM.DURATION)}</Form.Label>
          <Form.Select
            value={selectedDuration}
            onChange={(e) => setSelectedDuration(Number(e.target.value))}
          >
            {[2, 3, 4, 5, 6, 7, 8].map((h) => (
              <option key={h} value={h}>
                {h} hour{h > 1 ? "s" : ""}
              </option>
            ))}
          </Form.Select>
        </Form.Group>

        <Button>{t(TRANSLATION_KEYS.BOOKING.FORM.SUBMIT)}</Button>
      </Form>
    </>
  );
};

export default BookingForm;
