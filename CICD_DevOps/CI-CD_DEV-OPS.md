# 🔍 CI/CD 與 DevOps 完整深入教學

---

## 🗺️ 學習路線圖

```
CI/CD 與 DevOps
├── 1️⃣ DevOps 核心概念
│   ├── 什麼是 DevOps
│   ├── CI/CD 流水線
│   └── DevOps 工具鏈全覽
├── 2️⃣ Docker 深入
│   ├── 核心原理（Namespace / Cgroups）
│   ├── Dockerfile 最佳實踐
│   ├── Docker Compose
│   └── Docker 網路與儲存
├── 3️⃣ Kubernetes 深入
│   ├── 架構原理
│   ├── 核心資源對象
│   ├── 服務發現與負載均衡
│   ├── 配置與密鑰管理
│   └── 滾動更新與回滾
├── 4️⃣ Jenkins + GitHub Actions
│   ├── Jenkins Pipeline
│   ├── GitHub Actions Workflow
│   └── 完整 CI/CD 流水線
└── 5️⃣ 實戰：Spring Boot 完整部署流程
    ├── 程式碼 → Docker Image
    ├── Image → Kubernetes
    └── 自動化流水線
```

---

## 1️⃣ DevOps 核心概念

---

### 🔵 什麼是 DevOps？

```
傳統開發模式的問題：

開發團隊：「功能寫完了，丟給運維！」
運維團隊：「你這個跑不起來！」
開發團隊：「在我電腦上可以啊！」
↓
互相指責，上線困難，交付慢

DevOps = Development + Operations
打破開發與運維的隔閡

核心理念：
┌─────────────────────────────────────────────┐
│                  DevOps 無限循環               │
│                                             │
│  Plan → Code → Build → Test → Release      │
│    ↑                                   ↓   │
│  Monitor ← Operate ← Deploy ← Deploy ←    │
└─────────────────────────────────────────────┘

目標：
✅ 更快交付（Faster Delivery）
✅ 更高品質（Higher Quality）
✅ 更穩定運行（More Stability）
✅ 更快恢復（Faster Recovery）
```

---

### 🟡 CI/CD 流水線

```
CI（Continuous Integration）持續整合：
  開發者每次提交程式碼，自動觸發：
  建置 → 單元測試 → 程式碼分析 → 打包
  → 快速發現問題，防止「整合地獄」

CD（Continuous Delivery）持續交付：
  CI 通過後，自動部署到測試/預生產環境
  生產環境需要人工確認

CD（Continuous Deployment）持續部署：
  全自動，直接部署到生產環境（需要高度信任的測試）

完整流水線：

程式碼提交
    ↓
┌───────────────────────────────────────────────────────────────┐
│                        CI 階段                                 │
│  程式碼拉取 → 編譯建置 → 單元測試 → 代碼掃描 → 建置 Docker Image  │
└───────────────────────────┬───────────────────────────────────┘
                            ↓ CI 通過
┌───────────────────────────────────────────────────────────────┐
│                        CD 階段                                 │
│  推送 Image → 部署到測試環境 → 整合測試 → 部署到預生產 → 冒煙測試  │
└───────────────────────────┬───────────────────────────────────┘
                            ↓ 人工審核（或自動）
                    部署到生產環境
                            ↓
                    監控 & 告警
```

---

### 🟠 DevOps 工具鏈全覽

```
階段                工具
──────────────────────────────────────────────
計劃（Plan）        Jira、Trello、GitHub Issues
程式碼（Code）      Git、GitHub、GitLab
建置（Build）       Maven、Gradle、npm
測試（Test）        JUnit、Mockito、Testcontainers
掃描（Scan）        SonarQube、Checkstyle、OWASP
容器（Container）   Docker、Podman
倉庫（Registry）    Docker Hub、Harbor、ECR
編排（Orchestrate） Kubernetes、Docker Swarm
CI/CD（Pipeline）  Jenkins、GitHub Actions、GitLab CI
配置（Config）      Helm、Kustomize、Ansible
監控（Monitor）     Prometheus、Grafana、ELK
服務網格（Mesh）    Istio、Linkerd
```

---

## 2️⃣ Docker 深入

---

### 🔵 Docker 核心原理

```
Docker 不是虛擬機！

虛擬機（VM）：                    Docker 容器：
┌────────────────────┐           ┌────────────────────┐
│  App A  │  App B   │           │  App A  │  App B   │
├─────────┼──────────┤           ├─────────┼──────────┤
│ Guest OS│ Guest OS │           │    Container       │
├─────────┴──────────┤           │    Runtime         │
│    Hypervisor      │           ├────────────────────┤
├────────────────────┤           │    Host OS         │
│    Host OS         │           ├────────────────────┤
├────────────────────┤           │    Hardware        │
│    Hardware        │           └────────────────────┘
└────────────────────┘

VM：每個都有完整 OS，啟動慢（分鐘），佔用 GB
容器：共享 Host OS 核心，啟動快（秒），佔用 MB
```

