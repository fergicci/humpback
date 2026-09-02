import type { AxiosError } from "axios";
import { api, ApiError, ApiResponse, handleError } from "./api";

const AUTH_STORAGE_KEY = "hb_auth";
const USER_STORAGE_KEY = "hb_user";
const AUTH_EVENT_KEY = "hb_auth_event";

export interface User {
  id: string;
  username: string;
  fullname: string;
  roles: string[];
  email: string;
  createdAt: string;
  lastLogin?: string;
  twoFactorEnabled?: boolean;
}

export interface RegisterRequest {
  username: string;
  fullname: string;
  email: string;
  password: string;
}

export type RegisterFormState = RegisterRequest & {
  confirmPassword: string;
};

export type LoginData = User & {
  token?: string;
  requiresTwoFactor?: boolean;
  twoFactorChallengeToken?: string;
};

export type LoginResult =
  | {
      status: "authenticated";
      user: User;
    }
  | {
      status: "two_factor_required";
      challengeToken: string;
      username: string;
    };

export type TwoFactorSetupResponse = {
  manualEntryKey: string;
  otpAuthUrl: string;
  qrCodeDataUri: string;
  twoFactorEnabled: boolean;
};

export type TwoFactorStatusResponse = {
  twoFactorEnabled: boolean;
};

export type ForgotPasswordChallengeResponse = {
  requiresTwoFactor: boolean;
  challengeToken?: string;
};

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
): Promise<LoginResult> {
  try {
    const { data } = await api.post<ApiResponse<LoginData>>("/auth/login", {
      username,
      password,
    });

    if (!data?.success) throw new Error("Login failed");

    if (data.data?.requiresTwoFactor) {
      const challengeToken = data.data?.twoFactorChallengeToken;
      if (!challengeToken) throw new Error("Missing 2FA challenge token");
      await doLocalLogout();
      return {
        status: "two_factor_required",
        challengeToken,
        username: data.data?.username ?? username,
      };
    }

    const me = await completeAuthenticatedLogin(data.data, remember);
    return { status: "authenticated", user: me };
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function loginWithTwoFactor(
  challengeToken: string,
  code: string,
  remember = false
): Promise<User> {
  try {
    const { data } = await api.post<ApiResponse<LoginData>>("/auth/login/2fa", {
      challengeToken,
      code,
    });

    if (!data?.success) throw new Error("2FA login failed");

    return completeAuthenticatedLogin(data.data, remember);
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function setupTwoFactor(): Promise<TwoFactorSetupResponse> {
  try {
    const { data } = await api.post<ApiResponse<TwoFactorSetupResponse>>("/auth/2fa/setup");
    if (!data?.success || !data.data) throw new Error("2FA setup failed");
    return data.data;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function enableTwoFactor(code: string): Promise<TwoFactorStatusResponse> {
  try {
    const { data } = await api.post<ApiResponse<TwoFactorStatusResponse>>("/auth/2fa/enable", {
      code,
    });
    if (!data?.success || !data.data) throw new Error("2FA enable failed");
    return data.data;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function disableTwoFactor(code: string): Promise<TwoFactorStatusResponse> {
  try {
    const { data } = await api.post<ApiResponse<TwoFactorStatusResponse>>("/auth/2fa/disable", {
      code,
    });
    if (!data?.success || !data.data) throw new Error("2FA disable failed");
    return data.data;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function requestForgotPassword(
  username: string
): Promise<ForgotPasswordChallengeResponse> {
  try {
    const { data } = await api.post<ApiResponse<ForgotPasswordChallengeResponse>>(
      "/auth/forgot-password/request",
      { username }
    );
    if (!data?.success || !data.data) throw new Error("Forgot password request failed");
    return data.data;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function resetForgotPassword(
  challengeToken: string,
  code: string,
  newPassword: string
): Promise<void> {
  try {
    const { data } = await api.post<ApiResponse<void>>("/auth/forgot-password/reset", {
      challengeToken,
      code,
      newPassword,
    });
    if (!data?.success) throw new Error("Forgot password reset failed");
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}

export async function changePasswordWithCurrent(
  username: string,
  oldPassword: string,
  newPassword: string
): Promise<void> {
  try {
    const { data } = await api.post<ApiResponse<void>>("/auth/change-password", {
      username,
      oldPassword,
      newPassword,
    });
    if (!data?.success) throw new Error("Password update failed");
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
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

async function completeAuthenticatedLogin(
  payload: LoginData | null | undefined,
  remember: boolean
): Promise<User> {
  const maybeToken = payload?.token;
  const token =
    typeof maybeToken === "string" && maybeToken.length > 0
      ? maybeToken
      : null;

  if (!token) throw new Error("Missing token");

  inMemoryAccessToken = token;

  if (remember && token) {
    saveTokenToStorage(token);
  } else {
    saveTokenToStorage(null);
  }

  const me = (await fetchMe()) ?? toUser(payload);
  sessionUser = me;

  if (remember) {
    saveUserToStorage(me);
  } else {
    saveUserToStorage(null);
  }

  return me;
}

function toUser(d: Partial<LoginData> | null | undefined): User {
  return {
    id: d?.id ?? "",
    username: d?.username ?? "",
    fullname: d?.fullname ?? "",
    roles: (d?.roles as string[]) ?? [],
    email: d?.email ?? "",
    createdAt: d?.createdAt ?? "",
    lastLogin: d?.lastLogin,
    twoFactorEnabled: !!d?.twoFactorEnabled,
  };
}


export async function registerUser(payload: RegisterRequest, lang: string): Promise<void> {
  try {
    const { data: resp } = await api.post<ApiResponse<void>>("/auth/register", payload, {
      headers: {
        "Content-Type": "application/json",
        "Accept-Language": lang,
      },
    });

    if (resp.success) return;
    throw resp.error;
  } catch (e) {
    throw handleError(e as AxiosError<ApiResponse<ApiError>>);
  }
}
