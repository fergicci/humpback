// services/contactService.ts
import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, PagedResponse, handleError } from "./api";

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
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function getContacts(
  lang: string,
  page = 1,
  size = 20
): Promise<PagedResponse<ContactItem>> {
  try {
    const { data: resp } = await api.get<ApiResponse<PagedResponse<ContactItem>>>("/contacts", {
      params: { page, size },
      headers: { "Accept-Language": lang },
    });
    if (resp.success) return resp.data;   // <- narrowed
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function getContactById(id: string, lang: string): Promise<ContactItem | null> {
  try {
    const { data: resp } = await api.get<ApiResponse<ContactItem>>(`/contacts/${id}`, {
      headers: { "Accept-Language": lang },
    });
    if (resp.success) return resp.data;   // <- narrowed
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
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
