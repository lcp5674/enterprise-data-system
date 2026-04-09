import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const securityVariants = cva(
  "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium",
  {
    variants: {
      level: {
        public: "bg-gray-100 text-gray-700",
        internal: "bg-blue-100 text-blue-800",
        sensitive: "bg-amber-100 text-amber-800",
        confidential: "bg-red-100 text-red-800",
      },
    },
    defaultVariants: {
      level: "internal",
    },
  }
);

export interface SecurityBadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof securityVariants> {
  label?: string;
}

function SecurityBadge({ className, level, label, ...props }: SecurityBadgeProps) {
  const getLabel = (l: string | null | undefined): string => {
    const labels: Record<string, string> = {
      public: "公开",
      internal: "内部",
      sensitive: "敏感",
      confidential: "机密",
    };
    return label || labels[l || ""] || l || "内部";
  };

  return (
    <span
      className={cn(securityVariants({ level: level as any }), className)}
      {...props}
    >
      {getLabel(level)}
    </span>
  );
}

export { SecurityBadge, securityVariants };
