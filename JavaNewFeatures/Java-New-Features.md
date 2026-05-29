# ☕ Java 新特性 完整深入教學（Java 8 → 17 → 21）

---

## 🗺️ 學習路線圖

```
Java 新特性
├── 1️⃣ Java 8 核心回顧（基礎，面試常考）
│   ├── Lambda & 函數式接口
│   ├── Stream API 進階
│   └── Optional
├── 2️⃣ Java 9 ~ 16 重要特性
│   ├── var 局部類型推斷
│   ├── Text Block（文字塊）
│   └── Switch 表達式
├── 3️⃣ Java 17 LTS 重點
│   ├── Sealed Classes（封閉類）
│   ├── Pattern Matching for instanceof
│   └── Record Classes（記錄類）
└── 4️⃣ Java 21 LTS 重點 ⭐ 最重要
    ├── Virtual Threads（虛擬線程）← 革命性
    ├── Pattern Matching for Switch
    ├── Sequenced Collections
    └── Structured Concurrency
```

---

## 1️⃣ Java 8 核心回顧

---

### 🔵 函數式接口（Functional Interface）

Java 8 內建四大函數式接口：

```java
// 1. Supplier<T> - 無參數，有返回值（生產者）
Supplier<String> supplier = () -> "Hello World";
String result = supplier.get(); // "Hello World"

// 2. Consumer<T> - 有參數，無返回值（消費者）
Consumer<String> consumer = s -> System.out.println(s);
consumer.accept("Hello"); // 印出 Hello

// 3. Function<T, R> - 有參數，有返回值（轉換）
Function<String, Integer> function = s -> s.length();
Integer length = function.apply("Hello"); // 5

// 4. Predicate<T> - 有參數，返回 boolean（判斷）
Predicate<String> predicate = s -> s.length() > 3;
boolean result = predicate.test("Hello"); // true
```

---

### 🟡 Stream API 進階用法

```java
List<Order> orders = getOrders();

// 範例：統計每個用戶的訂單總金額，取前 3 名
Map<String, Double> top3UserAmount = orders.stream()
    .filter(o -> o.getStatus() == OrderStatus.PAID)
    .collect(Collectors.groupingBy(
        Order::getUserId,
        Collectors.summingDouble(Order::getAmount)
    ))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
    .limit(3)
    .collect(Collectors.toLinkedHashMap(
        Map.Entry::getKey,
        Map.Entry::getValue
    ));

// ⭐ flatMap - 扁平化（常考！）
List<List<Integer>> nested = List.of(
    List.of(1, 2, 3),
    List.of(4, 5, 6)
);
List<Integer> flat = nested.stream()
    .flatMap(Collection::stream)  // [[1,2,3],[4,5,6]] → [1,2,3,4,5,6]
    .collect(Collectors.toList());

// ⭐ reduce - 聚合
int sum = IntStream.rangeClosed(1, 100)
    .reduce(0, Integer::sum); // 5050

// ⭐ 並行流（注意線程安全！）
long count = orders.parallelStream()
    .filter(o -> o.getAmount() > 1000)
    .count();
```

---

### 🟠 Optional（正確使用方式）

```java
// ❌ 錯誤用法（跟 null 判斷一樣沒意義）
Optional<User> optional = userRepository.findById(id);
if (optional.isPresent()) {
    return optional.get();
}

// ✅ 正確用法

// 1. orElse - 提供預設值
User user = userRepository.findById(id)
    .orElse(new User("default"));

// 2. orElseThrow - 拋出例外
User user = userRepository.findById(id)
    .orElseThrow(() -> new UserNotFoundException("User not found: " + id));

// 3. map + filter 鏈式操作
String city = userRepository.findById(id)
    .map(User::getAddress)
    .map(Address::getCity)
    .filter(c -> !c.isEmpty())
    .orElse("Unknown");

// 4. ifPresent - 有值才執行
userRepository.findById(id)
    .ifPresent(user -> sendWelcomeEmail(user));

// 5. ifPresentOrElse (Java 9+)
userRepository.findById(id)
    .ifPresentOrElse(
        user -> log.info("找到用戶: {}", user.getName()),
        () -> log.warn("用戶不存在")
    );
```

