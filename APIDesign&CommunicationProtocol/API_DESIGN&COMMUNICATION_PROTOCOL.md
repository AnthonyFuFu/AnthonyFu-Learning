# 🔵 API 設計與通訊協議 完整深入教學

---

## 🗺️ 學習路線圖

```
API 設計與通訊協議
├── 1️⃣ RESTful API 設計最佳實踐
│   ├── REST 設計原則
│   ├── HTTP 方法與冪等性
│   ├── HTTP Status Code
│   ├── API 版本管理
│   ├── 統一響應格式
│   └── API 安全設計
├── 2️⃣ gRPC（高效能 RPC 框架）
│   ├── 核心原理與 Protobuf
│   ├── 四種通訊模式
│   ├── Spring Boot 整合
│   └── gRPC vs REST 比較
├── 3️⃣ WebSocket（即時通訊）
│   ├── WebSocket 原理
│   ├── Spring Boot 整合（STOMP）
│   └── 聊天室實戰
└── 4️⃣ GraphQL（靈活查詢語言）
    ├── 核心概念（Query / Mutation / Subscription）
    ├── Spring Boot 整合
    └── GraphQL vs REST 比較
```

---

## 1️⃣ RESTful API 設計最佳實踐

---

### 🔵 REST 設計六大原則

```
REST（Representational State Transfer）架構風格：

① 統一介面（Uniform Interface）
   資源用名詞表示，不用動詞
   ✅ GET /orders/123
   ❌ GET /getOrder?id=123

② 無狀態（Stateless）
   每個請求都包含完整資訊，服務器不儲存 Session
   ✅ 每次請求帶上 JWT Token
   ❌ 依賴服務器端 Session

③ 可快取（Cacheable）
   GET 請求應該支援快取
   使用 Cache-Control、ETag、Last-Modified 頭

④ 客戶端/服務器分離（Client-Server）
   前後端分離，各自獨立演化

⑤ 分層系統（Layered System）
   客戶端不需要知道是直接連服務器，
   還是通過 Load Balancer / API Gateway

⑥ 按需程式碼（Code on Demand，可選）
   服務器可以返回可執行的程式碼（如 JavaScript）
```

---

### 🟡 URI 設計規範

```
資源命名規則：

✅ 使用名詞（複數）
   /users          用戶列表
   /users/123      特定用戶
   /orders         訂單列表
   /orders/456     特定訂單

✅ 層次關係用路徑表示
   /users/123/orders        用戶 123 的所有訂單
   /users/123/orders/456    用戶 123 的訂單 456

✅ 過濾、排序、分頁用查詢參數
   GET /orders?status=PAID&sort=created_at&order=desc&page=1&size=20

✅ 搜索
   GET /products/search?keyword=iPhone&category=手機

❌ 不要用動詞
   /getUsers     ❌  → GET /users        ✅
   /createOrder  ❌  → POST /orders       ✅
   /deleteUser   ❌  → DELETE /users/123  ✅
   /updateOrder  ❌  → PUT /orders/456    ✅

❌ 不要用大寫和底線
   /UserOrders   ❌  → /user-orders       ✅
   /user_orders  ❌  → /user-orders       ✅

特殊操作（非 CRUD）：
   POST /orders/456/cancel    取消訂單（動作）
   POST /orders/456/pay       付款
   POST /users/123/activate   啟用帳號
```

---

### 🟠 HTTP 方法與冪等性

```
HTTP 方法     語義          安全性  冪等性
──────────────────────────────────────────────
GET          查詢           ✅      ✅
HEAD         查詢（只要頭）  ✅      ✅
OPTIONS      查詢支援的方法  ✅      ✅
POST         新增/提交      ❌      ❌
PUT          完整更新       ❌      ✅
PATCH        部分更新       ❌      ❌
DELETE       刪除           ❌      ✅

安全性（Safe）：不修改服務器狀態
冪等性（Idempotent）：多次執行結果相同

重點：
  GET /orders/123     → 每次都返回相同訂單（安全 + 冪等）
  POST /orders        → 每次呼叫可能建立新訂單（非冪等！）
  PUT /orders/123     → 多次更新結果相同（冪等）
  DELETE /orders/123  → 第一次刪除，之後呼叫返回 404（冪等）

POST 的冪等性設計（防重複提交）：
  客戶端生成唯一的 Idempotency-Key
  服務器用此 Key 判斷是否重複請求

  POST /payments
  Idempotency-Key: uuid-abc-123
  → 第一次：建立支付，存 Redis key=uuid-abc-123, value=result
  → 重複請求：直接返回 Redis 中的結果
```

---

### 🔴 HTTP Status Code 完整規範

