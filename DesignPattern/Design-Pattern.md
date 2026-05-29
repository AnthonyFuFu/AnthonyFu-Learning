# 🔶 設計模式（Design Patterns）完整深入教學

---

## 🗺️ 學習路線圖

```
設計模式（GoF 23種）
├── 1️⃣ 創建型模式（Creational）- 怎麼創建對象
│   ├── 單例模式（Singleton）
│   ├── 工廠方法模式（Factory Method）
│   ├── 抽象工廠模式（Abstract Factory）
│   └── 建造者模式（Builder）
├── 2️⃣ 結構型模式（Structural）- 怎麼組合對象
│   ├── 代理模式（Proxy）← AOP 底層原理
│   ├── 裝飾者模式（Decorator）
│   ├── 適配器模式（Adapter）
│   └── 外觀模式（Facade）
└── 3️⃣ 行為型模式（Behavioral）- 怎麼讓對象協作
    ├── 觀察者模式（Observer）← 事件驅動核心
    ├── 策略模式（Strategy）← 消滅 if-else
    ├── 模板方法模式（Template Method）
    ├── 責任鏈模式（Chain of Responsibility）
    └── 狀態模式（State）
```

---

## ⭐ 設計模式六大原則（SOLID）

```
S - Single Responsibility Principle（單一職責）
    一個類只做一件事
    ❌ UserService 既管用戶、又管訂單、又發郵件
    ✅ UserService / OrderService / EmailService 分開

O - Open/Closed Principle（開放封閉）
    對擴展開放，對修改關閉
    新增功能 → 新增類，不修改現有類
    ✅ 策略模式、工廠模式體現此原則

L - Liskov Substitution Principle（里氏替換）
    子類可以替換父類，且程式行為不變
    ❌ 正方形繼承矩形（setWidth 會破壞正方形的約束）

I - Interface Segregation Principle（介面隔離）
    介面要小而精，不要大而全
    ❌ 一個介面有 20 個方法（實作類被迫實作不需要的方法）
    ✅ 拆成多個小介面

D - Dependency Inversion Principle（依賴反轉）
    高層模組不依賴低層模組，都依賴抽象
    ✅ Spring 的依賴注入（DI）就是實踐此原則
    ❌ OrderService 直接 new EmailService()
    ✅ OrderService 依賴 NotificationService 介面
```

---

## 1️⃣ 創建型模式

---

### 🔵 單例模式（Singleton）

**目的**：確保一個類只有一個實例，並提供全局訪問點

```java
// ❌ 懶漢式（線程不安全）
public class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton(); // 多線程可能同時進入！
        }
        return instance;
    }
}
```

```java
// ✅ 方式一：餓漢式（線程安全，推薦）
// 類加載時就初始化，不存在並發問題
public class Singleton {
    // JVM 保證類加載是線程安全的
    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {}  // 私有構造器，防止外部 new

    public static Singleton getInstance() {
        return INSTANCE;
    }
}
```

```java
// ✅ 方式二：雙重檢查鎖定（DCL）
// 懶加載 + 線程安全
public class Singleton {
    // ⚠️ 必須加 volatile！防止指令重排序！
    private static volatile Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {               // 第一次檢查（不加鎖，性能高）
            synchronized (Singleton.class) {
                if (instance == null) {       // 第二次檢查（加鎖後再確認）
                    instance = new Singleton();
                    // new 分三步：① 分配記憶體 ② 初始化對象 ③ 指向記憶體地址
                    // volatile 防止 ①③② 的指令重排序
                }
            }
        }
        return instance;
    }
}
```

```java
// ✅ 方式三：靜態內部類（推薦！最優雅）
// 懶加載 + 線程安全 + 無鎖
public class Singleton {
    private Singleton() {}

    // 內部類：只有在第一次調用 getInstance() 時才加載
    private static class SingletonHolder {
        // JVM 保證類加載的線程安全
        private static final Singleton INSTANCE = new Singleton();
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

```java
// ✅ 方式四：枚舉（最安全）
// 防止反射攻擊、序列化問題
public enum Singleton {
    INSTANCE;

    public void doSomething() {
        System.out.println("業務方法");
    }
}

// 使用
Singleton.INSTANCE.doSomething();
```

```java
// Spring 中的單例
// Spring Bean 預設就是單例（Singleton Scope）
@Service  // = @Component，預設 Singleton
public class OrderService { ... }

// 注意：Spring 單例 ≠ JVM 單例
// Spring 單例：一個 ApplicationContext 中只有一個實例
// JVM 單例：整個 JVM 進程只有一個實例

// ⚠️ 單例中不能有可變的成員變數（線程不安全！）
@Service
public class OrderService {
    // ❌ 不安全！多個請求共用同一個 OrderService 實例
    private Order currentOrder;

    // ✅ 使用局部變數（每次調用都是新的）
    public void process() {
        Order currentOrder = new Order(); // 局部變數，線程安全
    }
}
```

---

### 🟡 工廠方法模式（Factory Method）

**目的**：定義創建對象的介面，讓子類決定實例化哪個類

```java
// 場景：不同的支付方式，有不同的支付實作

// 1. 產品介面
public interface PaymentHandler {
    void pay(BigDecimal amount);
    String getType();
}

// 2. 具體產品
@Component
public class AlipayHandler implements PaymentHandler {
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("支付寶支付：" + amount);
    }
    @Override
    public String getType() { return "ALIPAY"; }
}

@Component
public class WechatPayHandler implements PaymentHandler {
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("微信支付：" + amount);
    }
    @Override
    public String getType() { return "WECHAT"; }
}

@Component
public class CreditCardHandler implements PaymentHandler {
    @Override
    public void pay(BigDecimal amount) {
        System.out.println("信用卡支付：" + amount);
    }
    @Override
    public String getType() { return "CREDIT_CARD"; }
}

// 3. 工廠（Spring 自動注入所有實作）
@Component
public class PaymentHandlerFactory {

    private final Map<String, PaymentHandler> handlerMap;

    // Spring 自動注入所有 PaymentHandler 的實作
    public PaymentHandlerFactory(List<PaymentHandler> handlers) {
        this.handlerMap = handlers.stream()
            .collect(Collectors.toMap(
                PaymentHandler::getType,
                handler -> handler
            ));
    }

    public PaymentHandler getHandler(String paymentType) {
        PaymentHandler handler = handlerMap.get(paymentType);
        if (handler == null) {
            throw new IllegalArgumentException(
                "不支援的支付方式：" + paymentType);
        }
        return handler;
    }
}

// 4. 使用
@Service
@RequiredArgsConstructor
public class OrderService {

    private final PaymentHandlerFactory paymentHandlerFactory;

    public void pay(Long orderId, String paymentType, BigDecimal amount) {
        // ✅ 不需要 if-else！新增支付方式只需新增 Handler 類
        PaymentHandler handler = paymentHandlerFactory.getHandler(paymentType);
        handler.pay(amount);
    }
}

// ❌ 沒有工廠模式的寫法（違反開放封閉原則）
public void pay(String paymentType, BigDecimal amount) {
    if ("ALIPAY".equals(paymentType)) {
        // 支付寶邏輯
    } else if ("WECHAT".equals(paymentType)) {
        // 微信邏輯
    } else if ("CREDIT_CARD".equals(paymentType)) {
        // 信用卡邏輯
    }
    // 每次新增支付方式，都要修改這個 if-else！❌
}
```

---

### 🟠 抽象工廠模式（Abstract Factory）

**目的**：創建一系列相關的對象，而不指定具體類

```java
// 場景：不同的資料庫（MySQL / PostgreSQL），有不同的連接、查詢實作

// 1. 抽象產品
public interface Connection { void connect(); }
public interface QueryBuilder { String build(String table); }

// 2. 具體產品 - MySQL
public class MySQLConnection implements Connection {
    @Override public void connect() { System.out.println("MySQL 連接"); }
}
public class MySQLQueryBuilder implements QueryBuilder {
    @Override public String build(String table) {
        return "SELECT * FROM `" + table + "`";  // MySQL 語法
    }
}

// 3. 具體產品 - PostgreSQL
public class PostgreSQLConnection implements Connection {
    @Override public void connect() { System.out.println("PostgreSQL 連接"); }
}
public class PostgreSQLQueryBuilder implements QueryBuilder {
    @Override public String build(String table) {
        return "SELECT * FROM \"" + table + "\"";  // PostgreSQL 語法
    }
}

// 4. 抽象工廠
public interface DatabaseFactory {
    Connection createConnection();
    QueryBuilder createQueryBuilder();
}

// 5. 具體工廠
public class MySQLFactory implements DatabaseFactory {
    @Override public Connection createConnection()   { return new MySQLConnection(); }
    @Override public QueryBuilder createQueryBuilder() { return new MySQLQueryBuilder(); }
}

public class PostgreSQLFactory implements DatabaseFactory {
    @Override public Connection createConnection()   { return new PostgreSQLConnection(); }
    @Override public QueryBuilder createQueryBuilder() { return new PostgreSQLQueryBuilder(); }
}

// 6. 使用（只依賴抽象工廠，不知道具體是哪個資料庫）
public class DatabaseClient {
    private final Connection     connection;
    private final QueryBuilder   queryBuilder;

