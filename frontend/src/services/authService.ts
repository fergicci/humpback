import type { AxiosError } from "axios";
import { api, ApiResponse } from "./api";

const AUTH_STORAGE_KEY = "hb_auth";
const USER_STORAGE_KEY = "hb_user";
const AUTH_EVENT_KEY = "hb_auth_event"; // new

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

function broadcastAuthChange(type: "logout" | "login") {
  try {
    localStorage.setItem(
      AUTH_EVENT_KEY,
      JSON.stringify({ type, ts: Date.now() })
    );
  } catch {}
}

function loadTokenFromStorage(): string | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as { token?: string } | string;
    if (typeof parsed === "string") return parsed;
    return typeof parsed?.token === "string" && parsed.token.length > 0
      ? parsed.token
      : null;
  } catch {
    return null;
  }
}

function saveTokenToStorage(token: string | null) {
  try {
    if (token)
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify({ token }));
    else localStorage.removeItem(AUTH_STORAGE_KEY);
  } catch {}
}

function loadUserFromStorage(): User | null {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

function saveUserToStorage(u: User | null) {
  try {
    if (u) localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(u));
    else localStorage.removeItem(USER_STORAGE_KEY);
  } catch {}
}

let sessionUser: User | null = loadUserFromStorage();
let inMemoryAccessToken: string | null = loadTokenFromStorage();

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
    const url = (err.config?.url || "") as string;
    const isAuthRoute =
      url.includes("/auth/login") ||
      url.includes("/auth/refresh") ||
      url.includes("/auth/logout");

    if (status === 401 && !isAuthRoute) {
      await doLocalLogout();
      broadcastAuthChange("logout");

      if (!window.location.pathname.startsWith("/login")) {
        window.location.replace("/login");
      }
    }
    return Promise.reject(err);
  }
);

export async function fetchMe(): Promise<User | null> {
  try {
    const { data: resp } = await api.get<ApiResponse<User>>("/auth/me");
    sessionUser = resp.success ? resp.data : null;
    saveUserToStorage(sessionUser);
    return sessionUser;
  } catch {
    sessionUser = null;
    saveUserToStorage(null);
    return null;
  }
}

export async function login(
  username: string,
  password: string,
  remember = false
): Promise<User> {
  const { data } = await api.post<ApiResponse<LoginData>>("/auth/login", {
    username,
    password,
  });

  if (!data?.success) throw new Error("Login failed");

  const maybeToken = data.data?.token;
  const token =
    typeof maybeToken === "string" && maybeToken.length > 0 ? maybeToken : null;

  inMemoryAccessToken = token;

  if (remember && token) {
    saveTokenToStorage(token);
  } else {
    saveTokenToStorage(null);
  }

  const me = (await fetchMe()) ?? toUser(data.data);
  sessionUser = me;

  if (remember) {
    saveUserToStorage(me);
  } else {
    saveUserToStorage(null);
  }

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

  if (inMemoryAccessToken) {
    const me = await fetchMe();
    if (me) return me;
  }
  return null;
}

export function getUser(): User | null {
  return sessionUser;
}

export function isAuthenticated(): boolean {
  return !!inMemoryAccessToken && !!sessionUser;
}

async function doLocalLogout(): Promise<void> {
  sessionUser = null;
  inMemoryAccessToken = null;
  saveTokenToStorage(null);
  saveUserToStorage(null);
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
