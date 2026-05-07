export type TokenResponse = {
  tokenType: string;
  accessToken: string;
  issuer: string;
  audience: string;
  subject: string;
  username: string;
  role: string;
  permissions: string[];
  nodeIds: string[];
  region: string;
  orgId: string;
  requestId: string;
  issuedAt: string;
  expiresAt: string;
};

export type OrderIntentResponse = {
  orderIntentId: string;
  customerId: string;
  channel: string;
  paymentMode: string;
  currency: string;
  totalAmount: number;
  deliveryAddress: {
    name: string;
    line1: string;
    line2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    phone: string;
  };
  items: Array<{ sku: string; quantity: number }>;
  acceptedAt: string;
};

export type FulfillmentOrderResponse = {
  fulfillmentOrderId: string;
  workflowId: string;
  orderIntentId: string;
  customerId: string;
  fulfillmentNodeId: string;
  warehouseCode: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  tasks: Array<{
    taskId: string;
    taskType: string;
    status: string;
    assigneeId?: string;
  }>;
};

export type OrderWorkflowResponse = {
  workflowId: string;
  orderIntentId: string;
  sourceOutboxEventId: string;
  customerId: string;
  fulfillmentNodeId: string;
  status: string;
  failureReason?: string | null;
  acceptedAt?: string | null;
  completedAt?: string | null;
  createdAt: string;
  updatedAt: string;
  payment?: {
    paymentIntentId: string;
    providerReference?: string | null;
    status: string;
    failureReason?: string | null;
    processedAt?: string | null;
  } | null;
  reservations?: Array<{
    workflowReservationId: string;
    orderIntentItemId: string;
    inventoryReservationId: string;
    sku: string;
    nodeId: string;
    quantity: number;
    status: string;
    createdAt: string;
    updatedAt: string;
  }> | null;
};

export type ShipmentResponse = {
  shipmentId: string;
  fulfillmentOrderId: string;
  orderIntentId: string;
  workflowId: string;
  customerId: string;
  awbNumber: string;
  carrierCode: string;
  status: string;
  originNodeId: string;
  estimatedDeliveryAt?: string;
  manifestedAt?: string;
  dispatchedAt?: string;
  deliveredAt?: string;
  createdAt: string;
  updatedAt: string;
};

export type NetworkShipmentResponse = {
  networkShipmentId: string;
  shipmentId: string;
  fulfillmentOrderId: string;
  orderIntentId: string;
  awbNumber: string;
  carrierCode: string;
  customerId: string;
  originNodeId: string;
  currentNodeId: string;
  currentFacilityCode: string;
  status: string;
  lastScanType: string;
  lastScannedAt?: string;
  createdAt: string;
  updatedAt: string;
  scans: Array<{
    scanEventId: string;
    scanType: string;
    nodeId?: string;
    facilityCode?: string;
    notes?: string;
    occurredAt?: string;
  }>;
};

export type ScanTimelineResponse = {
  trackedShipmentId: string;
  shipmentId: string;
  networkShipmentId: string;
  fulfillmentOrderId: string;
  orderIntentId: string;
  awbNumber: string;
  carrierCode: string;
  customerId: string;
  currentStatus: string;
  lastScanType: string;
  lastScannedAt?: string;
  createdAt: string;
  updatedAt: string;
  events: Array<{
    normalizedScanEventId: string;
    scanEventId: string;
    sourceEventType: string;
    scanType: string;
    statusAfterEvent: string;
    nodeId?: string;
    facilityCode?: string;
    notes?: string;
    occurredAt?: string;
    ingestedAt: string;
  }>;
};

export type NotificationResponse = {
  notificationId: string;
  shipmentId: string;
  orderIntentId: string;
  awbNumber: string;
  audience: string;
  channel: string;
  templateCode: string;
  eventType: string;
  scanType: string;
  status: string;
  title: string;
  message: string;
  createdAt: string;
  deliveredAt?: string;
};

