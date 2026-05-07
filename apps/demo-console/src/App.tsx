import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "./components/AppShell";
import { LoginPage } from "./pages/LoginPage";
import { OverviewPage } from "./pages/OverviewPage";
import { OrderFlowPage } from "./pages/OrderFlowPage";
import { ForecastingPage } from "./pages/ForecastingPage";
import { OperationsPage } from "./pages/OperationsPage";
import { NodesPage } from "./pages/NodesPage";
import { ReturnsPage } from "./pages/ReturnsPage";
import type { TokenResponse } from "./types";

const SESSION_STORAGE_KEY = "iwos-demo-session";

export default function App() {
  const [session, setSession] = useState<TokenResponse | null>(null);

  useEffect(() => {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    if (raw) {
      try {
        setSession(JSON.parse(raw) as TokenResponse);
      } catch {
        window.localStorage.removeItem(SESSION_STORAGE_KEY);
      }
    }
  }, []);

  const handleLogin = (token: TokenResponse) => {
    setSession(token);
    window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(token));
  };

  const handleLogout = () => {
    setSession(null);
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
  };

  if (!session) {
    return <LoginPage onSuccess={handleLogin} />;
  }

  return (
    <Routes>
      <Route element={<AppShell onLogout={handleLogout} session={session} />}>
        <Route element={<OverviewPage token={session.accessToken} />} path="/" />
        <Route element={<OrderFlowPage token={session.accessToken} />} path="/order-flow" />
        <Route element={<ForecastingPage token={session.accessToken} />} path="/forecasting" />
        <Route element={<OperationsPage token={session.accessToken} />} path="/operations" />
        <Route element={<NodesPage token={session.accessToken} />} path="/nodes" />
        <Route element={<ReturnsPage token={session.accessToken} />} path="/returns" />
      </Route>
      <Route element={<Navigate replace to="/" />} path="*" />
    </Routes>
  );
}