```
Docker 底層技術：

① Namespace（命名空間）- 隔離
  ┌────────────────────────────────────────────┐
  │  PID Namespace    → 容器有自己的進程 ID 空間  │
  │  Network Namespace → 獨立的網路介面、IP       │
  │  Mount Namespace  → 獨立的文件系統視圖        │
  │  UTS Namespace    → 獨立的主機名             │
  │  IPC Namespace    → 獨立的進程間通訊          │
  │  User Namespace   → 獨立的用戶/組 ID         │
  └────────────────────────────────────────────┘

② Cgroups（Control Groups）- 限制資源
  限制 CPU 使用率：--cpus="0.5"（最多用半個核）
  限制記憶體：     --memory="512m"
  限制磁碟 IO：   --device-write-bps

③ Union File System（UnionFS）- 分層儲存
  Image 是只讀的分層結構
  容器運行時加一個可寫層（Container Layer）

  ┌─────────────────────────────┐ ← Container Layer（可寫，容器刪除即消失）
  ├─────────────────────────────┤ ← App Layer（你的應用）
  ├─────────────────────────────┤ ← Dependencies Layer（依賴）
  ├─────────────────────────────┤ ← JDK Layer
  └─────────────────────────────┘ ← Base OS Layer（ubuntu/alpine）
        Image（只讀分層）

優點：多個容器共享相同的底層（節省空間）
      ubuntu 層只需要下載一次
```

---

### 🟡 Dockerfile 最佳實踐

```dockerfile
# ❌ 糟糕的 Dockerfile（初學者常犯）
FROM ubuntu:20.04
RUN apt-get update
RUN apt-get install -y openjdk-17-jdk
RUN apt-get install -y maven
COPY . /app
WORKDIR /app
RUN mvn clean package
EXPOSE 8080
CMD ["java", "-jar", "target/app.jar"]

# 問題：
# 1. ubuntu 太大（72MB），應用不需要 apt-get
# 2. 每個 RUN 都是一層，層數太多
# 3. COPY . 複製了所有文件包括源碼（Image 很大）
# 4. Maven 也打進去了（不需要）
# 5. 沒有利用 Docker 緩存（每次都全量重建）
```

```dockerfile
# ✅ 多階段建置（Multi-Stage Build）最佳實踐

# ──────────── 階段一：建置階段 ────────────
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# ⭐ 技巧：先複製 pom.xml，下載依賴（利用 Docker 緩存！）
# 只要 pom.xml 不變，這一層永遠使用緩存，不重複下載依賴
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 再複製源碼（源碼經常變動，放後面）
COPY src ./src

# 建置（跳過測試，測試在 CI 中已執行）
RUN mvn clean package -DskipTests -B

# ──────────── 階段二：運行階段 ────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
# 只用 JRE（不需要 JDK），alpine 版本（~60MB vs ubuntu ~400MB）

# 安全：不用 root 運行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# 只從建置階段複製 jar（不含源碼、Maven、JDK！）
COPY --from=builder /build/target/app-*.jar app.jar

# ⭐ JVM 參數最佳化（容器友好）
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"
# UseContainerSupport：讓 JVM 感知容器的 CPU/記憶體限制
# MaxRAMPercentage：最多用容器記憶體的 75%
# java.security.egd：加速隨機數生成（解決容器啟動慢問題）

# 健康檢查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 切換非 root 用戶
USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

```bash
# 建置與查看分層
docker build -t myapp:1.0.0 .

# 查看 Image 大小
docker images myapp

# 查看 Image 分層
docker history myapp:1.0.0

# 標準 Image 命名規範
# registry/namespace/repository:tag
docker build -t harbor.example.com/backend/order-service:v1.2.3 .

# 推送到 Registry
docker push harbor.example.com/backend/order-service:v1.2.3
```

---

### 🟠 Docker Compose（本地開發環境）

```yaml
# docker-compose.yml（完整的本地開發環境）
version: '3.8'

services:

  # ── 應用服務 ──
  app:
    build:
      context: .
      dockerfile: Dockerfile
      target: runtime
    image: myapp:dev
    container_name: myapp
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/mydb
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: password
      SPRING_REDIS_HOST: redis
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
    volumes:
      - ./logs:/app/logs
    networks:
      - backend
    restart: unless-stopped

  # ── MySQL ──
  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: mydb
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./docker/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-ppassword"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend

  # ── Redis ──
  redis:
    image: redis:7.2-alpine
    container_name: redis
    command: redis-server --requirepass password --maxmemory 512mb --maxmemory-policy allkeys-lru
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "password", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3
    networks:
      - backend

  # ── RabbitMQ ──
  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: password
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - backend

  # ── Nginx 反向代理 ──
  nginx:
    image: nginx:1.25-alpine
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./docker/nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - app
    networks:
      - backend

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:

networks:
  backend:
    driver: bridge
```

```bash
# Docker Compose 常用指令

