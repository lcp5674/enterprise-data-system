import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatDate(date: Date | string): string {
  const d = new Date(date);
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(d);
}

export function formatRelativeTime(date: Date | string): string {
  const d = new Date(date);
  const now = new Date();
  const diffMs = now.getTime() - d.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return "刚刚";
  if (diffMins < 60) return `${diffMins}分钟前`;
  if (diffHours < 24) return `${diffHours}小时前`;
  if (diffDays < 7) return `${diffDays}天前`;
  return formatDate(date);
}

export function truncate(str: string, length: number): string {
  if (str.length <= length) return str;
  return str.slice(0, length) + "...";
}

export function getQualityColor(score: number): string {
  if (score >= 90) return "excellent";
  if (score >= 75) return "good";
  if (score >= 60) return "fair";
  return "poor";
}

export function getSecurityColor(level: string): string {
  switch (level.toLowerCase()) {
    case "public":
      return "public";
    case "internal":
      return "internal";
    case "sensitive":
      return "sensitive";
    case "confidential":
      return "confidential";
    default:
      return "internal";
  }
}

export function getSecurityLabel(level: string): string {
  switch (level.toLowerCase()) {
    case "public":
      return "公开";
    case "internal":
      return "内部";
    case "sensitive":
      return "敏感";
    case "confidential":
      return "机密";
    default:
      return level;
  }
}

export function getAssetTypeIcon(type: string): string {
  switch (type.toLowerCase()) {
    case "table":
      return "Table";
    case "file":
      return "FileText";
    case "api":
      return "Api";
    case "metric":
      return "BarChart3";
    case "dashboard":
      return "LayoutDashboard";
    default:
      return "Database";
  }
}