export type ForecastRunResponse = {
  forecastRunId: string;
  modelVersion: string;
  triggeredBy: string;
  runStatus: string;
  forecastCount: number;
  startedAt: string;
  completedAt?: string;
};

export type ForecastModelRunResponse = {
  modelRunId: string;
  forecastRunId: string;
  mlflowRunId: string;
  registeredModelName: string;
  registeredModelVersion: string;
  modelAlias: string;
  algorithm: string;
  trainingStatus: string;
  trainingSampleCount: number;
  validationSampleCount: number;
  featureCount: number;
  predictionHorizonMinutes: number;
  mae: number;
  rmse: number;
  r2: number;
  trackingUri: string;
  artifactUri: string;
  trainingStartedAt: string;
  trainingCompletedAt?: string;
  createdAt: string;
};

export type InventoryForecastResponse = {
  forecastId: string;
  forecastRunId: string;
  nodeId: string;
  sku: string;
  currentOnHandQuantity: number;
  currentReservedQuantity: number;
  availableQuantity: number;
  demandLast1h: number;
  demandLast6h: number;
  demandLast24h: number;
  predictedHourlyDemand: number;
  predicted15mDemand: number;
  predicted24hDemand: number;
  daysOfCover: number;
  stockoutRisk: string;
  recommendedReplenishmentQuantity: number;
  recommendedReorder: boolean;
  modelVersion: string;
  generatedAt: string;
};

export type ForecastSummaryResponse = {
  forecastRunId: string;
  modelVersion: string;
  runStatus: string;
  totalForecasts: number;
  criticalCount: number;
  highCount: number;
  mediumCount: number;
  lowCount: number;
  totalRecommendedReplenishmentQuantity: number;
  generatedAt: string;
  topReplenishmentForecasts: InventoryForecastResponse[];
};

export type ControlTowerSnapshotResponse = {
  controlTowerSnapshotId: string;
  snapshotType: string;
  modelVersion: string;
  generatedAt: string;
  forecastKpi: {
    totalForecasts: number;
    criticalCount: number;
    highCount: number;
    mediumCount: number;
    lowCount: number;
    totalRecommendedReplenishmentQuantity: number;
  };
  orderIntentsByStatus: Array<{ key: string; count: number }>;
  fulfillmentOrdersByStatus: Array<{ key: string; count: number }>;
  shipmentsByStatus: Array<{ key: string; count: number }>;
  networkShipmentsByStatus: Array<{ key: string; count: number }>;
  scanEventsByType: Array<{ key: string; count: number }>;
  notificationsByAudience: Array<{ key: string; count: number }>;
  notificationsByStatus: Array<{ key: string; count: number }>;
  topForecasts: Array<{
    forecastId: string;
    forecastRunId: string;
    nodeId: string;
    sku: string;
    availableQuantity: number;
    predicted15mDemand: number;
    predicted24hDemand: number;
    daysOfCover: number;
    stockoutRisk: string;
    recommendedReplenishmentQuantity: number;
  }>;
  recentExceptions: Array<{
    shipmentId: string;
    orderIntentId: string;
    awbNumber: string;
    currentStatus: string;
    lastScanType: string;
    occurredAt: string;
    notes?: string;
  }>;
};

export type NodeResponse = {
  nodeId: string;
  nodeCode: string;
  displayName: string;
  nodeType: string;
  city: string;
  state: string;
  country: string;
  postalCode: string;
  timezone: string;
  priority: number;
  supportsExpress: boolean;
  supportsParcel: boolean;
  active: boolean;
  externalReferenceId?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ReturnResponse = {
  returnRequestId: string;
  orderIntentId: string;
  fulfillmentOrderId?: string | null;
  shipmentId?: string;
  customerId: string;
  nodeId: string;
  status: string;
  reasonCode: string;
  reasonDetail?: string | null;
  itemCount: number;
  createdAt: string;
  updatedAt: string;
  requestedAt: string;
  approvedAt?: string;
  receivedAt?: string;
  items: Array<{
    sku: string;
    quantity: number;
  }>;
};
