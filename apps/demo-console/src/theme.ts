import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",
    primary: {
      main: "#0f5ea8",
      dark: "#0a3a67",
      light: "#dfeeff",
    },
    secondary: {
      main: "#ff8a3d",
      dark: "#c45f1f",
      light: "#fff1e8",
    },
    success: {
      main: "#169b62",
    },
    warning: {
      main: "#d08b1c",
    },
    error: {
      main: "#dc4f4f",
    },
    background: {
      default: "#f1f4f8",
      paper: "#fbfcfe",
    },
    text: {
      primary: "#112335",
      secondary: "#5f7286",
    },
    divider: "rgba(17, 35, 53, 0.1)",
  },
  shape: {
    borderRadius: 18,
  },
  typography: {
    fontFamily: "\"Manrope\", \"IBM Plex Sans\", \"Segoe UI\", sans-serif",
    h3: {
      fontWeight: 800,
      letterSpacing: -1.2,
    },
    h4: {
      fontWeight: 800,
      letterSpacing: -0.8,
    },
    h5: {
      fontWeight: 800,
    },
    h6: {
      fontWeight: 800,
    },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        "@keyframes fadeUp": {
          from: {
            opacity: 0,
            transform: "translateY(18px)",
          },
          to: {
            opacity: 1,
            transform: "translateY(0)",
          },
        },
        "@keyframes glowPulse": {
          "0%": {
            boxShadow: "0 0 0 rgba(15,94,168,0)",
          },
          "50%": {
            boxShadow: "0 0 0 8px rgba(15,94,168,0.06)",
          },
          "100%": {
            boxShadow: "0 0 0 rgba(15,94,168,0)",
          },
        },
        body: {
          background:
            "radial-gradient(circle at top right, rgba(15,94,168,0.08), transparent 18%), radial-gradient(circle at bottom left, rgba(255,138,61,0.08), transparent 18%), #f1f4f8",
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          boxShadow: "0 12px 36px rgba(9, 30, 55, 0.08)",
          border: "1px solid rgba(17, 35, 53, 0.08)",
          backgroundImage:
            "linear-gradient(180deg, rgba(255,255,255,0.98) 0%, rgba(248,251,254,0.96) 100%)",
        },
      },
    },
    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 14,
          textTransform: "none",
          fontWeight: 700,
          paddingLeft: 16,
          paddingRight: 16,
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          fontWeight: 700,
          borderRadius: 999,
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: "none",
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          boxShadow: "none",
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        head: {
          fontWeight: 800,
          color: "#3e5268",
          backgroundColor: "rgba(15, 94, 168, 0.04)",
        },
      },
    },
  },
});
