// services/contactService.ts
import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, PagedResponse } from "./api";

export interface ContactItem {
  id: string;
  name: string;
  email: string;
  message: string;
  createdAt: string;
  read: boolean;
}

export interface ContactRequest {
  name: string;
  email: string;
  message: string;
}

export async function sendContactMessage(payload: ContactRequest, lang: string): Promise<void> {
  try {
    await api.post("/contacts", payload, {
      headers: { "Content-Type": "application/json", "Accept-Language": lang },
    });
  } catch (e) {
    const err = e as AxiosError<ApiError>;
    throw err.response?.data ?? fallbackApiError(err);
  }
}

export async function getContacts(
  lang: string,
  page = 1,
  size = 20
): Promise<PagedResponse<ContactItem>> {
  try {
    const { data } = await api.get<ApiResponse<PagedResponse<ContactItem>>>("/contacts", {
      params: { page, size },
      headers: { "Accept-Language": lang },
    });
    return data.data;
  } catch (e) {
    const err = e as AxiosError<ApiError>;
    throw err.response?.data ?? fallbackApiError(err);
  }
}

export async function getContactById(id: string, lang: string): Promise<ContactItem | null> {
  try {
    const { data } = await api.get<ApiResponse<ContactItem>>(`/contacts/${id}`, {
      headers: { "Accept-Language": lang },
    });
    return data.data;
  } catch {
    return null;
  }
}

export async function setContactRead(id: string, value: boolean): Promise<void> {
  await api.patch(`/contacts/${id}/read/${value}`);
}

export async function markContactRead(id: string): Promise<void> {
  return setContactRead(id, true);
}

export async function markContactUnread(id: string): Promise<void> {
  return setContactRead(id, false);
}

export async function deleteContact(id: string): Promise<void> {
  await api.delete(`/contacts/${id}`);
}

function fallbackApiError(err: AxiosError<ApiError>): ApiError {
  return {
    timestamp: new Date().toISOString(),
    code: err.response?.status ?? 0,
    message: err.message || "Network error",
    details: [],
  };
}