```
1xx - 資訊性
  100 Continue         → 繼續發送請求體

2xx - 成功
  200 OK               → 查詢成功（GET / PUT / PATCH）
  201 Created          → 建立成功（POST），響應頭帶 Location
  202 Accepted         → 已接受，非同步處理（如：提交任務）
  204 No Content       → 成功但無返回內容（DELETE）

3xx - 重定向
  301 Moved Permanently → 永久重定向（更新書籤）
  302 Found            → 臨時重定向
  304 Not Modified     → 緩存未過期，使用本地緩存

4xx - 客戶端錯誤
  400 Bad Request      → 請求格式錯誤、參數非法
  401 Unauthorized     → 未認證（沒有 Token）
  403 Forbidden        → 已認證但無權限（有 Token 但角色不對）
  404 Not Found        → 資源不存在
  405 Method Not Allowed → HTTP 方法不支援
  409 Conflict         → 資源衝突（如：用戶名已存在）
  410 Gone             → 資源已永久刪除（和 404 的區別）
  422 Unprocessable Entity → 格式正確但業務驗證失敗
  429 Too Many Requests → 超過限流閾值

5xx - 服務器錯誤
  500 Internal Server Error → 服務器未預期的錯誤
  502 Bad Gateway           → 上游服務返回無效響應
  503 Service Unavailable   → 服務器暫時不可用（維護/過載）
  504 Gateway Timeout       → 上游服務超時
```

```java
// Spring Boot 正確使用 Status Code
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 查詢 → 200 OK
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrder(
            @PathVariable Long id) {
        OrderDTO order = orderService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    // 建立 → 201 Created（含 Location 頭）
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDTO order = orderService.create(request);

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(order.getId())
            .toUri();

        return ResponseEntity
            .created(location)  // 201 Created
            .body(ApiResponse.success(order));
    }

    // 更新 → 200 OK（返回更新後的資源）
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {
        OrderDTO order = orderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    // 刪除 → 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();  // 204
    }

    // 取消（非 CRUD 動作）→ 200 OK
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Long id,
            @RequestBody CancelOrderRequest request) {
        OrderDTO order = orderService.cancel(id, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    // 非同步操作 → 202 Accepted
    @PostMapping("/export")
    public ResponseEntity<ApiResponse<String>> exportOrders(
            @RequestBody ExportRequest request) {
        String taskId = orderService.asyncExport(request);
        return ResponseEntity
            .accepted()  // 202
            .body(ApiResponse.success(taskId));
    }
}
```

---

### 🟢 統一響應格式

```java
// 統一 API 響應格式
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String  code;       // 業務狀態碼
    private String  message;
    private T       data;
    private String  traceId;    // 請求追蹤 ID（方便排查）
    private Long    timestamp;

    // 成功
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("SUCCESS")
            .message("操作成功")
            .data(data)
            .traceId(MDC.get("traceId"))
            .timestamp(System.currentTimeMillis())
            .build();
    }

    // 失敗
    public static <T> ApiResponse<T> fail(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(code)
            .message(message)
            .traceId(MDC.get("traceId"))
            .timestamp(System.currentTimeMillis())
            .build();
    }
}

// 業務錯誤碼枚舉
public enum ErrorCode {
    // 通用錯誤
    PARAM_INVALID        ("PARAM_001", "請求參數不合法"),
    RESOURCE_NOT_FOUND   ("RESOURCE_001", "資源不存在"),
    RESOURCE_CONFLICT    ("RESOURCE_002", "資源已存在"),
    UNAUTHORIZED         ("AUTH_001", "請先登入"),
    FORBIDDEN            ("AUTH_002", "無操作權限"),
    RATE_LIMIT_EXCEEDED  ("RATE_001", "請求太頻繁，請稍後再試"),

    // 訂單錯誤
    ORDER_NOT_FOUND      ("ORDER_001", "訂單不存在"),
    ORDER_ALREADY_PAID   ("ORDER_002", "訂單已支付"),
    ORDER_CANCELLED      ("ORDER_003", "訂單已取消"),

    // 庫存錯誤
    INSUFFICIENT_STOCK   ("STOCK_001", "庫存不足"),

    // 系統錯誤
    SYSTEM_ERROR         ("SYS_001", "系統異常，請稍後再試");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code    = code;
        this.message = message;
    }
}

// 全局異常處理器
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 業務異常
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.warn("業務異常：{}, path={}", e.getMessage(), request.getRequestURI());
        return ResponseEntity
            .status(e.getHttpStatus())
            .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    // 參數驗證失敗（@Valid）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (a, b) -> a
            ));

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.<Map<String, String>>builder()
                .success(false)
                .code(ErrorCode.PARAM_INVALID.getCode())
                .message("請求參數驗證失敗")
                .data(errors)
                .traceId(MDC.get("traceId"))
                .timestamp(System.currentTimeMillis())
                .build());
    }

    // 資源不存在
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(
            ResourceNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.fail(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                e.getMessage()
            ));
    }

    // 限流異常
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(
            RateLimitException e) {
        return ResponseEntity
            .status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", "60")
            .body(ApiResponse.fail(
                ErrorCode.RATE_LIMIT_EXCEEDED.getCode(),
                ErrorCode.RATE_LIMIT_EXCEEDED.getMessage()
            ));
    }

    // 其他系統異常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception e, HttpServletRequest request) {
        log.error("系統異常：path={}", request.getRequestURI(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(
                ErrorCode.SYSTEM_ERROR.getCode(),
                ErrorCode.SYSTEM_ERROR.getMessage()
                // ⚠️ 不要把 e.getMessage() 返回給客戶端！
                // 可能洩漏系統內部資訊
            ));
    }
}
```

---

### 🔵 API 版本管理

