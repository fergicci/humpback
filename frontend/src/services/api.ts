import axios, { AxiosInstance, AxiosError } from "axios";

export const API_BASE = "/api/v1";

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type ApiResponse<T> =
  | { success: true; data: T }
  | { success: false; error: ApiError };

export interface ApiError {
  timestamp: string;
  code: number;
  message: string;
  details: string[];
}

export const api: AxiosInstance = axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  timeout: 15000,
  xsrfCookieName: "XSRF-TOKEN",
  xsrfHeaderName: "X-CSRF-Token",
});

export function handleError<T = unknown>(
  err: AxiosError<ApiResponse<T | ApiError>>
): ApiError {
  const resp = err.response?.data;

  if (resp && (resp as any).success === false && (resp as any).error) {
    return (resp as any).error as ApiError;
  }

  return {
    timestamp: new Date().toISOString(),
    code: err.response?.status ?? 0,
    message: err.message || "Network error",
    details: [],
  };
}
