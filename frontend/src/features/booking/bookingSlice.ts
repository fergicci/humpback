import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface Booking {
  name: string;
  email: string;
  date: string;
}

interface BookingState {
  bookings: Booking[];
}

const initialState: BookingState = {
  bookings: [],
};

const bookingSlice = createSlice({
  name: 'booking',
  initialState,
  reducers: {
    saveBooking: (state, action: PayloadAction<Booking>) => {
      state.bookings.push(action.payload);
    },
  },
});

export const { saveBooking } = bookingSlice.actions;
export default bookingSlice.reducer;