# 📊 監控與可觀測性 完整深入教學

---

## 🗺️ 學習路線圖

```
監控與可觀測性
├── 1️⃣ 可觀測性三大支柱
│   ├── Metrics（指標）
│   ├── Logs（日誌）
│   └── Traces（鏈路追蹤）
├── 2️⃣ Prometheus 深入
│   ├── 架構原理
│   ├── 資料模型與 PromQL
│   ├── Spring Boot 整合
│   └── 告警規則（AlertManager）
├── 3️⃣ Grafana 深入
│   ├── 儀表板設計原則
│   ├── 關鍵面板配置
│   └── 變數（Variables）
├── 4️⃣ ELK Stack 深入
│   ├── ELK 架構
│   ├── Spring Boot 結構化日誌
│   ├── Filebeat 配置
│   ├── Logstash Pipeline
│   └── Kibana 使用技巧
├── 5️⃣ 分散式鏈路追蹤
│   ├── SkyWalking 架構
│   ├── Spring Boot 整合
│   └── 鏈路分析實戰
└── 6️⃣ 實戰：完整可觀測性平台
    ├── 整合架構設計
    ├── Docker Compose 部署
    └── 生產告警體系
```

---

## 1️⃣ 可觀測性三大支柱

---

### 🔵 什麼是可觀測性？

```
監控（Monitoring）vs 可觀測性（Observability）：

監控：已知的問題 → 設置閾值 → 超過告警
     "我知道可能出什麼問題，所以我盯著那些指標"

可觀測性：未知的問題 → 通過數據推斷 → 找到根因
         "系統出了什麼問題，我可以從數據中找到答案"

可觀測性三大支柱：

  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
  │   Metrics    │  │     Logs     │  │   Traces     │
  │   （指標）    │  │   （日誌）    │  │ （鏈路追蹤）   │
  │              │  │              │  │              │
  │ 系統健康的    │  │ 發生了什麼    │  │ 請求經過了    │
  │ 數字化呈現    │  │ 詳細記錄      │  │ 哪些服務      │
  │              │  │              │  │              │
  │ CPU、記憶體   │  │ 錯誤訊息      │  │ A→B→C→D      │
  │ QPS、延遲    │  │ 業務日誌      │  │ 每段耗時      │
  │              │  │              │  │              │
  │ Prometheus   │  │   ELK Stack  │  │  SkyWalking  │
  │  + Grafana   │  │              │  │   / Zipkin   │
  └──────────────┘  └──────────────┘  └──────────────┘

三個支柱相輔相成：
  Metrics 告訴你：「訂單服務 P99 延遲突然升到 5 秒！」
  Traces  告訴你：「瓶頸在 MySQL 查詢這一步，耗時 4.8 秒」
  Logs    告訴你：「MySQL 慢查詢：SELECT * FROM orders WHERE ...（缺少索引）」
```

---

### 🟡 黃金指標（The Four Golden Signals）

```
Google SRE 提出的四個最重要的監控指標：

① 延遲（Latency）
   請求需要多長時間？
   關注 P99（99th percentile），而不只是平均值！

   平均值的陷阱：
   [1ms, 1ms, 1ms, 1ms, 1ms, 1ms, 1ms, 1ms, 1ms, 10000ms]
   平均 = 1001ms
   P50 = 1ms    → 50% 用戶體驗很好
   P99 = 10000ms → 1% 用戶體驗極差！

② 流量（Traffic）
   系統有多忙？
   QPS（每秒請求數）、TPS（每秒事務數）

③ 錯誤率（Errors）
   請求失敗的比例？
   HTTP 5xx 錯誤率、業務錯誤率

④ 飽和度（Saturation）
   系統資源使用了多少？
   CPU 使用率、記憶體使用率、連線池使用率
```

---

## 2️⃣ Prometheus 深入

---

### 🔵 架構原理

```
Prometheus 架構：

┌─────────────────────────────────────────────────────────────┐
│                      Prometheus Server                       │
│                                                             │
│  ┌─────────────────┐     ┌───────────────────────────────┐  │
│  │  Retrieval      │     │         TSDB                  │  │
│  │  （拉取引擎）    │────→ │  （Time Series Database）      │  │
│  │                 │     │  本地時序資料庫                  │  │
│  │  每 15 秒拉取    │     │  高效壓縮，預設保留 15 天        │  │
│  └────────┬────────┘     └──────────────┬────────────────┘  │
│           │                             │                   │
│           │                        ┌────┴─────┐             │
│           │                        │ HTTP API  │             │
│           │                        └────┬─────┘             │
└───────────│─────────────────────────────│───────────────────┘
            │ Scrape（HTTP Pull）          │ Query（PromQL）
            ↓                             ↓
  ┌──────────────────┐           ┌──────────────────┐
  │  被監控的服務     │           │    Grafana        │
  │  Spring Boot     │           │    AlertManager   │
  │  /actuator/      │           └──────────────────┘
  │  prometheus      │
  │  MySQL Exporter  │
  │  Node Exporter   │
  │  Redis Exporter  │
  └──────────────────┘

核心設計：Pull 模式（拉取）
  Prometheus 主動去服務的 /metrics 端點拉取指標
  優點：Prometheus 控制拉取頻率，服務不需要知道 Prometheus 的地址
  缺點：短生命週期的任務（Job）可能來不及被拉取
  解決：PushGateway（Job 主動推送到 Gateway，Prometheus 再拉取）
```

---

### 🟡 資料模型與指標類型