    public DatabaseClient(DatabaseFactory factory) {
        this.connection   = factory.createConnection();
        this.queryBuilder = factory.createQueryBuilder();
    }

    public void query(String table) {
        connection.connect();
        String sql = queryBuilder.build(table);
        System.out.println("執行：" + sql);
    }
}
```

---

### 🔴 建造者模式（Builder）

**目的**：分步驟創建複雜對象，避免大量構造器參數

```java
// ❌ 沒有 Builder：構造器參數爆炸（Telescoping Constructor）
public class Order {
    public Order(Long id, Long userId, String status, BigDecimal amount,
                 String address, String phone, LocalDateTime createdAt,
                 String remark, String couponCode) { ... }
}

// 使用時完全不知道每個參數是什麼意思！
new Order(1L, 2L, "PAID", new BigDecimal("100"), "台北市", "0912345678",
          LocalDateTime.now(), "無備註", "COUPON123");
```

```java
// ✅ Builder 模式（Lombok @Builder 自動生成）
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private Long          id;
    private Long          userId;
    private String        status;
    private BigDecimal    amount;
    private String        address;
    private String        phone;
    private LocalDateTime createdAt;
    private String        remark;
    private String        couponCode;
}

// 使用：清晰、可讀、可選
Order order = Order.builder()
    .userId(2L)
    .status("PAID")
    .amount(new BigDecimal("100"))
    .address("台北市信義區")
    .phone("0912345678")
    .createdAt(LocalDateTime.now())
    .build();
```

```java
// ✅ 手動實作 Builder（理解原理）
public class HttpRequest {
    private final String  url;
    private final String  method;
    private final Map<String, String> headers;
    private final String  body;
    private final int     timeout;

    private HttpRequest(Builder builder) {
        this.url     = builder.url;
        this.method  = builder.method;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.body    = builder.body;
        this.timeout = builder.timeout;
    }

    // 靜態內部 Builder 類
    public static class Builder {
        // 必填參數
        private final String url;
        // 選填參數（有預設值）
        private String              method  = "GET";
        private Map<String, String> headers = new HashMap<>();
        private String              body;
        private int                 timeout = 30;

        public Builder(String url) {  // 必填參數在構造器
            this.url = url;
        }

        public Builder method(String method) {
            this.method = method;
            return this;  // 返回 this 支援鏈式調用
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public HttpRequest build() {
            // 可以在這裡做驗證
            if (url == null || url.isEmpty()) {
                throw new IllegalStateException("URL 不能為空");
            }
            return new HttpRequest(this);
        }
    }
}

// 使用
HttpRequest request = new HttpRequest.Builder("https://api.example.com/orders")
    .method("POST")
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + token)
    .body("{\"orderId\": 123}")
    .timeout(10)
    .build();
```

---

### 🟣 原型模式（Prototype）

**目的**：通過複製（克隆）現有對象來創建新對象，避免重複的初始化開銷

```java
// 場景：每次建立報表配置，大部分設定相同，只有少數欄位不同
// 與其每次重新設定，直接複製已有的配置再微調

// 1. 實作 Cloneable 介面
@Data
public class ReportConfig implements Cloneable {
    private String       title;
    private List<String> columns;        // ⚠️ 注意：淺拷貝 vs 深拷貝
    private String       dateRange;
    private String       exportFormat;
    private Map<String, Object> filters;

    // 淺拷貝（Shallow Copy）
    // 基本型別和 String 是複製值
    // 但 List / Map 等引用型別仍指向同一個對象！
    @Override
    public ReportConfig clone() {
        try {
            return (ReportConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    // ✅ 深拷貝（Deep Copy）- 推薦
    public ReportConfig deepClone() {
        try {
            ReportConfig cloned = (ReportConfig) super.clone();
            // 手動深拷貝引用型別
            cloned.columns = new ArrayList<>(this.columns);
            cloned.filters = new HashMap<>(this.filters);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

// 2. 原型管理器（Prototype Registry）
@Component
public class ReportConfigRegistry {

    private final Map<String, ReportConfig> prototypes = new HashMap<>();

    @PostConstruct
    public void init() {
        // 預先建立幾個模板
        ReportConfig orderReport = new ReportConfig();
        orderReport.setTitle("訂單報表");
        orderReport.setColumns(List.of("訂單ID", "用戶", "金額", "狀態"));
        orderReport.setExportFormat("EXCEL");
        orderReport.setFilters(new HashMap<>());
        prototypes.put("ORDER_REPORT", orderReport);

        ReportConfig userReport = new ReportConfig();
        userReport.setTitle("用戶報表");
        userReport.setColumns(List.of("用戶ID", "姓名", "Email", "註冊時間"));
        userReport.setExportFormat("CSV");
        userReport.setFilters(new HashMap<>());
        prototypes.put("USER_REPORT", userReport);
    }

    // 取得原型的深拷貝，再自訂修改
    public ReportConfig getPrototype(String name) {
        ReportConfig prototype = prototypes.get(name);
        if (prototype == null) {
            throw new IllegalArgumentException("找不到報表模板：" + name);
        }
        return prototype.deepClone();  // 返回深拷貝，不影響原型
    }
}

// 3. 使用
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportConfigRegistry registry;

    public void generateMonthlyOrderReport(String month) {
        // 從原型複製，再微調
        ReportConfig config = registry.getPrototype("ORDER_REPORT");
        config.setTitle("訂單月報 - " + month);       // 只改標題
        config.setDateRange(month);                    // 只改日期範圍
        config.getFilters().put("status", "PAID");     // 只加一個過濾條件

        // 不需要重新設定 columns、exportFormat 等
        generateReport(config);
    }
}

// Java 中的原型模式：
// Object.clone()
// BeanUtils.copyProperties()（Spring 工具類）
// 序列化/反序列化 實現深拷貝：
public static <T> T deepCopyBySerialization(T obj) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    new ObjectOutputStream(bos).writeObject(obj);
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    return (T) new ObjectInputStream(bis).readObject();
}
```

---

## 2️⃣ 結構型模式

---

### 🔴 代理模式（Proxy）⭐ AOP 底層原理

**目的**：在不修改原始類的情況下，為其添加額外功能（日誌、事務、權限等）

```java
// 代理模式三種實作方式：
// 靜態代理、JDK 動態代理、CGLIB 動態代理

// ── 方式一：靜態代理 ──
// 目標介面
public interface OrderService {
    Order createOrder(OrderRequest request);
}

// 真實實作
public class OrderServiceImpl implements OrderService {
    @Override
    public Order createOrder(OrderRequest request) {
        // 業務邏輯
        return new Order();
    }
}

// 靜態代理（手動寫）
public class OrderServiceProxy implements OrderService {
    private final OrderService target;  // 被代理的真實對象

    public OrderServiceProxy(OrderService target) {
        this.target = target;
    }

    @Override
    public Order createOrder(OrderRequest request) {
        // 前置處理（如：記錄日誌）
        long start = System.currentTimeMillis();
        log.info("開始創建訂單，userId={}", request.getUserId());

        try {
            Order order = target.createOrder(request); // 調用真實方法

            // 後置處理（如：發送通知）
            log.info("訂單創建成功，orderId={}", order.getId());
            return order;

        } catch (Exception e) {
            log.error("訂單創建失敗", e);
            throw e;
        } finally {
            log.info("耗時：{} ms", System.currentTimeMillis() - start);
        }
    }
}

// 缺點：每個介面都要寫一個代理類，工程量大
```

```java
// ── 方式二：JDK 動態代理 ──
// 只能代理有介面的類（基於介面）

public class JdkProxyFactory {

    public static <T> T createProxy(T target) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),   // 類加載器
            target.getClass().getInterfaces(),    // 代理哪些介面
            new LoggingInvocationHandler(target)  // InvocationHandler
        );
    }
}

public class LoggingInvocationHandler implements InvocationHandler {
    private final Object target;

    public LoggingInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        // 所有方法調用都會經過這裡！
        log.info("調用方法：{}，參數：{}", method.getName(), Arrays.toString(args));
        long start = System.currentTimeMillis();

        try {
            Object result = method.invoke(target, args);  // 反射調用真實方法
            log.info("方法 {} 執行成功，耗時：{} ms",
                method.getName(), System.currentTimeMillis() - start);
            return result;

        } catch (InvocationTargetException e) {
            log.error("方法 {} 執行失敗", method.getName(), e.getCause());
            throw e.getCause();
        }
    }
}

// 使用
OrderService realService = new OrderServiceImpl();
OrderService proxy       = JdkProxyFactory.createProxy(realService);
proxy.createOrder(request);  // 自動觸發 InvocationHandler.invoke()
```

```java
// ── 方式三：CGLIB 動態代理 ──
// 可以代理沒有介面的類（基於繼承，生成子類）

// 目標類（沒有介面）
public class UserService {
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}

// CGLIB 代理
public class CglibProxyFactory implements MethodInterceptor {