---

## 2️⃣ Java 9 \~ 16 重要特性

---

### 🔵 var - 局部類型推斷（Java 10）

```java
// ❌ 之前
HashMap<String, List<Order>> orderMap = new HashMap<String, List<Order>>();

// ✅ 使用 var（編譯期推斷，不是動態類型！）
var orderMap = new HashMap<String, List<Order>>();

// ✅ 在 for 循環中最好用
for (var entry : orderMap.entrySet()) {
    var key   = entry.getKey();     // String
    var value = entry.getValue();   // List<Order>
}

// ⚠️ 不能用的場景：
var x;                    // ❌ 無法推斷
var x = null;             // ❌ 無法推斷
var x = () -> {};         // ❌ Lambda 不能用 var
private var name = "xxx"; // ❌ 只能用於局部變數
```

---

### 🟡 Text Block - 文字塊（Java 15 正式）

```java
// ❌ 之前寫 JSON / SQL / HTML 非常痛苦
String json = "{\n" +
              "  \"name\": \"John\",\n" +
              "  \"age\": 30\n" +
              "}";

// ✅ Text Block（三個雙引號）
String json = """
        {
          "name": "John",
          "age": 30
        }
        """;

// ✅ 寫 SQL 超方便！
String sql = """
        SELECT u.id, u.name, o.amount
        FROM users u
        JOIN orders o ON u.id = o.user_id
        WHERE u.status = 'ACTIVE'
          AND o.created_at > ?
        ORDER BY o.amount DESC
        LIMIT 10
        """;

// ✅ 支援 formatted()
String html = """
        <html>
            <body>
                <h1>%s</h1>
                <p>%s</p>
            </body>
        </html>
        """.formatted("標題", "內容");

// ⚠️ 縮排規則：結尾的 """ 位置決定縮排基準
String s1 = """
        Hello
        """;    // "Hello\n"（去掉 8 格縮排）

String s2 = """
        Hello
    """;        // "    Hello\n"（保留 4 格縮排）
```

---

### 🟠 Switch 表達式（Java 14 正式）

```java
// ❌ 舊式 Switch（容易 fall-through 忘記 break）
String result;
switch (day) {
    case MONDAY:
    case TUESDAY:
        result = "工作日";
        break;
    case SATURDAY:
    case SUNDAY:
        result = "假日";
        break;
    default:
        result = "其他";
}

// ✅ 新式 Switch 表達式（Arrow Switch）
String result = switch (day) {
    case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "工作日";
    case SATURDAY, SUNDAY -> "假日";
};

// ✅ 需要多行邏輯時用 yield 返回值
int price = switch (productType) {
    case "FOOD" -> 100;
    case "ELECTRONICS" -> {
        int base = 500;
        int tax  = base / 10;
        yield base + tax;  // ← 用 yield 返回（不是 return！）
    }
    default -> throw new IllegalArgumentException("Unknown type: " + productType);
};
```

---

### 🔵 集合工廠方法（Java 9）

```java
// ✅ 快速建立不可變集合
List<String> list = List.of("A", "B", "C");
Set<String>  set  = Set.of("X", "Y", "Z");
Map<String, Integer> map = Map.of(
    "one",   1,
    "two",   2,
    "three", 3
);

// ⚠️ List.of() 是不可變的
list.add("D"); // ❌ UnsupportedOperationException

// ✅ 要可變的話
List<String> mutable = new ArrayList<>(List.of("A", "B", "C"));
```

---

## 3️⃣ Java 17 LTS 重點 ⭐

---

### 🔵 Record Classes - 記錄類

**問題**：Java 的 POJO 類太冗長了！