```
Prometheus 資料模型：時間序列（Time Series）

每條時間序列由：指標名稱 + 標籤集合 唯一確定

http_requests_total{method="GET", path="/api/orders", status="200"} = 1234
──────────────────  ──────────────────────────────────────────────   ────
    指標名稱                        標籤（Labels）                    當前值
```

```java
// ① Counter（計數器）- 只增不減
// 適合：請求總數、錯誤次數
// 命名慣例：_total 結尾
// ⚠️ 服務重啟後會歸零！使用 rate() 計算增長速率
http_requests_total{method="POST"} 1234

// ② Gauge（儀表盤）- 可增可減
// 適合：當前值，如記憶體使用量、在線用戶數、隊列深度
jvm_memory_used_bytes{area="heap"} 536870912
active_connections 42

// ③ Histogram（直方圖）- 觀察值分布 + 百分位數
// 適合：請求延遲、響應大小
// 自動生成三個指標：
http_request_duration_seconds_bucket{le="0.1"}   800   // ≤0.1s 的請求有 800 個
http_request_duration_seconds_bucket{le="0.5"}   950
http_request_duration_seconds_bucket{le="1.0"}   990
http_request_duration_seconds_bucket{le="+Inf"} 1000   // 總共 1000 個請求
http_request_duration_seconds_sum             123.456   // 所有請求的總耗時
http_request_duration_seconds_count              1000   // 請求總數

// ④ Summary（摘要）- 客戶端計算百分位數
// ⚠️ Summary 不支援跨實例聚合！多實例場景用 Histogram
http_request_duration_seconds{quantile="0.5"}  0.05   // P50 = 50ms
http_request_duration_seconds{quantile="0.9"}  0.2    // P90 = 200ms
http_request_duration_seconds{quantile="0.99"} 0.8    // P99 = 800ms
```

---

### 🟠 PromQL 查詢語言

```promql
# ─── 標籤匹配器 ───
# =  完全匹配    {method="GET"}
# != 不等於      {status!="200"}
# =~ 正則匹配    {path=~"/api/.*"}
# !~ 正則不匹配  {status!~"2.*"}

# ─── 常用函數 ───
# rate()    計算範圍向量的每秒平均增長率（適合 Counter）
# irate()   計算最後兩個樣本的瞬時速率（對突發更敏感）
# increase() 計算範圍內的增長量
# histogram_quantile() 從 Histogram 計算百分位數

# ─── 常用 PromQL 查詢範例 ───

# 每秒請求數（QPS）
rate(http_requests_total{job="myapp"}[1m])

# 按路徑分組的 QPS
sum by (path) (
    rate(http_requests_total{job="myapp"}[1m])
)

# HTTP 錯誤率（5xx 佔總請求的比例）
sum(rate(http_requests_total{status=~"5.."}[5m]))
/
sum(rate(http_requests_total[5m]))
* 100

# P99 請求延遲（從 Histogram 計算）
histogram_quantile(0.99,
    sum by (le) (
        rate(http_request_duration_seconds_bucket{job="myapp"}[5m])
    )
)

# JVM 堆記憶體使用率（%）
jvm_memory_used_bytes{area="heap"}
/
jvm_memory_max_bytes{area="heap"}
* 100

# CPU 使用率（過去 1 分鐘）
100 - (
    avg by (instance) (
        rate(node_cpu_seconds_total{mode="idle"}[1m])
    ) * 100
)

# 連線池使用率（HikariCP）
hikaricp_connections_active
/
hikaricp_connections_max
* 100

# 過去 1 小時的 GC 暫停時間
increase(jvm_gc_pause_seconds_sum[1h])

# 多實例聚合（3 個實例的總 QPS）
sum(rate(http_requests_total{job="myapp"}[1m]))
```

---

### 🔴 Spring Boot 整合 Prometheus

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus, metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true        # 開啟 K8s 探針端點
    prometheus:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}
    distribution:
      # 開啟 Histogram（計算百分位數必須開啟）
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.75, 0.95, 0.99
      # SLO 邊界
      slo:
        http.server.requests: 100ms, 200ms, 500ms, 1s
```

```java
// 自定義業務指標
@Component
@RequiredArgsConstructor
public class OrderMetrics {

    private final MeterRegistry registry;

    private Counter             orderCreatedCounter;
    private Counter             orderFailedCounter;
    private AtomicInteger       pendingOrders = new AtomicInteger(0);
    private Timer               orderProcessingTimer;
    private DistributionSummary orderAmountSummary;

    @PostConstruct
    public void init() {
        // ① Counter
        orderCreatedCounter = Counter.builder("order.created.total")
            .description("已建立的訂單總數")
            .tag("service", "order-service")
            .register(registry);

        orderFailedCounter = Counter.builder("order.failed.total")
            .description("建立失敗的訂單總數")
            .register(registry);

        // ② Gauge（監控動態值）
        Gauge.builder("order.pending.count", pendingOrders, AtomicInteger::get)
            .description("當前待處理訂單數")
            .register(registry);

        // ③ Timer
        orderProcessingTimer = Timer.builder("order.processing.duration")
            .description("訂單處理耗時")
            .publishPercentileHistogram()
            .slo(
                Duration.ofMillis(100),
                Duration.ofMillis(500),
                Duration.ofSeconds(1)
            )
            .register(registry);

        // ④ DistributionSummary
        orderAmountSummary = DistributionSummary.builder("order.amount")
            .description("訂單金額分布（元）")
            .baseUnit("yuan")
            .publishPercentileHistogram()
            .register(registry);
    }

