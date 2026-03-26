# 🚀 Deployment Architecture

## 1. AWS Infrastructure Overview

```mermaid
graph TB
    subgraph "Region: ap-south-1 (Mumbai)"
        subgraph "AZ-1a"
            EKS_1A["EKS Node Group<br>m6i.xlarge × 4"]
            RDS_1A["RDS PostgreSQL<br>Primary"]
            EC_1A["ElastiCache Redis<br>Primary"]
        end
        subgraph "AZ-1b"
            EKS_1B["EKS Node Group<br>m6i.xlarge × 4"]
            RDS_1B["RDS PostgreSQL<br>Standby"]
            EC_1B["ElastiCache Redis<br>Replica"]
        end
        subgraph "AZ-1c"
            EKS_1C["EKS Node Group<br>m6i.xlarge × 2"]
        end

        MSK["Amazon MSK<br>Kafka 3.7<br>3 Brokers"]
        DOCDB["Amazon DocumentDB<br>MongoDB-compat"]
        DYNAMO["DynamoDB<br>On-demand"]
        OS["Amazon OpenSearch<br>2-node cluster"]
        ECR["ECR<br>Container Registry"]
        SM["SageMaker<br>ML Inference"]
        S3["S3<br>Images + Configs"]

        ALB["Application Load Balancer<br>+ WAF v2"]
    end

    subgraph "Global Edge"
        CF["CloudFront<br>42+ PoPs in Asia"]
        R53["Route 53<br>Latency-based"]
        GA["Global Accelerator"]
    end

    subgraph "CI/CD"
        GH["GitHub Actions"]
        ARGO["ArgoCD"]
    end

    CF --> ALB --> EKS_1A & EKS_1B & EKS_1C
    R53 --> CF
    GA --> ALB

    EKS_1A --> RDS_1A & EC_1A & MSK & DOCDB & DYNAMO & OS & SM
    EKS_1B --> RDS_1B & EC_1B & MSK & DOCDB & DYNAMO & OS & SM
    
    GH -->|"Build + Push"| ECR
    ARGO -->|"GitOps Deploy"| EKS_1A & EKS_1B & EKS_1C
```

## 2. EKS Cluster Layout

```mermaid
graph TB
    subgraph "Namespace: iwos"
        subgraph "Ingress"
            ISTIO_GW["Istio Ingress Gateway<br>3 pods"]
        end
        subgraph "Core Pods"
            GW_POD["api-gateway ×3"]
            ORD_POD["order-service ×3"]
            CART_POD["cart-service ×2"]
            PAY_POD["payment-service ×3"]
            CAT_POD["catalog-service ×2"]
        end
        subgraph "Quick Commerce Pods"
            DS_POD["darkstore-service ×3"]
            SVC_POD["serviceability ×2"]
            DISP_POD["dispatch-service ×3"]
            TRACK_POD["tracking-service ×3"]
        end
        subgraph "Intelligence Pods"
            PRED_POD["stock-predictor ×1"]
            RECOM_POD["recommendation ×1"]
            FRAUD_POD["fraud-detection ×2"]
        end
        subgraph "Infra Pods"
            CONFIG_POD["config-server ×2"]
            EUREKA_POD["discovery ×3"]
            TEMPORAL_POD["temporal-worker ×2"]
        end
    end
```

## 3. Auto-Scaling Policy

| Service | Min Pods | Max Pods | Scale Trigger |
|---------|----------|----------|--------------|
| api-gateway | 3 | 15 | CPU > 60% |
| order-service | 3 | 20 | CPU > 70% or RPS > 500 |
| cart-service | 2 | 10 | CPU > 70% |
| payment-service | 3 | 15 | CPU > 60% |
| darkstore-service | 3 | 20 | CPU > 60% (peak during flash sales) |
| tracking-service | 3 | 25 | WebSocket connections > 1000 |
| dispatch-service | 3 | 15 | Queue depth > 100 |
| search-service | 2 | 10 | Avg latency > 200ms |

## 4. CI/CD Pipeline

```mermaid
graph LR
    subgraph "CI (GitHub Actions)"
        PR["Pull Request"] --> LINT["Checkstyle + SpotBugs"]
        LINT --> TEST["Unit + Integration Tests"]
        TEST --> BUILD["Maven Build"]
        BUILD --> DOCKER["Docker Build"]
        DOCKER --> PUSH["Push to ECR"]
        PUSH --> SCAN["Trivy Security Scan"]
    end
    
    subgraph "CD (ArgoCD)"
        SCAN --> MANIFEST["Update K8s Manifests"]
        MANIFEST --> ARGO["ArgoCD Sync"]
        ARGO --> DEV["Dev Cluster"]
        DEV -->|"manual gate"| STAGING["Staging"]
        STAGING -->|"canary 10%"| PROD["Production"]
    end
```

## 5. Database Deployment

| Service | Engine | Instance | Multi-AZ | Backup | Encryption |
|---------|--------|----------|----------|--------|------------|
| Auth, Order, Payment | RDS PostgreSQL 16 | db.r6g.large | ✅ | 7-day automated | AES-256 |
| Catalog, Inventory | RDS PostgreSQL 16 | db.r6g.medium | ✅ | 7-day automated | AES-256 |
| Reviews, Recommendations | DocumentDB | db.r6g.medium | ✅ | Continuous | AES-256 |
| Tracking | DynamoDB | On-demand | Global Tables | PITR | AES-256 |
| Cart, Sessions | ElastiCache Redis 7 | cache.r6g.large | ✅ | Daily | In-transit TLS |
| Product Search | OpenSearch | r6g.large.search ×2 | ✅ | Automated | AES-256 |

## 6. Cost Estimation (Monthly)

| Resource | Specification | Estimated Cost |
|----------|--------------|----------------|
| EKS Cluster | 10 × m6i.xlarge | $1,200 |
| RDS PostgreSQL | 3 × db.r6g.large (Multi-AZ) | $900 |
| ElastiCache Redis | 1 × cache.r6g.large (Multi-AZ) | $450 |
| MSK Kafka | 3 × kafka.m5.large | $600 |
| DocumentDB | 1 × db.r6g.medium | $250 |
| OpenSearch | 2 × r6g.large.search | $350 |
| DynamoDB | On-demand (estimated) | $100 |
| CloudFront | 500GB transfer | $50 |
| S3 | 100GB storage | $5 |
| SageMaker | 1 × ml.m5.xlarge (inference) | $200 |
| **Total** | | **~$4,100/month** |
