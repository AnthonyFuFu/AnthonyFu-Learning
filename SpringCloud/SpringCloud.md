# ☁️ Spring Cloud 微服務 完整深入教學

---

## 🗺️ 學習路線圖

```
Spring Cloud 微服務
├── 1️⃣ 微服務核心概念
│   ├── 單體 vs 微服務
│   └── Spring Cloud 生態全覽
├── 2️⃣ Nacos
│   ├── 服務發現（Service Registry）
│   └── 配置中心（Config Center）
├── 3️⃣ OpenFeign（聲明式 HTTP 客戶端）
│   ├── 基本使用
│   ├── 攔截器 / 超時 / 重試
│   └── 整合 Sentinel 熔斷
├── 4️⃣ Spring Cloud Gateway
│   ├── 路由（Route）
│   ├── 斷言（Predicate）
│   ├── 過濾器（Filter）
│   └── 限流 / 認證 / 灰度發布
└── 5️⃣ Sentinel（限流 / 熔斷 / 降級）
    ├── 核心概念
    ├── 流量控制規則
    ├── 熔斷降級規則
    └── Spring Boot 整合
```

---

## 1️⃣ 微服務核心概念

---

### 🔵 單體 vs 微服務

```
單體架構（Monolith）：
┌─────────────────────────────────┐
│           單體應用               │
│  用戶模組 │ 訂單模組 │ 商品模組  │
│          資料庫（共用）           │
└─────────────────────────────────┘
✅ 開發簡單、部署方便、本地調用快
❌ 模組耦合、任一模組出問題全掛、無法獨立擴容

微服務架構（Microservices）：
┌──────────┐  ┌──────────┐  ┌──────────┐
│ 用戶服務  │  │ 訂單服務  │  │ 商品服務  │
│  DB:用戶  │  │  DB:訂單  │  │  DB:商品  │
└──────────┘  └──────────┘  └──────────┘
       ↑              ↑              ↑
       └──────────────┴──────────────┘
                API Gateway
                    ↑
                  客戶端

✅ 獨立部署、獨立擴容、技術棧靈活
❌ 分散式複雜度、網路開銷、分散式事務
```

---

### 🟡 Spring Cloud 生態全覽

```
組件                    功能                    替代方案
────────────────────────────────────────────────────────
Nacos                  服務發現 + 配置中心      Eureka + Config
OpenFeign              聲明式 HTTP 客戶端       RestTemplate
Spring Cloud Gateway   API 閘道               Zuul
Sentinel               限流 / 熔斷 / 降級      Hystrix / Resilience4j
Sleuth + Zipkin        分散式鏈路追蹤           SkyWalking
Spring Cloud Stream    消息驅動               直接用 Kafka/RabbitMQ
Seata                  分散式事務              TCC / SAGA 手動實作
```

```xml
<!-- pom.xml（Spring Cloud + Alibaba 依賴管理）-->
<properties>
    <spring-boot.version>3.2.0</spring-boot.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
    <spring-cloud-alibaba.version>2023.0.0.0</spring-cloud-alibaba.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-dependencies</artifactId>
            <version>${spring-cloud-alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 2️⃣ Nacos

---

### 🔵 服務發現（Service Registry）

```
服務發現核心流程：

訂單服務啟動
    → 向 Nacos 註冊（ip:port + 服務名 + 健康狀態）
    ↓
用戶服務要呼叫訂單服務
    → 問 Nacos：「order-service 在哪？」
    → Nacos 返回所有健康的 order-service 實例列表
    → 用戶服務根據負載均衡策略選一個呼叫

Nacos 健康檢查：
  客戶端主動心跳（預設 5 秒一次）
  超過 15 秒未收到心跳 → 標記為不健康
  超過 30 秒未收到心跳 → 從服務列表移除

臨時實例 vs 永久實例：
  臨時實例（預設）：客戶端心跳，服務下線自動移除
  永久實例：Nacos 主動探測，服務下線標記不健康但不移除
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  application:
    name: order-service        # 服務名（必填）
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: dev         # 命名空間（環境隔離）
        group: DEFAULT_GROUP
        # 元數據（可用於路由決策）
        metadata:
          version: v1
          region: cn-east
```

```java
// 主類開啟服務發現
@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

// 查詢服務實例（底層 API，了解原理用）
@Autowired
private DiscoveryClient discoveryClient;

public void printInstances() {
    List<ServiceInstance> instances =
        discoveryClient.getInstances("user-service");

    instances.forEach(instance ->
        log.info("實例：{}:{}", instance.getHost(), instance.getPort())
    );
}
```

---

### 🟡 配置中心（Config Center）

```
為什麼需要配置中心？