    public void recordOrderCreated(BigDecimal amount) {
        orderCreatedCounter.increment();
        orderAmountSummary.record(amount.doubleValue());
        pendingOrders.incrementAndGet();
    }

    public void recordOrderFailed(String reason) {
        orderFailedCounter.increment(Tags.of("reason", reason));
    }

    public void recordOrderCompleted() {
        pendingOrders.decrementAndGet();
    }

    // 用 Timer 包裝業務邏輯（自動計時）
    public Order createOrderWithMetrics(OrderRequest request) {
        return orderProcessingTimer.record(() -> createOrder(request));
    }
}

// 使用 @Timed 注解（更簡潔）
@Service
public class OrderService {

    @Timed(
        value       = "order.create.duration",
        description = "訂單建立耗時",
        percentiles = {0.5, 0.99},
        histogram   = true
    )
    public Order createOrder(OrderRequest request) {
        // 業務邏輯
    }
}
```

```yaml
# prometheus.yml（Prometheus 配置）
global:
  scrape_interval:     15s
  evaluation_interval: 15s

rule_files:
  - "rules/spring_boot_alerts.yml"
  - "rules/jvm_alerts.yml"
  - "rules/business_alerts.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

scrape_configs:
  # ── Spring Boot 應用 ──
  - job_name: 'spring-boot-apps'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s

    # Kubernetes 服務發現（自動發現 Pod）
    kubernetes_sd_configs:
      - role: pod
        namespaces:
          names: ['production', 'staging']

    relabel_configs:
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
        action: keep
        regex: "true"
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_path]
        action: replace
        target_label: __metrics_path__
        regex: (.+)
      - source_labels: [__meta_kubernetes_namespace]
        target_label: namespace
      - source_labels: [__meta_kubernetes_pod_name]
        target_label: pod
      - source_labels: [__meta_kubernetes_pod_label_app]
        target_label: app

  # ── Node Exporter（主機指標）──
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

  # ── MySQL Exporter ──
  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  # ── Redis Exporter ──
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']
```

---

### 🟡 AlertManager 告警規則

```yaml
# rules/spring_boot_alerts.yml
groups:
  - name: spring_boot_alerts
    interval: 30s

    rules:
      # ── 服務宕機 ──
      - alert: ServiceDown
        expr: up{job="spring-boot-apps"} == 0
        for: 1m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "服務宕機 🚨"
          description: "{{ $labels.app }} 在 {{ $labels.namespace }} 已宕機超過 1 分鐘"
          runbook: "https://wiki.example.com/runbook/service-down"

      # ── 高錯誤率 ──
      - alert: HighErrorRate
        expr: |
          (
            sum by (app) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
            /
            sum by (app) (rate(http_server_requests_seconds_count[5m]))
          ) * 100 > 5
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "HTTP 錯誤率過高 ⚠️"
          description: "{{ $labels.app }} 錯誤率為 {{ $value | humanize }}%（閾值 5%）"

      # ── 高延遲 ──
      - alert: HighLatency
        expr: |
          histogram_quantile(0.99,
            sum by (app, le) (
              rate(http_server_requests_seconds_bucket[5m])
            )
          ) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "P99 延遲過高 ⚠️"
          description: "{{ $labels.app }} P99 延遲 {{ $value | humanizeDuration }}（閾值 2s）"

      # ── JVM 堆記憶體告警 ──
      - alert: JvmHeapUsageHigh
        expr: |
          (
            jvm_memory_used_bytes{area="heap"}
            /
            jvm_memory_max_bytes{area="heap"}
          ) * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM 堆記憶體使用率過高"
          description: "{{ $labels.app }} 堆記憶體使用 {{ $value | humanize }}%（閾值 85%）"

      # ── 連線池耗盡 ──
      - alert: HikariPoolExhausted
        expr: hikaricp_connections_pending > 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "資料庫連線池耗盡 🚨"
          description: "{{ $labels.app }} 有 {{ $value }} 個請求等待資料庫連線"

---
# rules/business_alerts.yml
groups:
  - name: business_alerts
    rules:
      - alert: HighOrderFailureRate
        expr: |
          (
            rate(order_failed_total[5m])
            /
            rate(order_created_total[5m])
          ) * 100 > 10
        for: 3m
        labels:
          severity: critical
          team: business
        annotations:
          summary: "訂單失敗率過高 🚨"
          description: "訂單失敗率 {{ $value | humanize }}%，可能影響業務收入！"
```

```yaml
# alertmanager.yml（告警路由與通知）
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alert@example.com'

templates:
  - '/etc/alertmanager/templates/*.tmpl'

route:
  receiver: 'slack-ops'
  group_by: ['alertname', 'app', 'namespace']
  group_wait:      30s
  group_interval:  5m
  repeat_interval: 4h

  routes:
    - match:
        severity: critical
      receiver: 'pagerduty'
      continue: true

    - match:
        team: business
      receiver: 'slack-business'

    - match:
        severity: warning
      mute_time_intervals:
        - non-business-hours

receivers:
  - name: 'slack-ops'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/xxx'
        channel: '#ops-alerts'
        color: '{{ if eq .Status "firing" }}danger{{ else }}good{{ end }}'
        send_resolved: true

  - name: 'pagerduty'
    pagerduty_configs:
      - routing_key: 'your-pagerduty-key'

  - name: 'email-ops'
    email_configs:
      - to: 'ops-team@example.com'

time_intervals:
  - name: non-business-hours
    time_intervals:
      - times:
          - start_time: '00:00'
            end_time: '09:00'
          - start_time: '18:00'
            end_time: '24:00'
        weekdays: ['monday:friday']
      - weekdays: ['saturday', 'sunday']