```java
// 版本管理三種方式：

// ① URI 版本（最常用、最清晰）
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 { ... }

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 { ... }

// ② Header 版本
@GetMapping(value = "/users", headers = "X-API-Version=1")
public ResponseEntity<?> getUsersV1() { ... }

@GetMapping(value = "/users", headers = "X-API-Version=2")
public ResponseEntity<?> getUsersV2() { ... }

// ③ Accept Header 版本
@GetMapping(value = "/users",
    produces = "application/vnd.myapp.v1+json")
public ResponseEntity<?> getUsersV1() { ... }

@GetMapping(value = "/users",
    produces = "application/vnd.myapp.v2+json")
public ResponseEntity<?> getUsersV2() { ... }

// 版本演進策略：
// ① 新增欄位：向後兼容，不需要新版本
// ② 修改欄位含義/刪除欄位：需要新版本
// ③ 舊版本維護 6~12 個月後棄用
// ④ 在響應頭加上棄用警告：
//    Deprecation: true
//    Sunset: Sat, 01 Jan 2025 00:00:00 GMT

// API 棄用攔截器
@Component
public class DeprecationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/")) {
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", "Sat, 01 Jul 2025 00:00:00 GMT");
            response.setHeader("Link",
                "</api/v2/users>; rel=\"successor-version\"");
        }
        return true;
    }
}
```

---

### 🟡 API 設計最佳實踐清單

```
請求設計：
✅ 使用 HTTPS，禁止 HTTP
✅ 請求體用 JSON，Content-Type: application/json
✅ 使用 @Valid 驗證請求參數
✅ 敏感資訊不放 URL（如密碼、Token）
✅ 分頁參數統一：page（從0開始）, size, sort, order
✅ 時間統一用 ISO 8601（2024-01-15T10:30:00Z）
✅ 金額用字串或 Long（避免浮點數精度問題）

響應設計：
✅ 統一響應格式 ApiResponse<T>
✅ 正確使用 HTTP Status Code
✅ 錯誤響應帶業務錯誤碼和描述
✅ 響應帶 traceId（方便客戶端提供排查信息）
✅ 敏感欄位不返回（如密碼 hash、內部 ID）

安全設計：
✅ 所有接口需要認證（除了登入、公開接口）
✅ 使用 JWT 或 OAuth2
✅ 限流保護（防暴力破解、防刷接口）
✅ 冪等性設計（防重複提交）
✅ 接口日誌審計（誰、什麼時間、做了什麼）
```

---

## 2️⃣ gRPC（高效能 RPC 框架）

---

### 🔵 核心原理

```
gRPC 是 Google 開源的高效能 RPC 框架

為什麼用 gRPC？

REST（HTTP/1.1 + JSON）：
  ❌ 文本協議（JSON），序列化/反序列化慢
  ❌ HTTP/1.1 每次請求都要建立連接（或複用，但有限制）
  ❌ 只支援請求-響應模式

gRPC（HTTP/2 + Protobuf）：
  ✅ 二進制協議（Protobuf），序列化比 JSON 快 5~10 倍，體積小 3~5 倍
  ✅ HTTP/2 多路複用（一個連接處理多個請求）
  ✅ 支援四種通訊模式（單向 / 雙向流）
  ✅ 強類型（Protobuf 定義 schema）
  ✅ 跨語言（Java / Go / Python / C++ / Node.js ...）
  ✅ 自動生成客戶端代碼

適用場景：
  微服務間通訊（內部 API）
  需要高效能、低延遲的場景
  需要雙向流的場景（如：即時監控）
  多語言混合的微服務環境

不適合：
  瀏覽器客戶端（需要 gRPC-Web 轉換層）
  公開 API（可讀性差）
```

---

### 🟡 Protobuf 定義

```protobuf
// src/main/proto/order.proto

syntax = "proto3";

package com.example.order;

option java_package         = "com.example.grpc.order";
option java_outer_classname = "OrderProto";
option java_multiple_files  = true;

// 導入通用類型
import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

// ──── 消息定義 ────

message Order {
    int64  id         = 1;
    int64  user_id    = 2;
    string status     = 3;
    double amount     = 4;
    string address    = 5;
    repeated OrderItem items = 6;
    google.protobuf.Timestamp created_at = 7;
}

message OrderItem {
    int64  product_id = 1;
    string name       = 2;
    int32  quantity   = 3;
    double price      = 4;
}

message CreateOrderRequest {
    int64  user_id = 1;
    string address = 2;
    repeated OrderItem items = 3;
}

message GetOrderRequest {
    int64 id = 1;
}

message ListOrdersRequest {
    int64  user_id = 1;
    int32  page    = 2;
    int32  size    = 3;
    string status  = 4;
}

message ListOrdersResponse {
    repeated Order orders = 1;
    int64          total  = 2;
}

message OrderStatusUpdate {
    int64  order_id  = 1;
    string status    = 2;
    string message   = 3;
    google.protobuf.Timestamp updated_at = 4;
}

// ──── 服務定義（四種模式）────

service OrderService {

    // ① Unary RPC（單請求/單響應，最常用）
    rpc CreateOrder (CreateOrderRequest) returns (Order);
    rpc GetOrder    (GetOrderRequest)    returns (Order);

    // ② Server Streaming RPC（單請求/多響應）
    // 服務器返回訂單狀態更新的流
    rpc WatchOrderStatus (GetOrderRequest)
        returns (stream OrderStatusUpdate);

    // ③ Client Streaming RPC（多請求/單響應）
    // 客戶端批量發送訂單，服務器返回匯總結果
    rpc BatchCreateOrders (stream CreateOrderRequest)
        returns (ListOrdersResponse);

    // ④ Bidirectional Streaming RPC（雙向流）
    // 即時訂單追蹤
    rpc TrackOrders (stream GetOrderRequest)
        returns (stream OrderStatusUpdate);
}
```

