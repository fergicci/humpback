// src/pages/ContactsPage.tsx
import { useEffect, useMemo, useState } from "react";
import { Container } from "react-bootstrap";

import {
  getContacts,
  markContactRead,
  markContactUnread,
  type ContactItem,
  deleteContact,
} from "@/services/contactService";
import type { ApiError } from "@/services/api";

export default function ContactsPage() {
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(20);
  const [rows, setRows] = useState<ContactItem[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const from = totalElements === 0 ? 0 : (page - 1) * size + 1;
  const to = totalElements === 0 ? 0 : Math.min(page * size, totalElements);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);
  const [selected, setSelected] = useState<ContactItem | null>(null);

  const lang = useMemo(() => navigator.language || "en", []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getContacts(lang, page, size);
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
  }, [lang, page, size]);

  async function handleMarkRead(id: string) {
    try {
      await markContactRead(id);
      setRows((prev) =>
        prev.map((r) => (r.id === id ? { ...r, read: true } : r))
      );
      setSelected((prev) =>
        prev && prev.id === id ? { ...prev, read: true } : prev
      );
    } catch (e) {
      console.error("Failed to mark as read:", e);
    }
  }

  async function handleMarkUnread(id: string) {
    try {
      await markContactUnread(id);
      setRows((prev) =>
        prev.map((r) => (r.id === id ? { ...r, read: false } : r))
      );
      setSelected((prev) =>
        prev && prev.id === id ? { ...prev, read: false } : prev
      );
    } catch (e) {
      console.error("Failed to mark as read:", e);
    }
  }

  async function handleDelete(id: string) {
    const ok = window.confirm("Are you sure you want to delete this contact?");
    if (!ok) return;

    try {
      await deleteContact(id);

      setRows((prev) => prev.filter((r) => r.id !== id));
      setSelected((prev) => (prev && prev.id === id ? null : prev));
      setTotalElements((prev) => (prev > 0 ? prev - 1 : 0));
    } catch (e) {
      console.error("Failed to delete contact:", e);
    }
  }
  return (
    <Container className="my-5 contacts-admin">
      <h2 className="mb-4">Contacts</h2>

      <div className="d-flex mb-3">
        <div className="ms-auto">
          <div className="input-group input-group-sm" style={{ width: 160 }}>
            <span className="input-group-text">Page size</span>
            <select
              className="form-select"
              value={size}
              onChange={(e) => {
                setPage(1);
                setSize(parseInt(e.target.value, 10));
              }}
              aria-label="Select page size"
            >
              {[10, 20, 50, 100].map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-danger" role="alert">
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
        </div>
      )}

      <div className="table-responsive">
        <table className="table table-striped align-middle">
          <thead>
            <tr>
              <th style={{ width: "18rem" }}>Name</th>
              <th style={{ width: "22rem" }}>Email</th>
              <th>Message (preview)</th>
              <th style={{ width: "14rem" }}>Created</th>
              <th style={{ width: "8rem" }}>Status</th>
              <th style={{ width: "12rem" }} className="text-end">
                Actions
              </th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={6} className="text-center py-4">
                  <div
                    className="spinner-border"
                    role="status"
                    aria-label="Loading"
                  />
                </td>
              </tr>
            )}

            {!loading && rows.length === 0 && !error && (
              <tr>
                <td colSpan={6} className="text-center text-muted py-4">
                  No contacts found.
                </td>
              </tr>
            )}

            {!loading &&
              rows.map((c) => (
                <tr key={c.id}>
                  <td className="text-truncate" title={c.name}>
                    {c.name}
                  </td>
                  <td className="text-truncate">
                    <a href={`mailto:${c.email}`}>{c.email}</a>
                  </td>
                  <td
                    className="text-truncate"
                    style={{ maxWidth: 360 }}
                    title={c.message}
                  >
                    {c.message}
                  </td>
                  <td title={c.createdAt}>
                    {new Date(c.createdAt).toLocaleString()}
                  </td>
                  <td className="text-center">
                    {c.read ? (
                      <i
                        className="bi bi-envelope-open-fill text-success"
                        title="Read"
                        aria-label="Read"
                      />
                    ) : (
                      <i
                        className="bi bi-envelope-fill text-warning"
                        title="Unread"
                        aria-label="Unread"
                      />
                    )}
                  </td>
                  <td className="text-end">
                    <div
                      className="btn-group btn-group-sm"
                      role="group"
                      aria-label="Actions"
                    >
                      <button
                        className="btn btn-outline-primary btn-icon"
                        title="View"
                        aria-label="View"
                        onClick={() => setSelected(c)}
                      >
                        <i className="bi bi-eye" aria-hidden="true" />
                        <span className="visually-hidden">View</span>
                      </button>

                      {!c.read ? (
                        <button
                          className="btn btn-outline-success btn-icon"
                          title="Mark as read"
                          aria-label="Mark as read"
                          onClick={() => handleMarkRead(c.id)}
                        >
                          <i
                            className="bi bi-check2-circle"
                            aria-hidden="true"
                          />
                          <span className="visually-hidden">Mark as read</span>
                        </button>
                      ) : (
                        <button
                          className="btn btn-outline-warning btn-icon"
                          title="Mark as unread"
                          aria-label="Mark as unread"
                          onClick={() => handleMarkUnread(c.id)}
                        >
                          <i
                            className="bi bi-arrow-counterclockwise"
                            aria-hidden="true"
                          />
                          <span className="visually-hidden">
                            Mark as unread
                          </span>
                        </button>
                      )}

                      <button
                        className="btn btn-outline-danger btn-icon"
                        title="Delete"
                        aria-label="Delete"
                        onClick={() => handleDelete(c.id)}
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
                <strong>{totalElements}</strong> contacts • Page{" "}
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
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page <= 1 || loading}
            >
              ‹ Prev
            </button>
            <button
              className="btn btn-outline-secondary"
              onClick={() => setPage((p) => (p < totalPages ? p + 1 : p))}
              disabled={page >= totalPages || loading}
            >
              Next ›
            </button>
          </div>
        </div>
      </div>

      {selected && (
        <div
          className="position-fixed top-0 end-0 vh-100 bg-body border-start shadow"
          style={{ width: "min(520px, 100%)", zIndex: 1050 }}
          role="dialog"
          aria-modal="true"
        >
          <div className="d-flex align-items-center justify-content-between p-3 border-bottom">
            <h5 className="mb-0">Contact details</h5>
            <button
              className="btn btn-sm btn-outline-secondary"
              onClick={() => setSelected(null)}
            >
              Close
            </button>
          </div>
          <div className="p-3">
            <div className="mb-2">
              <div className="text-muted small">Name</div>
              <div>{selected.name}</div>
            </div>
            <div className="mb-2">
              <div className="text-muted small">Email</div>
              <div>
                <a href={`mailto:${selected.email}`}>{selected.email}</a>
              </div>
            </div>
            <div className="mb-2">
              <div className="text-muted small">Created</div>
              <div>{new Date(selected.createdAt).toLocaleString()}</div>
            </div>
            <div className="mb-3">
              <div className="text-muted small">Message</div>
              <div
                className="border rounded p-2"
                style={{ whiteSpace: "pre-wrap" }}
              >
                {selected.message}
              </div>
            </div>
            <div className="d-flex gap-2">
              {!selected.read && (
                <button
                  className="btn btn-success"
                  onClick={() => handleMarkRead(selected.id)}
                >
                  Mark as read
                </button>
              )}

              <button
                className="btn btn-danger"
                onClick={() => handleDelete(selected.id)}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </Container>
  );
}