```

---

## 3️⃣ Grafana 深入

---

### 🔵 儀表板設計原則

```
好的儀表板：從上到下，從概覽到詳情

┌────────────────────────────────────────────────────────┐
│  第一行：系統健康狀態（紅燈/綠燈）- 一眼看出是否正常      │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐        │
│  │服務A  │ │服務B  │ │MySQL │ │Redis │ │告警數 │        │
│  │  ✅  │ │  ✅  │ │  ✅  │ │  ⚠️  │ │  2   │        │
│  └──────┘ └──────┘ └──────┘ └──────┘ └──────┘        │
├────────────────────────────────────────────────────────┤
│  第二行：核心指標趨勢（時間序列圖）                       │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │   QPS 趨勢       │  │   錯誤率趨勢     │              │
│  └─────────────────┘  └─────────────────┘              │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │  P99 延遲趨勢    │  │   活躍連線數     │              │
│  └─────────────────┘  └─────────────────┘              │
├────────────────────────────────────────────────────────┤
│  第三行：資源使用（CPU / 記憶體 / JVM）                   │
├────────────────────────────────────────────────────────┤
│  第四行：業務指標（訂單數 / 用戶數 / 收入）               │
└────────────────────────────────────────────────────────┘
```

---

### 🟡 關鍵面板 PromQL 配置

```json
// 1. QPS 面板（時間序列圖）
{
  "title": "每秒請求數 (QPS)",
  "type": "timeseries",
  "targets": [
    {
      "expr": "sum by (app) (rate(http_server_requests_seconds_count{namespace='$namespace'}[$__rate_interval]))",
      "legendFormat": "{{app}}"
    }
  ],
  "fieldConfig": {
    "defaults": {
      "unit": "reqps",
      "thresholds": {
        "steps": [
          {"color": "green",  "value": 0},
          {"color": "yellow", "value": 1000},
          {"color": "red",    "value": 5000}
        ]
      }
    }
  }
}

// 2. P99 延遲面板
{
  "title": "P99 請求延遲",
  "type": "timeseries",
  "targets": [
    {
      "expr": "histogram_quantile(0.99, sum by (app, le) (rate(http_server_requests_seconds_bucket{namespace='$namespace'}[$__rate_interval])))",
      "legendFormat": "P99 - {{app}}"
    },
    {
      "expr": "histogram_quantile(0.95, sum by (app, le) (rate(http_server_requests_seconds_bucket{namespace='$namespace'}[$__rate_interval])))",
      "legendFormat": "P95 - {{app}}"
    },
    {
      "expr": "histogram_quantile(0.50, sum by (app, le) (rate(http_server_requests_seconds_bucket{namespace='$namespace'}[$__rate_interval])))",
      "legendFormat": "P50 - {{app}}"
    }
  ],
  "fieldConfig": {
    "defaults": { "unit": "s" }
  }
}

// 3. JVM 記憶體面板（Gauge）
{
  "title": "JVM 堆記憶體使用率",
  "type": "gauge",
  "targets": [
    {
      "expr": "(jvm_memory_used_bytes{area='heap', app='$app'} / jvm_memory_max_bytes{area='heap', app='$app'}) * 100"
    }
  ],
  "fieldConfig": {
    "defaults": {
      "unit": "percent",
      "min": 0,
      "max": 100,
      "thresholds": {
        "steps": [
          {"color": "green",  "value": 0},
          {"color": "yellow", "value": 70},
          {"color": "red",    "value": 85}
        ]
      }
    }
  }
}

// 4. 錯誤率面板（Stat 大數字）
{
  "title": "5xx 錯誤率",
  "type": "stat",
  "targets": [
    {
      "expr": "(sum(rate(http_server_requests_seconds_count{status=~'5..', namespace='$namespace'}[5m])) / sum(rate(http_server_requests_seconds_count{namespace='$namespace'}[5m]))) * 100"
    }
  ],
  "fieldConfig": {
    "defaults": {
      "unit": "percent",
      "thresholds": {
        "steps": [
          {"color": "green",  "value": 0},
          {"color": "yellow", "value": 1},
          {"color": "red",    "value": 5}
        ]
      }
    }
  }
}
```

---

### 🔴 Grafana 變數（Variables）

```
使用變數讓儀表板可以動態切換環境/服務：

① namespace 變數（從 Prometheus 標籤取得）
   Type:  Query
   Query: label_values(up, namespace)
   → 自動列出所有 namespace，可以在頂部下拉選擇

② app 變數（依賴 namespace 變數）
   Type:  Query
   Query: label_values(up{namespace="$namespace"}, app)
   → 選擇 namespace 後，自動更新 app 列表

③ interval 變數（時間間隔）
   Type:   Interval
   Values: 1m, 5m, 15m, 30m, 1h
   → 讓使用者選擇聚合粒度

在 PromQL 中使用：
  rate(http_requests_total{namespace="$namespace", app="$app"}[$interval])
```

---

## 4️⃣ ELK Stack 深入

---

### 🔵 ELK 架構

```
完整的日誌收集架構：

