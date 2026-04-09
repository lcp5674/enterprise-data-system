import * as React from "react";
import { cn } from "@/lib/utils";

interface QualityScoreProps {
  score: number;
  size?: "sm" | "md" | "lg";
  showLabel?: boolean;
  className?: string;
}

function QualityScore({ 
  score, 
  size = "md", 
  showLabel = true,
  className 
}: QualityScoreProps) {
  const getColor = (s: number): string => {
    if (s >= 90) return "text-emerald-600 bg-emerald-50 border-emerald-200";
    if (s >= 75) return "text-blue-600 bg-blue-50 border-blue-200";
    if (s >= 60) return "text-amber-600 bg-amber-50 border-amber-200";
    return "text-red-600 bg-red-50 border-red-200";
  };

  const getLabel = (s: number): string => {
    if (s >= 90) return "优秀";
    if (s >= 75) return "良好";
    if (s >= 60) return "一般";
    return "较差";
  };

  const sizeClasses = {
    sm: "h-6 w-6 text-xs",
    md: "h-8 w-8 text-sm",
    lg: "h-10 w-10 text-base",
  };

  return (
    <div className={cn("flex items-center gap-2", className)}>
      <div
        className={cn(
          "flex items-center justify-center rounded-full border font-medium",
          sizeClasses[size],
          getColor(score)
        )}
      >
        {score}
      </div>
      {showLabel && (
        <span className={cn("text-sm", getColor(score).split(" ")[0])}>
          {getLabel(score)}
        </span>
      )}
    </div>
  );
}

export { QualityScore };
