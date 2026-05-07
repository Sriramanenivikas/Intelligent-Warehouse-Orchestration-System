import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#144b7d",
      dark: "#0b3356",
      light: "#e7f0f8",
    },
    secondary: {
      main: "#b76712",
      light: "#fff2e6",
    },
    success: {
      main: "#1f7a4d",
    },
    warning: {
      main: "#b7791f",
    },
    error: {
      main: "#b03333",
    },
    background: {
      default: "#eef3f8",
      paper: "#ffffff",
    },
    divider: "rgba(20, 75, 125, 0.1)",
  },
  shape: {
    borderRadius: 14,
  },
  typography: {
    fontFamily: "\"IBM Plex Sans\", \"Aptos\", \"Segoe UI\", sans-serif",
    h4: {
      fontWeight: 700,
    },
    h5: {
      fontWeight: 700,
    },
    h6: {
      fontWeight: 700,
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: "0 18px 42px rgba(20, 75, 125, 0.08)",
          border: "1px solid rgba(20, 75, 125, 0.08)",
          backgroundImage:
            "linear-gradient(180deg, rgba(255,255,255,0.98) 0%, rgba(248,251,254,0.98) 100%)",
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 12,
          textTransform: "none",
          fontWeight: 600,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 600,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 700,
        },
      },
    },
  },
});
