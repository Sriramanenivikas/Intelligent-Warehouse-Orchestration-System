import { Card, CardContent, Stack, Typography } from "@mui/material";

type Props = {
  label: string;
  value: string | number;
  helper?: string;
};

export function StatCard({ label, value, helper }: Props) {
  return (
    <Card sx={{ minHeight: 148 }}>
      <CardContent>
        <Stack spacing={1.5}>
          <Typography color="text.secondary" sx={{ letterSpacing: 0.5, textTransform: "uppercase" }} variant="caption">
            {label}
          </Typography>
          <Typography sx={{ color: "primary.dark" }} variant="h4">
            {value}
          </Typography>
          {helper ? (
            <Typography color="text.secondary" variant="caption">
              {helper}
            </Typography>
          ) : null}
        </Stack>
      </CardContent>
    </Card>
  );
}