    public Object createProxy(Class<?> targetClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);    // 設定父類（繼承目標類）
        enhancer.setCallback(this);             // 設定攔截器
        return enhancer.create();               // 創建代理對象（子類）
    }

    @Override
    public Object intercept(Object obj, Method method,
                            Object[] args, MethodProxy proxy)
            throws Throwable {
        log.info("CGLIB 代理攔截：{}", method.getName());
        Object result = proxy.invokeSuper(obj, args);  // 調用父類（真實）方法
        log.info("CGLIB 代理完成：{}", method.getName());
        return result;
    }
}

// ⚠️ CGLIB 注意事項：
// final 類不能被代理（無法繼承）
// final 方法不能被攔截
// 需要無參構造器（或顯式指定構造器參數）
```

```
JDK 動態代理 vs CGLIB：

┌─────────────────┬─────────────────────┬──────────────────────┐
│                 │   JDK 動態代理       │   CGLIB              │
├─────────────────┼─────────────────────┼──────────────────────┤
│ 代理方式         │ 基於介面             │ 基於繼承（子類）       │
│ 目標要求         │ 必須實作介面         │ 可以是普通類           │
│ 效能（創建）     │ 慢（反射）           │ 稍慢（位元組碼生成）    │
│ 效能（調用）     │ 慢（反射 invoke）    │ 快（直接調用子類方法）  │
│ final 類         │ 不支援               │ 不支援               │
│ Spring 使用      │ 有介面時預設使用     │ 無介面時使用           │
└─────────────────┴─────────────────────┴──────────────────────┘

Spring AOP 底層：
  @Transactional → Spring 用代理模式攔截方法，開啟/提交/回滾事務
  @Cacheable     → 代理攔截，查詢前先查緩存
  @PreAuthorize  → 代理攔截，執行前檢查權限
```

```java
// Spring AOP 完整範例
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    // 切點：所有 Service 類的所有方法
    @Pointcut("execution(* com.example.service.*.*(..))")
    public void serviceMethod() {}

    // 環繞通知
    @Around("serviceMethod()")
    public Object aroundServiceMethod(ProceedingJoinPoint joinPoint)
            throws Throwable {

        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args     = joinPoint.getArgs();

        log.info("[{}] {}, 參數: {}",
            className, methodName, Arrays.toString(args));

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();  // 執行真實方法

            long duration = System.currentTimeMillis() - start;
            log.info("[{}] {} 執行成功，耗時: {} ms", className, methodName, duration);

            // 慢方法告警
            if (duration > 1000) {
                log.warn("[慢方法告警] [{}] {} 耗時 {} ms！",
                    className, methodName, duration);
            }

            return result;

        } catch (Exception e) {
            log.error("[{}] {} 執行失敗: {}", className, methodName, e.getMessage());
            throw e;
        }
    }

    // 前置通知
    @Before("@annotation(preAuthorize)")
    public void checkPermission(JoinPoint joinPoint, PreAuthorize preAuthorize) {
        // 方法執行前檢查權限
    }

    // 後置通知（成功才執行）
    @AfterReturning(pointcut = "serviceMethod()", returning = "result")
    public void afterReturning(Object result) {
        // 方法成功返回後執行
    }

    // 異常通知
    @AfterThrowing(pointcut = "serviceMethod()", throwing = "exception")
    public void afterThrowing(Exception exception) {
        log.error("方法拋出異常：{}", exception.getMessage());
    }
}
```

---

### 🟡 裝飾者模式（Decorator）

**目的**：動態地為對象添加功能，比繼承更靈活

```java
// 場景：咖啡飲品，可以加各種配料（牛奶、糖、奶泡...）

// 1. 基礎介面
public interface Coffee {
    String getDescription();
    BigDecimal getCost();
}

// 2. 基礎實作
public class SimpleCoffee implements Coffee {
    @Override
    public String getDescription() { return "黑咖啡"; }
    @Override
    public BigDecimal getCost() { return new BigDecimal("30"); }
}

// 3. 抽象裝飾者（關鍵！包含被裝飾的對象）
public abstract class CoffeeDecorator implements Coffee {
    protected final Coffee decoratedCoffee;  // 被裝飾的對象

    public CoffeeDecorator(Coffee coffee) {
        this.decoratedCoffee = coffee;
    }

    @Override
    public String getDescription() { return decoratedCoffee.getDescription(); }
    @Override
    public BigDecimal getCost() { return decoratedCoffee.getCost(); }
}

// 4. 具體裝飾者
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }

    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + " + 牛奶";
    }
    @Override
    public BigDecimal getCost() {
        return decoratedCoffee.getCost().add(new BigDecimal("10"));
    }
}

public class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) { super(coffee); }

    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + " + 糖";
    }
    @Override
    public BigDecimal getCost() {
        return decoratedCoffee.getCost().add(new BigDecimal("5"));
    }
}

public class FoamDecorator extends CoffeeDecorator {
    public FoamDecorator(Coffee coffee) { super(coffee); }

    @Override
    public String getDescription() {
        return decoratedCoffee.getDescription() + " + 奶泡";
    }
    @Override
    public BigDecimal getCost() {
        return decoratedCoffee.getCost().add(new BigDecimal("15"));
    }
}

// 5. 使用（動態組合！）
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);   // 加牛奶
coffee = new SugarDecorator(coffee);  // 加糖
coffee = new FoamDecorator(coffee);   // 加奶泡

System.out.println(coffee.getDescription()); // 黑咖啡 + 牛奶 + 糖 + 奶泡
System.out.println(coffee.getCost());        // 60

// Java 中的裝飾者模式：
// InputStream → FileInputStream
//            → BufferedInputStream（緩衝裝飾）
//            → DataInputStream（數據裝飾）

// new BufferedInputStream(new FileInputStream("file.txt"))
// BufferedInputStream 裝飾了 FileInputStream，加上緩衝功能
```

---

### 🟠 適配器模式（Adapter）

**目的**：讓介面不兼容的類可以一起工作

```java
// 場景：第三方支付 SDK 的介面和我們系統的介面不兼容

// 1. 我們系統的支付介面（目標介面）
public interface PaymentGateway {
    boolean pay(String orderId, BigDecimal amount, String currency);
}

// 2. 第三方 SDK（被適配者，介面不同，無法修改）
public class AlipaySDK {
    public String createPayment(String merchantOrderId, double amount) {
        // 支付寶 SDK 的邏輯
        return "SUCCESS";
    }
}

public class StripeSDK {
    public StripeResponse charge(StripeRequest request) {
        // Stripe SDK 的邏輯
        return new StripeResponse("succeeded");
    }
}

// 3. 適配器（讓不兼容的介面協同工作）
@Component
public class AlipayAdapter implements PaymentGateway {

    private final AlipaySDK alipaySDK = new AlipaySDK();

    @Override
    public boolean pay(String orderId, BigDecimal amount, String currency) {
        // 適配：將我們的介面轉換為 SDK 的介面
        String result = alipaySDK.createPayment(
            orderId,
            amount.doubleValue()  // BigDecimal → double
        );
        return "SUCCESS".equals(result);
    }
}

@Component
public class StripeAdapter implements PaymentGateway {

    private final StripeSDK stripeSDK = new StripeSDK();

    @Override
    public boolean pay(String orderId, BigDecimal amount, String currency) {
        StripeRequest request = new StripeRequest();
        request.setAmount(amount.multiply(new BigDecimal("100")).longValue()); // 元 → 分
        request.setCurrency(currency.toLowerCase());
        request.setMetadata(Map.of("orderId", orderId));

        StripeResponse response = stripeSDK.charge(request);
        return "succeeded".equals(response.getStatus());
    }
}

// 4. 使用（客戶端只依賴 PaymentGateway 介面）
@Service
public class OrderService {
    // 不關心具體是哪個支付方式
    @Autowired
    @Qualifier("alipayAdapter")
    private PaymentGateway paymentGateway;

    public void pay(Order order) {
        boolean success = paymentGateway.pay(
            order.getId().toString(),
            order.getAmount(),
            "CNY"
        );
    }
}
```

---

### 🔵 外觀模式（Facade）

**目的**：為複雜的子系統提供一個簡單的介面

```java
// 場景：下單流程涉及多個子系統

// 多個子系統
@Service public class InventoryService  { public boolean check(Long productId, int qty) { ... } }
@Service public class PriceService      { public BigDecimal calculate(Long productId, int qty) { ... } }
@Service public class PaymentService    { public boolean pay(BigDecimal amount) { ... } }
@Service public class ShippingService   { public String createShipping(Long orderId) { ... } }
@Service public class NotificationService { public void notify(Long userId, String msg) { ... } }

// ❌ 沒有 Facade：客戶端需要了解並協調所有子系統（複雜！）
public void order(OrderRequest request) {
    boolean inStock = inventoryService.check(request.getProductId(), request.getQuantity());
    if (!inStock) throw new RuntimeException("庫存不足");

    BigDecimal amount = priceService.calculate(request.getProductId(), request.getQuantity());

    boolean paid = paymentService.pay(amount);
    if (!paid) throw new RuntimeException("支付失敗");

    String trackingNo = shippingService.createShipping(orderId);

    notificationService.notify(request.getUserId(), "訂單已建立：" + orderId);
}