┌─────────────────────────────────────────────────────────────┐
│                       應用層                                 │
│  Spring Boot App → 輸出結構化日誌（JSON 格式）               │
└──────────────────────────┬──────────────────────────────────┘
                           │ 日誌文件 / Console
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                      收集層                                  │
│  Filebeat（輕量）                                            │
│  → 讀取日誌文件，發送到 Logstash 或直接到 Elasticsearch       │
│                                                             │
│  Logstash（重量級，適合複雜處理）                              │
│  → 過濾、轉換、豐富日誌數據                                    │
└──────────────────────────┬──────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                     儲存層                                   │
│  Elasticsearch                                              │
│  → 分散式全文搜索引擎，儲存和索引日誌                           │
└──────────────────────────┬──────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                     展示層                                   │
│  Kibana                                                     │
│  → 日誌搜索、視覺化、儀表板                                    │
└─────────────────────────────────────────────────────────────┘
```

---

### 🟡 Spring Boot 結構化日誌

```xml
<!-- pom.xml -->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

```xml
<!-- logback-spring.xml -->
<configuration>
    <!-- 開發環境：人類可讀格式 -->
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <!-- 生產環境：JSON 格式（讓 ELK 解析）-->
    <springProfile name="production,staging">
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <customFields>
                    {"service":"${spring.application.name}",
                     "env":"${spring.profiles.active}"}
                </customFields>
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
                <fieldNames>
                    <timestamp>@timestamp</timestamp>
                    <levelValue>[ignore]</levelValue>
                </fieldNames>
            </encoder>
        </appender>

        <!-- 非同步輸出（不阻塞業務線程）-->
        <appender name="ASYNC_JSON" class="ch.qos.logback.classic.AsyncAppender">
            <appender-ref ref="JSON_CONSOLE"/>
            <queueSize>1024</queueSize>
            <discardingThreshold>0</discardingThreshold>
            <includeCallerData>false</includeCallerData>
        </appender>

        <root level="INFO">
            <appender-ref ref="ASYNC_JSON"/>
        </root>

        <logger name="com.example" level="DEBUG"/>
        <logger name="org.springframework.web" level="WARN"/>
    </springProfile>
</configuration>
```

```java
// MDC（Mapped Diagnostic Context）- 為日誌加上請求上下文
@Component
@Order(1)
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain)
            throws ServletException, IOException {
        try {
            String traceId = Optional
                .ofNullable(request.getHeader("X-Trace-Id"))
                .orElse(UUID.randomUUID().toString().replace("-", ""));

            String requestId = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8);

            MDC.put("traceId",   traceId);
            MDC.put("requestId", requestId);
            MDC.put("method",    request.getMethod());
            MDC.put("path",      request.getRequestURI());
            MDC.put("clientIp",  getClientIp(request));

            response.setHeader("X-Trace-Id", traceId);

            chain.doFilter(request, response);

        } finally {
            MDC.clear();  // ⚠️ 必須清除！防止線程池中的線程污染
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}

// 日誌記錄最佳實踐
@Service
@Slf4j
public class OrderService {

    public Order createOrder(OrderRequest request) {
        // ✅ 結構化日誌（Key-Value 形式，ELK 容易解析）
        log.info("開始建立訂單, userId={}, productId={}, quantity={}",
            request.getUserId(), request.getProductId(), request.getQuantity());

        MDC.put("orderId", orderId.toString());
        MDC.put("userId",  request.getUserId().toString());

        try {
            Order order = processOrder(request);

            log.info("訂單建立成功, orderId={}, amount={}, status={}",
                order.getId(), order.getAmount(), order.getStatus());

            return order;

        } catch (InsufficientStockException e) {
            // ✅ 業務異常用 warn（不是 error）
            log.warn("庫存不足, orderId={}, productId={}, required={}, available={}",
                orderId, request.getProductId(),
                request.getQuantity(), e.getAvailableStock());
            throw e;

        } catch (Exception e) {
            // ✅ 系統異常用 error，並包含堆棧
            log.error("訂單建立失敗, orderId={}, errorType={}",
                orderId, e.getClass().getSimpleName(), e);
            throw new OrderCreationException("訂單建立失敗", e);

        } finally {
            MDC.remove("orderId");
            MDC.remove("userId");
        }
    }
}
```

---

### 🟠 Filebeat 配置

```yaml
# filebeat.yml
filebeat.inputs:
  - type: container
    paths:
      - /var/log/containers/*myapp*.log
    processors:
      - decode_json_fields:
          fields: ["message"]
          target: ""
          overwrite_keys: true

  - type: log
    paths:
      - /app/logs/*.log
    json.keys_under_root: true
    json.add_error_key: true
    # 多行日誌合併（Java 堆棧追蹤）
    multiline.pattern: '^\d{4}-\d{2}-\d{2}'
    multiline.negate: true
    multiline.match: after

processors:
  - add_host_metadata: ~
  - add_kubernetes_metadata:
      host: ${NODE_NAME}
      matchers:
        - logs_path:
            logs_path: "/var/log/containers/"

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  username: "elastic"
  password: "${ELASTICSEARCH_PASSWORD}"
  index: "myapp-logs-%{+yyyy.MM.dd}"

  bulk_max_size: 1000
  worker: 4

queue.mem:
  events: 4096
  flush.min_events: 512
  flush.timeout: 5s
```

---

### 🔴 Logstash Pipeline（複雜處理）

