// services/contactService.ts
import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, PagedResponse, handleError } from "./api";

export interface UserItem {
  id: string;
  username: string;
  fullname: string;
  email: string;
  createdAt: string;
  lastLoginAt: string;
  passwordExpiredAt: string;
  disabled: boolean;
  accountLocked: boolean;
  twoFactorEnabled: boolean;
  roles: string[];
}

export interface UserRequest {
  fullname: string;
  email: string;
  disabled: boolean;
  accountLocked: boolean;
  roles: string[];
}

export type RegisterRequest = {
  username: string;
  fullname: string;
  email: string;
  password: string;
};

export async function getUsers(
  lang: string,
  page = 1,
  size = 20
): Promise<PagedResponse<UserItem>> {
  try {
    const { data: resp } = await api.get<ApiResponse<PagedResponse<UserItem>>>("/users", {
      params: { page, size },
      headers: { "Accept-Language": lang },
    });
    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function getUserById(id: string, lang: string): Promise<UserItem | null> {
  try {
    const { data: resp } = await api.get<ApiResponse<UserItem>>(`/users/${id}`, {
      headers: { "Accept-Language": lang },
    });
    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function updateUser(id: string, userData: UserRequest): Promise<UserItem> {
  try {
    const { data: resp } = await api.put<ApiResponse<UserItem>>(`/users/${id}`, userData);
    if (resp.success) return resp.data;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function deleteUser(id: string): Promise<void> {
  try {
    await api.delete(`/users/${id}`);
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function setUserDisabled(id: string, desiredDisable: boolean): Promise<void> {
  try {
    const { data: resp } = await api.patch<ApiResponse<void>>(`/users/${id}/disable/${desiredDisable}`);
    if (resp.success) return;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function setUserLocked(id: string, desiredLock: boolean): Promise<void> {
  try {
    const { data: resp } = await api.patch<ApiResponse<void>>(`/users/${id}/lock/${desiredLock}`);
    if (resp.success) return;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}
