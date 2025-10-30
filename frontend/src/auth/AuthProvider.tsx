import { createContext, useContext, useEffect, useState } from "react";
import { ensureSession, login, logout, type User } from "@/services/authService";

type AuthContext = {
  user: User | null;
  signin: (u: string, p: string) => Promise<void>;
  signout: () => Promise<void>;
  ready: boolean;
};

const AuthContext = createContext<AuthContext | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    ensureSession().then(setUser).finally(() => setReady(true));
  }, []);

  return (
    <AuthContext.Provider
      value={{
        user,
        ready,
        signin: async (u, p) => {
          const me = await login(u, p);
          setUser(me);
        },
        signout: async () => {
          await logout();
          setUser(null);
        },
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const v = useContext(AuthContext);
  if (!v) throw new Error("useAuth must be used inside <AuthProvider>");
  return v;
}