// ✅ 外觀模式：提供一個簡單的下單介面
@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final InventoryService    inventoryService;
    private final PriceService        priceService;
    private final PaymentService      paymentService;
    private final ShippingService     shippingService;
    private final NotificationService notificationService;

    // 一個簡單的方法，封裝所有複雜邏輯
    public OrderResult placeOrder(OrderRequest request) {
        // 1. 檢查庫存
        if (!inventoryService.check(request.getProductId(), request.getQuantity())) {
            throw new InsufficientStockException("庫存不足");
        }

        // 2. 計算價格
        BigDecimal amount = priceService.calculate(
            request.getProductId(), request.getQuantity());

        // 3. 支付
        if (!paymentService.pay(amount)) {
            throw new PaymentFailedException("支付失敗");
        }

        // 4. 創建物流
        String trackingNo = shippingService.createShipping(request.getOrderId());

        // 5. 通知用戶
        notificationService.notify(request.getUserId(),
            "您的訂單已建立！追蹤號：" + trackingNo);

        return OrderResult.success(request.getOrderId(), trackingNo, amount);
    }
}

// 客戶端（Controller）只需要調用 Facade
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderFacade orderFacade;  // 只依賴 Facade！

    @PostMapping("/orders")
    public ResponseEntity<OrderResult> placeOrder(@RequestBody OrderRequest request) {
        OrderResult result = orderFacade.placeOrder(request);
        return ResponseEntity.ok(result);
    }
}
```

---

### 🟤 橋接模式（Bridge）

**目的**：將抽象與實作分離，兩者可以獨立變化（避免繼承爆炸）

```java
// 場景：訊息系統，有多種訊息類型 × 多種發送管道
// 訊息類型：普通訊息、加急訊息、系統訊息
// 發送管道：Email、SMS、Push 通知
// 如果用繼承：3 × 3 = 9 個子類！
// 用橋接：3 + 3 = 6 個類，任意組合

// 1. 實作介面（Implementor）- 發送管道
public interface MessageSender {
    void send(String to, String title, String content);
    String getChannel();
}

// 具體實作 - 各種發送管道
@Component
public class EmailSender implements MessageSender {
    @Override
    public void send(String to, String title, String content) {
        log.info("Email → {}: [{}] {}", to, title, content);
        // 呼叫 Email 服務
    }
    @Override public String getChannel() { return "EMAIL"; }
}

@Component
public class SmsSender implements MessageSender {
    @Override
    public void send(String to, String title, String content) {
        log.info("SMS → {}: {}", to, content);
        // 呼叫 SMS 服務（內容簡短）
    }
    @Override public String getChannel() { return "SMS"; }
}

@Component
public class PushSender implements MessageSender {
    @Override
    public void send(String to, String title, String content) {
        log.info("Push → {}: [{}] {}", to, title, content);
        // 呼叫推播服務
    }
    @Override public String getChannel() { return "PUSH"; }
}

// 2. 抽象類（Abstraction）- 訊息類型
// ⭐ 橋接的關鍵：持有實作介面的引用
public abstract class Message {
    protected final MessageSender sender;  // 橋接！

    protected Message(MessageSender sender) {
        this.sender = sender;
    }

    public abstract void send(String to, String content);
}

// 具體抽象 - 各種訊息類型
public class NormalMessage extends Message {
    public NormalMessage(MessageSender sender) { super(sender); }

    @Override
    public void send(String to, String content) {
        sender.send(to, "一般通知", content);
    }
}

public class UrgentMessage extends Message {
    public UrgentMessage(MessageSender sender) { super(sender); }

    @Override
    public void send(String to, String content) {
        // 加急訊息：加上前綴，標題強調
        sender.send(to, "【緊急】", "⚠️ 緊急通知：" + content);
    }
}

public class SystemMessage extends Message {
    public SystemMessage(MessageSender sender) { super(sender); }

    @Override
    public void send(String to, String content) {
        sender.send(to, "系統通知", "[System] " + content);
    }
}

// 3. 使用（自由組合，不需要 9 個子類！）
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailSender emailSender;
    private final SmsSender   smsSender;

    public void notifyUser(String userId, String event) {
        // 組合一：普通訊息 + Email
        Message emailMsg = new NormalMessage(emailSender);
        emailMsg.send(userId, "您有一筆新訂單：" + event);

        // 組合二：加急訊息 + SMS
        Message smsMsg = new UrgentMessage(smsSender);
        smsMsg.send(userId, "支付逾時提醒：" + event);

        // 新增渠道或訊息類型，互不影響！
    }
}
```

---

### 🌿 組合模式（Composite）

**目的**：將對象組合成樹形結構，讓客戶端對單個對象和組合對象統一處理

```java
// 場景：公司組織架構（部門可以包含子部門和員工）

// 1. 統一介面（Component）
public interface OrganizationComponent {
    String getName();
    void print(String indent);
    int headcount();  // 人數
    BigDecimal totalSalary();  // 總薪資
}

// 2. 葉節點（Leaf）- 員工
@Data
@AllArgsConstructor
public class Employee implements OrganizationComponent {
    private String     name;
    private String     position;
    private BigDecimal salary;

    @Override
    public void print(String indent) {
        System.out.println(indent + "👤 " + name + "（" + position + "）");
    }

    @Override public int headcount()         { return 1; }
    @Override public BigDecimal totalSalary() { return salary; }
}

// 3. 組合節點（Composite）- 部門
public class Department implements OrganizationComponent {
    private final String name;
    private final List<OrganizationComponent> children = new ArrayList<>();

    public Department(String name) { this.name = name; }

    // 管理子節點
    public void add(OrganizationComponent component) {
        children.add(component);
    }
    public void remove(OrganizationComponent component) {
        children.remove(component);
    }

    @Override
    public String getName() { return name; }

    @Override
    public void print(String indent) {
        System.out.println(indent + "🏢 " + name);
        // 遞迴打印所有子節點
        children.forEach(child -> child.print(indent + "  "));
    }

    // 遞迴計算：無論層級多深，調用方式完全相同！
    @Override
    public int headcount() {
        return children.stream().mapToInt(OrganizationComponent::headcount).sum();
    }

    @Override
    public BigDecimal totalSalary() {
        return children.stream()
            .map(OrganizationComponent::totalSalary)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

// 4. 使用
public class Main {
    public static void main(String[] args) {
        // 建立組織架構
        Department company = new Department("科技公司");

        Department engineering = new Department("工程部");
        engineering.add(new Employee("Alice", "後端工程師", new BigDecimal("80000")));
        engineering.add(new Employee("Bob",   "前端工程師", new BigDecimal("75000")));

        Department backend = new Department("後端組");
        backend.add(new Employee("Charlie", "資深工程師", new BigDecimal("100000")));
        backend.add(new Employee("Dave",    "工程師",     new BigDecimal("70000")));
        engineering.add(backend);  // 部門嵌套部門

        Department hr = new Department("人資部");
        hr.add(new Employee("Eve", "HR 主管", new BigDecimal("60000")));

        company.add(engineering);
        company.add(hr);

        // 客戶端調用方式完全相同，不管是部門還是員工！
        company.print("");
        System.out.println("總人數：" + company.headcount());
        System.out.println("總薪資：" + company.totalSalary());
    }
}
// 輸出：
// 🏢 科技公司
//   🏢 工程部
//     👤 Alice（後端工程師）
//     👤 Bob（前端工程師）
//     🏢 後端組
//       👤 Charlie（資深工程師）
//       👤 Dave（工程師）
//   🏢 人資部
//     👤 Eve（HR 主管）
// 總人數：5
```

---

### 🪶 享元模式（Flyweight）

**目的**：透過共享技術，複用大量細粒度對象，節省記憶體

```java
// 場景：線上遊戲中有大量子彈，每顆子彈屬性幾乎相同
// 若每顆子彈都創建新對象 → 記憶體爆炸！
// 享元：共享「不變的部分」，只保留「變動的部分」

// 1. 享元對象（Flyweight）- 存儲共享的內部狀態（不可變）
@Getter
public class BulletType {
    private final String color;    // 顏色（共享）
    private final int    speed;    // 速度（共享）
    private final int    damage;   // 傷害（共享）
    private final String imageUrl; // 圖片（共享，最大開銷！）

    public BulletType(String color, int speed, int damage, String imageUrl) {
        this.color    = color;
        this.speed    = speed;
        this.damage   = damage;
        this.imageUrl = imageUrl;
        log.info("載入子彈類型：{}（消耗大量記憶體）", color);
    }

    // 渲染子彈（外部狀態：位置是每顆子彈獨有的）
    public void render(int x, int y) {
        log.debug("渲染 {} 子彈 at ({}, {})", color, x, y);
    }
}

// 2. 享元工廠（Flyweight Factory）
@Component
public class BulletFactory {

    // 快取池（共享對象）
    private final Map<String, BulletType> cache = new ConcurrentHashMap<>();

    public BulletType getBulletType(String color) {
        // 不存在才創建，存在直接複用
        return cache.computeIfAbsent(color, key -> {
            // 模擬載入圖片資源（耗時耗記憶體）
            return new BulletType(key, 500, 10, "/images/bullet/" + key + ".png");
        });
    }

    public int getCacheSize() { return cache.size(); }
}

// 3. 子彈（只存外部狀態：位置）
public class Bullet {
    private final BulletType type;   // 共享的內部狀態
    private int x;                   // 外部狀態（每顆子彈獨有）
    private int y;

    public Bullet(BulletType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += type.getSpeed();
        type.render(x, y);
    }
}

// 4. 使用
@Service
@RequiredArgsConstructor
public class GameService {

    private final BulletFactory bulletFactory;
    private final List<Bullet>  bullets = new ArrayList<>();

    public void fireBullets(int count) {
        for (int i = 0; i < count; i++) {
            // 1000 顆子彈只有 3 個 BulletType 對象！
            String type = i % 3 == 0 ? "RED" : i % 3 == 1 ? "BLUE" : "GREEN";
            BulletType bulletType = bulletFactory.getBulletType(type);
            bullets.add(new Bullet(bulletType, i * 10, 0));
        }
        log.info("發射 {} 顆子彈，但只創建了 {} 個類型對象",
            count, bulletFactory.getCacheSize());
    }
}

// Java 中的享元模式：
// Integer.valueOf(-128 ~ 127)  → 整數緩存池
// String.intern()              → 字串常量池
// 資料庫連線池（HikariCP）      → 共享連線對象
// 線程池                       → 共享線程對象
```

---

## 3️⃣ 行為型模式

---

### 🔴 觀察者模式（Observer）⭐ 事件驅動核心

**目的**：定義一對多依賴，一個對象改變時，所有依賴者都自動收到通知

```java
// 場景：訂單建立後，需要通知多個系統（庫存、積分、郵件、推薦）

// ── 方式一：手動實作觀察者 ──

// 1. 事件（被觀察的對象）
@Data
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long      orderId;
    private Long      userId;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}

// 2. 觀察者介面
public interface OrderEventListener {
    void onOrderCreated(OrderCreatedEvent event);
    int getOrder();  // 執行優先順序
}

// 3. 具體觀察者
@Component
public class InventoryListener implements OrderEventListener {
    @Override
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("庫存服務：扣減庫存，orderId={}", event.getOrderId());
    }
    @Override public int getOrder() { return 1; }  // 優先執行
}

