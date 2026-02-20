import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Container, Modal } from "react-bootstrap";

import {
  getBookings,
  deleteBooking,
  markBookingPayed,
  markBookingUnpayed,
  updateBooking,
  type BookingItem,
} from "@/services/bookingService";

import type { ApiError } from "@/services/api";

type QuickRange = "ALL" | "TODAY" | "NEXT_2_DAYS" | "NEXT_10_DAYS";

type ConfirmDialogState = {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant: "danger" | "warning" | "primary";
  onConfirm: () => Promise<void>;
};

type EditBookingState = {
  name: string;
  email: string;
  phone: string;
  bookingAt: string;
  numberOfHours: number;
  type: string;
};

const BOOKING_TYPES = [
  "REHARSAL",
  "REHARSAL_RECORDING",
  "RECORDING",
  "MIXING",
  "MASTERING",
  "VIDEO_PRODUCTION",
] as const;

function toLocalDateTimeInput(value: string | null | undefined): string {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";

  const pad = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
    d.getHours()
  )}:${pad(d.getMinutes())}`;
}

function fromLocalDateTimeInput(value: string): string | null {
  if (!value) return null;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return null;
  return d.toISOString();
}

function deriveNumberOfHours(bookingAt: string, endAt: string): number {
  const start = new Date(bookingAt).getTime();
  const end = new Date(endAt).getTime();
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) return 2;
  return Math.max(2, Math.min(8, Math.round((end - start) / (1000 * 60 * 60))));
}

export default function BookingsPage() {
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(20);

  const [rows, setRows] = useState<BookingItem[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [formError, setFormError] = useState<ApiError | null>(null);
  const [actionBusy, setActionBusy] = useState<string | null>(null);
  const [confirmDialog, setConfirmDialog] =
    useState<ConfirmDialogState | null>(null);
  const [confirmBusy, setConfirmBusy] = useState(false);

  const [selected, setSelected] = useState<BookingItem | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [edit, setEdit] = useState<EditBookingState | null>(null);
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});

  const lang = useMemo(() => navigator.language || "en", []);

  const from = totalElements === 0 ? 0 : (page - 1) * size + 1;
  const to = totalElements === 0 ? 0 : Math.min(page * size, totalElements);

  const [range, setRange] = useState<QuickRange>("ALL");
  const [unpaidOnly, setUnpaidOnly] = useState(false);

  function startOfDay(d: Date) {
    const x = new Date(d);
    x.setHours(0, 0, 0, 0);
    return x;
  }

  function endOfDay(d: Date) {
    const x = new Date(d);
    x.setHours(23, 59, 59, 999);
    return x;
  }

  function addDays(d: Date, days: number) {
    const x = new Date(d);
    x.setDate(x.getDate() + days);
    return x;
  }

  const { fromDateIso, toDateIso } = useMemo(() => {
    const now = new Date();

    if (range === "ALL") {
      return { fromDateIso: undefined, toDateIso: undefined };
    }

    if (range === "TODAY") {
      return {
        fromDateIso: startOfDay(now).toISOString(),
        toDateIso: endOfDay(now).toISOString(),
      };
    }

    if (range === "NEXT_2_DAYS") {
      return {
        fromDateIso: startOfDay(now).toISOString(),
        toDateIso: endOfDay(addDays(now, 2)).toISOString(),
      };
    }

    return {
      fromDateIso: startOfDay(now).toISOString(),
      toDateIso: endOfDay(addDays(now, 10)).toISOString(),
    };
  }, [range]);

  const filters = useMemo(() => {
    const dsl: string[] = [];
    if (fromDateIso) dsl.push(`bookingAt:gte:${fromDateIso}`);
    if (toDateIso) dsl.push(`bookingAt:lte:${toDateIso}`);
    if (unpaidOnly) dsl.push("hasBeenPayed:eq:false");
    return { dsl };
  }, [fromDateIso, toDateIso, unpaidOnly]);

  const hasActiveFilters = range !== "ALL" || unpaidOnly;

  function clearFilters() {
    setPage(1);
    setRange("ALL");
    setUnpaidOnly(false);
  }

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getBookings(lang, page, size, filters);
        if (!cancelled) {
          setRows(res.content ?? []);
          setTotalPages(res.totalPages ?? 0);
          setTotalElements(res.totalElements ?? 0);
        }
      } catch (e: any) {
        if (!cancelled) {
          setRows([]);
          setTotalPages(0);
          setTotalElements(0);
          setError(e as ApiError);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [lang, page, size, filters]);

  function patchBookingInState(id: string, patch: Partial<BookingItem>) {
    setRows((prev) => prev.map((b) => (b.id === id ? { ...b, ...patch } : b)));
    setSelected((prev) =>
      prev && prev.id === id ? { ...prev, ...patch } : prev
    );
  }

  function showBookingScopedError(e: unknown, bookingId: string) {
    const err = e as ApiError;
    if (selected && selected.id === bookingId) {
      setFormError(err);
      return;
    }
    setError(err);
  }

  function openConfirmDialog(config: ConfirmDialogState) {
    setConfirmDialog(config);
  }

  async function runConfirmDialog() {
    if (!confirmDialog) return;

    try {
      setConfirmBusy(true);
      await confirmDialog.onConfirm();
      setConfirmDialog(null);
    } finally {
      setConfirmBusy(false);
    }
  }

  function openDrawer(b: BookingItem) {
    setSelected(b);
    setIsEditing(false);
    setEdit(null);
    setEditErrors({});
    setFormError(null);
  }

  function startEdit(b: BookingItem) {
    setSelected(b);
    setIsEditing(true);
    setEditErrors({});
    setFormError(null);

    setEdit({
      name: b.name ?? "",
      email: b.email ?? "",
      phone: b.phone ?? "",
      bookingAt: toLocalDateTimeInput(b.bookingAt),
      numberOfHours: deriveNumberOfHours(b.bookingAt, b.endAt),
      type: b.bookingType ?? "REHARSAL",
    });
  }

  function cancelEdit() {
    setIsEditing(false);
    setEdit(null);
    setEditErrors({});
    setFormError(null);
  }

  function handleDelete(booking: BookingItem) {
    openConfirmDialog({
      title: "Delete booking",
      message: `Are you sure you want to delete booking from "${booking.name}"?`,
      confirmLabel: "Delete",
      confirmVariant: "danger",
      onConfirm: async () => {
        try {
          setActionBusy(`${booking.id}:delete`);
          await deleteBooking(booking.id);
          setRows((prev) => prev.filter((r) => r.id !== booking.id));
          setSelected((prev) => (prev && prev.id === booking.id ? null : prev));
          setTotalElements((prev) => Math.max(0, prev - 1));
        } catch (e) {
          showBookingScopedError(e, booking.id);
        } finally {
          setActionBusy(null);
        }
      },
    });
  }

  async function togglePaidInView(b: BookingItem) {
    const desired = !b.hasBeenPayed;

    openConfirmDialog({
      title: desired ? "Mark booking as paid" : "Mark booking as unpaid",
      message: desired
        ? `Mark booking from "${b.name}" as paid?`
        : `Mark booking from "${b.name}" as unpaid?`,
      confirmLabel: desired ? "Mark paid" : "Mark unpaid",
      confirmVariant: "warning",
      onConfirm: async () => {
        try {
          setActionBusy(`${b.id}:payment`);
          if (desired) {
            await markBookingPayed(b.id);
          } else {
            await markBookingUnpayed(b.id);
          }
          patchBookingInState(b.id, { hasBeenPayed: desired });
        } catch (e) {
          showBookingScopedError(e, b.id);
        } finally {
          setActionBusy(null);
        }
      },
    });
  }

  async function handleSaveEdit() {
    if (!selected || !edit) return;

    setEditErrors({});
    setFormError(null);

    const nextErrors: Record<string, string> = {};
    if (edit.name.trim().length < 2) nextErrors.name = "Name is required.";
    if (
      edit.email.trim().length < 5 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(edit.email.trim())
    ) {
      nextErrors.email = "Email is invalid.";
    }
    if (edit.phone.trim().length < 6) nextErrors.phone = "Phone is required.";
    if (!edit.bookingAt) nextErrors.bookingAt = "Booking date/time is required.";
    if (edit.numberOfHours < 2 || edit.numberOfHours > 8) {
      nextErrors.numberOfHours = "Duration must be between 2 and 8 hours.";
    }
    if (!BOOKING_TYPES.includes(edit.type as (typeof BOOKING_TYPES)[number])) {
      nextErrors.type = "Booking type is invalid.";
    }

    if (Object.keys(nextErrors).length > 0) {
      setEditErrors(nextErrors);
      return;
    }

    const bookingAtIso = fromLocalDateTimeInput(edit.bookingAt);
    if (!bookingAtIso) {
      setEditErrors({ bookingAt: "Booking date/time is invalid." });
      return;
    }

    const payload = {
      name: edit.name.trim(),
      email: edit.email.trim(),
      phone: edit.phone.trim(),
      bookingAt: bookingAtIso,
      numberOfHours: edit.numberOfHours,
      type: edit.type,
    };

    try {
      setActionBusy(`${selected.id}:save`);
      const updated = await updateBooking(selected.id, payload, lang);

      patchBookingInState(selected.id, {
        name: updated.name,
        email: updated.email,
        phone: updated.phone,
        bookingAt: updated.bookingAt,
        endAt: updated.endAt,
        bookingType: updated.bookingType,
      });

      setIsEditing(false);
      setEdit(null);
      setEditErrors({});
      setFormError(null);
    } catch (e) {
      setFormError(e as ApiError);
    } finally {
      setActionBusy(null);
    }
  }

  return (
    <Container className="bookings-admin">
      <h1 className="mb-4">Bookings</h1>

      <div className="d-flex flex-wrap gap-2 mb-3 align-items-center">
        <div className="d-flex gap-2">
          <button
            type="button"
            className={`btn btn-sm ${
              range === "TODAY" ? "btn-primary" : "btn-outline-primary"
            }`}
            onClick={() => {
              setPage(1);
              setRange((r) => (r === "TODAY" ? "ALL" : "TODAY"));
            }}
          >
            Today
          </button>

          <button
            type="button"
            className={`btn btn-sm ${
              range === "NEXT_2_DAYS" ? "btn-primary" : "btn-outline-primary"
            }`}
            onClick={() => {
              setPage(1);
              setRange((r) => (r === "NEXT_2_DAYS" ? "ALL" : "NEXT_2_DAYS"));
            }}
          >
            Next 2 days
          </button>

          <button
            type="button"
            className={`btn btn-sm ${
              range === "NEXT_10_DAYS"
                ? "btn-primary"
                : "btn-outline-primary"
            }`}
            onClick={() => {
              setPage(1);
              setRange((r) => (r === "NEXT_10_DAYS" ? "ALL" : "NEXT_10_DAYS"));
            }}
          >
            Next 10 days
          </button>
        </div>

        <button
          type="button"
          className={`btn btn-sm ${
            unpaidOnly ? "btn-warning" : "btn-outline-warning"
          }`}
          onClick={() => {
            setPage(1);
            setUnpaidOnly((v) => !v);
          }}
        >
          Unpaid
        </button>

        {hasActiveFilters && (
          <button
            type="button"
            className="btn btn-sm btn-outline-secondary"
            onClick={clearFilters}
            title="Clear filters"
            aria-label="Clear filters"
          >
            Clear filters
          </button>
        )}

        <div className="flex-grow-1" />

        <div className="input-group input-group-sm" style={{ width: 160 }}>
          <span className="input-group-text">Page size</span>
          <select
            className="form-select"
            value={size}
            onChange={(e) => {
              setPage(1);
              setSize(parseInt(e.target.value, 10));
            }}
          >
            {[10, 20, 50, 100].map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
      </div>

      {error && (
        <Alert variant="danger" dismissible onClose={() => setError(null)}>
          <div className="fw-semibold">
            Error {error.code}: {error.message}
          </div>
          {Array.isArray(error.details) && error.details.length > 0 && (
            <ul className="mb-0">
              {error.details.map((d, i) => (
                <li key={i}>{d}</li>
              ))}
            </ul>
          )}
        </Alert>
      )}

      <div className="table-responsive">
        <table className="table table-striped align-middle">
          <thead>
            <tr>
              <th>Date</th>
              <th>Duration</th>
              <th>Type</th>
              <th>Name</th>
              <th>Email</th>
              <th className="text-center">Paid</th>
              <th className="text-end">Actions</th>
            </tr>
          </thead>

          <tbody>
            {loading && (
              <tr>
                <td colSpan={7} className="text-center py-4">
                  <div className="spinner-border" aria-label="Loading" />
                </td>
              </tr>
            )}

            {!loading && rows.length === 0 && !error && (
              <tr>
                <td colSpan={7} className="text-center text-muted py-4">
                  No bookings found.
                </td>
              </tr>
            )}

            {!loading &&
              rows.map((b) => (
                <tr key={b.id}>
                  <td>{new Date(b.bookingAt).toLocaleString()}</td>
                  <td>{deriveNumberOfHours(b.bookingAt, b.endAt)} h</td>
                  <td>{b.bookingType}</td>
                  <td className="text-truncate">{b.name}</td>
                  <td>
                    <a href={`mailto:${b.email}`}>{b.email}</a>
                  </td>
                  <td className="text-center">
                    {b.hasBeenPayed ? (
                      <span className="badge bg-success">Paid</span>
                    ) : (
                      <span className="badge bg-warning text-dark">Unpaid</span>
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group btn-group-sm">
                      <button
                        className="btn btn-outline-primary btn-icon"
                        title="View"
                        aria-label="View"
                        onClick={() => openDrawer(b)}
                      >
                        <i className="bi bi-eye" aria-hidden="true" />
                        <span className="visually-hidden">View</span>
                      </button>

                      <button
                        className="btn btn-outline-secondary"
                        title="Edit"
                        aria-label="Edit"
                        onClick={() => startEdit(b)}
                      >
                        <i className="bi bi-pencil" aria-hidden="true" />
                        <span className="visually-hidden">Edit</span>
                      </button>

                      <button
                        className="btn btn-outline-danger"
                        onClick={() => handleDelete(b)}
                        title="Delete"
                        aria-label="Delete"
                        disabled={actionBusy === `${b.id}:delete`}
                      >
                        <i className="bi bi-trash" aria-hidden="true" />
                        <span className="visually-hidden">Delete</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      <div className="row align-items-center mt-3">
        <div className="col">
          <div className="small text-muted">
            {loading ? (
              ""
            ) : (
              <>
                Showing <strong>{from}</strong>–<strong>{to}</strong> of{" "}
                <strong>{totalElements}</strong> bookings • Page{" "}
                <strong>{page}</strong> of{" "}
                <strong>{Math.max(1, totalPages)}</strong>
              </>
            )}
          </div>
        </div>

        <div className="col-auto">
          <div className="btn-group" role="group" aria-label="Pagination">
            <button
              className="btn btn-outline-secondary"
              disabled={page <= 1 || loading}
              onClick={() => setPage((p) => Math.max(1, p - 1))}
            >
              ‹ Prev
            </button>
            <button
              className="btn btn-outline-secondary"
              disabled={page >= totalPages || loading}
              onClick={() => setPage((p) => (p < totalPages ? p + 1 : p))}
            >
              Next ›
            </button>
          </div>
        </div>
      </div>

      {selected && (
        <div
          className="position-fixed top-0 end-0 vh-100 bg-body border-start shadow"
          style={{ width: "min(560px, 100%)", zIndex: 1050 }}
          role="dialog"
          aria-modal="true"
        >
          <div className="d-flex align-items-center justify-content-between p-3 border-bottom">
            <h5 className="mb-0">{isEditing ? "Edit booking" : "Booking details"}</h5>
            <button
              className="btn btn-sm btn-outline-secondary"
              onClick={() => {
                setSelected(null);
                cancelEdit();
              }}
            >
              Close
            </button>
          </div>

          <div className="p-3">
            {formError && (
              <Alert variant="danger" dismissible onClose={() => setFormError(null)}>
                <div className="fw-semibold">
                  Error {formError.code}: {formError.message}
                </div>
                {Array.isArray(formError.details) && formError.details.length > 0 && (
                  <ul className="mb-0">
                    {formError.details.map((d, i) => (
                      <li key={i}>{d}</li>
                    ))}
                  </ul>
                )}
              </Alert>
            )}

            <div className="mb-3">
              <div className="text-muted small">Name</div>
              {isEditing ? (
                <>
                  <input
                    className={`form-control ${editErrors.name ? "is-invalid" : ""}`}
                    value={edit?.name ?? ""}
                    onChange={(e) => setEdit((s) => (s ? { ...s, name: e.target.value } : s))}
                  />
                  {editErrors.name && <div className="invalid-feedback d-block">{editErrors.name}</div>}
                </>
              ) : (
                <div>{selected.name}</div>
              )}
            </div>

            <div className="mb-3">
              <div className="text-muted small">Email</div>
              {isEditing ? (
                <>
                  <input
                    className={`form-control ${editErrors.email ? "is-invalid" : ""}`}
                    value={edit?.email ?? ""}
                    onChange={(e) => setEdit((s) => (s ? { ...s, email: e.target.value } : s))}
                  />
                  {editErrors.email && <div className="invalid-feedback d-block">{editErrors.email}</div>}
                </>
              ) : (
                <div>
                  <a href={`mailto:${selected.email}`}>{selected.email}</a>
                </div>
              )}
            </div>

            <div className="mb-3">
              <div className="text-muted small">Phone</div>
              {isEditing ? (
                <>
                  <input
                    className={`form-control ${editErrors.phone ? "is-invalid" : ""}`}
                    value={edit?.phone ?? ""}
                    onChange={(e) => setEdit((s) => (s ? { ...s, phone: e.target.value } : s))}
                  />
                  {editErrors.phone && <div className="invalid-feedback d-block">{editErrors.phone}</div>}
                </>
              ) : (
                <div>{selected.phone}</div>
              )}
            </div>

            <div className="mb-3">
              <div className="text-muted small">Booking date/time</div>
              {isEditing ? (
                <>
                  <input
                    type="datetime-local"
                    className={`form-control ${editErrors.bookingAt ? "is-invalid" : ""}`}
                    value={edit?.bookingAt ?? ""}
                    onChange={(e) => setEdit((s) => (s ? { ...s, bookingAt: e.target.value } : s))}
                  />
                  {editErrors.bookingAt && (
                    <div className="invalid-feedback d-block">{editErrors.bookingAt}</div>
                  )}
                </>
              ) : (
                <div>{new Date(selected.bookingAt).toLocaleString()}</div>
              )}
            </div>

            <div className="mb-3">
              <div className="text-muted small">Duration (hours)</div>
              {isEditing ? (
                <>
                  <input
                    type="number"
                    min={2}
                    max={8}
                    className={`form-control ${editErrors.numberOfHours ? "is-invalid" : ""}`}
                    value={edit?.numberOfHours ?? 2}
                    onChange={(e) =>
                      setEdit((s) =>
                        s
                          ? {
                              ...s,
                              numberOfHours: Number.parseInt(e.target.value || "2", 10),
                            }
                          : s
                      )
                    }
                  />
                  {editErrors.numberOfHours && (
                    <div className="invalid-feedback d-block">{editErrors.numberOfHours}</div>
                  )}
                </>
              ) : (
                <div>{deriveNumberOfHours(selected.bookingAt, selected.endAt)} h</div>
              )}
            </div>

            <div className="mb-3">
              <div className="text-muted small">Booking type</div>
              {isEditing ? (
                <>
                  <select
                    className={`form-select ${editErrors.type ? "is-invalid" : ""}`}
                    value={edit?.type ?? "REHARSAL"}
                    onChange={(e) => setEdit((s) => (s ? { ...s, type: e.target.value } : s))}
                  >
                    {BOOKING_TYPES.map((type) => (
                      <option key={type} value={type}>
                        {type}
                      </option>
                    ))}
                  </select>
                  {editErrors.type && <div className="invalid-feedback d-block">{editErrors.type}</div>}
                </>
              ) : (
                <div>{selected.bookingType}</div>
              )}
            </div>

            <div className="mb-4">
              <div className="text-muted small">Payment status</div>
              <div className="mt-1">
                {selected.hasBeenPayed ? (
                  <span className="badge bg-success">Paid</span>
                ) : (
                  <span className="badge bg-warning text-dark">Unpaid</span>
                )}
              </div>
            </div>

            {!isEditing ? (
              <div className="d-flex flex-wrap gap-2">
                <button
                  className={`btn ${selected.hasBeenPayed ? "btn-warning" : "btn-success"}`}
                  onClick={() => togglePaidInView(selected)}
                  disabled={actionBusy === `${selected.id}:payment`}
                >
                  {selected.hasBeenPayed ? "Mark as unpaid" : "Mark as paid"}
                </button>

                <button
                  className="btn btn-outline-secondary"
                  onClick={() => startEdit(selected)}
                >
                  Edit
                </button>

                <button
                  className="btn btn-danger"
                  onClick={() => handleDelete(selected)}
                  disabled={actionBusy === `${selected.id}:delete`}
                >
                  Delete
                </button>
              </div>
            ) : (
              <div className="d-flex flex-wrap gap-2">
                <button
                  className="btn btn-primary"
                  onClick={handleSaveEdit}
                  disabled={!edit || actionBusy === `${selected.id}:save`}
                >
                  Save
                </button>
                <button className="btn btn-outline-secondary" onClick={cancelEdit}>
                  Cancel
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      <Modal
        show={!!confirmDialog}
        onHide={() => !confirmBusy && setConfirmDialog(null)}
        centered
      >
        <Modal.Header closeButton={!confirmBusy}>
          <Modal.Title>{confirmDialog?.title}</Modal.Title>
        </Modal.Header>
        <Modal.Body>{confirmDialog?.message}</Modal.Body>
        <Modal.Footer>
          <Button
            variant="secondary"
            onClick={() => setConfirmDialog(null)}
            disabled={confirmBusy}
          >
            Cancel
          </Button>
          <Button
            variant={confirmDialog?.confirmVariant ?? "primary"}
            onClick={runConfirmDialog}
            disabled={confirmBusy}
          >
            {confirmBusy ? "Please wait..." : confirmDialog?.confirmLabel}
          </Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
}