```ruby
# logstash/pipeline/main.conf

input {
  beats {
    port => 5044
  }
}

filter {
  json {
    source => "message"
    target => "parsed"
  }

  mutate {
    rename => {
      "[parsed][traceId]" => "trace_id"
      "[parsed][level]"   => "log_level"
      "[parsed][service]" => "service"
      "[parsed][message]" => "log_message"
    }
    remove_field => ["parsed", "message", "agent", "ecs"]
    uppercase    => ["log_level"]
  }

  date {
    match  => ["[parsed][@timestamp]", "ISO8601"]
    target => "@timestamp"
  }

  # 識別慢請求（超過 1000ms）
  if [duration_ms] and [duration_ms] > 1000 {
    mutate {
      add_tag   => ["slow_request"]
      add_field => { "slow_request" => true }
    }
  }

  # IP 地址轉地理位置
  if [client_ip] {
    geoip {
      source => "client_ip"
      target => "geoip"
    }
  }
}

output {
  elasticsearch {
    hosts    => ["elasticsearch:9200"]
    user     => "elastic"
    password => "${ES_PASSWORD}"
    index    => "%{service}-logs-%{+YYYY.MM.dd}"
    ilm_enabled           => true
    ilm_rollover_alias    => "%{service}-logs"
  }

  # 錯誤日誌額外發到 Slack
  if [log_level] == "ERROR" {
    http {
      url         => "https://hooks.slack.com/services/xxx"
      http_method => "post"
      format      => "json"
      mapping     => {
        "text" => "🚨 ERROR in %{service}: %{log_message} | traceId: %{trace_id}"
      }
    }
  }
}
```

---

### 🟢 Kibana 使用技巧

```
KQL（Kibana Query Language）常用查詢：

# 查找特定服務的錯誤日誌
service: "order-service" AND log_level: "ERROR"

# 查找特定 traceId 的所有日誌（排查問題！）
trace_id: "abc123def456"

# 查找慢請求
duration_ms > 1000 AND service: "order-service"

# 查找包含特定錯誤訊息
log_message: "OutOfMemoryError" OR log_message: "connection refused"

# 查找特定時間段
@timestamp >= "2024-01-01T00:00:00" AND @timestamp <= "2024-01-01T01:00:00"

# 正則查詢
log_message: /.*timeout.*/

重要功能：
① Discover：實時搜索日誌
② Dashboard：視覺化日誌分析
③ Alerts：基於日誌的告警（如錯誤日誌突增）
④ Index Lifecycle Management（ILM）：
   Hot（7天）→ Warm（30天）→ Cold（90天）→ Delete
   自動管理索引，節省儲存空間
```

---

## 5️⃣ 分散式鏈路追蹤

---

### 🔵 SkyWalking 架構

```
為什麼需要鏈路追蹤？

一個用戶請求在微服務中的旅程：

用戶請求
   ↓
API Gateway（10ms）
   ↓
訂單服務（500ms）← 哪裡慢？
   ├→ 用戶服務（20ms）
   ├→ 庫存服務（30ms）
   ├→ MySQL 查詢（400ms）← 原來是這裡！
   └→ Redis 查詢（5ms）

沒有鏈路追蹤：看著 500ms 不知道哪裡慢
有了鏈路追蹤：立刻定位到 MySQL 查詢耗時 400ms

SkyWalking 架構：

┌─────────────────────────────────────────────────────────────┐
│                    應用層（Agent 自動注入）                    │
│  訂單服務              用戶服務              庫存服務           │
│  [SW Agent]          [SW Agent]          [SW Agent]        │
└─────────────────────────────┬───────────────────────────────┘
                              │ gRPC 上報 Span 數據
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    OAP Server（分析引擎）                     │
│  接收 Span → 分析依賴關係 → 計算指標 → 存儲                   │
└──────────────────┬──────────────────────┬───────────────────┘
                   │                      │
                   ↓                      ↓
         ┌──────────────┐       ┌──────────────────┐
         │ Elasticsearch│       │  SkyWalking UI   │
         │（存儲鏈路數據）│       │ （視覺化展示）      │
         └──────────────┘       └──────────────────┘
```

---

### 🟡 Spring Boot 整合（OpenTelemetry 現代標準）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<dependency>
    <groupId>io.opentelemetry.instrumentation</groupId>
    <artifactId>opentelemetry-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

```yaml
# application.yml
management:
  tracing:
    sampling:
      probability: 1.0   # 採樣率：1.0 = 100%（生產建議 0.1 = 10%）
  otlp:
    tracing:
      endpoint: http://skywalking-oap:4317

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

```java
// 自定義 Span（追蹤業務操作）
@Service
@RequiredArgsConstructor
public class OrderService {

    private final Tracer tracer;

    public Order createOrder(OrderRequest request) {
        Span span = tracer.nextSpan()
            .name("createOrder")
            .tag("userId",    request.getUserId().toString())
            .tag("productId", request.getProductId().toString())
            .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            Order order = processOrder(request);

            span.tag("orderId", order.getId().toString())
                .tag("amount",  order.getAmount().toString());

            return order;

        } catch (Exception e) {
            span.tag("error", e.getMessage()).error(e);
            throw e;
        } finally {
            span.end();  // 必須結束 Span
        }
    }

    // 使用 @Observed 注解（更簡潔，自動建立 Span + Metrics）
    @Observed(
        name            = "order.payment",
        contextualName  = "處理訂單支付",
        lowCardinalityKeyValues = {"service", "order"}
    )
    public void processPayment(Long orderId) {
        // 自動追蹤這個方法
    }
}

// 跨服務傳遞 TraceId（Feign 客戶端攔截器）
@Component
public class TraceIdPropagationInterceptor implements RequestInterceptor {

    private final Tracer tracer;

    @Override
    public void apply(RequestTemplate template) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            template.header("X-Trace-Id",
                currentSpan.context().traceId());
        }
    }
}
```

---

### 🔴 鏈路追蹤實戰分析

```
SkyWalking UI 關鍵功能：