@Component
public class PointsListener implements OrderEventListener {
    @Override
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("積分服務：增加積分，userId={}", event.getUserId());
    }
    @Override public int getOrder() { return 2; }
}

@Component
public class EmailListener implements OrderEventListener {
    @Override
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("郵件服務：發送確認郵件，userId={}", event.getUserId());
    }
    @Override public int getOrder() { return 3; }
}

// 4. 事件發布者（被觀察者）
@Component
public class OrderEventPublisher {

    private final List<OrderEventListener> listeners;

    // Spring 自動注入所有 OrderEventListener 實作
    public OrderEventPublisher(List<OrderEventListener> listeners) {
        // 按 order 排序
        this.listeners = listeners.stream()
            .sorted(Comparator.comparingInt(OrderEventListener::getOrder))
            .collect(Collectors.toList());
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        listeners.forEach(listener -> {
            try {
                listener.onOrderCreated(event);
            } catch (Exception e) {
                // 一個觀察者失敗，不影響其他觀察者！
                log.error("觀察者 {} 執行失敗", listener.getClass().getSimpleName(), e);
            }
        });
    }
}
```

```java
// ── 方式二：Spring 事件機制（推薦！）──

// 1. 定義事件（繼承 ApplicationEvent）
public class OrderCreatedEvent extends ApplicationEvent {
    private final Long      orderId;
    private final Long      userId;
    private final BigDecimal amount;

    public OrderCreatedEvent(Object source, Long orderId,
                             Long userId, BigDecimal amount) {
        super(source);
        this.orderId = orderId;
        this.userId  = userId;
        this.amount  = amount;
    }
    // getter...
}

// 2. 發布事件
@Service
@RequiredArgsConstructor
public class OrderService {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(OrderRequest request) {
        Order order = saveOrder(request);

        // 發布事件（在事務提交後執行）
        eventPublisher.publishEvent(
            new OrderCreatedEvent(this, order.getId(),
                request.getUserId(), order.getAmount())
        );

        return order;
    }
}

// 3. 監聽事件
@Component
@Slf4j
public class OrderEventHandler {

    // ✅ @EventListener：同步執行（在同一個事務中）
    @EventListener
    public void handleInventory(OrderCreatedEvent event) {
        log.info("扣減庫存，orderId={}", event.getOrderId());
    }

    // ✅ @TransactionalEventListener：事務提交後才執行！
    // 避免事務回滾後還在扣庫存的問題
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePoints(OrderCreatedEvent event) {
        log.info("增加積分，userId={}", event.getUserId());
    }

    // ✅ @Async + @EventListener：非同步執行（不阻塞主流程）
    @Async
    @EventListener
    public void handleEmail(OrderCreatedEvent event) {
        log.info("發送確認郵件，userId={}", event.getUserId());
        // 即使這裡很慢，也不會影響下單的響應時間
    }
}
```

---

### 🟡 策略模式（Strategy）⭐ 消滅 if-else

**目的**：定義一系列算法，封裝起來，使它們可以互相替換

```java
// 場景：不同的促銷活動有不同的折扣計算方式

// 1. 策略介面
public interface DiscountStrategy {
    BigDecimal calculateDiscount(BigDecimal originalPrice, Object... params);
    String getStrategyCode();
}

// 2. 具體策略
@Component
public class NoDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal price, Object... params) {
        return BigDecimal.ZERO;  // 無折扣
    }
    @Override public String getStrategyCode() { return "NONE"; }
}

@Component
public class PercentageDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal price, Object... params) {
        // params[0] = 折扣比例（如 0.8 = 八折）
        BigDecimal discountRate = (BigDecimal) params[0];
        return price.multiply(BigDecimal.ONE.subtract(discountRate));
    }
    @Override public String getStrategyCode() { return "PERCENTAGE"; }
}

@Component
public class FixedAmountDiscountStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal price, Object... params) {
        // params[0] = 折扣金額（如 50 = 減 50 元）
        BigDecimal discountAmount = (BigDecimal) params[0];
        return discountAmount.min(price);  // 折扣不能超過原價
    }
    @Override public String getStrategyCode() { return "FIXED_AMOUNT"; }
}

@Component
public class BuyXGetYFreeStrategy implements DiscountStrategy {
    @Override
    public BigDecimal calculateDiscount(BigDecimal price, Object... params) {
        // 買 X 送 Y，params[0] = 購買數量, params[1] = 單價
        int quantity   = (int) params[0];
        BigDecimal unitPrice = (BigDecimal) params[1];
        int freeItems  = quantity / 3;  // 買三送一
        return unitPrice.multiply(new BigDecimal(freeItems));
    }
    @Override public String getStrategyCode() { return "BUY_X_GET_Y_FREE"; }
}

// 3. 策略上下文（Context）
@Component
public class DiscountContext {

    private final Map<String, DiscountStrategy> strategyMap;

    public DiscountContext(List<DiscountStrategy> strategies) {
        this.strategyMap = strategies.stream()
            .collect(Collectors.toMap(
                DiscountStrategy::getStrategyCode,
                s -> s
            ));
    }

    public BigDecimal applyDiscount(String strategyCode,
                                    BigDecimal originalPrice,
                                    Object... params) {
        DiscountStrategy strategy = strategyMap.getOrDefault(
            strategyCode,
            strategyMap.get("NONE")  // 預設無折扣
        );
        return strategy.calculateDiscount(originalPrice, params);
    }
}

// 4. 使用（完全沒有 if-else！）
@Service
@RequiredArgsConstructor
public class OrderService {

    private final DiscountContext discountContext;

    public BigDecimal calculateFinalPrice(Order order, Promotion promotion) {
        // 根據促銷活動類型，自動選擇策略
        BigDecimal discount = discountContext.applyDiscount(
            promotion.getStrategyCode(),
            order.getOriginalAmount(),
            promotion.getDiscountValue()
        );
        return order.getOriginalAmount().subtract(discount);
    }
}
```

---

### 🟠 模板方法模式（Template Method）

**目的**：定義算法骨架，將某些步驟延遲到子類實作

```java
// 場景：不同的數據導出（Excel / CSV / PDF），流程相同但格式不同

// 抽象類（模板）
public abstract class DataExporter<T> {

    // 模板方法（定義算法骨架，final 防止子類覆蓋）
    public final void export(List<T> data, String filePath) {
        // 固定流程
        log.info("開始導出，數據量：{}", data.size());

        validateData(data);              // 1. 驗證（通用）
        List<T> processed = processData(data);  // 2. 處理（通用）
        String  content   = format(processed);  // 3. 格式化（子類實作）
        writeToFile(content, filePath);  // 4. 寫文件（通用）
        notifyComplete(filePath);        // 5. 通知（通用）

        log.info("導出完成：{}", filePath);
    }