docker-compose up -d              # 啟動所有服務（背景執行）
docker-compose up -d --build      # 啟動並重新建置
docker-compose ps                 # 查看服務狀態
docker-compose logs -f app        # 查看日誌（持續追蹤）
docker-compose restart app        # 只重啟 app 服務
docker-compose down               # 停止並刪除容器（保留數據卷）
docker-compose down -v            # 停止並刪除容器+數據卷（慎用！）
docker-compose exec app sh        # 進入容器 shell
docker-compose up -d --scale app=3 # 擴展服務實例數量
```

---

### 🔴 Docker 網路

```
Docker 網路模式：

① Bridge（預設）：
   容器有自己的 IP，通過虛擬網橋通訊
   容器間可以用 container_name 互相訪問

   Host ─── docker0(172.17.0.1) ─── container1(172.17.0.2)
                                 └── container2(172.17.0.3)

② Host：
   容器直接使用主機網路，效能最好，但沒有網路隔離
   --network host

③ None：
   無網路（完全隔離）

④ Overlay：
   跨主機的容器通訊（Swarm / K8s 使用）
```

```bash
# 自定義 Bridge 網路（推薦）
docker network create my-network

# 容器加入網路後，可以用 container name 直接互相訪問
docker run --network my-network --name app myapp
docker run --network my-network --name mysql mysql:8.0
# app 容器中可以直接用 mysql:3306 訪問 MySQL ✅
```

---

## 3️⃣ Kubernetes 深入

---

### 🔵 K8s 整體架構

```
Kubernetes 集群架構：

┌────────────────────────────────────────────────────────────────┐
│                      Control Plane（主節點）                    │
│                                                                │
│  ┌──────────────┐  ┌───────────┐  ┌────────────────────────┐  │
│  │  API Server  │  │  etcd     │  │  Controller Manager    │  │
│  │（集群入口，   │  │（分散式    │  │  ├─ Node Controller    │  │
│  │  所有操作     │  │  KV存儲，  │  │  ├─ Deployment Ctrl   │  │
│  │  都經過它）   │  │  集群狀態） │  │  ├─ ReplicaSet Ctrl   │  │
│  └──────────────┘  └───────────┘  │  └─ Service Ctrl       │  │
│                                   └────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Scheduler（調度器）                                       │  │
│  │  決定 Pod 應該運行在哪個 Node 上                            │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
                              │ 控制
         ┌────────────────────┼────────────────────┐
         ↓                    ↓                    ↓
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│   Worker Node 1  │ │   Worker Node 2  │ │   Worker Node 3  │
│  ┌────┐  ┌────┐  │ │  ┌────┐  ┌────┐  │ │  ┌────┐          │
│  │Pod │  │Pod │  │ │  │Pod │  │Pod │  │ │  │Pod │          │
│  └────┘  └────┘  │ │  └────┘  └────┘  │ │  └────┘          │
│  ┌────────────┐  │ │  ┌────────────┐  │ │  ┌────────────┐  │
│  │  kubelet   │  │ │  │  kubelet   │  │ │  │  kubelet   │  │
│  │ kube-proxy │  │ │  │ kube-proxy │  │ │  │ kube-proxy │  │
│  └────────────┘  │ │  └────────────┘  │ │  └────────────┘  │
└──────────────────┘ └──────────────────┘ └──────────────────┘

核心組件職責：
API Server：   集群的統一入口，所有操作都通過 RESTful API
etcd：         儲存所有集群狀態（高可用 KV 存儲）
Scheduler：    根據資源需求選擇最合適的 Node
Controller：   持續監控集群狀態，確保實際狀態 = 期望狀態
kubelet：      每個 Node 上的代理，負責 Pod 的生命週期
kube-proxy：   每個 Node 上的網路代理，實現 Service 負載均衡
```

---

### 🟡 核心資源對象

#### Pod（最小部署單元）

```yaml
# pod.yaml
apiVersion: v1
kind: Pod
metadata:
  name: myapp-pod
  namespace: production
  labels:
    app: myapp
    version: v1.0.0
spec:
  containers:
    - name: myapp
      image: harbor.example.com/backend/myapp:v1.0.0

      resources:
        requests:
          cpu: "250m"
          memory: "512Mi"
        limits:
          cpu: "500m"
          memory: "1Gi"

      ports:
        - containerPort: 8080

      env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: myapp-secrets
              key: db-password

      # 存活探針（失敗則重啟容器）
      livenessProbe:
        httpGet:
          path: /actuator/health/liveness
          port: 8080
        initialDelaySeconds: 60
        periodSeconds: 15
        failureThreshold: 3

      # 就緒探針（失敗則從 Service 摘除）
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 8080
        initialDelaySeconds: 30
        periodSeconds: 10
        failureThreshold: 3

      # 啟動探針（慢啟動應用用這個）
      startupProbe:
        httpGet:
          path: /actuator/health
          port: 8080
        failureThreshold: 30
        periodSeconds: 10