① 拓撲圖（Topology）
   自動繪製服務依賴關係圖
   顯示每條鏈路的 QPS 和錯誤率

   用戶 → API Gateway → 訂單服務 → MySQL
                                 → 庫存服務
                                 → 支付服務 → 外部支付平台

② Span 詳情（定位瓶頸）
   點開一條 Trace，看到完整的調用樹：

   OrderService.createOrder         [0ms ~ 523ms]  總耗時 523ms
   ├─ UserService.getUser           [5ms ~ 25ms]   耗時 20ms
   ├─ InventoryService.check        [30ms ~ 60ms]  耗時 30ms
   ├─ MySQL: SELECT * FROM orders   [65ms ~ 465ms] 耗時 400ms ← 瓶頸！
   │    SQL: SELECT * FROM orders WHERE user_id=?
   │    行數: 50,000 行 ← 缺少索引！
   └─ Redis: SET order:123          [470ms ~ 475ms] 耗時 5ms

③ 服務指標（Service Metrics）
   SLA、吞吐量、延遲的趨勢圖

④ 告警規則
   - 服務 P99 延遲 > 2s → 告警
   - 服務錯誤率 > 5% → 告警
   - 服務不可用 → 告警
```

---

## 6️⃣ 實戰：完整可觀測性平台

---

### 🔵 整合架構設計

```
完整可觀測性平台架構：

┌─────────────────────────────────────────────────────────────┐
│                       應用層                                 │
│  Spring Boot Apps（Micrometer + Logback JSON + OTel Agent） │
└──────┬────────────────┬────────────────┬────────────────────┘
       │ Metrics        │ Logs           │ Traces
       ↓                ↓                ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐
│  Prometheus  │ │   Filebeat   │ │  SkyWalking OAP /   │
│  （拉取指標） │ │ （收集日誌）  │ │  OpenTelemetry      │
└──────┬───────┘ └──────┬───────┘ └──────────┬───────────┘
       │                │                    │
       ↓                ↓                    ↓
┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐
│   Grafana    │ │Elasticsearch │ │  SkyWalking UI       │
│（指標儀表板） │ │ + Kibana     │ │  （鏈路視覺化）        │
│（告警）      │ │（日誌搜索）   │ └──────────────────────┘
└──────┬───────┘ └──────────────┘
       │
       ↓
┌──────────────────┐
│  統一告警通知      │
│  AlertManager    │
│  → Slack         │
│  → PagerDuty     │
│  → Email         │
└──────────────────┘
```

---

### 🟡 Docker Compose 部署完整監控棧

```yaml
# docker-compose-monitoring.yml
version: '3.8'

services:

  # ══════════ Prometheus ══════════
  prometheus:
    image: prom/prometheus:v2.48.0
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./monitoring/prometheus/rules:/etc/prometheus/rules:ro
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    networks:
      - monitoring

  # ══════════ AlertManager ══════════
  alertmanager:
    image: prom/alertmanager:v0.26.0
    container_name: alertmanager
    ports:
      - "9093:9093"
    volumes:
      - ./monitoring/alertmanager/alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
      - alertmanager_data:/alertmanager
    networks:
      - monitoring

  # ══════════ Grafana ══════════
  grafana:
    image: grafana/grafana:10.2.0
    container_name: grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER:     admin
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD}
      GF_USERS_ALLOW_SIGN_UP:     "false"
      GF_SMTP_ENABLED:            "true"
      GF_SMTP_HOST:               smtp.example.com:587
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources:ro
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
    networks:
      - monitoring

  # ══════════ Node Exporter（主機指標）══════════
  node-exporter:
    image: prom/node-exporter:v1.7.0
    container_name: node-exporter
    ports:
      - "9100:9100"
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.ignored-mount-points=^/(sys|proc|dev|host|etc)($$|/)'
    networks:
      - monitoring

  # ══════════ Elasticsearch ══════════
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms1g -Xmx1g"
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - monitoring

  # ══════════ Kibana ══════════
  kibana:
    image: kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - monitoring

  # ══════════ Filebeat ══════════
  filebeat:
    image: elastic/filebeat:8.11.0
    container_name: filebeat
    user: root
    volumes:
      - ./monitoring/filebeat/filebeat.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - filebeat_data:/usr/share/filebeat/data
    networks:
      - monitoring

  # ══════════ SkyWalking OAP ══════════
  skywalking-oap:
    image: apache/skywalking-oap-server:9.7.0
    container_name: skywalking-oap
    ports:
      - "11800:11800"   # gRPC（Agent 上報）
      - "12800:12800"   # HTTP（UI 查詢）
    environment:
      SW_STORAGE: elasticsearch
      SW_STORAGE_ES_CLUSTER_NODES: elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - monitoring

  # ══════════ SkyWalking UI ══════════
  skywalking-ui:
    image: apache/skywalking-ui:9.7.0
    container_name: skywalking-ui
    ports:
      - "8080:8080"
    environment:
      SW_OAP_ADDRESS: http://skywalking-oap:12800
    networks:
      - monitoring

volumes:
  prometheus_data:
  alertmanager_data:
  grafana_data:
  elasticsearch_data:
  filebeat_data:

networks:
  monitoring:
    driver: bridge
```

---

### 🔴 生產告警體系設計

```
告警分級：

P0 - 緊急（立即響應，5 分鐘內）：
  ✅ 服務完全宕機
  ✅ 所有請求 5xx（錯誤率 100%）
  ✅ 資料庫連線失敗
  ✅ 核心業務功能不可用
  → 通知：PagerDuty 電話 + Slack @channel