---

### 🟠 Spring Boot 整合 gRPC

```xml
<!-- pom.xml -->
<dependencies>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-spring-boot-starter</artifactId>
        <version>2.15.0.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>net.devh</groupId>
        <artifactId>grpc-client-spring-boot-starter</artifactId>
        <version>2.15.0.RELEASE</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- 自動從 .proto 生成 Java 代碼 -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <version>0.6.1</version>
            <configuration>
                <protocArtifact>
                    com.google.protobuf:protoc:3.25.0:exe:${os.detected.classifier}
                </protocArtifact>
                <pluginId>grpc-java</pluginId>
                <pluginArtifact>
                    io.grpc:protoc-gen-grpc-java:1.59.0:exe:${os.detected.classifier}
                </pluginArtifact>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

```yaml
# application.yml（Server 端）
grpc:
  server:
    port: 9090
    enable-reflection: true   # 開啟反射（方便 grpcurl 測試）
    security:
      enabled: false          # 生產環境要開啟 TLS

# application.yml（Client 端）
grpc:
  client:
    order-service:
      address:      "static://localhost:9090"
      negotiation-type: plaintext   # 生產用 TLS
```

```java
// ═══════════════ Server 端實作 ═══════════════

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private final OrderService orderService;

    // ① Unary RPC
    @Override
    public void createOrder(CreateOrderRequest request,
                            StreamObserver<Order> responseObserver) {
        try {
            log.info("gRPC createOrder, userId={}", request.getUserId());

            // 業務邏輯
            com.example.model.Order order =
                orderService.create(fromProto(request));

            // 返回響應
            responseObserver.onNext(toProto(order));
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("gRPC createOrder 失敗", e);
            responseObserver.onError(
                Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException()
            );
        }
    }

    // ② Server Streaming RPC
    @Override
    public void watchOrderStatus(GetOrderRequest request,
                                  StreamObserver<OrderStatusUpdate> responseObserver) {
        Long orderId = request.getId();
        log.info("開始監聽訂單狀態，orderId={}", orderId);

        try {
            // 模擬訂單狀態流
            List<String> statuses = List.of("PAID", "PREPARING", "SHIPPED", "DELIVERED");
            for (String status : statuses) {
                Thread.sleep(2000); // 模擬狀態變化

                OrderStatusUpdate update = OrderStatusUpdate.newBuilder()
                    .setOrderId(orderId)
                    .setStatus(status)
                    .setMessage("訂單狀態更新為：" + status)
                    .setUpdatedAt(Timestamps.fromMillis(System.currentTimeMillis()))
                    .build();

                responseObserver.onNext(update); // 推送狀態更新
            }
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    // ③ Client Streaming RPC
    @Override
    public StreamObserver<CreateOrderRequest> batchCreateOrders(
            StreamObserver<ListOrdersResponse> responseObserver) {

        List<com.example.model.Order> createdOrders = new ArrayList<>();

        return new StreamObserver<CreateOrderRequest>() {
            @Override
            public void onNext(CreateOrderRequest request) {
                // 接收客戶端發送的每個請求
                com.example.model.Order order =
                    orderService.create(fromProto(request));
                createdOrders.add(order);
                log.info("批量創建訂單：{}", order.getId());
            }

            @Override
            public void onError(Throwable t) {
                log.error("批量創建訂單出錯", t);
            }

            @Override
            public void onCompleted() {
                // 客戶端發送完畢，返回匯總結果
                ListOrdersResponse response = ListOrdersResponse.newBuilder()
                    .addAllOrders(createdOrders.stream()
                        .map(OrderGrpcService.this::toProto)
                        .collect(Collectors.toList()))
                    .setTotal(createdOrders.size())
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    // Proto 轉換工具方法
    private Order toProto(com.example.model.Order order) {
        return Order.newBuilder()
            .setId(order.getId())
            .setUserId(order.getUserId())
            .setStatus(order.getStatus().name())
            .setAmount(order.getAmount().doubleValue())
            .build();
    }

    private com.example.model.OrderRequest fromProto(CreateOrderRequest proto) {
        return com.example.model.OrderRequest.builder()
            .userId(proto.getUserId())
            .address(proto.getAddress())
            .build();
    }
}

// gRPC 攔截器（實作認證、日誌等）
@Component
public class AuthGrpcInterceptor implements ServerInterceptor {

    private static final Metadata.Key<String> AUTH_KEY =
        Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <Q, R> ServerCall.Listener<Q> interceptCall(
            ServerCall<Q, R> call, Metadata headers,
            ServerCallHandler<Q, R> next) {

        String token = headers.get(AUTH_KEY);
        if (token == null || !token.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED
                .withDescription("缺少認證 Token"), new Metadata());
            return new ServerCall.Listener<Q>() {};
        }

        // 驗證 JWT Token
        // ...

        return next.startCall(call, headers);
    }
}
```

```java
// ═══════════════ Client 端使用 ═══════════════

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderGrpcClient {

    // 自動注入 gRPC Channel 並創建 Stub
    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceBlockingStub blockingStub;

    @GrpcClient("order-service")
    private OrderServiceGrpc.OrderServiceStub asyncStub;

    // ① Unary RPC - 同步調用
    public Order createOrder(CreateOrderRequest request) {
        try {
            return blockingStub
                .withDeadlineAfter(5, TimeUnit.SECONDS)  // 超時設定
                .createOrder(request);
        } catch (StatusRuntimeException e) {
            log.error("gRPC createOrder 失敗: {}", e.getStatus());
            throw new ServiceException("訂單服務不可用", e);
        }
    }

    // ② Server Streaming - 異步監聽
    public void watchOrderStatus(Long orderId,
                                  Consumer<OrderStatusUpdate> onUpdate,
                                  Runnable onComplete) {
        GetOrderRequest request = GetOrderRequest.newBuilder()
            .setId(orderId)
            .build();

        asyncStub.watchOrderStatus(request, new StreamObserver<OrderStatusUpdate>() {
            @Override
            public void onNext(OrderStatusUpdate update) {
                log.info("收到訂單狀態更新：{}", update.getStatus());
                onUpdate.accept(update);
            }

            @Override
            public void onError(Throwable t) {
                log.error("監聽訂單狀態出錯", t);
            }

            @Override
            public void onCompleted() {
                log.info("訂單狀態監聽完成");
                onComplete.run();
            }
        });
    }

    // ③ Client Streaming - 批量發送
    public ListOrdersResponse batchCreateOrders(
            List<CreateOrderRequest> requests) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<ListOrdersResponse> result = new AtomicReference<>();

        StreamObserver<CreateOrderRequest> requestObserver =
            asyncStub.batchCreateOrders(new StreamObserver<ListOrdersResponse>() {
                @Override
                public void onNext(ListOrdersResponse response) {
                    result.set(response);
                }
                @Override public void onError(Throwable t) { latch.countDown(); }
                @Override public void onCompleted()        { latch.countDown(); }
            });

        // 發送多個請求
        for (CreateOrderRequest request : requests) {
            requestObserver.onNext(request);
        }
        requestObserver.onCompleted();

        latch.await(30, TimeUnit.SECONDS);
        return result.get();
    }
}
```

---

### 🔴 gRPC vs REST 對比

```
┌──────────────────┬─────────────────────┬───────────────────────┐
│                  │        REST         │        gRPC           │
├──────────────────┼─────────────────────┼───────────────────────┤
│ 協議             │  HTTP/1.1           │  HTTP/2               │
│ 序列化           │  JSON（文本）        │  Protobuf（二進制）    │
│ 效能             │  較慢               │  快 5~10 倍           │
│ 可讀性           │  ✅ 高（人可以讀）   │  ❌ 低（二進制）        │
│ 通訊模式         │  請求-響應           │  單向/雙向流            │
│ 強類型           │  ❌（JSON 弱類型）   │  ✅（Protobuf Schema）  │
│ 瀏覽器支援       │  ✅ 原生支援         │  ❌（需要 gRPC-Web）    │
│ 跨語言           │  ✅                 │  ✅                   │
│ 代碼生成         │  需要 OpenAPI/Swagger│  自動生成              │
│ 適用場景         │  對外 API、公開接口   │  微服務內部通訊         │
└──────────────────┴─────────────────────┴───────────────────────┘
```

---

## 3️⃣ WebSocket（即時通訊）

---

### 🔵 WebSocket 原理

```
HTTP 請求/響應模式的問題：
  客戶端必須主動發請求才能獲得數據
  服務器無法主動推送！

解決方案的演進：
  輪詢（Polling）：
    客戶端每秒發一次請求：「有新消息嗎？」
    ❌ 浪費頻寬，延遲高

  長輪詢（Long Polling）：
    客戶端發請求，服務器掛起直到有數據才返回
    ❌ 連接數多，資源浪費

  WebSocket：
    ✅ 一次握手，建立持久雙向連接
    ✅ 服務器可以主動推送
    ✅ 低延遲，低開銷

WebSocket 握手過程：
  ① 客戶端發 HTTP 請求（帶特殊頭）
     GET /ws HTTP/1.1
     Upgrade: websocket
     Connection: Upgrade
     Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==

  ② 服務器返回 101 Switching Protocols
     HTTP/1.1 101 Switching Protocols
     Upgrade: websocket
     Connection: Upgrade
     Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=

  ③ 連接建立，雙方可以互相發送數據幀
     ws://  → 非加密
     wss:// → 加密（生產環境必須用！）
```

---

### 🟡 Spring Boot 整合（STOMP over WebSocket）

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

```java
// WebSocket 配置
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 啟用記憶體內消息代理，處理帶有 /topic 和 /queue 前綴的消息
        registry.enableSimpleBroker("/topic", "/queue");

        // 應用程序處理帶有 /app 前綴的消息
        registry.setApplicationDestinationPrefixes("/app");

        // 點對點消息前綴（用戶私信）
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")           // WebSocket 連接端點
            .setAllowedOriginPatterns("*")    // 允許跨域
            .withSockJS();                    // 支援 SockJS 降級（不支援 WS 時用輪詢）
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 加入認證攔截器
        registration.interceptors(new AuthChannelInterceptor());
    }
}

