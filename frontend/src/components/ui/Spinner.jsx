import { Loader2 } from "lucide-react";
import { cn } from "../../lib/utils";

const Spinner = ({ className, label }) => (
  <div className="flex flex-col items-center justify-center gap-3 text-muted">
    <Loader2 className={cn("h-6 w-6 animate-spin text-brand-500", className)} />
    {label && <p className="text-sm">{label}</p>}
  </div>
);

export const FullScreenSpinner = ({ label = "Loading…" }) => (
  <div className="flex h-screen items-center justify-center">
    <Spinner label={label} />
  </div>
);

export default Spinner;