```java
// ❌ 傳統 DTO，需要寫大量樣板程式碼
public class UserDTO {
    private final String name;
    private final Integer age;
    private final String email;

    public UserDTO(String name, Integer age, String email) {
        this.name  = name;
        this.age   = age;
        this.email = email;
    }

    public String  getName()  { return name;  }
    public Integer getAge()   { return age;   }
    public String  getEmail() { return email; }

    @Override public boolean equals(Object o) { /* ... */ }
    @Override public int     hashCode()       { /* ... */ }
    @Override public String  toString()       { /* ... */ }
}
```

```java
// ✅ Record（一行搞定！）
public record UserDTO(String name, Integer age, String email) {}

// 自動生成：
// - 全參構造器
// - getter（直接用 name()，不是 getName()）
// - equals() / hashCode() / toString()
// - 不可變（所有欄位 final）

// 使用方式
UserDTO user = new UserDTO("John", 30, "john@example.com");
System.out.println(user.name());  // John
System.out.println(user.age());   // 30
System.out.println(user);         // UserDTO[name=John, age=30, email=john@example.com]
```

```java
// ✅ Record 可以自定義 Compact Constructor（驗證）
public record UserDTO(String name, Integer age, String email) {

    // Compact Constructor - 不用寫參數，直接驗證
    public UserDTO {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("年齡不合法: " + age);
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email 格式錯誤: " + email);
        }
        name = name.trim(); // 可以修改值
    }

    // ✅ 可以新增靜態工廠方法
    public static UserDTO of(String name, Integer age, String email) {
        return new UserDTO(name, age, email);
    }

    // ✅ 可以新增實例方法
    public String displayName() {
        return name + " (" + age + " 歲)";
    }

    // ❌ 不能新增實例欄位（只能有靜態欄位）
    // private String extra; ← 不允許！
}
```

```java
// ✅ Record 與 Spring Boot 整合
public record ApiResponse<T>(
    boolean success,
    String  message,
    T       data
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "success", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(false, message, null);
    }
}

// Controller 使用
@GetMapping("/user/{id}")
public ApiResponse<UserDTO> getUser(@PathVariable Long id) {
    UserDTO user = userService.findById(id);
    return ApiResponse.ok(user);
}
```

---

### 🟡 Sealed Classes - 封閉類

**問題**：如何限制一個類只能被特定的子類繼承？

```java
// ✅ Sealed Class - 明確聲明允許的子類
public sealed class Shape
    permits Circle, Rectangle, Triangle {}
//  ↑ 只有這三個類可以繼承 Shape

// 子類必須選擇以下其中一個修飾符：

// 1. final - 不能再被繼承
public final class Circle extends Shape {
    private final double radius;
    public Circle(double radius) { this.radius = radius; }
    public double radius() { return radius; }
}

// 2. sealed - 可以再限制繼承
public sealed class Rectangle extends Shape
    permits Square {}

public final class Square extends Rectangle {}

// 3. non-sealed - 開放繼承（任何人都可以繼承）
public non-sealed class Triangle extends Shape {}
public class IsoscelesTriangle extends Triangle {} // ✅ 可以
```

```java
// ✅ Sealed Class 最大用途：配合 Pattern Matching（不需要 default！）
public double calculateArea(Shape shape) {
    return switch (shape) {
        case Circle    c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.width() * r.height();
        case Triangle  t -> t.base() * t.height() / 2;
        // ✅ 不需要 default！編譯器知道只有這三種
    };
}
```

```java
// ✅ 實際業務場景：支付結果
public sealed interface PaymentResult
    permits PaymentSuccess, PaymentFailed, PaymentPending {}

public record PaymentSuccess(String transactionId, BigDecimal amount)
    implements PaymentResult {}

public record PaymentFailed(String errorCode, String errorMessage)
    implements PaymentResult {}

public record PaymentPending(String paymentId, String redirectUrl)
    implements PaymentResult {}

// 處理支付結果（編譯器保證窮舉，不需要 default）
public ResponseEntity<?> handlePayment(PaymentResult result) {
    return switch (result) {
        case PaymentSuccess s -> ResponseEntity.ok(
            Map.of("txId", s.transactionId(), "amount", s.amount())
        );
        case PaymentFailed f -> ResponseEntity.badRequest()
            .body(Map.of("error", f.errorCode(), "msg", f.errorMessage()));
        case PaymentPending p -> ResponseEntity.status(302)
            .header("Location", p.redirectUrl())
            .build();
    };
}
```