// WebSocket 認證攔截器
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
            MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (jwtTokenProvider.validateToken(token)) {
                    // 設定用戶身份（用於點對點消息）
                    String username = jwtTokenProvider.getUsername(token);
                    accessor.setUser(() -> username);
                }
            }
        }
        return message;
    }
}
```

```java
// ════════ 聊天室實戰 ════════

// 消息 DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    public enum MessageType { CHAT, JOIN, LEAVE, SYSTEM }

    private MessageType   type;
    private String        content;
    private String        sender;
    private String        roomId;
    private LocalDateTime timestamp;
}

// 聊天室 Controller
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomService       chatRoomService;

    // ① 群組消息（廣播到聊天室所有成員）
    // 客戶端：stompClient.send("/app/chat/{roomId}", {}, message)
    @MessageMapping("/chat/{roomId}")
    public void sendGroupMessage(@DestinationVariable String roomId,
                                  ChatMessage message,
                                  Principal principal) {
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());
        message.setType(ChatMessage.MessageType.CHAT);

        log.info("[{}] {} 發送消息：{}", roomId, message.getSender(), message.getContent());

        // 廣播到聊天室訂閱者
        // 訂閱：stompClient.subscribe('/topic/chat/{roomId}', callback)
        messagingTemplate.convertAndSend(
            "/topic/chat/" + roomId,
            message
        );

        // 保存消息記錄
        chatRoomService.saveMessage(roomId, message);
    }

    // ② 點對點私信
    // 客戶端：stompClient.send("/app/private/{recipientId}", {}, message)
    @MessageMapping("/private/{recipientId}")
    public void sendPrivateMessage(@DestinationVariable String recipientId,
                                    ChatMessage message,
                                    Principal principal) {
        message.setSender(principal.getName());
        message.setTimestamp(LocalDateTime.now());
        message.setType(ChatMessage.MessageType.CHAT);

        // 發送給特定用戶
        // 訂閱：stompClient.subscribe('/user/queue/private', callback)
        messagingTemplate.convertAndSendToUser(
            recipientId,
            "/queue/private",
            message
        );

        log.info("{} → {}: {}", message.getSender(), recipientId, message.getContent());
    }

    // ③ 用戶加入聊天室
    @MessageMapping("/chat/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId,
                          Principal principal) {
        String username = principal.getName();
        chatRoomService.addUser(roomId, username);

        ChatMessage notification = new ChatMessage(
            ChatMessage.MessageType.JOIN,
            username + " 加入了聊天室",
            "系統",
            roomId,
            LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/chat/" + roomId, notification);
    }

    // ④ 用戶離開（WebSocket 斷開時觸發）
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
            StompHeaderAccessor.wrap(event.getMessage());
        String username = Optional.ofNullable(accessor.getUser())
            .map(Principal::getName)
            .orElse("Unknown");

        String roomId = chatRoomService.getUserRoom(username);
        if (roomId != null) {
            chatRoomService.removeUser(roomId, username);

            ChatMessage notification = new ChatMessage(
                ChatMessage.MessageType.LEAVE,
                username + " 離開了聊天室",
                "系統",
                roomId,
                LocalDateTime.now()
            );
            messagingTemplate.convertAndSend("/topic/chat/" + roomId, notification);
        }
    }
}

