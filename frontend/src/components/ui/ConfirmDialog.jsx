import Modal from "./Modal";
import Button from "./Button";

/**
 * Reusable confirmation modal (replaces window.confirm).
 */
const ConfirmDialog = ({
  open,
  onClose,
  onConfirm,
  title = "Are you sure?",
  description,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
  danger = false,
  loading = false,
}) => (
  <Modal open={open} onClose={onClose} title={title} description={description} size="sm">
    <div className="flex justify-end gap-2 pt-1">
      <Button variant="ghost" onClick={onClose}>{cancelLabel}</Button>
      <Button variant={danger ? "danger" : "primary"} onClick={onConfirm} loading={loading}>
        {confirmLabel}
      </Button>
    </div>
  </Modal>
);

export default ConfirmDialog;
