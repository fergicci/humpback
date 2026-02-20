import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, handleError } from "./api";

export interface DashboardData {
  generatedAt: string;
  calendarYear: number;
  calendarMonth: number;
  counters: Record<string, number>;
  unreadContacts: number;
  byType: Record<string, number>;
  bookingsByDay: Record<string, number>;
}

export async function getDashboard(): Promise<DashboardData> {
  try {
    const { data: resp } = await api.get<ApiResponse<DashboardData>>("/dashboard");

    if (resp.success) {
      return resp.data;
    }

    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}