❌ 傳統方式：配置寫在 application.yml
  → 修改配置需要重新打包部署
  → 多個服務的配置散落各處，難以管理
  → 生產環境密碼暴露在程式碼中

✅ 配置中心：
  → 統一管理所有服務的配置
  → 修改配置即時生效（不需要重啟）
  → 配置版本管理（可以回滾）
  → 環境隔離（dev / staging / prod）

Nacos 配置 DataId 命名規則：
  ${spring.application.name}-${spring.profiles.active}.${file-extension}
  例：order-service-dev.yaml
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-bootstrap</artifactId>
</dependency>
```

```yaml
# bootstrap.yml（比 application.yml 更早加載）
spring:
  application:
    name: order-service
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yaml
        # 共享配置（多個服務共用）
        shared-configs:
          - data-id: common-dev.yaml
            group: DEFAULT_GROUP
            refresh: true
        # 擴展配置（優先級更高）
        extension-configs:
          - data-id: order-service-extra.yaml
            group: DEFAULT_GROUP
            refresh: true
```

```java
// 動態刷新配置（@RefreshScope）
@RestController
@RefreshScope  // ⭐ 加上這個注解，配置更新後自動刷新
public class OrderController {

    @Value("${order.timeout:30}")
    private int orderTimeout;

    @Value("${order.max-items:100}")
    private int maxItems;

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        return Map.of(
            "timeout",  orderTimeout,
            "maxItems", maxItems
        );
    }
}

// 監聽配置變化
@Component
public class ConfigChangeListener {

    @NacosValue(value = "${order.timeout:30}", autoRefreshed = true)
    private int orderTimeout;

    // 方式二：@ConfigurationProperties（推薦，類型安全）
    @Configuration
    @ConfigurationProperties(prefix = "order")
    @RefreshScope
    @Data
    public static class OrderProperties {
        private int    timeout  = 30;
        private int    maxItems = 100;
        private String currency = "TWD";
    }
}
```

---

## 3️⃣ OpenFeign（聲明式 HTTP 客戶端）

---

### 🔵 基本使用

```java
// ❌ 傳統方式（RestTemplate）：冗長、硬編碼 URL
@Autowired
private RestTemplate restTemplate;

public User getUser(Long id) {
    return restTemplate.getForObject(
        "http://user-service/api/v1/users/" + id,
        User.class
    );
}

// ✅ OpenFeign：像呼叫本地方法一樣呼叫遠端服務
@FeignClient(
    name       = "user-service",          // 對應 Nacos 服務名
    path       = "/api/v1",               // 統一前綴
    fallback   = UserServiceFallback.class // 降級處理
)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable Long id);

    @PostMapping("/users")
    UserDTO createUser(@RequestBody CreateUserRequest request);

    @GetMapping("/users")
    PageResult<UserDTO> listUsers(
        @RequestParam int page,
        @RequestParam int size
    );
}

// 啟用 OpenFeign
@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.client")
public class OrderServiceApplication { }
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<!-- 負載均衡（必須，OpenFeign 依賴）-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

---

### 🟡 攔截器 / 超時 / 重試

```yaml
# application.yml
spring:
  cloud:
    openfeign:
      client:
        config:
          # 全局配置
          default:
            connect-timeout: 2000   # 連接超時 2s
            read-timeout:    5000   # 讀取超時 5s
            logger-level:    BASIC  # 日誌級別：NONE/BASIC/HEADERS/FULL
          # 針對特定服務配置
          user-service:
            connect-timeout: 1000
            read-timeout:    3000
      # 開啟 OkHttp（比預設 URLConnection 效能更好）
      httpclient:
        ok-http:
          enabled: true
```

```java
// Feign 攔截器（傳遞 Token / TraceId）
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 從當前請求的 Header 取得 Token，傳遞到下游服務
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token    = request.getHeader("Authorization");
            String traceId  = request.getHeader("X-Trace-Id");

            if (token   != null) template.header("Authorization", token);
            if (traceId != null) template.header("X-Trace-Id", traceId);
        }
    }
}

// Feign 重試（謹慎！非冪等操作不能重試）
@Bean
public Retryer feignRetryer() {
    // 初始間隔 100ms，最大間隔 1s，最多重試 3 次
    return new Retryer.Default(100, 1000, 3);
}

// 降級實作（Fallback）
@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public UserDTO getUser(Long id) {
        // 降級：返回預設值，不讓異常傳播
        log.warn("user-service 不可用，返回預設用戶, id={}", id);
        return UserDTO.builder()
            .id(id)
            .username("Unknown")
            .build();
    }

    @Override
    public UserDTO createUser(CreateUserRequest request) {
        throw new ServiceUnavailableException("用戶服務暫時不可用");
    }
}

// 帶原因的 Fallback（可以知道失敗原因）
@Component
public class UserServiceFallbackFactory
        implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        log.error("user-service 呼叫失敗：{}", cause.getMessage());
        return new UserServiceClient() {
            @Override
            public UserDTO getUser(Long id) {
                return UserDTO.defaultUser(id);
            }
            // ...
        };
    }
}
```

