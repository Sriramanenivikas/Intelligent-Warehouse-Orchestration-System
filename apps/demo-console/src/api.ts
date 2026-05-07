import type {
  ControlTowerSnapshotResponse,
  ForecastModelRunResponse,
  ForecastRunResponse,
  ForecastSummaryResponse,
  FulfillmentOrderResponse,
  InventoryForecastResponse,
  NetworkShipmentResponse,
  NodeResponse,
  NotificationResponse,
  OrderIntentResponse,
  ReturnResponse,
  ScanTimelineResponse,
  ShipmentResponse,
  TokenResponse,
} from "./types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "";

export class ApiError extends Error {
  readonly status: number;
  readonly details?: unknown;

  constructor(message: string, status: number, details?: unknown) {
    super(message);
    this.status = status;
    this.details = details;
  }
}

type RequestOptions = {
  method?: string;
  token?: string | null;
  body?: unknown;
  headers?: Record<string, string>;
};

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers: {
      "Content-Type": "application/json",
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...options.headers,
    },
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  if (!response.ok) {
    let details: unknown;
    try {
      details = await response.json();
    } catch {
      details = await response.text();
    }
    throw new ApiError(`Request failed with status ${response.status}`, response.status, details);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export const api = {
  login(payload: { username: string; password: string; requestedNodeId?: string | null }) {
    return request<TokenResponse>("/api/v1/auth/token", {
      method: "POST",
      body: payload,
    });
  },
  createOrderIntent(input: {
    token?: string | null;
    idempotencyKey: string;
    payload: unknown;
  }) {
    return request<OrderIntentResponse>("/api/v1/order-intents", {
      method: "POST",
      token: input.token,
      headers: {
        "Idempotency-Key": input.idempotencyKey,
      },
      body: input.payload,
    });
  },
  getOrderIntent(orderIntentId: string, token?: string | null) {
    return request<OrderIntentResponse>(`/api/v1/order-intents/${orderIntentId}`, { token });
  },
  processOrderWorkflow(orderIntentId: string, token: string) {
    return request<{ workflowId: string; orderIntentId: string; status: string }>(
      `/api/v1/order-workflows/${orderIntentId}/process`,
      { method: "POST", token },
    );
  },
  processFulfillment(orderIntentId: string, token: string) {
    return request<{ fulfillmentOrderId: string; orderIntentId: string; status: string }>(
      `/api/v1/fulfillment-orders/${orderIntentId}/process`,
      { method: "POST", token },
    );
  },
  getFulfillmentByOrderIntent(orderIntentId: string, token: string) {
    return request<FulfillmentOrderResponse>(`/api/v1/fulfillment-orders/by-order-intent/${orderIntentId}`, { token });
  },
  createShipment(fulfillmentOrderId: string, token: string) {
    return request<ShipmentResponse>(`/api/v1/shipments/fulfillment-order/${fulfillmentOrderId}`, {
      method: "POST",
      token,
      body: { carrierCode: "INTERNAL", weightGrams: 1250, packageCount: 1 },
    });
  },
  getShipmentByOrderIntent(orderIntentId: string, token: string) {
    return request<ShipmentResponse>(`/api/v1/shipments/by-order-intent/${orderIntentId}`, { token });
  },
  manifestShipment(shipmentId: string, token: string) {
    return request<ShipmentResponse>(`/api/v1/shipments/${shipmentId}/manifest`, { method: "POST", token });
  },
  dispatchShipment(shipmentId: string, token: string) {
    return request<ShipmentResponse>(`/api/v1/shipments/${shipmentId}/dispatch`, { method: "POST", token });
  },
  deliverShipment(shipmentId: string, token: string) {
    return request<ShipmentResponse>(`/api/v1/shipments/${shipmentId}/deliver`, { method: "POST", token });
  },
  getNetworkShipmentByOrderIntent(orderIntentId: string, token: string) {
    return request<NetworkShipmentResponse>(`/api/v1/network-shipments/by-order-intent/${orderIntentId}`, { token });
  },
  recordScan(
    shipmentId: string,
    token: string,
    payload: { scanType: string; nodeId: string; facilityCode: string; notes: string; occurredAt: string },
  ) {
    return request<NetworkShipmentResponse>(`/api/v1/network-shipments/${shipmentId}/scans`, {
      method: "POST",
      token,
      body: payload,
    });
  },
  getScanTimelineByOrderIntent(orderIntentId: string, token: string) {
    return request<ScanTimelineResponse>(`/api/v1/scan-events/order-intents/${orderIntentId}`, { token });
  },
  listNotificationsByOrderIntent(orderIntentId: string, token: string) {
    return request<NotificationResponse[]>(`/api/v1/notifications/order-intents/${orderIntentId}`, { token });
  },
  getForecastRunLatest(token: string) {
    return request<ForecastRunResponse>("/api/v1/forecast-runs/latest", { token });
  },
  getForecastSummary(token: string) {
    return request<ForecastSummaryResponse>("/api/v1/forecast-runs/latest/summary", { token });
  },
  getModelRunLatest(token: string) {
    return request<ForecastModelRunResponse>("/api/v1/model-runs/latest", { token });
  },
  listForecasts(token: string, params = "?limit=25") {
    return request<InventoryForecastResponse[]>(`/api/v1/forecasts${params}`, { token });
  },
  getControlTowerLatest(token: string) {
    return request<ControlTowerSnapshotResponse>("/api/v1/control-tower/latest", { token });
  },
  refreshControlTower(token: string) {
    return request<ControlTowerSnapshotResponse>("/api/v1/control-tower/refresh", { method: "POST", token });
  },
  listNodes(token: string) {
    return request<NodeResponse[]>("/api/v1/nodes", { token });
  },
  listReturns(token: string, customerId?: string) {
    const query = customerId ? `?customerId=${encodeURIComponent(customerId)}` : "";
    return request<ReturnResponse[]>(`/api/v1/returns${query}`, { token });
  },
};

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return "N/A";
  }
  return new Date(value).toLocaleString();
}

export function riskColor(risk: string): "error" | "warning" | "success" | "default" {
  switch (risk) {
    case "CRITICAL":
    case "HIGH":
      return "error";
    case "MEDIUM":
      return "warning";
    case "LOW":
      return "success";
    default:
      return "default";
  }
}