    // 通用步驟（在抽象類中實作）
    private void validateData(List<T> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("導出數據不能為空");
        }
    }

    private List<T> processData(List<T> data) {
        return data.stream()
            .filter(this::filter)  // 過濾（子類可選覆蓋）
            .collect(Collectors.toList());
    }

    private void writeToFile(String content, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("寫文件失敗：" + filePath, e);
        }
    }

    private void notifyComplete(String filePath) {
        log.info("文件已生成：{}", filePath);
    }

    // 抽象方法（子類必須實作）
    protected abstract String format(List<T> data);

    // 鉤子方法（子類可選覆蓋，有預設實作）
    protected boolean filter(T item) {
        return true;  // 預設不過濾
    }
}

// 子類：CSV 導出
public class CsvExporter<T> extends DataExporter<T> {
    @Override
    protected String format(List<T> data) {
        StringBuilder sb = new StringBuilder();
        // CSV 格式化邏輯
        for (T item : data) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }
}

// 子類：Excel 導出
public class ExcelExporter<T> extends DataExporter<T> {
    @Override
    protected String format(List<T> data) {
        // Excel 格式化邏輯（使用 POI 等庫）
        return "Excel content...";
    }

    // 覆蓋鉤子：只導出金額大於 0 的訂單
    @Override
    protected boolean filter(T item) {
        if (item instanceof Order) {
            return ((Order) item).getAmount().compareTo(BigDecimal.ZERO) > 0;
        }
        return true;
    }
}

// Spring 中模板方法的體現：
// JdbcTemplate.query()  → 模板（連接、執行、關閉連接）
//                          你只需要提供 SQL 和 ResultSetExtractor
// RestTemplate.exchange() → 模板（建立連接、發送請求、處理響應）
// AbstractApplicationContext.refresh() → 模板（整個容器初始化流程）
```

---

### 🔵 責任鏈模式（Chain of Responsibility）

**目的**：請求沿著處理鏈傳遞，每個節點決定是否處理或傳遞給下一個

```java
// 場景：訂單審核流程（金額不同，需不同級別的審核）

// 1. 抽象處理者
public abstract class OrderApprover {
    protected OrderApprover nextApprover;  // 下一個審核者

    public OrderApprover setNext(OrderApprover next) {
        this.nextApprover = next;
        return next;  // 返回 next，支援鏈式設定
    }

    // 模板方法
    public final ApprovalResult process(Order order) {
        if (canApprove(order)) {
            return approve(order);
        } else if (nextApprover != null) {
            return nextApprover.process(order);  // 傳遞給下一個
        } else {
            return ApprovalResult.rejected("超出所有審核權限");
        }
    }

    protected abstract boolean canApprove(Order order);
    protected abstract ApprovalResult approve(Order order);
}

// 2. 具體處理者
@Component
public class TeamLeaderApprover extends OrderApprover {
    @Override
    protected boolean canApprove(Order order) {
        return order.getAmount().compareTo(new BigDecimal("1000")) <= 0;
    }
    @Override
    protected ApprovalResult approve(Order order) {
        log.info("組長審核通過，orderId={}", order.getId());
        return ApprovalResult.approved("組長");
    }
}

@Component
public class ManagerApprover extends OrderApprover {
    @Override
    protected boolean canApprove(Order order) {
        return order.getAmount().compareTo(new BigDecimal("10000")) <= 0;
    }
    @Override
    protected ApprovalResult approve(Order order) {
        log.info("主管審核通過，orderId={}", order.getId());
        return ApprovalResult.approved("主管");
    }
}

@Component
public class DirectorApprover extends OrderApprover {
    @Override
    protected boolean canApprove(Order order) {
        return order.getAmount().compareTo(new BigDecimal("100000")) <= 0;
    }
    @Override
    protected ApprovalResult approve(Order order) {
        log.info("總監審核通過，orderId={}", order.getId());
        return ApprovalResult.approved("總監");
    }
}

// 3. 組裝責任鏈
@Configuration
@RequiredArgsConstructor
public class ApprovalChainConfig {

    private final TeamLeaderApprover teamLeaderApprover;
    private final ManagerApprover    managerApprover;
    private final DirectorApprover   directorApprover;

    @Bean
    public OrderApprover approvalChain() {
        // 組長 → 主管 → 總監
        teamLeaderApprover
            .setNext(managerApprover)
            .setNext(directorApprover);
        return teamLeaderApprover;  // 返回鏈頭
    }
}

// 4. 使用
@Service
@RequiredArgsConstructor
public class OrderService {

    @Qualifier("approvalChain")
    private final OrderApprover approvalChain;

    public ApprovalResult approveOrder(Order order) {
        return approvalChain.process(order);
        // 金額 ≤ 1000    → 組長審核
        // 1000 < 金額 ≤ 10000  → 主管審核
        // 10000 < 金額 ≤ 100000 → 總監審核
        // 金額 > 100000  → 拒絕
    }
}

// Spring 中責任鏈的體現：
// Filter Chain（過濾器鏈）
// Interceptor Chain（攔截器鏈）
// Spring Security Filter Chain
```

---

### 🟢 狀態模式（State）

**目的**：讓對象在不同狀態下有不同的行為，消滅複雜的狀態判斷

```java
// 場景：訂單狀態機

// 訂單狀態枚舉
public enum OrderStatus {
    PENDING,     // 待付款
    PAID,        // 已付款
    SHIPPED,     // 已出貨
    DELIVERED,   // 已送達
    CANCELLED    // 已取消
}

// 1. 狀態介面
public interface OrderState {
    void pay(Order order);
    void ship(Order order);
    void deliver(Order order);
    void cancel(Order order);
    OrderStatus getStatus();
}

// 2. 具體狀態

// 待付款狀態
@Component
public class PendingState implements OrderState {
    @Override
    public void pay(Order order) {
        order.setStatus(OrderStatus.PAID);
        log.info("訂單 {} 支付成功", order.getId());
    }
    @Override
    public void ship(Order order) {
        throw new IllegalStateException("待付款訂單不能出貨");
    }
    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("待付款訂單不能確認送達");
    }
    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        log.info("訂單 {} 已取消", order.getId());
    }
    @Override
    public OrderStatus getStatus() { return OrderStatus.PENDING; }
}

// 已付款狀態
@Component
public class PaidState implements OrderState {
    @Override
    public void pay(Order order) {
        throw new IllegalStateException("訂單已支付，不能重複支付");
    }
    @Override
    public void ship(Order order) {
        order.setStatus(OrderStatus.SHIPPED);
        log.info("訂單 {} 已出貨", order.getId());
    }
    @Override
    public void deliver(Order order) {
        throw new IllegalStateException("尚未出貨，不能確認送達");
    }
    @Override
    public void cancel(Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        // 已付款取消，需要退款
        log.info("訂單 {} 取消，觸發退款流程", order.getId());
    }
    @Override
    public OrderStatus getStatus() { return OrderStatus.PAID; }
}

// 3. 狀態管理器（Context）
@Component
@RequiredArgsConstructor
public class OrderStateManager {

    private final Map<OrderStatus, OrderState> stateMap;

    public OrderStateManager(List<OrderState> states) {
        this.stateMap = states.stream()
            .collect(Collectors.toMap(OrderState::getStatus, s -> s));
    }

    public void handleAction(Order order, String action) {
        OrderState currentState = stateMap.get(order.getStatus());

        switch (action) {
            case "PAY"     -> currentState.pay(order);
            case "SHIP"    -> currentState.ship(order);
            case "DELIVER" -> currentState.deliver(order);
            case "CANCEL"  -> currentState.cancel(order);
            default        -> throw new IllegalArgumentException("未知操作：" + action);
        }
    }
}
```

---

### ⌨️ 命令模式（Command）

**目的**：將請求封裝成對象，支援撤銷、重做、隊列、日誌等操作

```java
// 場景：文字編輯器的撤銷/重做功能

// 1. 命令介面
public interface Command {
    void execute();   // 執行
    void undo();      // 撤銷
    String describe();
}

// 2. 接收者（真正執行操作的對象）
@Data
public class TextEditor {
    private StringBuilder content = new StringBuilder();

    public void insertText(int position, String text) {
        content.insert(position, text);
    }

    public void deleteText(int position, int length) {
        content.delete(position, position + length);
    }

    public String getContent() { return content.toString(); }
}

// 3. 具體命令
public class InsertCommand implements Command {
    private final TextEditor editor;
    private final int        position;
    private final String     text;

    public InsertCommand(TextEditor editor, int position, String text) {
        this.editor   = editor;
        this.position = position;
        this.text     = text;
    }

    @Override
    public void execute() {
        editor.insertText(position, text);
    }

    @Override
    public void undo() {
        editor.deleteText(position, text.length()); // 撤銷：刪除剛插入的文字
    }

    @Override
    public String describe() {
        return "插入「" + text + "」at " + position;
    }
}

public class DeleteCommand implements Command {
    private final TextEditor editor;
    private final int        position;
    private final int        length;
    private String           deletedText;  // 備份，用於撤銷

    public DeleteCommand(TextEditor editor, int position, int length) {
        this.editor   = editor;
        this.position = position;
        this.length   = length;
    }

    @Override
    public void execute() {
        // 先備份要刪除的文字
        deletedText = editor.getContent().substring(position, position + length);
        editor.deleteText(position, length);
    }

    @Override
    public void undo() {
        editor.insertText(position, deletedText); // 撤銷：把文字插回去
    }

    @Override
    public String describe() { return "刪除 " + length + " 個字元 at " + position; }
}