// 服務器主動推送（如：訂單狀態更新、系統通知）
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // 推送給特定用戶
    public void pushToUser(String userId, Object notification) {
        messagingTemplate.convertAndSendToUser(
            userId,
            "/queue/notifications",
            notification
        );
    }

    // 廣播給所有連線用戶
    public void broadcast(Object notification) {
        messagingTemplate.convertAndSend(
            "/topic/system",
            notification
        );
    }
}
```

```javascript
// 前端 JavaScript（使用 SockJS + STOMP.js）

const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// 連接（帶 JWT Token）
stompClient.connect(
    { 'Authorization': 'Bearer ' + jwtToken },
    function(frame) {
        console.log('WebSocket 連接成功：' + frame);

        // 訂閱聊天室消息
        stompClient.subscribe('/topic/chat/' + roomId, function(message) {
            const chatMsg = JSON.parse(message.body);
            displayMessage(chatMsg);
        });

        // 訂閱私信
        stompClient.subscribe('/user/queue/private', function(message) {
            const privateMsg = JSON.parse(message.body);
            displayPrivateMessage(privateMsg);
        });

        // 訂閱系統通知
        stompClient.subscribe('/topic/system', function(message) {
            const notification = JSON.parse(message.body);
            showNotification(notification);
        });

        // 加入聊天室
        stompClient.send('/app/chat/' + roomId + '/join', {}, '');
    },
    function(error) {
        console.log('連接失敗：' + error);
        setTimeout(() => reconnect(), 5000);  // 自動重連
    }
);

// 發送消息
function sendMessage(content) {
    stompClient.send('/app/chat/' + roomId, {}, JSON.stringify({
        content: content,
        roomId:  roomId
    }));
}

// 斷開連接
function disconnect() {
    if (stompClient) {
        stompClient.disconnect(() => console.log('已斷開連接'));
    }
}
```

---

## 4️⃣ GraphQL（靈活查詢語言）

---

### 🔵 核心概念

```
GraphQL 解決的問題：

