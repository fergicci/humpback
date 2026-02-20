import { useEffect, useState } from "react";

type AutoRefreshOptions = {
  enabled?: boolean;
  pauseWhenHidden?: boolean;
};

export function useAutoRefresh(
  intervalMs = 60_000,
  options: AutoRefreshOptions = {}
): number {
  const { enabled = true, pauseWhenHidden = true } = options;
  const [tick, setTick] = useState(0);

  useEffect(() => {
    if (!enabled || intervalMs <= 0) {
      return;
    }

    let intervalId: number | null = null;

    const stopInterval = () => {
      if (intervalId !== null) {
        window.clearInterval(intervalId);
        intervalId = null;
      }
    };

    const startInterval = () => {
      if (intervalId === null) {
        intervalId = window.setInterval(() => {
          setTick((prev) => prev + 1);
        }, intervalMs);
      }
    };

    if (pauseWhenHidden) {
      const handleVisibilityChange = () => {
        if (document.hidden) {
          stopInterval();
          return;
        }
        setTick((prev) => prev + 1);
        startInterval();
      };

      document.addEventListener("visibilitychange", handleVisibilityChange);

      if (!document.hidden) {
        startInterval();
      }

      return () => {
        document.removeEventListener("visibilitychange", handleVisibilityChange);
        stopInterval();
      };
    }

    startInterval();

    return () => {
      stopInterval();
    };
  }, [enabled, intervalMs, pauseWhenHidden]);

  return tick;
}
