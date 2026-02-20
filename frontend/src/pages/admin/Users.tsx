import { useEffect, useMemo, useState } from "react";
import { Alert, Button, Container, Modal } from "react-bootstrap";

import {
  getUsers,
  type UserItem,
  deleteUser,
  setUserDisabled,
  setUserLocked,
  updateUser,
} from "@/services/userService";
import type { ApiError } from "@/services/api";

type EditRoles = {
  ADMIN: boolean;
  READER: boolean;
};

type EditUserState = {
  fullname: string;
  email: string;
  passwordExpiredAt: string; // datetime-local string
  disabled: boolean;
  accountLocked: boolean;
  roles: EditRoles;
};

type ConfirmDialogState = {
  title: string;
  message: string;
  confirmLabel: string;
  confirmVariant: "danger" | "warning" | "primary";
  onConfirm: () => Promise<void>;
};

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

export default function UsersPage() {
  const [page, setPage] = useState(1);
  const [size, setSize] = useState(20);
  const [rows, setRows] = useState<UserItem[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const from = totalElements === 0 ? 0 : (page - 1) * size + 1;
  const to = totalElements === 0 ? 0 : Math.min(page * size, totalElements);

  const [loading, setLoading] = useState(false);
  const [actionBusy, setActionBusy] = useState<string | null>(null);
  const [error, setError] = useState<ApiError | null>(null);

  const [selected, setSelected] = useState<UserItem | null>(null);

  const [isEditing, setIsEditing] = useState(false);
  const [edit, setEdit] = useState<EditUserState | null>(null);
  const [editErrors, setEditErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<ApiError | null>(null);
  const [confirmDialog, setConfirmDialog] = useState<ConfirmDialogState | null>(
    null
  );
  const [confirmBusy, setConfirmBusy] = useState(false);

  const lang = useMemo(() => navigator.language || "en", []);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await getUsers(lang, page, size);
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

  function patchUserInState(id: string, patch: Partial<UserItem>) {
    setRows((prev) => prev.map((u) => (u.id === id ? { ...u, ...patch } : u)));
    setSelected((prev) =>
      prev && prev.id === id ? { ...prev, ...patch } : prev
    );
  }

  function showActionError(e: unknown) {
    const err = e as ApiError;
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

  function handleDelete(user: UserItem) {
    openConfirmDialog({
      title: "Delete user",
      message: `Are you sure you want to delete "${user.username}"?`,
      confirmLabel: "Delete",
      confirmVariant: "danger",
      onConfirm: async () => {
        try {
          setActionBusy(`${user.id}:delete`);
          await deleteUser(user.id);
          setRows((prev) => prev.filter((r) => r.id !== user.id));
          setSelected((prev) => (prev && prev.id === user.id ? null : prev));
          setTotalElements((prev) => (prev > 0 ? prev - 1 : 0));
        } catch (e) {
          showActionError(e);
        } finally {
          setActionBusy(null);
        }
      },
    });
  }

  function renderRoles(roles: string[]) {
    if (!roles || roles.length === 0) return "—";
    return (
      <div className="d-flex gap-2 align-items-center">
        {roles.includes("ADMIN") && (
          <i
            className="bi bi-shield-lock-fill text-primary"
            title="Admin"
            aria-label="Admin"
          />
        )}
        {roles.includes("READER") && (
          <i
            className="bi bi-eye-fill text-secondary"
            title="Reader"
            aria-label="Reader"
          />
        )}
      </div>
    );
  }

  function renderStatus(u: { disabled: boolean; accountLocked: boolean }) {
    if (u.disabled) {
      return (
        <i
          className="bi bi-slash-circle-fill text-secondary"
          title="Disabled"
          aria-label="Disabled"
        />
      );
    }
    if (u.accountLocked) {
      return (
        <i
          className="bi bi-lock-fill text-danger"
          title="Locked"
          aria-label="Locked"
        />
      );
    }
    return (
      <i
        className="bi bi-check-circle-fill text-success"
        title="Active"
        aria-label="Active"
      />
    );
  }

  function openDrawer(u: UserItem) {
    setSelected(u);
    setIsEditing(false);
    setEdit(null);
    setEditErrors({});
    setFormError(null);
  }

  function startEdit(u: UserItem) {
    setSelected(u);
    setIsEditing(true);
    setEditErrors({});
    setFormError(null);

    setEdit({
      fullname: u.fullname ?? "",
      email: u.email ?? "",
      passwordExpiredAt: toLocalDateTimeInput(u.passwordExpiredAt),
      disabled: !!u.disabled,
      accountLocked: !!u.accountLocked,
      roles: {
        ADMIN: u.roles?.includes("ADMIN") ?? false,
        READER: u.roles?.includes("READER") ?? false,
      },
    });
  }

  function cancelEdit() {
    setIsEditing(false);
    setEdit(null);
    setEditErrors({});
    setFormError(null);
  }

  function toggleDisabledInView(u: UserItem) {
    const desired = !u.disabled;

    openConfirmDialog({
      title: desired ? "Disable user" : "Enable user",
      message: desired
        ? `Disable user "${u.username}"?`
        : `Enable user "${u.username}"?`,
      confirmLabel: desired ? "Disable" : "Enable",
      confirmVariant: "warning",
      onConfirm: async () => {
        try {
          setActionBusy(`${u.id}:disable`);
          await setUserDisabled(u.id, desired);
          patchUserInState(u.id, { disabled: desired });
        } catch (e) {
          showActionError(e);
        } finally {
          setActionBusy(null);
        }
      },
    });
  }

  function toggleLockedInView(u: UserItem) {
    const desired = !u.accountLocked;

    openConfirmDialog({
      title: desired ? "Lock user" : "Unlock user",
      message: desired
        ? `Lock user "${u.username}"?`
        : `Unlock user "${u.username}"?`,
      confirmLabel: desired ? "Lock" : "Unlock",
      confirmVariant: "warning",
      onConfirm: async () => {
        try {
          setActionBusy(`${u.id}:lock`);
          await setUserLocked(u.id, desired);
          patchUserInState(u.id, { accountLocked: desired });
        } catch (e) {
          showActionError(e);
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
    if (edit.fullname.trim().length < 3)
      nextErrors.fullname = "Full name is required.";
    if (
      edit.email.trim().length < 5 ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(edit.email.trim())
    ) {
      nextErrors.email = "Email is invalid.";
    }
    if (!edit.passwordExpiredAt) {
      nextErrors.passwordExpiredAt = "Password expiration date is required.";
    }

    if (Object.keys(nextErrors).length > 0) {
      setEditErrors(nextErrors);
      return;
    }

    const roles: string[] = [];
    if (edit.roles.ADMIN) roles.push("ADMIN");
    if (edit.roles.READER) roles.push("READER");
    if (roles.length === 0) {
      setEditErrors({ roles: "Select at least one role." });
      return;
    }

    const payload = {
      fullname: edit.fullname.trim(),
      email: edit.email.trim(),
      passwordExpiredAt: fromLocalDateTimeInput(edit.passwordExpiredAt),
      disabled: edit.disabled,
      accountLocked: edit.accountLocked,
      roles,
    };

    try {
      setActionBusy(`${selected.id}:save`);

      await updateUser(selected.id, payload);

      // For now: update UI only (keeps your app working until API exists)
      patchUserInState(selected.id, {
        fullname: payload.fullname,
        email: payload.email,
        passwordExpiredAt:
          payload.passwordExpiredAt ?? selected.passwordExpiredAt,
        disabled: payload.disabled,
        accountLocked: payload.accountLocked,
        roles: payload.roles,
      });

      setIsEditing(false);
      setEdit(null);
      setEditErrors({});
    } catch (e) {
      setFormError(e as ApiError);
    } finally {
      setActionBusy(null);
    }
  }

  return (
    <Container className="admin-users">
      <h1 className="mb-4">Users</h1>

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
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
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
          <button
            type="button"
            className="btn-close"
            aria-label="Close"
            onClick={() => setError(null)}
          />
        </div>
      )}

      <div className="table-responsive">
        <table className="table table-striped align-middle">
          <thead>
            <tr>
              <th style={{ width: "14rem" }}>Username</th>
              <th style={{ width: "18rem" }}>Full Name</th>
              <th style={{ width: "20rem" }}>Email</th>
              <th style={{ width: "14rem" }}>Created</th>
              <th style={{ width: "6rem" }}>Roles</th>
              <th style={{ width: "6rem" }}>Status</th>
              <th style={{ width: "12rem" }} className="text-end">
                Actions
              </th>
            </tr>
          </thead>

          <tbody>
            {loading && (
              <tr>
                <td colSpan={7} className="text-center py-4">
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
                <td colSpan={7} className="text-center text-muted py-4">
                  No users found.
                </td>
              </tr>
            )}

            {!loading &&
              rows.map((u) => (
                <tr key={u.id}>
                  <td className="text-truncate" title={u.username}>
                    {u.username}
                  </td>
                  <td className="text-truncate" title={u.fullname}>
                    {u.fullname}
                  </td>
                  <td className="text-truncate">
                    <a href={`mailto:${u.email}`}>{u.email}</a>
                  </td>
                  <td title={u.createdAt}>
                    {new Date(u.createdAt).toLocaleString()}
                  </td>
                  <td className="text-start">{renderRoles(u.roles)}</td>
                  <td className="text-start">{renderStatus(u)}</td>

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
                        onClick={() => openDrawer(u)}
                      >
                        <i className="bi bi-eye" aria-hidden="true" />
                        <span className="visually-hidden">View</span>
                      </button>

                      <button
                        className="btn btn-outline-secondary btn-icon"
                        title="Edit"
                        aria-label="Edit"
                        onClick={() => startEdit(u)}
                      >
                        <i className="bi bi-pencil" aria-hidden="true" />
                        <span className="visually-hidden">Edit</span>
                      </button>

                      <button
                        className="btn btn-outline-danger btn-icon"
                        title="Delete"
                        aria-label="Delete"
                        disabled={actionBusy === `${u.id}:delete`}
                        onClick={() => handleDelete(u)}
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
                <strong>{totalElements}</strong> users • Page{" "}
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

      {/* Drawer */}
      {selected && (
        <div
          className="position-fixed top-0 end-0 vh-100 bg-body border-start shadow"
          style={{ width: "min(520px, 100%)", zIndex: 1050 }}
          role="dialog"
          aria-modal="true"
        >
          <div className="d-flex align-items-center justify-content-between p-3 border-bottom">
            <h5 className="mb-0">{isEditing ? "Edit user" : "User details"}</h5>
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
              <Alert
                variant="danger"
                onClose={() => setFormError(null)}
                dismissible
              >
                <div className="fw-semibold">
                  Error {formError.code}: {formError.message}
                </div>
                {Array.isArray(formError.details) &&
                  formError.details.length > 0 && (
                    <ul className="mb-0">
                      {formError.details.map((d, i) => (
                        <li key={i}>{d}</li>
                      ))}
                    </ul>
                  )}
              </Alert>
            )}

            {/* Username (never editable) */}
            <div className="mb-3">
              <div className="text-muted small">Username</div>
              <div className="fw-semibold">{selected.username}</div>
            </div>

            {/* Full name */}
            <div className="mb-3">
              <div className="text-muted small">Full name</div>
              {isEditing ? (
                <>
                  <input
                    className={`form-control ${
                      editErrors.fullname ? "is-invalid" : ""
                    }`}
                    value={edit?.fullname ?? ""}
                    onChange={(e) =>
                      setEdit((s) =>
                        s ? { ...s, fullname: e.target.value } : s
                      )
                    }
                  />
                  {editErrors.fullname && (
                    <div className="invalid-feedback d-block">
                      {editErrors.fullname}
                    </div>
                  )}
                </>
              ) : (
                <div>{selected.fullname}</div>
              )}
            </div>

            {/* Email */}
            <div className="mb-3">
              <div className="text-muted small">Email</div>
              {isEditing ? (
                <>
                  <input
                    className={`form-control ${
                      editErrors.email ? "is-invalid" : ""
                    }`}
                    value={edit?.email ?? ""}
                    onChange={(e) =>
                      setEdit((s) => (s ? { ...s, email: e.target.value } : s))
                    }
                  />
                  {editErrors.email && (
                    <div className="invalid-feedback d-block">
                      {editErrors.email}
                    </div>
                  )}
                </>
              ) : (
                <div>
                  <a href={`mailto:${selected.email}`}>{selected.email}</a>
                </div>
              )}
            </div>

            {/* Password expires */}
            <div className="mb-3">
              <div className="text-muted small">Password expires</div>
              {isEditing ? (
                <>
                  <input
                    type="datetime-local"
                    className={`form-control ${
                      editErrors.passwordExpiredAt ? "is-invalid" : ""
                    }`}
                    value={edit?.passwordExpiredAt ?? ""}
                    onChange={(e) =>
                      setEdit((s) =>
                        s ? { ...s, passwordExpiredAt: e.target.value } : s
                      )
                    }
                  />
                  {editErrors.passwordExpiredAt && (
                    <div className="invalid-feedback d-block">
                      {editErrors.passwordExpiredAt}
                    </div>
                  )}
                </>
              ) : (
                <div>
                  {new Date(selected.passwordExpiredAt).toLocaleString()}
                </div>
              )}
            </div>

            {/* Roles */}
            <div className="mb-3">
              <div className="text-muted small">Roles</div>
              {isEditing ? (
                <>
                  <div className="d-flex flex-column gap-2 mt-1">
                    <label className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        checked={!!edit?.roles.ADMIN}
                        onChange={(e) =>
                          setEdit((s) =>
                            s
                              ? {
                                  ...s,
                                  roles: { ...s.roles, ADMIN: e.target.checked },
                                }
                              : s
                          )
                        }
                      />
                      <span className="form-check-label">Admin</span>
                    </label>

                    <label className="form-check">
                      <input
                        type="checkbox"
                        className="form-check-input"
                        checked={!!edit?.roles.READER}
                        onChange={(e) =>
                          setEdit((s) =>
                            s
                              ? {
                                  ...s,
                                  roles: { ...s.roles, READER: e.target.checked },
                                }
                              : s
                          )
                        }
                      />
                      <span className="form-check-label">Reader</span>
                    </label>
                  </div>
                  {editErrors.roles && (
                    <div className="invalid-feedback d-block">
                      {editErrors.roles}
                    </div>
                  )}
                </>
              ) : (
                <div className="d-flex gap-2 align-items-center">
                  {renderRoles(selected.roles)}
                </div>
              )}
            </div>

            {/* Disabled + Locked */}
            <div className="mb-4">
              <div className="text-muted small">Status</div>

              {isEditing ? (
                <div className="d-flex flex-column gap-2 mt-1">
                  <label className="form-check">
                    <input
                      type="checkbox"
                      className="form-check-input"
                      checked={!!edit?.disabled}
                      onChange={(e) =>
                        setEdit((s) =>
                          s ? { ...s, disabled: e.target.checked } : s
                        )
                      }
                    />
                    <span className="form-check-label">Disabled</span>
                  </label>

                  <label className="form-check">
                    <input
                      type="checkbox"
                      className="form-check-input"
                      checked={!!edit?.accountLocked}
                      onChange={(e) =>
                        setEdit((s) =>
                          s ? { ...s, accountLocked: e.target.checked } : s
                        )
                      }
                      disabled={!!edit?.disabled}
                    />
                    <span className="form-check-label">Account locked</span>
                  </label>
                </div>
              ) : (
                <div className="d-flex gap-2 align-items-center mt-1">
                  {renderStatus(selected)}
                  <span className="small text-muted">
                    {selected.disabled
                      ? "Disabled"
                      : selected.accountLocked
                      ? "Locked"
                      : "Active"}
                  </span>
                </div>
              )}
            </div>

            {/* Buttons */}
            {!isEditing ? (
              <div className="d-flex flex-wrap gap-2">
                <button
                  className={`btn ${
                    selected.disabled ? "btn-success" : "btn-warning"
                  }`}
                  disabled={actionBusy === `${selected.id}:disable`}
                  onClick={() => toggleDisabledInView(selected)}
                >
                  {selected.disabled ? "Enable" : "Disable"}
                </button>

                <button
                  className={`btn ${
                    selected.accountLocked ? "btn-success" : "btn-warning"
                  }`}
                  disabled={
                    selected.disabled || actionBusy === `${selected.id}:lock`
                  }
                  onClick={() => toggleLockedInView(selected)}
                >
                  {selected.accountLocked ? "Unlock" : "Lock"}
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
                <button
                  className="btn btn-outline-secondary"
                  onClick={cancelEdit}
                >
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