---

### 🟠 Pattern Matching for instanceof（Java 16 正式）

```java
// ❌ 舊式：先判斷再強轉，又臭又長
Object obj = getObject();
if (obj instanceof String) {
    String str = (String) obj;  // 多此一舉的強轉
    System.out.println(str.toUpperCase());
}

// ✅ Pattern Matching：判斷 + 轉換 + 命名 一步完成
if (obj instanceof String str) {
    System.out.println(str.toUpperCase());
}

// ✅ 可以直接在條件中使用
if (obj instanceof String str && str.length() > 5) {
    System.out.println("長字串: " + str);
}

// ✅ 複雜的類型處理
public String describe(Object obj) {
    if (obj instanceof Integer i)    return "整數：" + i;
    if (obj instanceof Long l)       return "長整數：" + l;
    if (obj instanceof String s)     return "字串，長度：" + s.length();
    if (obj instanceof List<?> list) return "列表，大小：" + list.size();
    return "未知類型：" + obj.getClass().getSimpleName();
}

// ✅ 實際業務：處理不同事件類型
public void handleEvent(Event event) {
    if (event instanceof OrderCreatedEvent e) {
        inventoryService.reserve(e.productId(), e.quantity());
        notificationService.sendOrderConfirmation(e.userId());
    } else if (event instanceof OrderCancelledEvent e) {
        inventoryService.release(e.productId(), e.quantity());
        paymentService.refund(e.orderId());
    } else if (event instanceof PaymentFailedEvent e) {
        orderService.markFailed(e.orderId(), e.reason());
    }
}
```

---

## 4️⃣ Java 21 LTS 重點 ⭐⭐⭐

---

### 🔴 Virtual Threads - 虛擬線程（最革命性！）

#### 先理解問題：傳統線程的瓶頸

```
傳統平台線程（Platform Thread）：

Java Thread  ←──1:1映射──→  OS Thread
                              │
                              ↓
                          佔用大量記憶體（~1MB/線程）
                          線程切換成本高（OS 層面）

問題：
Spring Boot 預設線程池大小 200
→ 最多同時處理 200 個請求
→ 請求在等待 DB / IO 時，線程被白白佔用！
```

```
虛擬線程（Virtual Thread）：

Virtual Thread  ←──M:N映射──→  少量 Carrier Threads（OS Thread）
（可建立百萬個）                  （= CPU 核心數）

Virtual Thread 等待 IO 時，自動讓出 Carrier Thread
IO 完成時，Virtual Thread 重新掛載繼續執行
→ 10,000 個請求同時進行，佔用極少資源！
```

#### 實際使用

```java
// ✅ 方式一：直接建立虛擬線程
Thread vThread = Thread.ofVirtual()
    .name("my-virtual-thread")
    .start(() -> {
        System.out.println("虛擬線程執行：" + Thread.currentThread());
    });

// ✅ 方式二：虛擬線程 ExecutorService
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 100_000; i++) {
        executor.submit(() -> {
            Thread.sleep(Duration.ofMillis(100));
            return processRequest();
        });
    }
} // try-with-resources 自動 shutdown

// ✅ 方式三：Spring Boot 3.2+ 設定（一行開啟！）
# application.yml
spring:
  threads:
    virtual:
      enabled: true

// ✅ 方式四：自定義虛擬線程池 Bean
@Configuration
public class ThreadConfig {

    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
```

#### ⚠️ 虛擬線程注意事項

