import { Card, CardContent, Stack, Typography } from "@mui/material";

type Props = {
  label: string;
  value: string | number;
  helper?: string;
};

export function StatCard({ label, value, helper }: Props) {
  return (
    <Card>
      <CardContent>
        <Stack spacing={1}>
          <Typography color="text.secondary" variant="body2">
            {label}
          </Typography>
          <Typography variant="h5">{value}</Typography>
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

