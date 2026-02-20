// services/bookingService.ts
import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, PagedResponse, handleError } from "./api";

export interface BookingItem {
  id: string;
  name: string;
  email: string;
  phone: string;
  bookingAt: string;
  endAt: string;
  bookingType: string;
  hasBeenPayed: boolean;
}

export interface BookingRequest {
  name: string;
  email: string;
  phone: string;
  bookingAt: string;
  numberOfHours: number;
  type: string;
}

export interface BookingTypeOption {
  value: string;
  label: string;
}

export interface PublicBookingItem {
  bookingAt: string;
  endAt: string;
}

export async function getPublicBookingsBetween(
  lang: string,
  from: string,
  to: string
): Promise<PublicBookingItem[]> {
  try {
    const { data: resp } = await api.get<ApiResponse<PublicBookingItem[]>>(
      "/bookings/on",
      {
        params: { from, to },
        headers: { "Accept-Language": lang },
      }
    );

    if (resp.success) return resp.data ?? [];
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function createBooking(payload: BookingRequest, lang: string): Promise<void> {
  try {
    await api.post("/bookings", payload, {
      headers: { "Content-Type": "application/json", "Accept-Language": lang },
    });
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function getBookingTypes(lang: string): Promise<BookingTypeOption[]> {
  try {
    const { data: resp } = await api.get<ApiResponse<BookingTypeOption[]>>("/bookings/types", {
      headers: { "Accept-Language": lang },
    });

    if (resp.success) return resp.data ?? [];
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

type BookingFilters = {
  dsl?: string[];
};

export async function getBookings(
  lang: string,
  page = 1,
  size = 20,
  filters: BookingFilters = {}
): Promise<PagedResponse<BookingItem>> {
  try {
    const { dsl = [] } = filters;

    const params = new URLSearchParams();
    params.set("page", String(page));
    params.set("size", String(size));
    dsl.forEach((filter) => params.append("dsl", filter));

    const { data: resp } = await api.get<ApiResponse<PagedResponse<BookingItem>>>(
      "/bookings",
      {
        params,
        headers: { "Accept-Language": lang },
      }
    );

    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function getBookingById(id: string, lang: string): Promise<BookingItem | null> {
  try {
    const { data: resp } = await api.get<ApiResponse<BookingItem>>(`/bookings/${id}`, {
      headers: { "Accept-Language": lang },
    });
    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function updateBooking(
  id: string,
  payload: BookingRequest,
  lang: string
): Promise<BookingItem> {
  try {
    const { data: resp } = await api.put<ApiResponse<BookingItem>>(`/bookings/${id}`, payload, {
      headers: { "Content-Type": "application/json", "Accept-Language": lang },
    });
    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function setBookingPayment(id: string, value: boolean): Promise<void> {
  try {
    const { data: resp } = await api.patch<ApiResponse<void>>(`/bookings/${id}/payment/${value}`);
    if (resp.success) return;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function markBookingPayed(id: string): Promise<void> {
  return setBookingPayment(id, true);
}

export async function markBookingUnpayed(id: string): Promise<void> {
  return setBookingPayment(id, false);
}

export async function deleteBooking(id: string): Promise<void> {
  try {
    await api.delete(`/bookings/${id}`);
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}