REST 的問題：
  ❌ Over-fetching：返回太多不需要的欄位
     GET /users/123 → 返回 50 個欄位，但我只需要 name 和 email

  ❌ Under-fetching：一個頁面需要多次請求
     GET /users/123       → 取用戶資料
     GET /orders?userId=123 → 取訂單列表
     GET /products/xxx    → 取商品詳情
     需要 3 次請求！

GraphQL 的解決方式：
  客戶端精確描述「我需要什麼」
  服務器只返回需要的欄位
  一次請求可以取多個資源

三個核心操作：
  Query（查詢）：讀取數據，類似 GET
  Mutation（變更）：寫入/修改/刪除，類似 POST/PUT/DELETE
  Subscription（訂閱）：實時數據推送，類似 WebSocket
```

---

### 🟡 Spring Boot 整合 GraphQL

```xml
<!-- pom.xml（Spring Boot 3.x 內建 GraphQL 支援）-->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

```graphql
# src/main/resources/graphql/schema.graphqls

# ──── 類型定義 ────

type User {
    id:        ID!
    username:  String!
    email:     String!
    orders(page: Int = 0, size: Int = 10): [Order!]!
    createdAt: String!
}

type Order {
    id:        ID!
    status:    OrderStatus!
    amount:    Float!
    items:     [OrderItem!]!
    user:      User!
    createdAt: String!
}

type OrderItem {
    product:   Product!
    quantity:  Int!
    price:     Float!
}

type Product {
    id:          ID!
    name:        String!
    price:       Float!
    description: String
    category:    String!
    stock:       Int!
}

enum OrderStatus {
    PENDING
    PAID
    SHIPPED
    DELIVERED
    CANCELLED
}

# ──── 分頁類型 ────
type PageInfo {
    total:      Int!
    page:       Int!
    size:       Int!
    totalPages: Int!
}

type ProductConnection {
    items:    [Product!]!
    pageInfo: PageInfo!
}

# ──── 輸入類型 ────
input CreateOrderInput {
    userId:  ID!
    address: String!
    items:   [OrderItemInput!]!
}

input OrderItemInput {
    productId: ID!
    quantity:  Int!
}

input ProductFilterInput {
    keyword:  String
    category: String
    minPrice: Float
    maxPrice: Float
}

# ──── 根類型 ────
type Query {
    # 用戶查詢
    user(id: ID!):     User
    me:                User

    # 商品查詢
    product(id: ID!):  Product
    products(
        filter: ProductFilterInput,
        page:   Int = 0,
        size:   Int = 20,
        sort:   String = "id"
    ): ProductConnection!

    # 訂單查詢
    order(id: ID!):    Order
    myOrders(
        status: OrderStatus,
        page:   Int = 0,
        size:   Int = 10
    ): [Order!]!
}

type Mutation {
    createOrder(input: CreateOrderInput!): Order!
    cancelOrder(id: ID!, reason: String): Order!
    updateProductStock(id: ID!, quantity: Int!): Product!
}

type Subscription {
    orderStatusChanged(orderId: ID!): Order!
}
```

```java
// GraphQL Controller（@Controller，不是 @RestController）

// ──── Query 解析器 ────
@Controller
@RequiredArgsConstructor
public class UserGraphQLController {

    private final UserService    userService;
    private final OrderService   orderService;

    @QueryMapping  // 對應 schema 中的 query.user
    public User user(@Argument Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("用戶不存在：" + id));
    }

    @QueryMapping
    public User me(@AuthenticationPrincipal UserPrincipal currentUser) {
        return userService.findById(currentUser.getId()).orElseThrow();
    }

    // 解析 User.orders 欄位（N+1 問題：用 DataLoader 解決）
    @SchemaMapping(typeName = "User", field = "orders")
    public CompletableFuture<List<Order>> orders(
            User user,
            @Argument int page,
            @Argument int size,
            DataLoader<Long, List<Order>> orderLoader) {
        // DataLoader 自動批量合併 N 個用戶的訂單查詢為 1 個查詢！
        return orderLoader.load(user.getId());
    }
}

@Controller
@RequiredArgsConstructor
public class ProductGraphQLController {

    private final ProductService productService;

    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.findById(id).orElseThrow();
    }

    @QueryMapping
    public ProductConnection products(
            @Argument ProductFilterInput filter,
            @Argument int page,
            @Argument int size,
            @Argument String sort) {

        Page<Product> productPage = productService.search(filter, page, size, sort);

        return ProductConnection.builder()
            .items(productPage.getContent())
            .pageInfo(PageInfo.builder()
                .total((int) productPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(productPage.getTotalPages())
                .build())
            .build();
    }
}

// ──── Mutation 解析器 ────
@Controller
@RequiredArgsConstructor
public class OrderGraphQLController {

    private final OrderService              orderService;
    private final SimpMessagingTemplate     messagingTemplate;

    @MutationMapping
    public Order createOrder(@Argument CreateOrderInput input,
                              @AuthenticationPrincipal UserPrincipal user) {
        return orderService.create(input, user.getId());
    }

    @MutationMapping
    public Order cancelOrder(@Argument Long id,
                              @Argument String reason) {
        return orderService.cancel(id, reason);
    }

    // ──── Subscription 解析器 ────
    @SubscriptionMapping
    public Flux<Order> orderStatusChanged(@Argument Long orderId) {
        // 返回 Flux（響應式流），每次訂單狀態變化都推送
        return orderService.watchOrderStatus(orderId)
            .map(status -> orderService.findById(orderId).orElseThrow());
    }
}

// ──── DataLoader（解決 N+1 問題）────
@Configuration
public class DataLoaderConfig {

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry(OrderService orderService) {
        BatchLoaderRegistry registry = new DefaultBatchLoaderRegistry();

        // 批量加載用戶的訂單（多個用戶的訂單查詢合併為一次）
        registry.forTypePair(Long.class, List.class)
            .withName("orderLoader")
            .registerBatchLoader((userIds, env) -> {
                // userIds = [1, 2, 3, 4, 5]（所有需要查訂單的用戶 ID）
                Map<Long, List<Order>> orderMap =
                    orderService.findByUserIds(new ArrayList<>(userIds));
                // 返回與 userIds 順序對應的結果
                return Flux.fromIterable(userIds)
                    .map(id -> orderMap.getOrDefault(id, Collections.emptyList()));
            });

        return registry;
    }
}
```

