# AnthonyFu-Learning

## 👨‍💻 關於我

**傅勝宏 ・ 全端工程師 ・ 4.5 年經驗**

擁有 4 年半全端開發經驗，熟悉 Java Spring Boot 後端架構與前後端整合，具備獨立交付完整系統的能力。曾執行系統重構與資料庫優化專案，顯著提升系統效能與可維護性。

技術涵蓋後端架構設計、資料庫調優、Docker / Kubernetes 容器化部署、CI/CD 流水線建置與監控系統整合。熱衷研究底層原理，相信理解「為什麼」比學會「怎麼用」更有價值，這份筆記正是這個理念的具體實踐。

具備強烈自學驅動力，持續追蹤技術趨勢並系統性整理成學習成果。溝通清晰、與人為善，擅長跨部門協作，能在技術與業務之間扮演良好橋樑。

目前積極尋求 **半導體 / 金融產業** 全端工程師機會，期待以紮實的技術底蘊與快速學習的能力，為團隊帶來實質貢獻。

---

<div align="center">

# 📚 Java 後端工程師 深度學習筆記

> 系統性整理 Java 後端核心知識體系  
> 涵蓋架構設計、底層原理、資安、DevOps、資料庫、設計模式等 11 大主題

![Java](https://img.shields.io/badge/Java-17%20%7C%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestration-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)

</div>

---

## 🗂️ 目錄

| # | 主題 | 核心技術 |
|---|------|---------|
| 01 | [🏗️ 系統設計](./SystemDesign/SystemDesign.md) | CAP / BASE / 分散式事務 / 限流熔斷 / 分庫分表 |
| 02 | [☕ Java 新特性](./JavaNewFeatures/JavaNewFeatures.md) | Virtual Threads / Record / Sealed Class / Pattern Matching |
| 03 | [🔥 JVM 深度原理](./JVMDeepDive/JVMDeepDive.md) | 記憶體模型 / GC / 類加載 / JMM / 性能調優 |
| 04 | [🔐 資安](./InformationSecurity/InformationSecurity.md) | Spring Security / OAuth2 / JWT / SSO / 攻擊防禦 |
| 05 | [🔍 CI/CD 與 DevOps](./CI_CD%26DevOps/CI_CD%26DEV_OPS.md) | Docker / Kubernetes / Jenkins / GitHub Actions / Helm |
| 06 | [📊 監控與可觀測性](./Monitoring%26Observability/Monitoring%26Observability.md) | Prometheus / Grafana / ELK / SkyWalking / SLO |
| 07 | [🟣 MySQL 深度優化](./MySQLDeepOptimization/MySQLDeepOptimization.md) | B+樹索引 / MVCC / 主從複製 / 分庫分表 / 雪花算法 |
| 08 | [🔶 設計模式](./DesignPattern/DesignPattern.md) | GoF 23 種 / SOLID 原則 / Spring 中的應用 |
| 09 | [🟤 Elasticsearch](./Elasticsearch/Elasticsearch.md) | 倒排索引 / DSL 查詢 / 聚合 / ELK Stack |
| 10 | [🔵 API 設計與通訊協議](./APIDesign%26CommunicationProtocol/APIDesign%26CommunicationProtocol.md) | RESTful / gRPC / WebSocket / GraphQL |
| 11 | [🔴 測試](./Testing/Testing.md) | JUnit 5 / Mockito / Testcontainers / TDD |

---

## 🏗️ 系統設計

### 核心理論
| 理論 | 說明 |
|------|------|
| **CAP 定理** | Consistency / Availability / Partition Tolerance，三者最多同時滿足兩個 |
| **BASE 理論** | Basically Available / Soft State / Eventually Consistent，最終一致性 |
| **PACELC 模型** | CAP 的延伸，考慮正常情況下延遲與一致性的取捨 |

### 高可用架構設計
```
用戶請求 → DNS 解析 → CDN 加速
    → Load Balancer（Nginx / HAProxy）
    → API Gateway（限流 / 認證 / 路由）
    → 微服務集群（Spring Cloud）
    → 快取層（Redis Cluster）
    → 資料庫（MySQL 主從 / 分庫分表）
    → 消息佇列（Kafka / RabbitMQ）
```

### 掌握重點
- ✅ **分散式事務**：2PC / TCC / SAGA / 本地消息表
- ✅ **限流策略**：令牌桶、漏桶、滑動視窗（Sentinel 實作）
- ✅ **熔斷降級**：Resilience4j / Sentinel 原理與實作
- ✅ **分庫分表**：垂直切分 / 水平切分 / ShardingSphere
- ✅ **一致性 Hash**：解決分散式節點增減的資料分布問題
- ✅ **消息佇列**：Kafka 高吞吐量原理（Partition / Consumer Group / 冪等性）

---

## ☕ Java 新特性

### 版本特性速查

| 特性 | 版本 | 核心價值 |
|------|------|---------|
| **Lambda / Stream** | Java 8 | 函數式編程，簡化集合操作 |
| **Optional** | Java 8 | 消滅 NullPointerException |
| **var** | Java 10 | 局部類型推斷，減少重複代碼 |
| **Text Block** | Java 15 | 多行字串，SQL / JSON 更易讀 |
| **Switch 表達式** | Java 14 | 箭頭語法，消滅 break 和 fall-through |
| **Record** | Java 16 | 一行定義不可變 DTO，自動生成 getter / equals / toString |
| **Sealed Class** | Java 17 | 限制繼承，配合 Pattern Matching 實現型別安全 |
| **Pattern Matching** | Java 17/21 | 判斷與轉換合而為一，消滅強轉樣板 |
| **Virtual Threads** | Java 21 ⭐ | 百萬並發，IO 密集型應用效能革命 |
| **Structured Concurrency** | Java 21 | 子任務跟著 Scope 走，生命週期管理更安全 |

### Virtual Threads 核心原理
```
傳統平台線程：Java Thread ──1:1──→ OS Thread（~1MB/線程）
虛擬線程：    Virtual Thread ──M:N──→ Carrier Thread（= CPU 核心數）

等待 IO 時 → 自動讓出 Carrier Thread → Carrier Thread 處理其他虛擬線程
IO 完成時 → 重新掛載繼續執行

Spring Boot 3.2 開啟：spring.threads.virtual.enabled: true
```

---

## 🔥 JVM 深度原理

### 記憶體模型架構
```
┌────────────────────────────────────────────────┐
│                  JVM 記憶體                      │
│  ┌──────────┐ ┌──────────┐ ┌────────────────┐  │
│  │程序計數器  │ │ 虛擬機棧  │ │   本地方法棧    │  │
│  │（線程私有）│ │（線程私有）│ │  （線程私有）   │  │
│  └──────────┘ └──────────┘ └────────────────┘  │
│  ┌─────────────────────────────────────────┐   │
│  │                堆（Heap）                 │   │
│  │  ┌───────────────────┐ ┌─────────────┐  │   │
│  │  │  新生代（1/3）      │ │ 老年代（2/3）│  │   │
│  │  │  Eden : S0 : S1   │ │             │  │   │
│  │  │  = 8 : 1 : 1      │ │             │  │   │
│  │  └───────────────────┘ └─────────────┘  │   │
│  └─────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────┐   │
│  │           方法區 / Metaspace              │   │
│  │    類資訊 / 常量池 / 靜態變數 / JIT 緩存   │   │
│  └─────────────────────────────────────────┘   │
└────────────────────────────────────────────────┘
```

### GC 收集器對比

| 收集器 | 最大停頓 | 適用場景 | JVM 參數 |
|--------|---------|---------|---------|
| Serial | 幾百ms\~幾秒 | 小型應用 | `-XX:+UseSerialGC` |
| Parallel | 幾百ms | 後台批次 | `-XX:+UseParallelGC` |
| **G1** | < 200ms | 大多數應用（預設）| `-XX:+UseG1GC` |
| **ZGC** | < 10ms | 超低延遲系統 | `-XX:+UseZGC` |

### 掌握重點
- ✅ **類加載機制**：七步驟（載入→驗證→準備→解析→初始化→使用→卸載）
- ✅ **雙親委派**：安全性保護，可打破（SPI / Tomcat / OSGi）
- ✅ **MVCC 等效**：可達性分析 + 四種引用（強 > 軟 > 弱 > 虛）
- ✅ **JMM 三大問題**：原子性 / 可見性 / 有序性
- ✅ **volatile 原理**：記憶體屏障 + MESI 緩存一致性協議
- ✅ **happens-before**：8 大規則，多線程可見性的核心保證

---

## 🔐 資安

### 認證與授權架構
```
用戶登入 → Spring Security Filter Chain
    → AuthenticationManager
    → DaoAuthenticationProvider
    → UserDetailsService（查 DB）
    → BCryptPasswordEncoder（驗密碼）
    → 生成 JWT（Access Token + Refresh Token）
    → 返回 Token 給客戶端

後續請求 → JwtAuthenticationFilter
    → 驗證 JWT 簽名 / 過期時間
    → 設定 SecurityContextHolder
    → @PreAuthorize 方法級授權
```

### JWT 安全設計

| 項目 | 策略 |
|------|------|
| **Access Token** | 短效（15 分鐘），存前端 Memory |
| **Refresh Token** | 長效（7 天），存 HttpOnly Cookie + Redis |
| **簽名算法** | RS256（非對稱）適合微服務 |
| **吊銷機制** | Redis 黑名單 / Refresh Token 輪換 |

### 常見攻擊防禦

| 攻擊類型 | 防禦方式 |
|---------|---------|
| **SQL 注入** | Prepared Statement，MyBatis 用 `#{}` 不用 `${}` |
| **XSS** | 輸出編碼 + CSP Header + HttpOnly Cookie |
| **CSRF** | JWT + Bearer Header 天然防禦；Session 需 CSRF Token |
| **暴力破解** | Redis 計數 + 帳號鎖定策略 |
| **重放攻擊** | Idempotency-Key + Redis setIfAbsent |

---

## 🔍 CI/CD 與 DevOps

### 完整流水線架構
```
git push → GitHub / GitLab
    ↓
CI 階段：程式碼拉取 → Maven 建置 → 單元測試（>80%覆蓋率）
    → SonarQube 靜態分析 → OWASP 漏洞掃描
    → 多階段 Docker Build → Trivy 安全掃描
    → 推送 Image 到 Harbor Registry
    ↓
CD Staging：Helm upgrade → 等待 Rollout → Smoke Test
    ↓
CD Production（人工審核 / Release Tag）：
    Helm upgrade → 滾動更新（0 停機）→ 生產冒煙測試 → Slack 通知
```

### Kubernetes 核心資源

| 資源 | 用途 |
|------|------|
| **Pod** | 最小部署單元，三種探針（liveness/readiness/startup）|
| **Deployment** | 管理無狀態應用，滾動更新 / 回滾 |
| **StatefulSet** | 管理有狀態應用（MySQL / Kafka），穩定網路標識 |
| **Service** | ClusterIP（內部）/ NodePort（測試）/ LoadBalancer（生產）|
| **Ingress** | HTTP 路由層，TLS 終止，統一入口 |
| **HPA** | 基於 CPU / 記憶體 / 自定義指標自動擴縮容 |
| **ConfigMap / Secret** | 配置與程式碼分離，生產用 Vault 管理 Secret |

### Docker 多階段建置
```dockerfile
# 建置階段（含 JDK + Maven）
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder
COPY pom.xml .
RUN mvn dependency:go-offline -B   # 利用 Docker 層緩存
COPY src ./src
RUN mvn clean package -DskipTests

# 運行階段（只含 JRE，Image 最小化）
FROM eclipse-temurin:17-jre-alpine AS runtime
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/app-*.jar app.jar
USER appuser
EXPOSE 8080
```

---

## 📊 監控與可觀測性

### 可觀測性三大支柱

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   Metrics    │  │     Logs     │  │   Traces     │
│   （指標）    │  │   （日誌）    │  │ （鏈路追蹤）   │
│              │  │              │  │              │
│ Prometheus   │  │  ELK Stack   │  │ SkyWalking   │
│  + Grafana   │  │              │  │  / Zipkin    │
└──────────────┘  └──────────────┘  └──────────────┘

三者協作：
  Metrics 告訴你「P99 延遲突然升到 5 秒！」
  Traces  告訴你「瓶頸在 MySQL 查詢，耗時 4.8 秒」
  Logs    告訴你「慢查詢 SQL：缺少索引」
```

### 告警分級體系

| 等級 | 條件 | 響應時間 | 通知方式 |
|------|------|---------|---------|
| **P0 緊急** | 服務宕機 / 全量 5xx | 5 分鐘內 | PagerDuty 電話 + Slack @channel |
| **P1 嚴重** | 錯誤率 > 10% / P99 > 5s | 30 分鐘內 | Slack @here + Email |
| **P2 警告** | 錯誤率 > 1% / CPU > 80% | 工作時間內 | Slack 通知 |
| **P3 提示** | 輕微效能退化 | 下個迭代 | 週報匯總 |

### 關鍵 PromQL

```promql
# QPS
sum by (app) (rate(http_server_requests_seconds_count[1m]))

# P99 延遲
histogram_quantile(0.99,
  sum by (app, le) (rate(http_server_requests_seconds_bucket[5m])))

# 5xx 錯誤率（%）
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count[5m])) * 100

