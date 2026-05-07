import { Box, Chip, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

type Props = {
  eyebrow?: string;
  title: string;
  description: string;
  actions?: ReactNode;
  badges?: Array<{ label: string; color?: "default" | "primary" | "secondary" | "success" | "warning" | "error" }>;
};

export function PageHeader({ eyebrow, title, description, actions, badges = [] }: Props) {
  return (
    <Box
      sx={{
        border: "1px solid rgba(20, 75, 125, 0.1)",
        borderRadius: 4,
        px: { xs: 2.5, md: 3.5 },
        py: { xs: 2.5, md: 3 },
        background:
          "radial-gradient(circle at top right, rgba(20,75,125,0.14), transparent 24%), linear-gradient(135deg, #ffffff 0%, #f4f8fc 100%)",
      }}
    >
      <Stack direction={{ xs: "column", lg: "row" }} justifyContent="space-between" spacing={2}>
        <Stack spacing={1.25}>
          {eyebrow ? (
            <Typography color="primary.main" sx={{ letterSpacing: 1.4, textTransform: "uppercase" }} variant="caption">
              {eyebrow}
            </Typography>
          ) : null}
          <Typography variant="h4">{title}</Typography>
          <Typography color="text.secondary" sx={{ maxWidth: 760 }} variant="body1">
            {description}
          </Typography>
          {badges.length ? (
            <Stack direction="row" flexWrap="wrap" spacing={1} useFlexGap>
              {badges.map((badge) => (
                <Chip color={badge.color ?? "default"} key={badge.label} label={badge.label} size="small" variant="outlined" />
              ))}
            </Stack>
          ) : null}
        </Stack>
        {actions ? <Box>{actions}</Box> : null}
      </Stack>
    </Box>
  );
}

