import { useEffect, useRef, useState } from "react";
import Modal from "./Modal";
import Button from "./Button";
import { Input } from "./Input";

/**
 * Reusable single-field prompt modal (replaces window.prompt).
 * Calls onSubmit(trimmedValue) when confirmed.
 */
const PromptDialog = ({
  open,
  onClose,
  onSubmit,
  title = "Add",
  description,
  label,
  placeholder,
  defaultValue = "",
  submitLabel = "Add",
  loading = false,
}) => {
  const [value, setValue] = useState(defaultValue);
  const submitted = useRef(false);

  useEffect(() => {
    if (open) {
      setValue(defaultValue);
      submitted.current = false;
    }
  }, [open, defaultValue]);

  const submit = (e) => {
    e.preventDefault();
    const v = value.trim();
    if (!v || submitted.current) return;
    submitted.current = true;
    onSubmit(v);
  };

  return (
    <Modal open={open} onClose={onClose} title={title} description={description} size="sm">
      <form onSubmit={submit} className="space-y-4">
        <Input label={label} placeholder={placeholder} autoFocus value={value} onChange={(e) => setValue(e.target.value)} />
        <div className="flex justify-end gap-2 pt-1">
          <Button type="button" variant="ghost" onClick={onClose}>Cancel</Button>
          <Button type="submit" loading={loading} disabled={!value.trim()}>{submitLabel}</Button>
        </div>
      </form>
    </Modal>
  );
};

export default PromptDialog;