// 4. 命令管理器（Invoker）- 支援撤銷/重做
public class CommandManager {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();  // 執行新命令後，清除重做棧
        log.info("執行：{}", command.describe());
    }

    public void undo() {
        if (undoStack.isEmpty()) { log.info("沒有可撤銷的操作"); return; }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        log.info("撤銷：{}", command.describe());
    }

    public void redo() {
        if (redoStack.isEmpty()) { log.info("沒有可重做的操作"); return; }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        log.info("重做：{}", command.describe());
    }
}

// 5. 使用
TextEditor    editor  = new TextEditor();
CommandManager manager = new CommandManager();

manager.execute(new InsertCommand(editor, 0, "Hello"));
manager.execute(new InsertCommand(editor, 5, " World"));
System.out.println(editor.getContent()); // Hello World

manager.undo();
System.out.println(editor.getContent()); // Hello

manager.redo();
System.out.println(editor.getContent()); // Hello World

// Spring 中命令模式的應用：
// @Async 方法 → 任務封裝成 Runnable（命令）放入線程池
// Spring Batch → Job / Step 都是命令的封裝
// JdbcTemplate → SQL 操作封裝成 PreparedStatementCallback
```

---

### 🔁 迭代器模式（Iterator）

**目的**：提供統一的方式遍歷不同類型的集合，而不暴露其內部結構

```java
// Java 已內建 Iterator 介面，這裡展示自定義迭代器

// 場景：分頁迭代器（每次從 DB 查一頁，對外像遍歷一個大集合）

public class PageIterator<T> implements Iterator<T> {

    private final Function<PageRequest, Page<T>> fetchPage;
    private int                currentPage = 0;
    private final int          pageSize;
    private List<T>            currentBatch = new ArrayList<>();
    private int                batchIndex   = 0;
    private boolean            hasMore      = true;

    public PageIterator(Function<PageRequest, Page<T>> fetchPage, int pageSize) {
        this.fetchPage = fetchPage;
        this.pageSize  = pageSize;
        fetchNextBatch();
    }

    private void fetchNextBatch() {
        PageRequest pageRequest = PageRequest.of(currentPage++, pageSize);
        Page<T>     page        = fetchPage.apply(pageRequest);
        currentBatch = page.getContent();
        batchIndex   = 0;
        hasMore      = page.hasNext();
    }

    @Override
    public boolean hasNext() {
        if (batchIndex < currentBatch.size()) return true;
        if (hasMore) {
            fetchNextBatch();
            return !currentBatch.isEmpty();
        }
        return false;
    }

    @Override
    public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        return currentBatch.get(batchIndex++);
    }
}

// 使用：對外像在遍歷一個巨大集合，實際上每次只查一頁
@Service
public class OrderExportService {

    private final OrderRepository orderRepository;

    public void exportAllOrders() {
        Iterator<Order> iterator = new PageIterator<>(
            pageRequest -> orderRepository.findAll(pageRequest),
            1000  // 每頁 1000 筆
        );

        // 像遍歷普通集合一樣使用！不用關心分頁邏輯
        while (iterator.hasNext()) {
            Order order = iterator.next();
            exportOrder(order);
        }
    }
}

// Java 中迭代器無處不在：
// for-each 循環底層就是 Iterator
// Stream API 底層也是 Spliterator
// ResultSet（JDBC）遍歷查詢結果
```

---

### 📡 中介者模式（Mediator）

**目的**：用一個中介對象封裝多個對象之間的交互，降低耦合

```java
// 場景：聊天室（用戶之間不直接通訊，都通過聊天室中轉）

// 1. 中介者介面
public interface ChatRoom {
    void sendMessage(String message, ChatUser sender);
    void addUser(ChatUser user);
}

// 2. 具體中介者
@Component
public class ChatRoomImpl implements ChatRoom {

    private final List<ChatUser> users = new ArrayList<>();

    @Override
    public void addUser(ChatUser user) {
        users.add(user);
        broadcast(user.getName() + " 加入了聊天室", null);
    }

    @Override
    public void sendMessage(String message, ChatUser sender) {
        users.stream()
            .filter(user -> user != sender)  // 不發給自己
            .forEach(user -> user.receive(message, sender.getName()));
    }

    private void broadcast(String message, ChatUser exclude) {
        users.stream()
            .filter(user -> user != exclude)
            .forEach(user -> user.receive(message, "系統"));
    }
}

// 3. 同事類（Colleague）- 用戶
public class ChatUser {
    private final String   name;
    private final ChatRoom chatRoom;  // 持有中介者引用

    public ChatUser(String name, ChatRoom chatRoom) {
        this.name     = name;
        this.chatRoom = chatRoom;
        chatRoom.addUser(this);
    }

    // 發送訊息：通過中介者發送，不直接聯絡其他用戶
    public void send(String message) {
        log.info("[{}] 發送：{}", name, message);
        chatRoom.sendMessage(message, this);
    }

    // 接收訊息：從中介者接收
    public void receive(String message, String from) {
        log.info("[{}] 收到來自 [{}] 的訊息：{}", name, from, message);
    }

    public String getName() { return name; }
}

// 4. 使用
ChatRoom chatRoom = new ChatRoomImpl();
ChatUser alice    = new ChatUser("Alice", chatRoom);
ChatUser bob      = new ChatUser("Bob",   chatRoom);
ChatUser charlie  = new ChatUser("Charlie", chatRoom);

alice.send("大家好！");
// Bob 收到來自 Alice 的訊息：大家好！
// Charlie 收到來自 Alice 的訊息：大家好！

// Spring 中的中介者：
// ApplicationEventPublisher → 事件匯流排，各組件通過它通訊
// MQ（RabbitMQ/Kafka）    → 服務間的中介者
// API Gateway              → 微服務間通訊的中介者
```

---

### 📸 備忘錄模式（Memento）

**目的**：捕獲對象的內部狀態，並在不破壞封裝的前提下，在之後恢復它

```java
// 場景：訂單草稿的自動保存與恢復

// 1. 備忘錄（Memento）- 存儲快照
// 只有創建它的 Originator 可以存取內部狀態
public class OrderDraftMemento {
    private final String     title;
    private final List<OrderItem> items;
    private final String     couponCode;
    private final LocalDateTime savedAt;

    // 只有同包的 OrderDraft 可以存取
    OrderDraftMemento(String title, List<OrderItem> items, String couponCode) {
        this.title      = title;
        this.items      = new ArrayList<>(items);  // 深拷貝
        this.couponCode = couponCode;
        this.savedAt    = LocalDateTime.now();
    }

    String          getTitle()      { return title; }
    List<OrderItem> getItems()      { return new ArrayList<>(items); }
    String          getCouponCode() { return couponCode; }
    LocalDateTime   getSavedAt()    { return savedAt; }
}

// 2. 發起者（Originator）- 可以創建和恢復備忘錄
public class OrderDraft {
    private String          title;
    private List<OrderItem> items      = new ArrayList<>();
    private String          couponCode;

    // 創建快照
    public OrderDraftMemento save() {
        return new OrderDraftMemento(title, items, couponCode);
    }

    // 從快照恢復
    public void restore(OrderDraftMemento memento) {
        this.title      = memento.getTitle();
        this.items      = memento.getItems();
        this.couponCode = memento.getCouponCode();
        log.info("恢復到 {} 的草稿狀態", memento.getSavedAt());
    }

    // 業務方法
    public void addItem(OrderItem item)    { items.add(item); }
    public void setTitle(String title)     { this.title = title; }
    public void setCoupon(String coupon)   { this.couponCode = coupon; }
}

// 3. 管理者（Caretaker）- 管理備忘錄，不查看其內容
@Component
public class OrderDraftHistory {

    // 每個草稿可以有多個歷史版本
    private final Map<Long, Deque<OrderDraftMemento>> history = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY = 10;

    public void save(Long draftId, OrderDraftMemento memento) {
        history.computeIfAbsent(draftId, k -> new ArrayDeque<>());
        Deque<OrderDraftMemento> versions = history.get(draftId);
        versions.push(memento);
        if (versions.size() > MAX_HISTORY) {
            versions.pollLast();  // 超過上限，移除最舊的
        }
    }

    public Optional<OrderDraftMemento> undo(Long draftId) {
        Deque<OrderDraftMemento> versions = history.get(draftId);
        if (versions == null || versions.size() <= 1) {
            return Optional.empty();
        }
        versions.pop();  // 移除當前版本
        return Optional.of(versions.peek());  // 返回上一個版本
    }
}

// 4. 使用
OrderDraft        draft   = new OrderDraft();
OrderDraftHistory history = new OrderDraftHistory();

draft.setTitle("我的訂單");
history.save(1L, draft.save());  // 自動保存 v1

draft.addItem(new OrderItem("商品A", 2));
history.save(1L, draft.save());  // 自動保存 v2

draft.addItem(new OrderItem("商品B", 1));
// 用戶誤操作，想撤銷

history.undo(1L).ifPresent(draft::restore);  // 恢復到 v2
```

---

### 👁️ 訪問者模式（Visitor）

**目的**：在不修改元素類的情況下，定義作用於元素的新操作

```java
// 場景：對不同類型的商品（電子品、食品、服飾）
// 執行不同操作（計算稅率、生成報表、計算折扣）
// 且不同商品 × 不同操作組合複雜

