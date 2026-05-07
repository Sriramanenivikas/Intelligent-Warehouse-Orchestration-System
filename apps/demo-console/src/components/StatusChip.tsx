import { Chip } from "@mui/material";

const colorByStatus = (value: string): "default" | "primary" | "secondary" | "success" | "warning" | "error" => {
  const normalized = value.toUpperCase();
  if (normalized.includes("DELIVERED") || normalized.includes("COMPLETED") || normalized.includes("ACTIVE")) {
    return "success";
  }
  if (normalized.includes("FAILED") || normalized.includes("EXCEPTION") || normalized.includes("CRITICAL")) {
    return "error";
  }
  if (normalized.includes("HIGH") || normalized.includes("BLOCKED") || normalized.includes("PENDING")) {
    return "warning";
  }
  if (normalized.includes("MANIFEST") || normalized.includes("IN_PROGRESS") || normalized.includes("READY")) {
    return "primary";
  }
  return "default";
};

export function StatusChip({ value }: { value: string }) {
  return <Chip color={colorByStatus(value)} label={value.replaceAll("_", " ")} size="small" variant="filled" />;
}