# JVM 堆記憶體使用率
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100
```

---

## 🟣 MySQL 深度優化

### 索引核心原理

```
B+樹特性：
  ✅ 所有數據存在葉節點，雙向鏈表串連（支援範圍查詢）
  ✅ 樹高 3~4 層，千萬級數據只需 3 次磁碟 IO
  ✅ 非葉節點只存索引鍵（可存更多鍵，降低樹高）

索引類型：
  聚簇索引（Clustered）  → 主鍵，葉節點存完整行資料
  非聚簇索引（Secondary）→ 葉節點存主鍵值，需回表
  覆蓋索引（Covering）  → 所需欄位都在索引中，不需回表 ✅
```

### 執行計劃 EXPLAIN 速查

| type | 說明 | 效能 |
|------|------|------|
| `system` / `const` | 主鍵/唯一索引等值查詢 | ⭐⭐⭐⭐⭐ |
| `eq_ref` | JOIN 時被驅動表用主鍵 | ⭐⭐⭐⭐⭐ |
| `ref` | 非唯一索引等值查詢 | ⭐⭐⭐⭐ |
| `range` | 索引範圍查詢 | ⭐⭐⭐ |
| `index` | 全索引掃描 | ⭐⭐ |
| `ALL` | 全表掃描 🚨 | ⭐ |

### MVCC 與事務隔離

| 隔離級別 | 臟讀 | 不可重複讀 | 幻讀 |
|---------|------|----------|------|
| READ UNCOMMITTED | ✅ | ✅ | ✅ |
| READ COMMITTED | ❌ | ✅ | ✅ |
| **REPEATABLE READ（預設）** | ❌ | ❌ | ⚠️（Gap Lock 解決）|
| SERIALIZABLE | ❌ | ❌ | ❌ |

> MVCC 核心：每行隱藏 `trx_id` + `roll_pointer`，通過 ReadView 判斷可見版本，讀不加鎖實現高並發

---

## 🔶 設計模式（GoF 23 種）

### 創建型（5 種）

| 模式 | 核心思想 | Spring 應用 |
|------|---------|------------|
| **單例** | 只有一個實例 | Spring Bean（預設 Singleton）|
| **工廠方法** | 子類決定創建哪個類 | BeanFactory，消滅 if-else |
| **抽象工廠** | 創建一系列相關對象 | 跨平台/跨資料庫場景 |
| **建造者** | 分步驟創建複雜對象 | Lombok @Builder / ResponseEntity |
| **原型** | 克隆現有對象 | BeanUtils.copyProperties |

### 結構型（7 種）

| 模式 | 核心思想 | Spring 應用 |
|------|---------|------------|
| **代理** ⭐ | 為對象添加額外功能 | AOP / @Transactional / @Cacheable |
| **裝飾者** | 動態添加功能 | IO 流 / BeanWrapper |
| **適配器** | 讓不兼容介面協同 | HandlerAdapter / 第三方 SDK |
| **外觀** | 簡化複雜子系統介面 | JdbcTemplate / RestTemplate |
| **橋接** | 抽象與實作分離 | 避免繼承爆炸 |
| **組合** | 樹形結構統一處理 | 菜單樹 / 組織架構 |
| **享元** | 共享細粒度對象 | Integer 緩存池 / 資料庫連線池 |

### 行為型（11 種）

| 模式 | 核心思想 | Spring 應用 |
|------|---------|------------|
| **觀察者** ⭐ | 一對多事件通知 | ApplicationEvent / MQ |
| **策略** ⭐ | 算法族可互換 | 消滅 if-else，工廠 + Map 路由 |
| **模板方法** | 定義算法骨架 | JdbcTemplate / AbstractApplicationContext |
| **責任鏈** | 請求沿鏈傳遞 | Filter Chain / Security Filter Chain |
| **狀態** | 不同狀態不同行為 | 訂單狀態機 |
| **命令** | 請求封裝成對象 | 撤銷/重做 / Spring Batch |
| **迭代器** | 統一遍歷集合 | Iterator / Stream / 分頁迭代器 |
| **中介者** | 集中管理交互 | ApplicationEventPublisher / MQ |
| **備忘錄** | 捕獲與恢復狀態 | 草稿自動保存 |
| **訪問者** | 不修改類就加新操作 | 跨類型操作（報表/稅率）|
| **直譯器** | 定義語言文法 | SpEL / 正則表達式 |

---

## 🟤 Elasticsearch

### 倒排索引原理

```
文件內容 → 分詞 → 建立倒排索引