```

---

#### Deployment（管理無狀態應用）

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: myapp-deployment
  namespace: production
spec:
  replicas: 3

  selector:
    matchLabels:
      app: myapp

  # 滾動更新策略
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # 更新時最多多出 1 個 Pod
      maxUnavailable: 0  # 更新時最多不可用 0 個（始終保持 3 個可用）
      # 這個配置保證 0 停機更新！

  template:
    metadata:
      labels:
        app: myapp
        version: v1.0.0
    spec:
      # Pod 反親和性（讓 Pod 分散到不同 Node）
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 100
              podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: app
                      operator: In
                      values:
                        - myapp
                topologyKey: kubernetes.io/hostname

      terminationGracePeriodSeconds: 60

      containers:
        - name: myapp
          image: harbor.example.com/backend/myapp:v1.0.0
          imagePullPolicy: Always

          resources:
            requests:
              cpu: "250m"
              memory: "512Mi"
            limits:
              cpu: "1000m"
              memory: "1Gi"

          ports:
            - containerPort: 8080

          envFrom:
            - configMapRef:
                name: myapp-config
            - secretRef:
                name: myapp-secrets

          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 60
            periodSeconds: 15

          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10

          volumeMounts:
            - name: config-volume
              mountPath: /app/config
              readOnly: true
            - name: log-volume
              mountPath: /app/logs

      volumes:
        - name: config-volume
          configMap:
            name: myapp-config-files
        - name: log-volume
          emptyDir: {}

      imagePullSecrets:
        - name: harbor-credentials
```

---

#### Service（服務發現與負載均衡）

```yaml
# ── ClusterIP（集群內部訪問，預設）──
apiVersion: v1
kind: Service
metadata:
  name: myapp-service
  namespace: production
spec:
  type: ClusterIP
  selector:
    app: myapp
  ports:
    - name: http
      port: 80
      targetPort: 8080

---
# ── NodePort（暴露到節點端口，適合測試）──
apiVersion: v1
kind: Service
metadata:
  name: myapp-nodeport
spec:
  type: NodePort
  selector:
    app: myapp
  ports:
    - port: 80
      targetPort: 8080
      nodePort: 30080   # 訪問方式：http://<Node_IP>:30080

---
# ── LoadBalancer（雲平台負載均衡器，適合生產）──
apiVersion: v1
kind: Service
metadata:
  name: myapp-lb
spec:
  type: LoadBalancer
  selector:
    app: myapp
  ports:
    - port: 80
      targetPort: 8080
```

---

#### Ingress（HTTP 路由層）

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: myapp-ingress
  namespace: production
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    nginx.ingress.kubernetes.io/limit-rps: "100"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  ingressClassName: nginx

  tls:
    - hosts:
        - api.example.com
      secretName: myapp-tls-secret

  rules:
    - host: api.example.com
      http:
        paths:
          - path: /api/orders
            pathType: Prefix
            backend:
              service:
                name: order-service
                port:
                  number: 80

          - path: /api/users
            pathType: Prefix
            backend:
              service:
                name: user-service
                port:
                  number: 80

          - path: /api/products
            pathType: Prefix
            backend:
              service:
                name: product-service
                port:
                  number: 80
```

---

#### ConfigMap 與 Secret

```yaml
# configmap.yaml（非敏感配置）
apiVersion: v1
kind: ConfigMap
metadata:
  name: myapp-config
  namespace: production
data:
  SPRING_PROFILES_ACTIVE: "production"
  SERVER_PORT: "8080"
  LOG_LEVEL: "INFO"

  application.yml: |
    spring:
      datasource:
        url: jdbc:mysql://mysql-service:3306/mydb
        driver-class-name: com.mysql.cj.jdbc.Driver
      redis:
        host: redis-service
        port: 6379
    server:
      port: 8080

---
# secret.yaml（敏感配置，Base64 編碼）
apiVersion: v1
kind: Secret
metadata:
  name: myapp-secrets
  namespace: production
type: Opaque
data:
  # echo -n "mypassword" | base64
  db-password:    bXlwYXNzd29yZA==
  redis-password: cmVkaXNwYXNz
  jwt-secret:     c3VwZXItc2VjcmV0LWtleQ==

# ⚠️ Secret 只是 Base64 編碼，不是加密！
# 生產環境建議：
# ① Vault（HashiCorp）- 企業級密鑰管理
# ② Sealed Secrets（Bitnami）- 加密儲存在 Git
# ③ 雲平台 KMS（AWS Secrets Manager / GCP Secret Manager）
```

```bash
# 從命令行建立 Secret（不用手動 base64）
kubectl create secret generic myapp-secrets \
  --from-literal=db-password=mypassword \
  --from-literal=redis-password=redispass \
  -n production

# 從文件建立 Secret
kubectl create secret generic tls-secret \
  --from-file=tls.crt=./cert.pem \
  --from-file=tls.key=./key.pem \
  -n production
```

---

#### HPA（Horizontal Pod Autoscaler）自動擴縮容

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: myapp-hpa
  namespace: production
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: myapp-deployment

  minReplicas: 2
  maxReplicas: 20

  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 70

    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 80

    - type: Pods
      pods:
        metric:
          name: http_requests_per_second
        target:
          type: AverageValue
          averageValue: "1000"

  behavior:
    scaleUp:
      stabilizationWindowSeconds: 30
      policies:
        - type: Pods
          value: 4
          periodSeconds: 60
    scaleDown:
      stabilizationWindowSeconds: 300   # 5分鐘，防止抖動
      policies:
        - type: Pods
          value: 2
          periodSeconds: 60
```

