import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import { saveBooking } from '../features/booking/bookingSlice';

function BookingForm() {
  const dispatch = useDispatch();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [date, setDate] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    dispatch(saveBooking({ name, email, date }));
    alert('Booking submitted!');
    setName('');
    setEmail('');
    setDate('');
  };

  return (
    <form onSubmit={handleSubmit} className="card p-4 shadow-sm">
      <div className="mb-3">
        <label htmlFor="name" className="form-label">Your Name</label>
        <input type="text" id="name" className="form-control" value={name} onChange={(e) => setName(e.target.value)} required />
      </div>

      <div className="mb-3">
        <label htmlFor="email" className="form-label">Email Address</label>
        <input type="email" id="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
      </div>

      <div className="mb-3">
        <label htmlFor="date" className="form-label">Preferred Date</label>
        <input type="date" id="date" className="form-control" value={date} onChange={(e) => setDate(e.target.value)} required />
      </div>

      <button type="submit" className="btn btn-primary w-100">Submit Booking</button>
    </form>
  );
}

export default BookingForm;