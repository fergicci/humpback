import type { AxiosError } from "axios";
import { api, ApiResponse } from "./api";

export interface User {
  id: string;
  username: string;
  fullName: string;
  roles: string[];
  email: string;
  createdAt: string;
  lastLogin?: string;
}

export type LoginData = User & { token?: string };

let sessionUser: User | null = null;
let inMemoryAccessToken: string | null = null;
let refreshPromise: Promise<boolean> | null = null;

api.interceptors.request.use((config) => {
  if (inMemoryAccessToken) {
    config.headers = config.headers ?? {};
    (config.headers as any).Authorization = `Bearer ${inMemoryAccessToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  async (err: AxiosError) => {
    const status = err.response?.status;
    const cfg: any = err.config || {};
    const url: string = (cfg.url || "") as string;

    const isAuthRoute =
      url.includes("/auth/login") ||
      url.includes("/auth/refresh") ||
      url.includes("/auth/logout");

    if (status === 401 && !cfg.__retried && !isAuthRoute) {
      cfg.__retried = true;
      const ok = await refreshSessionOnce();
      if (ok) return api(cfg);
    }
    return Promise.reject(err);
  }
);

async function refreshSessionOnce(): Promise<boolean> {
  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const { data } = await api.post<ApiResponse<Partial<LoginData>>>("/auth/refresh");

        const maybeToken = (data?.data as any)?.token;
        inMemoryAccessToken =
          typeof maybeToken === "string" && maybeToken.length > 0 ? maybeToken : null;

        await fetchMe().catch(() => {});
        return true;
      } catch {
        await doLocalLogout();
        return false;
      } finally {
        refreshPromise = null;
      }
    })();
  }
  return refreshPromise;
}

export async function fetchMe(): Promise<User | null> {
  try {
    const { data } = await api.get<ApiResponse<User>>("/auth/me");
    sessionUser = data?.data ?? null;
    return sessionUser;
  } catch {
    sessionUser = null;
    return null;
  }
}

export async function login(username: string, password: string): Promise<User> {
  const { data } = await api.post<ApiResponse<LoginData>>("/auth/login", { username, password });

  if (!data?.success) {
    throw new Error("Login failed");
  }

  const maybeToken = data.data?.token;
  inMemoryAccessToken =
    typeof maybeToken === "string" && maybeToken.length > 0 ? maybeToken : null;

  const me = (await fetchMe()) ?? toUser(data.data);
  sessionUser = me;
  return me;
}

export async function logout(): Promise<void> {
  try {
    await api.post("/auth/logout");
  } finally {
    await doLocalLogout();
  }
}

export async function ensureSession(): Promise<User | null> {
  if (sessionUser) return sessionUser;
  return await fetchMe();
}

export function getUser(): User | null {
  return sessionUser;
}

export function isAuthenticated(): boolean {
  return !!sessionUser;
}

async function doLocalLogout(): Promise<void> {
  sessionUser = null;
  inMemoryAccessToken = null;
}

function toUser(d: Partial<LoginData> | null | undefined): User {
  return {
    id: d?.id ?? "",
    username: d?.username ?? "",
    fullName: d?.fullName ?? "",
    roles: (d?.roles as string[]) ?? [],
    email: d?.email ?? "",
    createdAt: d?.createdAt ?? "",
    lastLogin: d?.lastLogin,
  };
}