```yaml
# application.yml
spring:
  graphql:
    graphiql:
      enabled: true    # 開啟 GraphiQL 瀏覽器（開發環境）
      path: /graphiql
    path: /graphql     # GraphQL 端點路徑
    websocket:
      path: /graphql-ws  # Subscription 用 WebSocket
    schema:
      printer:
        enabled: true  # 打印 Schema（開發用）
```

```graphql
# 客戶端查詢範例

# ① 精確取需要的欄位
query GetUserProfile {
    me {
        id
        username
        email
        # 只取最近 5 個訂單，只要 id 和 status
        orders(page: 0, size: 5) {
            id
            status
            amount
        }
    }
}

# ② 一次請求取多個資源
query GetOrderDetail($orderId: ID!) {
    order(id: $orderId) {
        id
        status
        amount
        user {
            username
            email
        }
        items {
            quantity
            price
            product {
                name
                imageUrl
            }
        }
    }
}

# ③ 商品搜索
query SearchProducts($keyword: String!, $page: Int) {
    products(
        filter: { keyword: $keyword, minPrice: 100, maxPrice: 5000 }
        page: $page
        size: 20
        sort: "rating"
    ) {
        items {
            id
            name
            price
            rating
        }
        pageInfo {
            total
            totalPages
        }
    }
}

# ④ Mutation
mutation CreateNewOrder($input: CreateOrderInput!) {
    createOrder(input: $input) {
        id
        status
        amount
    }
}

# ⑤ Subscription（即時訂閱）
subscription WatchOrder($orderId: ID!) {
    orderStatusChanged(orderId: $orderId) {
        id
        status
        updatedAt
    }
}
```

---

### 🔴 GraphQL vs REST 比較

```
┌──────────────────┬─────────────────────┬───────────────────────┐
│                  │        REST         │       GraphQL         │
├──────────────────┼─────────────────────┼───────────────────────┤
│ 數據獲取          │ 固定結構             │ 客戶端自定義           │
│ Over-fetching    │ ❌ 常見              │ ✅ 完全避免            │
│ Under-fetching   │ ❌ 常見              │ ✅ 一次請求解決        │
│ API 端點         │ 多個（每個資源一個）   │ 單一（/graphql）       │
│ 版本管理          │ 需要（v1, v2）       │ 不需要（欄位演化）      │
│ 學習曲線          │ ✅ 低               │ ❌ 較高               │
│ 快取             │ ✅ HTTP 快取         │ ❌ 較複雜（需 Apollo）  │
│ N+1 問題         │ 需手動處理           │ DataLoader 解決        │
│ 文件             │ Swagger/OpenAPI     │ GraphiQL（自文件化）    │
│ 適用場景          │ 公開 API、簡單場景   │ 複雜前端需求、BFF 層    │
└──────────────────┴─────────────────────┴───────────────────────┘

BFF（Backend For Frontend）模式：
  不同客戶端（Web / iOS / Android）有不同的數據需求
  GraphQL 作為 BFF 層，各客戶端按需查詢
  避免為每個客戶端維護不同的 REST API
```

---

## 📝 API 設計與通訊協議 總結

| 主題 | 核心要點 |
|------|---------|
| **REST 設計** | 名詞 URI、HTTP 方法語義、正確 Status Code |
| **冪等性** | GET/PUT/DELETE 冪等，POST 不冪等，用 Idempotency-Key |
| **統一響應** | ApiResponse<T> 包含 success/code/message/data/traceId |
| **異常處理** | @RestControllerAdvice 全局處理，不暴露系統內部資訊 |
| **API 版本** | URI 版本（/v1/）最清晰，舊版本維護後棄用 |
| **gRPC** | Protobuf 二進制協議，HTTP/2，比 REST 快 5\~10 倍 |
| **gRPC 模式** | Unary / Server Streaming / Client Streaming / 雙向流 |
| **WebSocket** | 持久雙向連接，一次握手，服務器主動推送 |
| **STOMP** | WebSocket 上層協議，支援 topic 廣播和 queue 點對點 |
| **GraphQL** | 客戶端精確取需要的欄位，解決 Over/Under-fetching |
| **DataLoader** | 批量加載解決 GraphQL N+1 問題 |
| **選擇建議** | 對外公開 API → REST；微服務內部 → gRPC；即時推送 → WebSocket；複雜前端 → GraphQL |
