import { cn } from "../../lib/utils";

export const Skeleton = ({ className }) => (
  <div className={cn("skeleton rounded-md", className)} />
);

export const BoardCardSkeleton = () => (
  <div className="card space-y-3 rounded-3xl p-5">
    <Skeleton className="h-3 w-2/3" />
    <Skeleton className="h-2.5 w-full" />
    <Skeleton className="h-2.5 w-4/5" />
    <div className="flex items-center justify-between pt-2">
      <Skeleton className="h-6 w-16 rounded-full" />
      <Skeleton className="h-6 w-6 rounded-full" />
    </div>
  </div>
);

export const ColumnSkeleton = () => (
  <div className="w-[330px] shrink-0 space-y-3 rounded-2xl bg-surface-2/60 p-2.5">
    <div className="flex items-center gap-2 px-1 pt-1">
      <Skeleton className="h-2.5 w-2.5 rounded-full" />
      <Skeleton className="h-4 w-24" />
    </div>
    {[0, 1, 2].map((i) => (
      <div key={i} className="space-y-2.5 rounded-2xl border border-line bg-surface p-4">
        <Skeleton className="h-4 w-14 rounded-md" />
        <Skeleton className="h-3.5 w-3/4" />
        <Skeleton className="h-2.5 w-full" />
        <div className="flex items-center justify-between border-t border-line/70 pt-3">
          <Skeleton className="h-6 w-20 rounded-full" />
          <Skeleton className="h-5 w-12 rounded-md" />
        </div>
      </div>
    ))}
  </div>
);
