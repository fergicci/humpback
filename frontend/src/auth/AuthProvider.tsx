import {
  ensureSession,
  fetchMe,
  loginWithTwoFactor,
  login,
  logout,
  type LoginResult,
  type User,
} from "@/services/authService";
import { createContext, useContext, useEffect, useState } from "react";

const AUTH_EVENT_KEY = "hb_auth_event";
const AUTH_STORAGE_KEY = "hb_auth";
const USER_STORAGE_KEY = "hb_user";

type AuthContext = {
  user: User | null;
  ready: boolean;
  signin: (u: string, p: string, remember: boolean) => Promise<LoginResult>;
  completeTwoFactorSignin: (
    challengeToken: string,
    code: string,
    remember: boolean
  ) => Promise<void>;
  refreshUser: () => Promise<User | null>;
  signout: () => Promise<void>;
};

const Context = createContext<AuthContext | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const me = await ensureSession();
        setUser(me);
      } finally {
        setReady(true);
      }
    })();
  }, []);

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {

      if (!e.key) return;

      if (e.key === AUTH_EVENT_KEY) {
        ensureSession()
          .then(setUser)
          .catch(() => setUser(null));
        return;
      }

      if (e.key === AUTH_STORAGE_KEY || e.key === USER_STORAGE_KEY) {
        const rawUser = localStorage.getItem(USER_STORAGE_KEY);
        setUser(rawUser ? (JSON.parse(rawUser) as User) : null);
      }
    };

    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  return (
    <Context.Provider
      value={{
        user,
        ready,
        signin: async (u, p, remember?: boolean) => {
          const result = await login(u, p, remember);
          if (result.status === "authenticated") {
            setUser(result.user);
            localStorage.setItem(
              AUTH_EVENT_KEY,
              JSON.stringify({ type: "login", ts: Date.now() })
            );
          }
          return result;
        },
        completeTwoFactorSignin: async (challengeToken, code, remember) => {
          const me = await loginWithTwoFactor(challengeToken, code, remember);
          setUser(me);
          localStorage.setItem(
            AUTH_EVENT_KEY,
            JSON.stringify({ type: "login", ts: Date.now() })
          );
        },
        refreshUser: async () => {
          const me = await fetchMe();
          setUser(me);
          return me;
        },
        signout: async () => {
          await logout();
          setUser(null);
          localStorage.setItem(
            AUTH_EVENT_KEY,
            JSON.stringify({ type: "logout", ts: Date.now() })
          );
        },
      }}
    >
      {children}
    </Context.Provider>
  );
}

export function useAuth() {
  const v = useContext(Context);
  if (!v) throw new Error("useAuth must be used inside <AuthProvider>");
  return v;
}
