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
  const [leading, trailing] = title.split(/ (.+)/);

  return (
    <Box
      sx={{
        border: "1px solid rgba(17,35,53,0.08)",
        borderRadius: 6,
        px: { xs: 2.75, md: 4 },
        py: { xs: 3, md: 3.5 },
        background:
          "radial-gradient(circle at top right, rgba(15,94,168,0.14), transparent 22%), radial-gradient(circle at center left, rgba(255,138,61,0.12), transparent 18%), linear-gradient(135deg, #ffffff 0%, #f6f8fb 100%)",
        boxShadow: "0 18px 40px rgba(9,30,55,0.06)",
      }}
    >
      <Stack direction={{ xs: "column", lg: "row" }} justifyContent="space-between" spacing={2}>
        <Stack spacing={1.25}>
          {eyebrow ? (
            <Typography color="secondary.dark" sx={{ letterSpacing: 1.8, textTransform: "uppercase", fontWeight: 800 }} variant="caption">
              {eyebrow}
            </Typography>
          ) : null}
          <Typography sx={{ color: "text.primary", maxWidth: 760 }} variant="h4">
            {trailing ? (
              <>
                {leading} <Box component="span" sx={{ color: "secondary.main" }}>{trailing}</Box>
              </>
            ) : (
              title
            )}
          </Typography>
          <Typography color="text.secondary" sx={{ maxWidth: 760 }} variant="body1">
            {description}
          </Typography>
          {badges.length ? (
            <Stack direction="row" flexWrap="wrap" spacing={1} useFlexGap sx={{ pt: 0.5 }}>
              {badges.map((badge) => (
                <Chip color={badge.color ?? "default"} key={badge.label} label={badge.label} size="small" variant="filled" />
              ))}
            </Stack>
          ) : null}
        </Stack>
        {actions ? <Box sx={{ alignSelf: { xs: "stretch", lg: "flex-start" } }}>{actions}</Box> : null}
      </Stack>
    </Box>
  );
}