---

### 🔴 滾動更新與回滾

```bash
# ── 滾動更新 ──

# 方式一：更新 Image（最常用）
kubectl set image deployment/myapp-deployment \
  myapp=harbor.example.com/backend/myapp:v1.1.0 \
  -n production

# 查看更新進度
kubectl rollout status deployment/myapp-deployment -n production

# 查看更新歷史
kubectl rollout history deployment/myapp-deployment -n production
# REVISION  CHANGE-CAUSE
# 1         初始部署
# 2         更新到 v1.1.0

# ── 回滾 ──

# 回滾到上一個版本
kubectl rollout undo deployment/myapp-deployment -n production

# 回滾到指定版本
kubectl rollout undo deployment/myapp-deployment \
  --to-revision=2 -n production

# 記錄變更原因
kubectl annotate deployment/myapp-deployment \
  kubernetes.io/change-cause="修復登入 Bug，更新到 v1.1.0" \
  -n production
```

```
滾動更新流程（maxSurge=1, maxUnavailable=0, replicas=3）：

初始狀態：  [v1] [v1] [v1]
步驟 1：    [v1] [v1] [v1] [v2]     ← 新增 v2（surge）
步驟 2：    [v1] [v1]      [v2]     ← 移除一個 v1
步驟 3：    [v1] [v1] [v2] [v2]     ← 新增 v2
步驟 4：    [v1]      [v2] [v2]     ← 移除一個 v1
步驟 5：    [v1] [v2] [v2] [v2]     ← 新增 v2
步驟 6：         [v2] [v2] [v2]     ← 移除最後一個 v1

✅ 全程保持 3 個可用 Pod（0 停機！）
```

---

### 🟠 StatefulSet（有狀態應用）

```yaml
# statefulset.yaml（適合 MySQL、Kafka、Elasticsearch 等）
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: production
spec:
  serviceName: mysql-headless
  replicas: 3
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
        - name: mysql
          image: mysql:8.0
          env:
            - name: MYSQL_ROOT_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: mysql-secret
                  key: root-password
          ports:
            - containerPort: 3306
          volumeMounts:
            - name: mysql-data
              mountPath: /var/lib/mysql

  # 每個 Pod 都有自己的 PVC！
  volumeClaimTemplates:
    - metadata:
        name: mysql-data
      spec:
        accessModes: ["ReadWriteOnce"]
        storageClassName: ssd-storage
        resources:
          requests:
            storage: 100Gi

# StatefulSet 的 Pod 有穩定的網路標識：
#   mysql-0、mysql-1、mysql-2
# 刪除重建後，名字和 PVC 不變！
# 適合：主從 MySQL、Kafka、ZooKeeper、Redis Cluster
```

---

## 4️⃣ Jenkins + GitHub Actions

---

### 🔵 Jenkins Pipeline