---

## 4️⃣ Spring Cloud Gateway

---

### 🔵 核心架構

```
請求流程：

客戶端請求
    ↓
Gateway Handler Mapping（匹配路由）
    ↓
Gateway Web Handler
    ↓
Pre Filter Chain（前置過濾器）
    ├── 認證過濾器（驗證 JWT）
    ├── 限流過濾器（令牌桶）
    └── 日誌過濾器（記錄請求）
    ↓
下游微服務（轉發請求）
    ↓
Post Filter Chain（後置過濾器）
    ├── 響應頭過濾器
    └── 日誌過濾器（記錄響應）
    ↓
返回客戶端
```

```yaml
# application.yml（Gateway 配置）
spring:
  cloud:
    gateway:
      routes:
        # 訂單服務路由
        - id:  order-service-route
          uri: lb://order-service      # lb:// = 從 Nacos 負載均衡
          predicates:
            - Path=/api/v1/orders/**   # 路徑匹配
            - Method=GET,POST          # HTTP 方法匹配
            - Header=X-Request-Type, order  # Header 匹配
          filters:
            - StripPrefix=0            # 不去除路徑前綴
            - AddRequestHeader=X-Gateway-Source, true  # 新增請求頭
            - AddResponseHeader=X-Response-Time, ${T(System).currentTimeMillis()}
            # 限流（令牌桶）
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate:  100  # 每秒補充 100 個令牌
                redis-rate-limiter.burstCapacity:  200  # 桶的容量 200
                key-resolver: "#{@userKeyResolver}"     # 限流 key 策略

        # 用戶服務路由
        - id:  user-service-route
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**

        # 靜態資源路由（直接返回）
        - id:  static-route
          uri: https://static.example.com
          predicates:
            - Path=/static/**

      # 全局過濾器配置
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Credentials Access-Control-Allow-Origin

      # 全局跨域配置
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "*"
            allowed-methods: "*"
            allowed-headers: "*"
```

```java
// 自定義全局過濾器（JWT 認證）
@Component
@Order(-1)  // 優先級（數字越小越先執行）
@Slf4j
public class AuthGlobalFilter implements GlobalFilter {

    private final JwtTokenProvider jwtProvider;

    // 不需要認證的路徑
    private static final Set<String> WHITE_LIST = Set.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/products"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名單放行
        if (WHITE_LIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 驗證 JWT Token
        String token = exchange.getRequest()
            .getHeaders()
            .getFirst("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String jwt    = token.substring(7);
            Claims claims = jwtProvider.parseToken(jwt);
            String userId = claims.getSubject();

            // 將用戶 ID 傳遞給下游服務
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-User-Id", userId)
                .build();

            return chain.filter(exchange.mutate()
                .request(mutatedRequest)
                .build());

        } catch (Exception e) {
            log.warn("JWT 驗證失敗：{}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}

// 限流 key 策略（按用戶 ID 限流）
@Bean
public KeyResolver userKeyResolver() {
    return exchange -> Mono.justOrEmpty(
        exchange.getRequest().getHeaders().getFirst("X-User-Id")
    ).defaultIfEmpty("anonymous");
}

// 按 IP 限流
@Bean
public KeyResolver ipKeyResolver() {
    return exchange -> Mono.just(
        Objects.requireNonNull(
            exchange.getRequest().getRemoteAddress()
        ).getAddress().getHostAddress()
    );
}
```

---

### 🟡 灰度發布（金絲雀發布）

```yaml
# 灰度路由：Header 帶有 X-Version:v2 的請求路由到新版本
spring:
  cloud:
    gateway:
      routes:
        # 灰度版本（新版本，10% 流量）
        - id:  order-service-canary
          uri: lb://order-service-v2
          predicates:
            - Path=/api/v1/orders/**
            - Header=X-Version, v2       # Header 匹配
          filters:
            - StripPrefix=0

        # 正式版本（舊版本，90% 流量）
        - id:  order-service-stable
          uri: lb://order-service-v1
          predicates:
            - Path=/api/v1/orders/**
```

---

## 5️⃣ Sentinel（限流 / 熔斷 / 降級）