```java
// ⚠️ 1. 避免 synchronized（會 pin 住 Carrier Thread！）
// ❌ 不好
public synchronized void badMethod() {
    someBlockingIO(); // Carrier Thread 無法讓出！
}

// ✅ 改用 ReentrantLock
private final ReentrantLock lock = new ReentrantLock();
public void goodMethod() {
    lock.lock();
    try {
        someBlockingIO(); // 虛擬線程可以自由調度
    } finally {
        lock.unlock();
    }
}

// ⚠️ 2. 不要池化虛擬線程
// ❌ 錯誤做法
var pool = Executors.newFixedThreadPool(100);
// ✅ 直接 newVirtualThreadPerTaskExecutor()，每個任務一個虛擬線程

// ⚠️ 3. 虛擬線程不適合 CPU 密集型任務
// CPU 密集型：計算量大，不等待 IO → 虛擬線程無優勢
// IO  密集型：等待 DB、網路、文件 → 虛擬線程大幅提升
```

---

### 🟡 Pattern Matching for Switch（Java 21 正式）

```java
// 結合 Sealed Class + Record + Pattern Matching
sealed interface Shape permits Circle, Rectangle, Triangle {}
record Circle   (double radius)              implements Shape {}
record Rectangle(double width, double height) implements Shape {}
record Triangle (double base,  double height) implements Shape {}

// ✅ Pattern Matching for Switch
public String describeShape(Shape shape) {
    return switch (shape) {
        // 1. 類型匹配
        case Circle c -> "圓形，半徑 " + c.radius();

        // 2. 類型匹配 + 守衛條件（when）
        case Rectangle r when r.width() == r.height() ->
            "正方形，邊長 " + r.width();
        case Rectangle r ->
            "矩形，寬 " + r.width() + " 高 " + r.height();

        // 3. 類型匹配
        case Triangle t -> "三角形";

        // 因為是 sealed，不需要 default！
    };
}

// ✅ 處理 null 的新方式
public String process(Object obj) {
    return switch (obj) {
        case null           -> "null 值";   // Java 21 可以匹配 null！
        case Integer i      -> "整數: " + i;
        case String s       -> "字串: " + s;
        case int[] arr      -> "整數陣列，長度: " + arr.length;
        default             -> "其他: " + obj.getClass();
    };
}

// ✅ 實際業務場景：訂單事件處理
sealed interface OrderEvent permits
    OrderPlaced, OrderPaid, OrderShipped, OrderCancelled {}

record OrderPlaced   (Long orderId, Long userId, BigDecimal amount) implements OrderEvent {}
record OrderPaid     (Long orderId, String paymentMethod)           implements OrderEvent {}
record OrderShipped  (Long orderId, String trackingNo)              implements OrderEvent {}
record OrderCancelled(Long orderId, String reason)                  implements OrderEvent {}

@Service
public class OrderEventHandler {

    public void handle(OrderEvent event) {
        switch (event) {
            case OrderPlaced e -> {
                inventoryService.reserve(e.orderId());
                notificationService.sendConfirmation(e.userId());
                log.info("訂單建立: {}, 金額: {}", e.orderId(), e.amount());
            }
            case OrderPaid e -> {
                warehouseService.prepareShipment(e.orderId());
                log.info("訂單付款: {}, 方式: {}", e.orderId(), e.paymentMethod());
            }
            case OrderShipped e -> {
                notificationService.sendShippingNotice(e.orderId(), e.trackingNo());
                log.info("訂單出貨: {}, 追蹤號: {}", e.orderId(), e.trackingNo());
            }
            case OrderCancelled e when e.reason().contains("payment") -> {
                paymentService.refund(e.orderId());
                log.info("付款失敗取消: {}", e.orderId());
            }
            case OrderCancelled e -> {
                inventoryService.release(e.orderId());
                log.info("訂單取消: {}, 原因: {}", e.orderId(), e.reason());
            }
        }
    }
}
```

---

### 🟠 Sequenced Collections（Java 21）

**問題**：之前沒有統一的接口來表示「有序集合的首尾操作」

```java
// ❌ Java 21 之前：不同集合取首尾元素方式不統一！
List<String>      list  = new ArrayList<>();
Deque<String>     deque = new ArrayDeque<>();
SortedSet<String> set   = new TreeSet<>();

list.get(0);           // List
deque.getFirst();      // Deque
set.first();           // SortedSet

list.get(list.size() - 1); // List
deque.getLast();            // Deque
set.last();                 // SortedSet
```