```groovy
// Jenkinsfile（放在項目根目錄）
pipeline {
    agent {
        kubernetes {
            yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
    - name: maven
      image: maven:3.9.5-eclipse-temurin-17-alpine
      command: ['sleep', 'infinity']
      resources:
        requests:
          cpu: '500m'
          memory: '1Gi'
    - name: docker
      image: docker:24-dind
      securityContext:
        privileged: true
      volumeMounts:
        - name: docker-sock
          mountPath: /var/run/docker.sock
  volumes:
    - name: docker-sock
      hostPath:
        path: /var/run/docker.sock
"""
        }
    }

    environment {
        APP_NAME        = 'myapp'
        HARBOR_REGISTRY = 'harbor.example.com'
        HARBOR_REPO     = "${HARBOR_REGISTRY}/backend/${APP_NAME}"
        IMAGE_TAG       = "${BUILD_NUMBER}"
        K8S_NAMESPACE   = 'production'
        HARBOR_CREDS    = credentials('harbor-credentials')
        KUBECONFIG      = credentials('k8s-config')
        SONAR_TOKEN     = credentials('sonar-token')
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                    env.IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT_SHORT}"
                    echo "Building: ${APP_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('Code Analysis') {
            parallel {
                stage('Unit Test') {
                    steps {
                        container('maven') {
                            sh 'mvn test --no-transfer-progress'
                        }
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/*.xml'
                            jacoco(
                                execPattern: 'target/*.exec',
                                minimumLineCoverage: '80'
                            )
                        }
                    }
                }

                stage('SonarQube') {
                    steps {
                        container('maven') {
                            withSonarQubeEnv('SonarQube') {
                                sh """
                                    mvn sonar:sonar \
                                        -Dsonar.projectKey=${APP_NAME} \
                                        -Dsonar.token=${SONAR_TOKEN} \
                                        --no-transfer-progress
                                """
                            }
                        }
                    }
                }

                stage('Security Scan') {
                    steps {
                        container('maven') {
                            sh 'mvn dependency-check:check -DfailBuildOnCVSS=7 --no-transfer-progress'
                        }
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build & Push Image') {
            steps {
                container('docker') {
                    sh """
                        echo ${HARBOR_CREDS_PSW} | \
                            docker login ${HARBOR_REGISTRY} \
                            -u ${HARBOR_CREDS_USR} \
                            --password-stdin

                        DOCKER_BUILDKIT=1 docker build \
                            --cache-from ${HARBOR_REPO}:cache \
                            --build-arg BUILDKIT_INLINE_CACHE=1 \
                            -t ${HARBOR_REPO}:${IMAGE_TAG} \
                            -t ${HARBOR_REPO}:latest \
                            .

                        docker push ${HARBOR_REPO}:${IMAGE_TAG}
                        docker push ${HARBOR_REPO}:latest
                        docker push ${HARBOR_REPO}:cache
                    """
                }
            }
        }

        stage('Deploy to Staging') {
            steps {
                container('maven') {
                    sh """
                        kubectl --kubeconfig=${KUBECONFIG} \
                            set image deployment/${APP_NAME}-deployment \
                            ${APP_NAME}=${HARBOR_REPO}:${IMAGE_TAG} \
                            -n staging

                        kubectl --kubeconfig=${KUBECONFIG} \
                            rollout status deployment/${APP_NAME}-deployment \
                            -n staging --timeout=300s
                    """
                }
            }
        }

        stage('Integration Test') {
            steps {
                container('maven') {
                    sh """
                        mvn verify \
                            -Pintegration-test \
                            -Dapp.url=http://myapp-service.staging.svc.cluster.local \
                            --no-transfer-progress
                    """
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    input message: "確認部署 ${APP_NAME}:${IMAGE_TAG} 到生產環境？",
                          ok: "部署！",
                          submitter: "tech-lead,devops-team"
                }

                container('maven') {
                    sh """
                        kubectl --kubeconfig=${KUBECONFIG} \
                            set image deployment/${APP_NAME}-deployment \
                            ${APP_NAME}=${HARBOR_REPO}:${IMAGE_TAG} \
                            -n production

                        kubectl --kubeconfig=${KUBECONFIG} \
                            rollout status deployment/${APP_NAME}-deployment \
                            -n production --timeout=300s

                        kubectl --kubeconfig=${KUBECONFIG} \
                            annotate deployment/${APP_NAME}-deployment \
                            kubernetes.io/change-cause="Jenkins Build #${BUILD_NUMBER}: ${IMAGE_TAG}" \
                            -n production
                    """
                }
            }
        }
    }

    post {
        success {
            slackSend(
                color: 'good',
                message: "✅ ${APP_NAME}:${IMAGE_TAG} 部署成功！\n${BUILD_URL}"
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "❌ ${APP_NAME} 建置失敗！\n${BUILD_URL}"
            )
            script {
                if (env.BRANCH_NAME == 'main') {
                    container('maven') {
                        sh """
                            kubectl --kubeconfig=${KUBECONFIG} \
                                rollout undo deployment/${APP_NAME}-deployment \
                                -n production
                        """
                    }
                }
            }
        }
        always {
            cleanWs()
        }
    }
}
```

---

### 🟡 GitHub Actions Workflow

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  release:
    types: [published]

env:
  JAVA_VERSION: '17'
  REGISTRY:     ghcr.io
  IMAGE_NAME:   ${{ github.repository }}