詞（Term） → 文件列表（Posting List）
"Java"     → [Doc1, Doc2, Doc3]
"Spring"   → [Doc1, Doc3]

搜索 "Java" → 直接查倒排索引 → 毫秒級返回
（無需掃描任何文件內容！）

相關性評分（BM25）：
  TF（詞頻）× IDF（稀有度）→ 越稀有的詞越重要
```

### DSL 查詢核心

```json
{
  "query": {
    "bool": {
      "must":     [{ "multi_match": { "query": "iPhone", "fields": ["name^3", "description"] }}],
      "filter":   [{ "term": { "category": "手機" }},
                   { "range": { "price": { "gte": 1000, "lte": 10000 }}}],
      "must_not": [{ "term": { "status": "OFFLINE" }}]
    }
  },
  "sort":      [{ "_score": "desc" }, { "rating": "desc" }],
  "highlight": { "fields": { "name": {}}},
  "aggs":      { "by_brand": { "terms": { "field": "brand" }}}
}
```

> **must**：影響相關性分數 | **filter**：不影響分數，有緩存，效能最好

---

## 🔵 API 設計與通訊協議

### RESTful 設計規範

| HTTP 方法 | 用途 | 冪等性 | 成功 Status |
|----------|------|-------|------------|
| GET | 查詢 | ✅ | 200 OK |
| POST | 新增 | ❌ | 201 Created |
| PUT | 完整更新 | ✅ | 200 OK |
| PATCH | 部分更新 | ❌ | 200 OK |
| DELETE | 刪除 | ✅ | 204 No Content |

### 通訊協議選型

```
對外公開 API     → RESTful（可讀性高，瀏覽器友好）
微服務內部通訊   → gRPC（Protobuf 二進制，比 REST 快 5~10 倍）
即時推播 / 聊天  → WebSocket（持久雙向連接）
複雜前端數據需求 → GraphQL（客戶端精確指定需要的欄位）
```

### gRPC 四種通訊模式

| 模式 | 說明 | 適用場景 |
|------|------|---------|
| Unary | 單請求 / 單響應 | 一般 CRUD 操作 |
| Server Streaming | 單請求 / 多響應 | 訂單狀態監聽 |
| Client Streaming | 多請求 / 單響應 | 批量數據上傳 |
| Bidirectional Streaming | 雙向流 | 即時協作 / 聊天 |

---

## 🔴 測試

### 測試金字塔

```
              ╱▔▔▔▔▔▔▔▔▔╲
             ╱   E2E 測試  ╲   10%  → Selenium / Playwright
            ╱───────────────╲
           ╱   整合測試       ╲  20%  → @SpringBootTest / Testcontainers
          ╱─────────────────── ╲
         ╱      單元測試         ╲ 70%  → JUnit 5 + Mockito（毫秒級）
        ╱▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁╲
```

### 測試工具對照

| 工具 | 用途 |
|------|------|
| **JUnit 5** | 測試框架，@Test / @Nested / @ParameterizedTest |
| **Mockito** | Mock / Stub / Spy / verify / ArgumentCaptor |
| **AssertJ** | 流式斷言，比 JUnit 原生更可讀 |
| **@WebMvcTest** | Controller 層切片測試，MockMvc 模擬 HTTP |
| **@DataJpaTest** | Repository 層切片測試，H2 記憶體資料庫 |
| **@SpringBootTest** | 完整應用上下文整合測試 |
| **Testcontainers** | Docker 啟動真實 MySQL / Redis / Kafka 做整合測試 |
| **JaCoCo** | 測試覆蓋率報告，核心業務 > 80% |

### TDD 循環

```
🔴 Red（寫失敗的測試）
    ↓
🟢 Green（寫最少程式碼讓測試通過）
    ↓
🔵 Refactor（在測試保護下重構）
    ↓
🔴 Red（繼續下一個測試）...
```

### 測試命名規範
```java
// methodName_StateUnderTest_ExpectedBehavior
void createOrder_WhenInsufficientStock_ShouldThrowException() { }
void getUser_WhenUserExists_ShouldReturnUserDTO() { }
void deleteOrder_WhenOrderNotFound_ShouldReturn404() { }
```

---

## 🛠️ 技術棧總覽

```
語言 & 框架
  Java 17 / 21（Virtual Threads / Record / Sealed Class）
  Spring Boot 3.x / Spring Cloud / Spring Security
  MyBatis-Plus / Spring Data JPA / Hibernate

