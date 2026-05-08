import { Card, CardContent, Stack, Typography } from "@mui/material";

type Props = {
  label: string;
  value: string | number;
  helper?: string;
};

export function StatCard({ label, value, helper }: Props) {
  return (
    <Card
      sx={{
        minHeight: 156,
        position: "relative",
        overflow: "hidden",
      }}
    >
      <CardContent
        sx={{
          position: "relative",
          "&::after": {
            content: '""',
            position: "absolute",
            top: 0,
            right: 0,
            width: 84,
            height: 84,
            borderRadius: "0 0 0 100%",
            background: "linear-gradient(180deg, rgba(255,138,61,0.18), rgba(15,94,168,0.08))",
          },
        }}
      >
        <Stack spacing={1.35}>
          <Typography color="text.secondary" sx={{ letterSpacing: 1.1, textTransform: "uppercase", fontWeight: 800 }} variant="caption">
            {label}
          </Typography>
          <Typography sx={{ color: "text.primary", fontSize: { xs: "2rem", md: "2.4rem" }, lineHeight: 1 }} variant="h4">
            {value}
          </Typography>
          {helper ? (
            <Typography color="text.secondary" sx={{ maxWidth: 220 }} variant="caption">
              {helper}
            </Typography>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
}