jobs:
  # ══════════════════════════════════════
  # Job 1：測試與程式碼品質
  # ══════════════════════════════════════
  test:
    name: 🧪 Test & Quality
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: testpassword
          MYSQL_DATABASE: testdb
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=5

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: ☕ Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: 🧪 Run Tests
        run: |
          mvn test \
            -Dspring.profiles.active=test \
            -Dspring.datasource.url=jdbc:mysql://localhost:3306/testdb \
            -Dspring.redis.host=localhost \
            --no-transfer-progress

      - name: 📊 Upload Coverage Report
        uses: codecov/codecov-action@v3
        with:
          files: target/site/jacoco/jacoco.xml
          fail_ci_if_error: true
          minimum_coverage: 80

      - name: 🔍 SonarQube Analysis
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN:  ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn sonar:sonar \
            -Dsonar.projectKey=myapp \
            -Dsonar.host.url=https://sonar.example.com \
            --no-transfer-progress

  # ══════════════════════════════════════
  # Job 2：建置 Docker Image
  # ══════════════════════════════════════
  build:
    name: 🐳 Build & Push Image
    needs: test
    runs-on: ubuntu-latest

    outputs:
      image-tag:    ${{ steps.meta.outputs.tags }}
      image-digest: ${{ steps.build.outputs.digest }}

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: 🔑 Login to Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: 🏷️ Docker Metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=pr
            type=ref,event=branch
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}
            type=sha,prefix=sha-

      - name: 🛠️ Setup Buildx
        uses: docker/setup-buildx-action@v3

      - name: 🐳 Build & Push
        id: build
        uses: docker/build-push-action@v5
        with:
          context: .
          push: ${{ github.event_name != 'pull_request' }}
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          platforms: linux/amd64,linux/arm64
          cache-from: type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:cache
          cache-to:   type=registry,ref=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:cache,mode=max

      - name: 🛡️ Container Security Scan（Trivy）
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }}
          format:    'sarif'
          output:    'trivy-results.sarif'
          severity:  'CRITICAL,HIGH'
          exit-code: '1'

  # ══════════════════════════════════════
  # Job 3：部署到 Staging
  # ══════════════════════════════════════
  deploy-staging:
    name: 🚀 Deploy to Staging
    needs: build
    runs-on: ubuntu-latest
    environment:
      name: staging
      url: https://staging.example.com

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: ⚙️ Setup kubectl
        uses: azure/setup-kubectl@v3
        with:
          version: 'v1.28.0'

      - name: 🔑 Configure K8s
        run: |
          mkdir -p ~/.kube
          echo "${{ secrets.KUBECONFIG_STAGING }}" | base64 -d > ~/.kube/config

      - name: 🚀 Deploy to Staging
        run: |
          IMAGE="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:sha-${{ github.sha }}"
          kubectl set image deployment/myapp-deployment myapp=${IMAGE} -n staging
          kubectl rollout status deployment/myapp-deployment -n staging --timeout=300s

      - name: 🧪 Smoke Test
        run: |
          sleep 30
          curl -f https://staging.example.com/actuator/health || exit 1

  # ══════════════════════════════════════
  # Job 4：部署到生產（只有 Release）
  # ══════════════════════════════════════
  deploy-production:
    name: 🌟 Deploy to Production
    needs: deploy-staging
    runs-on: ubuntu-latest
    if: github.event_name == 'release'

    environment:
      name: production
      url: https://api.example.com

    steps:
      - name: 📥 Checkout
        uses: actions/checkout@v4

      - name: ⚙️ Setup kubectl
        uses: azure/setup-kubectl@v3

      - name: 🔑 Configure K8s
        run: |
          mkdir -p ~/.kube
          echo "${{ secrets.KUBECONFIG_PROD }}" | base64 -d > ~/.kube/config

      - name: 🌟 Deploy to Production
        run: |
          IMAGE="${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.event.release.tag_name }}"
          kubectl set image deployment/myapp-deployment myapp=${IMAGE} -n production
          kubectl rollout status deployment/myapp-deployment -n production --timeout=300s
          kubectl annotate deployment/myapp-deployment \
            kubernetes.io/change-cause="Release ${{ github.event.release.tag_name }}" \
            -n production

      - name: 📣 Slack Notification
        uses: slackapi/slack-github-action@v1.25.0
        with:
          payload: |
            {
              "text": "✅ *${{ github.repository }}* 已部署到生產！\n版本：${{ github.event.release.tag_name }}"
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## 5️⃣ 實戰：Spring Boot 完整部署流程

---

### 🔵 Helm Chart（K8s 應用包管理）

```
Helm 是 K8s 的「套件管理器」（類似 apt / npm）
將複雜的 K8s YAML 模板化，方便管理多環境

目錄結構：
myapp/
├── Chart.yaml              ← Chart 基本資訊
├── values.yaml             ← 預設值
├── values-staging.yaml     ← Staging 覆蓋值
├── values-prod.yaml        ← 生產覆蓋值
└── templates/
    ├── deployment.yaml
    ├── service.yaml
    ├── ingress.yaml
    ├── configmap.yaml
    ├── hpa.yaml
    └── _helpers.tpl        ← 共用模板函數
```

```yaml
# values.yaml（預設值）
replicaCount: 2

image:
  repository:  harbor.example.com/backend/myapp
  tag:         latest
  pullPolicy:  IfNotPresent

service:
  type:       ClusterIP
  port:       80
  targetPort: 8080

ingress:
  enabled: true
  host:    api.example.com
  tls:     true

resources:
  requests:
    cpu:    250m
    memory: 512Mi
  limits:
    cpu:    1000m
    memory: 1Gi

autoscaling:
  enabled:                        true
  minReplicas:                    2
  maxReplicas:                    10
  targetCPUUtilizationPercentage: 70

config:
  springProfile: production
  logLevel:      INFO

---
# values-prod.yaml（生產覆蓋）
replicaCount: 5
resources:
  requests:
    cpu:    500m
    memory: 1Gi
  limits:
    cpu:    2000m
    memory: 2Gi
autoscaling:
  maxReplicas: 20
```

```bash
# Helm 常用指令

# 安裝（部署到 K8s）
helm install myapp ./myapp \
  -n production \
  -f values-prod.yaml \
  --set image.tag=v1.2.3

# 升級（更新）
helm upgrade myapp ./myapp \
  -n production \
  -f values-prod.yaml \
  --set image.tag=v1.2.4

# 查看已安裝的 Chart
helm list -n production

# 查看歷史版本
helm history myapp -n production

# 回滾
helm rollback myapp 2 -n production

# 模擬安裝（不實際執行，查看生成的 YAML）
helm install myapp ./myapp --dry-run --debug

# 卸載
helm uninstall myapp -n production
```

---

### 🟡 完整 CI/CD 流程總覽

```
開發者 git push → GitHub
         │
         ▼
GitHub Actions 觸發
         │
    ┌────┴──────────────────────────────────────┐
    │              CI 階段                       │
    │  ① 拉取程式碼                              │
    │  ② Maven 建置                             │
    │  ③ 單元測試 + 覆蓋率檢查（>80%）           │
    │  ④ SonarQube 靜態分析                     │
    │  ⑤ OWASP 依賴漏洞掃描                     │
    │  ⑥ 建置 Docker Image（多平台）             │
    │  ⑦ Trivy 容器安全掃描                     │
    │  ⑧ 推送到 Harbor Registry                 │
    └────┬──────────────────────────────────────┘
         │ CI 通過
    ┌────┴──────────────────────────────────────┐
    │            CD - Staging 階段               │
    │  ① Helm upgrade（更新 Staging）           │
    │  ② 等待 Rollout 完成                      │
    │  ③ Smoke Test（基本功能驗證）             │
    │  ④ 整合測試                               │
    └────┬──────────────────────────────────────┘
         │ Staging 通過
         │ ↓ 打 Release Tag 或人工審核
    ┌────┴──────────────────────────────────────┐
    │            CD - Production 階段            │
    │  ① GitHub Environment 保護（審核）        │
    │  ② Helm upgrade（滾動更新生產）           │
    │  ③ 等待 Rollout 完成                      │
    │  ④ 生產冒煙測試                           │
    │  ⑤ 通知 Slack                            │
    └────┬──────────────────────────────────────┘
         │
    ┌────┴──────────────────────────────────────┐
    │              監控告警                      │
    │  Prometheus 指標 → Grafana 儀表板          │
    │  ELK 日誌分析                             │
    │  PagerDuty 告警通知                       │
    └───────────────────────────────────────────┘
```

---

### 🔴 常用 kubectl 指令速查

```bash
# ── 查看資源 ──
kubectl get pods -n production                  # 查看 Pod
kubectl get pods -n production -o wide          # 顯示 Node 信息
kubectl get all -n production                   # 查看所有資源
kubectl describe pod myapp-xxx -n production    # 詳細信息（排查問題）

# ── 日誌 ──
kubectl logs myapp-pod-xxx -n production        # 查看日誌
kubectl logs myapp-pod-xxx -n production -f     # 追蹤日誌
kubectl logs myapp-pod-xxx -n production \
  --previous                                    # 查看上次崩潰的日誌
kubectl logs -l app=myapp -n production \
  --max-log-requests=5                          # 查看所有 Pod 日誌

# ── 進入容器 ──
kubectl exec -it myapp-pod-xxx -n production \
  -- /bin/sh                                    # 進入容器 Shell

# ── 資源管理 ──
kubectl apply -f deployment.yaml                # 應用配置
kubectl delete pod myapp-pod-xxx -n production  # 刪除 Pod（自動重建）
kubectl scale deployment myapp-deployment \
  --replicas=5 -n production                    # 手動擴縮容

# ── 臨時調試 ──
kubectl port-forward pod/myapp-pod-xxx \
  8080:8080 -n production                       # 本地端口轉發（調試用）
kubectl run debug-pod --rm -it \
  --image=busybox -- /bin/sh                    # 臨時調試 Pod

# ── 資源使用情況 ──
kubectl top nodes                               # 查看節點資源使用
kubectl top pods -n production                  # 查看 Pod 資源使用

# ── 強制重啟（滾動重啟）──
kubectl rollout restart deployment/myapp-deployment -n production
```

---

## 📝 CI/CD 與 DevOps 總結

| 主題 | 核心要點 |
|------|---------|
| **Docker 原理** | Namespace（隔離）+ Cgroups（限制）+ UnionFS（分層）|
| **Dockerfile** | 多階段建置、利用緩存層、非 root 用戶、最小化 Image |
| **K8s 架構** | Control Plane（API Server/etcd/調度器）+ Worker Node |
| **Pod** | 最小部署單元，三種探針（liveness/readiness/startup）|
| **Deployment** | 無狀態應用，滾動更新 maxSurge/maxUnavailable |
| **Service** | ClusterIP（內部）/ NodePort（測試）/ LoadBalancer（生產）|
| **Ingress** | HTTP 路由層，統一入口，TLS 終止 |
| **HPA** | 基於 CPU/記憶體自動擴縮容，防止頻繁抖動 |
| **ConfigMap/Secret** | 配置與代碼分離，Secret 生產用 Vault |
| **Jenkins** | Jenkinsfile Pipeline，多 Stage 並行，自動回滾 |
| **GitHub Actions** | YAML Workflow，Job 依賴，Environment 保護 |
| **Helm** | K8s 套件管理，模板化，多環境管理 |