資料庫 & 快取
  MySQL 8.0（B+樹索引 / MVCC / 主從複製）
  Redis（快取 / 分散式鎖 / Session / 限流）
  Elasticsearch 8.x（全文搜索 / ELK 日誌）

消息佇列
  Apache Kafka（高吞吐量 / 事件驅動）
  RabbitMQ（可靠消息傳遞 / 死信佇列）

容器化 & 編排
  Docker（多階段建置 / 最小化 Image）
  Kubernetes（Deployment / Service / HPA / Helm）

CI/CD
  GitHub Actions / Jenkins Pipeline
  SonarQube / Trivy / Harbor Registry

監控
  Prometheus + Grafana（指標）
  ELK Stack（日誌）
  SkyWalking（鏈路追蹤）
  Spring Boot Actuator（應用健康）

API 通訊
  RESTful API / gRPC / WebSocket / GraphQL

測試
  JUnit 5 / Mockito / Testcontainers / TDD / JaCoCo
```

---

## 📖 學習方法

> 本筆記採用 **深度優先** 策略，每個主題不只停留在「會用」的層次，
> 而是深入到**底層原理**，理解「為什麼這樣設計」。

每個主題的學習路徑：
1. **Why**：為什麼需要這個技術？解決了什麼問題？
2. **What**：核心概念與架構是什麼？
3. **How**：底層是怎麼實作的？
4. **Practice**：如何在實際專案中正確使用？
5. **Pitfalls**：有哪些常見陷阱和最佳實踐？

---

<div align="center">

**持續學習，持續成長** 🚀

\*「不理解底層原理的工程師，只是在調用 API 而已」\*

</div>