```java
// ✅ Java 21：統一的首尾操作
List<String> list = new ArrayList<>(List.of("A", "B", "C"));

list.getFirst();   // "A"
list.getLast();    // "C"

list.addFirst("Z");    // ["Z", "A", "B", "C"]
list.addLast("X");     // ["Z", "A", "B", "C", "X"]
list.removeFirst();    // 移除並返回 "Z"
list.removeLast();     // 移除並返回 "X"

// ✅ 反轉視圖（不是複製，是視圖！）
List<String> reversed = list.reversed();
System.out.println(reversed); // [C, B, A]

// ✅ SequencedMap
LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

map.firstEntry();     // A=1
map.lastEntry();      // C=3
map.pollFirstEntry(); // 移除並返回 A=1
map.reversed();       // 反轉的視圖
```

---

### 🔴 Structured Concurrency - 結構化並發（Java 21 Preview）

```java
// 場景：查詢訂單詳情需要同時查 3 個服務

// ❌ 傳統方式（難以管理取消和錯誤）
ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
Future<User>      userFuture    = executor.submit(() -> userService.findById(userId));
Future<Product>   productFuture = executor.submit(() -> productService.findById(productId));
Future<Inventory> stockFuture   = executor.submit(() -> inventoryService.check(productId));

try {
    User      user    = userFuture.get();
    Product   product = productFuture.get();
    Inventory stock   = stockFuture.get();
    // 問題：如果 userFuture 失敗，其他 Future 不會自動取消！
} catch (Exception e) {
    userFuture.cancel(true);    // 需要手動取消，容易遺漏
    productFuture.cancel(true);
    stockFuture.cancel(true);
}
```

```java
// ✅ Structured Concurrency（Java 21 Preview）
public OrderDetail getOrderDetail(Long userId, Long productId)
        throws InterruptedException, ExecutionException {

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        // 1. 分叉：啟動子任務
        Subtask<User>      userTask    = scope.fork(() -> userService.findById(userId));
        Subtask<Product>   productTask = scope.fork(() -> productService.findById(productId));
        Subtask<Inventory> stockTask   = scope.fork(() -> inventoryService.check(productId));

        // 2. 等待：等所有任務完成（任一失敗則取消其他）
        scope.join()
             .throwIfFailed();

        // 3. 合併：所有任務成功才會到這裡
        return new OrderDetail(
            userTask.get(),
            productTask.get(),
            stockTask.get()
        );

    } // scope 關閉時，自動取消所有未完成的子任務！
}

// ✅ ShutdownOnSuccess - 任一成功就回傳（競速模式）
public String fetchFromFastestSource(String query)
        throws InterruptedException, ExecutionException {

    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
        scope.fork(() -> primaryDatabase.query(query));
        scope.fork(() -> replicaDatabase.query(query));
        scope.fork(() -> cacheService.get(query));

        scope.join();
        return scope.result(); // 返回最快完成的結果
    }
}
```

---

### 🔵 ScopedValue - 作用域值（Java 21 Preview）

```java
// ❌ ThreadLocal 的問題
// 1. 記憶體洩漏（不 remove 就殘留）
// 2. 虛擬線程中繼承 ThreadLocal 耗費大量記憶體
// 3. 可以在任何地方修改，難以追蹤

// ✅ ScopedValue - 更安全的替代方案
public class RequestContext {
    static final ScopedValue<User>   CURRENT_USER = ScopedValue.newInstance();
    static final ScopedValue<String> REQUEST_ID   = ScopedValue.newInstance();
}

// 設定值（只在 run 的範圍內有效）
User user = authService.authenticate(token);
String requestId = UUID.randomUUID().toString();

ScopedValue.where(RequestContext.CURRENT_USER, user)
           .where(RequestContext.REQUEST_ID, requestId)
           .run(() -> {
               orderService.createOrder(orderRequest);
           });
// scope 結束後，值自動清除！

// 在任何地方讀取（不需要傳參數）
@Service
public class OrderService {
    public void createOrder(OrderRequest req) {
        User   user  = RequestContext.CURRENT_USER.get();
        String reqId = RequestContext.REQUEST_ID.get();
        log.info("[{}] 用戶 {} 建立訂單", reqId, user.getId());
    }
}
```