P1 - 嚴重（30 分鐘內響應）：
  ✅ 錯誤率 > 10%
  ✅ P99 延遲 > 5s
  ✅ JVM OOM（記憶體 > 95%）
  ✅ 磁碟空間 > 90%
  → 通知：Slack @here + Email

P2 - 警告（工作時間內處理）：
  ✅ 錯誤率 > 1%
  ✅ P99 延遲 > 2s
  ✅ CPU > 80%（持續 15 分鐘）
  ✅ 連線池使用率 > 80%
  → 通知：Slack 通知

P3 - 提示（下個迭代處理）：
  ✅ 輕微效能退化
  ✅ 日誌中出現大量 WARN
  → 通知：週報匯總

告警黃金法則：
① 告警必須是可操作的（Actionable）
② 避免告警疲勞（Alert Fatigue）
   太多無效告警 → 工程師麻木 → 漏掉真正的問題
③ 告警要有 Runbook
   每個告警對應一份操作手冊：
   "收到這個告警，做以下步驟 1、2、3..."
```

```yaml
# 完整的生產告警規則
groups:
  - name: p0_critical
    rules:
      - alert: AllInstancesDown
        expr: |
          sum by (app) (up{job="spring-boot-apps"}) == 0
        for: 1m
        labels:
          severity: p0
          pagerduty: "true"
        annotations:
          summary: "🚨 P0：{{ $labels.app }} 所有實例宕機！"
          description: "{{ $labels.app }} 在 {{ $labels.namespace }} 完全不可用"
          runbook: "https://wiki.example.com/runbook/p0-service-down"

  - name: p1_serious
    rules:
      - alert: HighErrorRateP1
        expr: |
          (
            sum by (app) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
            /
            sum by (app) (rate(http_server_requests_seconds_count[5m]))
          ) * 100 > 10
        for: 3m
        labels:
          severity: p1
        annotations:
          summary: "🔴 P1：{{ $labels.app }} 錯誤率 {{ $value | printf \"%.1f\" }}%"
          description: |
            服務 {{ $labels.app }} 的 5xx 錯誤率超過 10%，持續 3 分鐘。
            排查步驟：
            1. 查看 Kibana 最近的 ERROR 日誌
            2. 查看 SkyWalking 的失敗追蹤
            3. 檢查依賴服務（DB、Redis）是否正常
          runbook: "https://wiki.example.com/runbook/high-error-rate"

  - name: p2_warning
    rules:
      - alert: SlowResponseP2
        expr: |
          histogram_quantile(0.99,
            sum by (app, le) (
              rate(http_server_requests_seconds_bucket[5m])
            )
          ) > 2
        for: 5m
        labels:
          severity: p2
        annotations:
          summary: "⚠️ P2：{{ $labels.app }} P99 延遲 {{ $value | humanizeDuration }}"

      - alert: HikariConnectionPoolWarning
        expr: |
          (hikaricp_connections_active / hikaricp_connections_max) * 100 > 80
        for: 3m
        labels:
          severity: p2
        annotations:
          summary: "⚠️ P2：{{ $labels.app }} 資料庫連線池使用率 {{ $value | printf \"%.0f\" }}%"
```

---

### 🟠 SLI / SLO 設計

```
SLI（Service Level Indicator）服務水平指標：
  - 可用性：成功請求數 / 總請求數
  - 延遲：P99 < 500ms 的請求比例
  - 錯誤率：5xx 錯誤比例

SLO（Service Level Objective）服務水平目標：
  - 可用性 SLO：99.9%（每月最多 43 分鐘停機）
  - 延遲 SLO：  95% 的請求 P99 < 500ms
  - 錯誤率 SLO：5xx 錯誤率 < 0.1%

Error Budget（錯誤預算）：
  SLO = 99.9% → Error Budget = 0.1%（每月 43 分鐘）
  Error Budget 消耗完 → 停止新功能發布，專注穩定性
```

```java
// 追蹤 SLO 違規
@Component
public class SloMetrics {

    private final MeterRegistry registry;

    public void trackSloViolation(String sloName, boolean violated) {
        Counter.builder("slo.violations.total")
            .tag("slo_name", sloName)
            .tag("violated", String.valueOf(violated))
            .register(registry)
            .increment();
    }
}
```

---

## 📝 監控與可觀測性 總結

| 主題 | 核心要點 |
|------|---------|
| **三大支柱** | Metrics（數字）/ Logs（文字）/ Traces（鏈路），三者互補 |
| **黃金指標** | 延遲（P99）/ 流量（QPS）/ 錯誤率 / 飽和度 |
| **Prometheus** | Pull 模式，時序資料庫，PromQL 查詢，四種指標類型 |
| **PromQL** | rate() 計算速率，histogram_quantile() 計算百分位數 |
| **AlertManager** | 告警路由、分組、靜音，多種通知渠道 |
| **Grafana** | 從概覽到詳情，使用變數動態切換，SLO 儀表板 |
| **Logback** | JSON 結構化日誌，MDC 加入 traceId，非同步輸出 |
| **ELK** | Filebeat 收集 → Elasticsearch 存儲 → Kibana 查詢 |
| **SkyWalking** | 自動注入 Agent，追蹤全鏈路，定位瓶頸服務 |
| **告警分級** | P0（立即）/ P1（30分鐘）/ P2（工作時間），避免告警疲勞 |
| **SLI/SLO** | 量化服務品質目標，Error Budget 驅動工程決策 |
