import { Card, CardContent, Divider, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

type Props = {
  title: string;
  subtitle?: string;
  action?: ReactNode;
  children: ReactNode;
};

export function SectionCard({ title, subtitle, action, children }: Props) {
  return (
    <Card
      sx={{
        overflow: "hidden",
      }}
    >
      <CardContent sx={{ p: 0 }}>
        <Stack
          alignItems={{ xs: "flex-start", md: "center" }}
          direction={{ xs: "column", md: "row" }}
          justifyContent="space-between"
          spacing={2}
          sx={{
            px: 3,
            py: 2.5,
            background:
              "linear-gradient(180deg, rgba(15,94,168,0.06) 0%, rgba(255,255,255,0.2) 100%)",
          }}
        >
          <Stack spacing={0.5}>
            <Typography sx={{ color: "text.primary" }} variant="h6">
              {title}
            </Typography>
            {subtitle ? (
              <Typography color="text.secondary" variant="body2">
                {subtitle}
              </Typography>
            ) : null}
          </Stack>
          {action}
        </Stack>
        <Divider />
        <Stack spacing={2} sx={{ p: 3.25 }}>
          {children}
        </Stack>
      </CardContent>
    </Card>
  );
}