---

### 🔵 核心概念

```
Sentinel 三大功能：

限流（Flow Control）：
  控制請求速率，超過閾值拒絕請求
  目的：保護服務不被流量壓垮

熔斷（Circuit Breaker）：
  當下游服務錯誤率/慢響應率過高時，自動切斷請求
  目的：防止錯誤擴散（雪崩效應）

降級（Degrade）：
  服務不可用時，返回預設值（Fallback）
  目的：保證核心功能可用

熔斷器三種狀態：

CLOSED（關閉）
  → 正常通過請求，統計錯誤率
  
  錯誤率 / 慢響應率 超過閾值
  ↓
  
OPEN（打開）
  → 直接拒絕所有請求（快速失敗）
  
  等待 timeWindow 秒後
  ↓
  
HALF_OPEN（半開）
  → 放入一個探測請求
  → 成功 → 回到 CLOSED
  → 失敗 → 回到 OPEN
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
<!-- Sentinel Dashboard 整合 -->
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-transport-simple-http</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  cloud:
    sentinel:
      transport:
        dashboard: localhost:8080  # Sentinel 控制台地址
        port:      8719            # 本服務暴露給控制台的端口
      eager: true                  # 立即向控制台註冊
      datasource:
        # 從 Nacos 加載規則（持久化）
        flow:
          nacos:
            server-addr: localhost:8848
            data-id:     ${spring.application.name}-flow-rules
            group-id:    SENTINEL_GROUP
            data-type:   json
            rule-type:   flow
```

```java
// 方式一：@SentinelResource 注解
@Service
public class OrderService {

    // blockHandler：限流/熔斷時調用
    // fallback：業務異常時調用
    @SentinelResource(
        value        = "createOrder",
        blockHandler = "createOrderBlocked",
        fallback     = "createOrderFallback"
    )
    public Order createOrder(OrderRequest request) {
        // 可能拋出業務異常
        return orderRepository.save(convert(request));
    }

    // 限流處理（必須和原方法簽名一樣，最後多一個 BlockException）
    public Order createOrderBlocked(OrderRequest request, BlockException ex) {
        log.warn("createOrder 被限流：{}", ex.getRuleLimitApp());
        throw new RateLimitException("系統繁忙，請稍後再試");
    }

    // 降級處理（必須和原方法簽名一樣，最後多一個 Throwable）
    public Order createOrderFallback(OrderRequest request, Throwable ex) {
        log.error("createOrder 發生異常，執行降級：{}", ex.getMessage());
        return Order.defaultOrder(); // 返回預設值
    }
}

// 方式二：程式碼方式定義規則（測試 / 本地開發用）
@PostConstruct
public void initSentinelRules() {
    // 流量控制規則
    FlowRule flowRule = new FlowRule();
    flowRule.setResource("createOrder");
    flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS); // 按 QPS 限流
    flowRule.setCount(100);                          // 每秒最多 100 個請求
    FlowRuleManager.loadRules(List.of(flowRule));

    // 熔斷規則
    DegradeRule degradeRule = new DegradeRule();
    degradeRule.setResource("createOrder");
    // 按錯誤率熔斷
    degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
    degradeRule.setCount(0.5);      // 錯誤率 > 50% 觸發熔斷
    degradeRule.setMinRequestAmount(10); // 最少 10 個請求才統計
    degradeRule.setTimeWindow(10);  // 熔斷後等待 10 秒
    DegradeRuleManager.loadRules(List.of(degradeRule));
}

// 方式三：整合 OpenFeign（呼叫下游服務時的熔斷）
@FeignClient(
    name     = "user-service",
    fallback = UserServiceFallback.class
)
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable Long id);
}
```

---

## 📝 Spring Cloud 微服務 總結

| 組件 | 核心功能 | 關鍵設定 |
|------|---------|---------|
| **Nacos 服務發現** | 服務註冊 / 發現 / 健康檢查 | server-addr / namespace |
| **Nacos 配置中心** | 統一配置 / 動態刷新 | @RefreshScope / bootstrap.yml |
| **OpenFeign** | 聲明式 HTTP 客戶端 | @FeignClient / 攔截器傳遞 Token |
| **Gateway** | 路由 / 認證 / 限流 / 跨域 | 全局 Filter / KeyResolver |
| **Sentinel 限流** | QPS / 並發數控制 | FlowRule |
| **Sentinel 熔斷** | 錯誤率 / 慢響應率控制 | DegradeRule |
| **Sentinel 降級** | 異常時返回預設值 | @SentinelResource fallback |
| **灰度發布** | Header 路由到新版本 | Predicate + 多路由 |