---

## 🎯 版本對比速查表

| 特性 | Java 版本 | 狀態 |
|------|-----------|------|
| Lambda / Stream | **8** | ✅ 穩定 |
| var | **10** | ✅ 穩定 |
| Switch 表達式 | **14** | ✅ 穩定 |
| Text Block | **15** | ✅ 穩定 |
| Record | **16** | ✅ 穩定 |
| Pattern Matching instanceof | **16** | ✅ 穩定 |
| Sealed Classes | **17** | ✅ 穩定 |
| Virtual Threads | **21** | ✅ 穩定 |
| Pattern Matching Switch | **21** | ✅ 穩定 |
| Sequenced Collections | **21** | ✅ 穩定 |
| Structured Concurrency | **21** | 🔶 Preview |
| ScopedValue | **21** | 🔶 Preview |

---

## 📦 實戰整合範例

```java
// 把所有新特性整合到一個真實業務場景中！

// 1. Sealed Interface + Record 定義領域模型
public sealed interface PaymentCommand
    permits PaymentCommand.Pay, PaymentCommand.Refund, PaymentCommand.Query {}

public record Pay   (Long orderId, BigDecimal amount, String method) implements PaymentCommand {}
public record Refund(Long orderId, String reason)                     implements PaymentCommand {}
public record Query (Long orderId)                                    implements PaymentCommand {}

// 2. Virtual Threads 處理高並發
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/process")
    public ApiResponse<String> process(@RequestBody PaymentCommand command) {
        // Spring Boot 3.2 + virtual.enabled=true，這個請求就在虛擬線程上！

        // 3. Pattern Matching Switch 處理命令
        String result = switch (command) {
            case Pay p when p.amount().compareTo(BigDecimal.ZERO) <= 0 ->
                throw new IllegalArgumentException("金額必須大於 0");
            case Pay    p -> paymentService.pay(p.orderId(), p.amount(), p.method());
            case Refund r -> paymentService.refund(r.orderId(), r.reason());
            case Query  q -> paymentService.queryStatus(q.orderId());
        };

        return ApiResponse.ok(result);
    }

    // 4. Structured Concurrency 並行查詢
    @GetMapping("/dashboard/{userId}")
    public ApiResponse<DashboardData> getDashboard(@PathVariable Long userId)
            throws Exception {

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var totalTask   = scope.fork(() -> paymentService.getTotalAmount(userId));
            var historyTask = scope.fork(() -> paymentService.getHistory(userId));
            var pendingTask = scope.fork(() -> paymentService.getPending(userId));

            scope.join().throwIfFailed();

            // 5. Record 封裝結果
            var dashboard = new DashboardData(
                totalTask.get(),
                historyTask.get(),
                pendingTask.get()
            );

            return ApiResponse.ok(dashboard);
        }
    }
}

// 6. Record 作為 DTO
public record DashboardData(
    BigDecimal           totalAmount,
    List<PaymentHistory> history,
    List<PendingPayment> pending
) {}
```

---

## 📝 Java 新特性 總結

| 特性 | 核心價值 | 記憶口訣 |
|------|---------|---------|
| **Record** | 消滅 POJO 樣板程式碼 | 一行定義不可變 DTO |
| **Sealed Class** | 限制繼承，型別安全 | 告訴編譯器「只有這幾種」|
| **Pattern Matching** | 消滅強轉樣板 | 判斷同時完成轉換 |
| **Text Block** | 多行字串更易讀 | 三個引號包住 |
| **Switch 表達式** | 消滅 break 和 fall-through | 箭頭 → 返回值 |
| **Virtual Threads** | IO 密集型應用效能革命 | 幾KB vs 1MB，百萬並發 |
| **Sequenced Collections** | 統一首尾操作 API | getFirst/getLast 通用 |
| **Structured Concurrency** | 並發任務生命週期管理 | 子任務跟著 scope 走 |