// 1. 訪問者介面（定義對每種元素的操作）
public interface ProductVisitor {
    void visit(ElectronicsProduct product);
    void visit(FoodProduct product);
    void visit(ClothingProduct product);
}

// 2. 元素介面
public interface Product {
    String getName();
    BigDecimal getPrice();
    void accept(ProductVisitor visitor);  // 接受訪問者
}

// 3. 具體元素
@Data
@AllArgsConstructor
public class ElectronicsProduct implements Product {
    private String     name;
    private BigDecimal price;
    private int        warrantyMonths;

    @Override
    public void accept(ProductVisitor visitor) {
        visitor.visit(this);  // 雙重分派
    }
}

@Data
@AllArgsConstructor
public class FoodProduct implements Product {
    private String        name;
    private BigDecimal    price;
    private LocalDate     expiryDate;
    private boolean       organic;

    @Override
    public void accept(ProductVisitor visitor) {
        visitor.visit(this);
    }
}

@Data
@AllArgsConstructor
public class ClothingProduct implements Product {
    private String     name;
    private BigDecimal price;
    private String     size;

    @Override
    public void accept(ProductVisitor visitor) {
        visitor.visit(this);
    }
}

// 4. 具體訪問者（新增操作，不修改元素類！）

// 訪問者一：計算稅率
public class TaxCalculatorVisitor implements ProductVisitor {
    private BigDecimal totalTax = BigDecimal.ZERO;

    @Override
    public void visit(ElectronicsProduct p) {
        BigDecimal tax = p.getPrice().multiply(new BigDecimal("0.05")); // 5%
        totalTax = totalTax.add(tax);
        log.info("{} 電子稅：{}", p.getName(), tax);
    }
    @Override
    public void visit(FoodProduct p) {
        BigDecimal rate = p.isOrganic() ?
            new BigDecimal("0.0") : new BigDecimal("0.02");
        BigDecimal tax  = p.getPrice().multiply(rate);
        totalTax = totalTax.add(tax);
        log.info("{} 食品稅：{}", p.getName(), tax);
    }
    @Override
    public void visit(ClothingProduct p) {
        BigDecimal tax = p.getPrice().multiply(new BigDecimal("0.03")); // 3%
        totalTax = totalTax.add(tax);
        log.info("{} 服飾稅：{}", p.getName(), tax);
    }
    public BigDecimal getTotalTax() { return totalTax; }
}

// 訪問者二：生成報表（新操作，不需要修改任何商品類！）
public class ReportVisitor implements ProductVisitor {
    private final List<String> report = new ArrayList<>();

    @Override
    public void visit(ElectronicsProduct p) {
        report.add(String.format("電子品：%s | 價格：%s | 保固：%d個月",
            p.getName(), p.getPrice(), p.getWarrantyMonths()));
    }
    @Override
    public void visit(FoodProduct p) {
        report.add(String.format("食品：%s | 價格：%s | 到期：%s",
            p.getName(), p.getPrice(), p.getExpiryDate()));
    }
    @Override
    public void visit(ClothingProduct p) {
        report.add(String.format("服飾：%s | 價格：%s | 尺寸：%s",
            p.getName(), p.getPrice(), p.getSize()));
    }
    public List<String> getReport() { return report; }
}

// 5. 使用
List<Product> products = List.of(
    new ElectronicsProduct("iPhone 15", new BigDecimal("30000"), 12),
    new FoodProduct("有機蘋果", new BigDecimal("100"), LocalDate.now().plusDays(7), true),
    new ClothingProduct("T-Shirt", new BigDecimal("500"), "M")
);

// 計算稅金
TaxCalculatorVisitor taxVisitor = new TaxCalculatorVisitor();
products.forEach(p -> p.accept(taxVisitor));
System.out.println("總稅金：" + taxVisitor.getTotalTax());

// 生成報表（不需要修改任何商品類，只需新增 Visitor！）
ReportVisitor reportVisitor = new ReportVisitor();
products.forEach(p -> p.accept(reportVisitor));
reportVisitor.getReport().forEach(System.out::println);
```

---

### 📖 直譯器模式（Interpreter）

**目的**：為語言定義文法，並建立直譯器來解釋語言中的句子

```java
// 場景：解析簡單的權限表達式
// "ADMIN AND (USER OR MANAGER)"

// 1. 抽象表達式
public interface Expression {
    boolean interpret(Set<String> context);  // context = 用戶擁有的角色
}

// 2. 終端表達式（Terminal）
public class RoleExpression implements Expression {
    private final String role;

    public RoleExpression(String role) { this.role = role; }

    @Override
    public boolean interpret(Set<String> context) {
        return context.contains(role);
    }
}

// 3. 非終端表達式（Non-Terminal）
public class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Set<String> context) {
        return left.interpret(context) && right.interpret(context);
    }
}

public class OrExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Set<String> context) {
        return left.interpret(context) || right.interpret(context);
    }
}

public class NotExpression implements Expression {
    private final Expression expression;

    public NotExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean interpret(Set<String> context) {
        return !expression.interpret(context);
    }
}

// 4. 使用
// 建立表達式：ADMIN AND (USER OR MANAGER)
Expression adminExpr   = new RoleExpression("ADMIN");
Expression userExpr    = new RoleExpression("USER");
Expression managerExpr = new RoleExpression("MANAGER");

Expression userOrManager = new OrExpression(userExpr, managerExpr);
Expression rule          = new AndExpression(adminExpr, userOrManager);

// 測試
Set<String> userRoles1 = Set.of("ADMIN", "USER");
Set<String> userRoles2 = Set.of("ADMIN");
Set<String> userRoles3 = Set.of("USER", "MANAGER");

System.out.println(rule.interpret(userRoles1)); // true  (ADMIN AND USER)
System.out.println(rule.interpret(userRoles2)); // false (有 ADMIN 但沒 USER 也沒 MANAGER)
System.out.println(rule.interpret(userRoles3)); // false (沒有 ADMIN)

// Java 中直譯器模式的應用：
// java.util.regex.Pattern  → 正則表達式直譯器
// javax.el.ExpressionLanguage → EL 表達式直譯器
// Spring SpEL               → @PreAuthorize("hasRole('ADMIN')")
// SQL Parser                → SQL 語句解析
```

---

## 📝 設計模式 GoF 23 種完整總結

### 創建型（5種）
| 模式 | 核心思想 | 解決問題 | Spring 應用 |
|------|---------|---------|------------|
| **單例** | 只有一個實例 | 全局共享資源 | Spring Bean |
| **工廠方法** | 子類決定創建哪個類 | 消滅 if-else 創建邏輯 | BeanFactory |
| **抽象工廠** | 創建一系列相關對象 | 跨平台/跨資料庫 | — |
| **建造者** | 分步驟創建複雜對象 | 構造器參數爆炸 | @Builder、ResponseEntity |
| **原型** | 克隆現有對象 | 避免重複初始化開銷 | BeanUtils.copyProperties |

### 結構型（7種）
| 模式 | 核心思想 | 解決問題 | Spring 應用 |
|------|---------|---------|------------|
| **代理** | 為對象添加額外功能 | AOP（日誌/事務/權限）| @Transactional、AOP |
| **裝飾者** | 動態添加功能 | 比繼承更靈活 | BeanWrapper、IO 流 |
| **適配器** | 讓不兼容介面協同 | 整合第三方 SDK | HandlerAdapter |
| **外觀** | 為複雜系統提供簡單介面 | 降低客戶端複雜度 | JdbcTemplate |
| **橋接** | 抽象與實作分離 | 避免繼承爆炸 | — |
| **組合** | 樹形結構統一處理 | 遞迴結構（菜單/組織）| — |
| **享元** | 共享細粒度對象 | 節省大量記憶體 | 連線池、Integer 緩存 |

### 行為型（11種）
| 模式 | 核心思想 | 解決問題 | Spring 應用 |
|------|---------|---------|------------|
| **觀察者** | 一對多事件通知 | 解耦發布與消費 | ApplicationEvent |
| **策略** | 算法族可互換 | 消滅 if-else 業務邏輯 | Resource 策略 |
| **模板方法** | 定義算法骨架 | 複用固定流程 | JdbcTemplate |
| **責任鏈** | 請求沿鏈傳遞 | 審核流程、過濾鏈 | Filter Chain |
| **狀態** | 不同狀態不同行為 | 消滅複雜狀態判斷 | 訂單狀態機 |
| **命令** | 請求封裝成對象 | 撤銷/重做/隊列 | Spring Batch、Runnable |
| **迭代器** | 統一遍歷集合 | 不暴露內部結構 | Iterator、Stream |
| **中介者** | 集中管理交互 | 降低對象間耦合 | ApplicationEventPublisher、MQ |
| **備忘錄** | 捕獲與恢復狀態 | 撤銷操作、自動保存 | — |
| **訪問者** | 不修改類就加新操作 | 操作與結構分離 | — |
| **直譯器** | 定義語言文法 | 解析表達式/規則 | SpEL、正則表達式 |

