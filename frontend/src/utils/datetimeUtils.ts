
export function formatTime(time24h: string): string {
  const [hours, minutes] = time24h.split(":").map(Number);
  return new Date(0, 0, 0, hours, minutes).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}
