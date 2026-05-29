# 🔴 測試（Testing）完整深入教學

---

## 🗺️ 學習路線圖

```
測試
├── 1️⃣ 測試基礎概念
│   ├── 測試金字塔
│   ├── 測試分類
│   └── 好測試的標準（FIRST 原則）
├── 2️⃣ JUnit 5 深入
│   ├── 核心注解
│   ├── 參數化測試
│   ├── 條件測試
│   └── 測試生命週期
├── 3️⃣ Mockito 深入
│   ├── Mock / Stub / Spy
│   ├── 常用 API
│   ├── 驗證行為
│   └── ArgumentCaptor
├── 4️⃣ Spring Boot Test
│   ├── @SpringBootTest（整合測試）
│   ├── @WebMvcTest（Controller 層測試）
│   ├── @DataJpaTest（Repository 層測試）
│   └── @MockBean / @SpyBean
├── 5️⃣ Testcontainers（真實環境整合測試）
│   ├── 核心原理
│   ├── MySQL / Redis / Kafka 整合
│   └── 與 Spring Boot 整合
└── 6️⃣ TDD（測試驅動開發）
    ├── Red-Green-Refactor 循環
    ├── TDD 實戰範例
    └── 測試覆蓋率
```

---

## 1️⃣ 測試基礎概念

---

### 🔵 測試金字塔

```
                    ╱▔▔▔▔▔▔▔╲
                   ╱  E2E 測試  ╲      ← 少量、慢、貴
                  ╱  (端對端測試)  ╲       Selenium / Playwright
                 ╱─────────────────╲
                ╱    整合測試         ╲   ← 中量、較慢
               ╱  (Integration Test)  ╲    @SpringBootTest
              ╱                         ╲   Testcontainers
             ╱─────────────────────────╲
            ╱          單元測試           ╲ ← 大量、快、便宜
           ╱        (Unit Test)           ╲  JUnit 5 + Mockito
          ╱▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁╲

測試比例（建議）：
  單元測試    70%  → 快速、隔離、大量
  整合測試    20%  → 驗證組件間協作
  E2E 測試    10%  → 驗證核心用戶流程

原則：
  越往上層 → 越慢、越貴、越脆弱（環境問題）
  越往下層 → 越快、越穩定、越易於定位問題
```

---

### 🟡 FIRST 原則（好測試的標準）

```
F - Fast（快速）
    單元測試應該在毫秒內執行
    整個測試套件應該在幾分鐘內完成

I - Independent（獨立）
    測試之間不相互依賴
    每個測試可以單獨執行（順序無關）
    ❌ TestB 依賴 TestA 的執行結果

R - Repeatable（可重複）
    在任何環境下結果一致
    不依賴外部狀態（日期、網路、隨機數）
    ❌ 今天通過，明天因為日期不同而失敗

S - Self-validating（自驗證）
    測試結果只有通過或失敗，不需要人工判斷
    ❌ 需要人工查看日誌來判斷是否正確

T - Timely（及時）
    TDD：在生產程式碼之前寫測試
    最遲在功能完成時同步寫測試
    ❌ 等所有功能都完成後才補測試（往往來不及）
```

---

## 2️⃣ JUnit 5 深入

---

### 🔵 依賴設定

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot 測試（已包含 JUnit 5 + Mockito + AssertJ）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Testcontainers -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

### 🟡 JUnit 5 核心注解

```java
// 基礎測試結構
@DisplayName("訂單服務測試")   // 測試類的顯示名稱
class OrderServiceTest {

    private OrderService  orderService;
    private OrderRepository orderRepository;

    // ─── 生命週期 ───

    @BeforeAll  // 所有測試前執行一次（必須是 static）
    static void setUpAll() {
        System.out.println("初始化測試環境（整個類只執行一次）");
    }

    @AfterAll   // 所有測試後執行一次（必須是 static）
    static void tearDownAll() {
        System.out.println("清理測試環境");
    }

    @BeforeEach // 每個測試前執行
    void setUp() {
        orderRepository = Mockito.mock(OrderRepository.class);
        orderService    = new OrderService(orderRepository);
    }

    @AfterEach  // 每個測試後執行
    void tearDown() {
        Mockito.reset(orderRepository);
    }

    // ─── 測試方法 ───

    @Test
    @DisplayName("建立訂單 - 正常情況")
    void createOrder_ShouldSuccess_WhenValidRequest() {
        // 測試內容...
    }

    @Test
    @DisplayName("建立訂單 - 庫存不足時拋出例外")
    void createOrder_ShouldThrowException_WhenInsufficientStock() {
        // 測試內容...
    }

    @Disabled("暫時停用：等待 API 確認")  // 跳過此測試
    @Test
    void temporarilyDisabledTest() { }
}
```

---

### 🟠 AssertJ 斷言（推薦，比 JUnit 原生斷言更強）

```java
import static org.assertj.core.api.Assertions.*;

@Test
@DisplayName("斷言範例")
void assertionExamples() {

    // ─── 基本斷言 ───
    assertThat(1 + 1).isEqualTo(2);
    assertThat("Hello").isNotNull()
                       .startsWith("H")
                       .endsWith("o")
                       .hasSize(5)
                       .isEqualToIgnoringCase("hello");

    // ─── 數字斷言 ───
    assertThat(3.14).isGreaterThan(3.0)
                    .isLessThan(4.0)
                    .isBetween(3.0, 4.0);

    // ─── 集合斷言 ───
    List<String> list = List.of("Apple", "Banana", "Cherry");
    assertThat(list).isNotEmpty()
                    .hasSize(3)
                    .contains("Apple", "Banana")
                    .doesNotContain("Durian")
                    .containsExactlyInAnyOrder("Cherry", "Apple", "Banana");

    // ─── 例外斷言 ───
    assertThatThrownBy(() -> orderService.findById(-1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("ID 不能為負數")
        .hasMessageContaining("負數");

    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> orderService.findById(999L))
        .withMessage("訂單不存在：999");

    // 確保不拋出例外
    assertThatNoException().isThrownBy(() -> orderService.findById(1L));

    // ─── 對象斷言 ───
    Order order = orderService.findById(1L);
    assertThat(order).isNotNull()
                     .extracting(Order::getId, Order::getStatus)
                     .containsExactly(1L, OrderStatus.PAID);

    // ─── 多個斷言（軟斷言，全部執行後再報告所有失敗）───
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(order.getId()).isEqualTo(1L);
    softly.assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    softly.assertThat(order.getAmount()).isGreaterThan(BigDecimal.ZERO);
    softly.assertAll();  // 收集所有失敗，一次性報告
}
```

---

### 🔴 參數化測試（Parameterized Test）

```java
// 用同一個測試邏輯測試多組數據

@ParameterizedTest
@DisplayName("不同金額的折扣計算")
@CsvSource({
    "100.0,  0.9,  90.0",    // 100 元九折 = 90
    "200.0,  0.8,  160.0",   // 200 元八折 = 160
    "500.0,  0.7,  350.0",   // 500 元七折 = 350
    "1000.0, 0.5,  500.0"    // 1000 元五折 = 500
})
void calculateDiscount_ShouldReturnCorrectAmount(
        double originalPrice,
        double discountRate,
        double expectedPrice) {

    BigDecimal result = discountService.calculate(
        new BigDecimal(originalPrice),
        new BigDecimal(discountRate)
    );

    assertThat(result).isEqualByComparingTo(new BigDecimal(expectedPrice));
}

// ValueSource（單一參數）
@ParameterizedTest
@ValueSource(strings = {"", " ", "  ", "\t", "\n"})
@DisplayName("空白用戶名應該驗證失敗")
void validate_ShouldFail_WhenUsernameIsBlank(String username) {
    assertThatThrownBy(() -> userService.create(username, "password"))
        .isInstanceOf(IllegalArgumentException.class);
}

// EnumSource（枚舉參數）
@ParameterizedTest
@EnumSource(value = OrderStatus.class,
            names  = {"SHIPPED", "DELIVERED"},
            mode   = EnumSource.Mode.INCLUDE)
@DisplayName("已出貨/已送達的訂單不能取消")
void cancelOrder_ShouldFail_WhenOrderShippedOrDelivered(
        OrderStatus status) {

    Order order = Order.builder().id(1L).status(status).build();
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.cancel(1L, "不想要了"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("無法取消");
}

// MethodSource（複雜參數，從方法取得）
@ParameterizedTest
@MethodSource("invalidOrderRequests")
@DisplayName("非法訂單請求應該驗證失敗")
void createOrder_ShouldFail_WhenRequestInvalid(
        CreateOrderRequest request,
        String expectedError) {

    assertThatThrownBy(() -> orderService.create(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(expectedError);
}

static Stream<Arguments> invalidOrderRequests() {
    return Stream.of(
        Arguments.of(
            CreateOrderRequest.builder().userId(null).build(),
            "用戶ID不能為空"
        ),
        Arguments.of(
            CreateOrderRequest.builder().userId(1L).items(List.of()).build(),
            "訂單項目不能為空"
        ),
        Arguments.of(
            CreateOrderRequest.builder().userId(-1L).build(),
            "用戶ID不合法"
        )
    );
}
```

---

### 🟢 條件測試與巢狀測試

```java
@DisplayName("訂單服務測試")
class OrderServiceTest {

    // 巢狀測試（按業務場景分組）
    @Nested
    @DisplayName("建立訂單")
    class CreateOrderTests {

        @Test
        @DisplayName("正常建立訂單成功")
        void shouldCreateOrderSuccessfully() { ... }

        @Test
        @DisplayName("庫存不足時失敗")
        void shouldFailWhenInsufficientStock() { ... }

        @Nested
        @DisplayName("金額驗證")
        class AmountValidationTests {

            @Test
            @DisplayName("金額為零時應該失敗")
            void shouldFailWhenAmountIsZero() { ... }

            @Test
            @DisplayName("金額為負數時應該失敗")
            void shouldFailWhenAmountIsNegative() { ... }
        }
    }

    @Nested
    @DisplayName("取消訂單")
    class CancelOrderTests {

        @Test
        @DisplayName("待付款訂單可以取消")
        void shouldCancelPendingOrder() { ... }
    }

    // 條件測試（根據條件決定是否執行）
    @Test
    @EnabledOnOs(OS.LINUX)              // 只在 Linux 上執行
    void shouldRunOnLinuxOnly() { ... }

    @Test
    @EnabledIfEnvironmentVariable(
        named  = "TEST_ENV",
        matches = "staging"
    )
    void shouldRunInStagingOnly() { ... }

    @Test
    @EnabledIfSystemProperty(
        named  = "java.version",
        matches = "17.*"
    )
    void shouldRunOnJava17() { ... }
}
```

---

## 3️⃣ Mockito 深入

---

### 🔵 Mock / Stub / Spy 的區別

```
Mock（模擬）：
  完全虛假的對象，所有方法都返回預設值（null / 0 / false）
  用來隔離測試對象的依賴
  Mockito.mock(OrderRepository.class)

Stub（存根）：
  為 Mock 對象的方法設定返回值
  when(mock.method()).thenReturn(value)

Spy（間諜）：
  包裝真實對象，默認調用真實方法
  可以選擇性 Stub 部分方法
  Mockito.spy(new OrderService())
  用途：測試遺留代碼，只 Mock 部分方法

Fake（偽造）：
  一個有真實邏輯但簡化的實作
  如：H2 記憶體資料庫（代替真實 MySQL）
```

---

### 🟡 Mockito 完整 API

```java
@ExtendWith(MockitoExtension.class)  // JUnit 5 整合 Mockito
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks  // 自動注入上面的 Mock 到被測對象
    private OrderService orderService;

    // ─── Stubbing（設定返回值）───

    @Test
    void stubbingExamples() {
        Order mockOrder = Order.builder()
            .id(1L)
            .status(OrderStatus.PENDING)
            .amount(new BigDecimal("500"))
            .build();

        // 基本 stubbing
        when(orderRepository.findById(1L))
            .thenReturn(Optional.of(mockOrder));

        // 連續返回不同值
        when(orderRepository.findById(2L))
            .thenReturn(Optional.empty())           // 第一次
            .thenReturn(Optional.of(mockOrder));    // 第二次以後

        // 拋出例外
        when(orderRepository.findById(-1L))
            .thenThrow(new IllegalArgumentException("ID 不能為負數"));

        // 根據參數動態返回值
        when(orderRepository.findById(anyLong()))
            .thenAnswer(invocation -> {
                Long id = invocation.getArgument(0);
                if (id > 0) {
                    return Optional.of(Order.builder().id(id).build());
                }
                return Optional.empty();
            });

        // void 方法
        doNothing().when(notificationService).send(any());          // 不做任何事
        doThrow(new RuntimeException("發送失敗"))
            .when(notificationService).send(any());                  // 拋出例外
        doAnswer(invocation -> {
            log.info("通知已發送（Mock）");
            return null;
        }).when(notificationService).send(any());                    // 自定義行為
    }

    // ─── 參數匹配器（Argument Matchers）───

    @Test
    void argumentMatchersExamples() {
        // any()：匹配任意值
        when(orderRepository.findByUserId(anyLong()))
            .thenReturn(List.of());

        // 精確值匹配（不能混用 any() 和精確值！）
        // ❌ 錯誤：when(repo.save(order, anyString()))
        // ✅ 正確：用 eq() 包裝精確值
        when(orderRepository.findByUserIdAndStatus(eq(1L), eq("PAID")))
            .thenReturn(List.of());

        // 自定義匹配器
        when(orderRepository.save(argThat(order ->
            order.getAmount().compareTo(BigDecimal.ZERO) > 0
        ))).thenReturn(new Order());

        // 常用匹配器
        anyString()           // 任意非 null 字串
        anyInt()              // 任意 int
        anyLong()             // 任意 long
        any(Order.class)      // 任意 Order 類型（包含 null）
        notNull()             // 非 null
        isNull()              // 為 null
        startsWith("ORDER-")  // 以 ORDER- 開頭的字串
    }

    // ─── 驗證行為（Verify）───

    @Test
    @DisplayName("建立訂單後應該發送通知")
    void createOrder_ShouldSendNotification() {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .items(List.of(new OrderItem(1L, 2)))
            .build();

        when(inventoryService.checkAndReserve(anyLong(), anyInt()))
            .thenReturn(true);
        when(orderRepository.save(any()))
            .thenReturn(Order.builder().id(1L).build());

        // Act
        orderService.create(request);

        // Assert - 驗證方法被調用了
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(notificationService, times(1)).send(any());
        verify(inventoryService, times(1))
            .checkAndReserve(eq(1L), eq(2));

        // 驗證不應該被調用
        verify(notificationService, never()).sendUrgent(any());

        // 驗證調用順序
        InOrder inOrder = inOrder(inventoryService, orderRepository, notificationService);
        inOrder.verify(inventoryService).checkAndReserve(anyLong(), anyInt());
        inOrder.verify(orderRepository).save(any());
        inOrder.verify(notificationService).send(any());

        // 驗證沒有其他未被驗證的互動
        verifyNoMoreInteractions(notificationService);

        // 驗證零互動
        verifyNoInteractions(notificationService);
    }
}
```

---

### 🟠 ArgumentCaptor（捕獲參數）

```java
@Test
@DisplayName("建立訂單時，保存的訂單應包含正確資訊")
void createOrder_ShouldSaveOrderWithCorrectData() {
    // Arrange
    ArgumentCaptor<Order> orderCaptor =
        ArgumentCaptor.forClass(Order.class);

    CreateOrderRequest request = CreateOrderRequest.builder()
        .userId(1L)
        .address("台北市信義區")
        .items(List.of(
            new OrderItemRequest(1L, 2, new BigDecimal("100"))
        ))
        .build();

    when(orderRepository.save(any()))
        .thenAnswer(i -> i.getArgument(0));

    // Act
    orderService.create(request);

    // Assert - 捕獲 save() 的參數，驗證內容
    verify(orderRepository).save(orderCaptor.capture());
    Order savedOrder = orderCaptor.getValue();

    assertThat(savedOrder.getUserId()).isEqualTo(1L);
    assertThat(savedOrder.getAddress()).isEqualTo("台北市信義區");
    assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(savedOrder.getAmount()).isEqualByComparingTo(new BigDecimal("200"));

    // 捕獲多次調用的參數
    ArgumentCaptor<String> emailCaptor =
        ArgumentCaptor.forClass(String.class);
    verify(notificationService, times(2)).sendEmail(emailCaptor.capture());
    List<String> allEmails = emailCaptor.getAllValues();
    assertThat(allEmails).containsExactly("order@example.com", "user@example.com");
}
```

---

### 🔴 Spy（間諜）使用

```java
@Test
@DisplayName("Spy 使用範例")
void spyExample() {
    // Spy 包裝真實對象
    List<String> realList = new ArrayList<>();
    List<String> spyList  = Mockito.spy(realList);

    // 大部分方法調用真實實作
    spyList.add("Apple");
    spyList.add("Banana");

    assertThat(spyList).hasSize(2);  // 真實的 size()

    // 只 Stub 特定方法
    doReturn(100).when(spyList).size();

    assertThat(spyList.size()).isEqualTo(100);   // Stub 的返回值
    assertThat(spyList.get(0)).isEqualTo("Apple"); // 真實的 get()

    // 驗證也適用於 Spy
    verify(spyList, times(2)).add(anyString());
}

// 實際業務場景：測試含有複雜邏輯的類
@Test
void realWorldSpyExample() {
    OrderService spyService = Mockito.spy(orderService);

    // 只 Stub 複雜的內部方法（如：呼叫外部 API）
    doReturn(new BigDecimal("0.1"))
        .when(spyService)
        .calculateTaxRate(anyString());

    // 測試真實的業務邏輯（稅率計算被 Stub）
    Order order = spyService.create(request);
    assertThat(order.getTax()).isEqualByComparingTo(new BigDecimal("10.0"));
}
```

---

## 4️⃣ Spring Boot Test

---

### 🔵 測試切片（Test Slices）

```
Spring Boot 提供多種測試切片，只加載相關的 Bean：

@SpringBootTest    → 加載完整應用上下文（最慢，適合整合測試）
@WebMvcTest        → 只加載 Web 層（Controller、Filter、Interceptor）
@DataJpaTest       → 只加載 JPA 相關（Repository、DataSource）
@JsonTest          → 只加載 JSON 序列化相關
@RestClientTest    → 只加載 RestTemplate 相關

選擇原則：
  Controller 層測試 → @WebMvcTest
  Repository 層測試 → @DataJpaTest
  Service 層測試    → 不用 Spring，直接 JUnit + Mockito（最快！）
  整合測試          → @SpringBootTest + Testcontainers
```

---

### 🟡 @WebMvcTest（Controller 層測試）

```java
@WebMvcTest(OrderController.class)  // 只加載 OrderController
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;  // 模擬 HTTP 請求，不啟動真實服務器

    @MockBean  // 替換 Spring Context 中的 Bean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/orders/{id} - 查詢訂單成功")
    void getOrder_ShouldReturn200_WhenOrderExists() throws Exception {
        // Arrange
        OrderDTO orderDTO = OrderDTO.builder()
            .id(1L)
            .status("PAID")
            .amount(new BigDecimal("500"))
            .build();

        when(orderService.findById(1L)).thenReturn(orderDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/1")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())  // 打印請求/響應詳情（調試用）
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1))
            .andExpect(jsonPath("$.data.status").value("PAID"))
            .andExpect(jsonPath("$.data.amount").value(500));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{id} - 訂單不存在返回 404")
    void getOrder_ShouldReturn404_WhenOrderNotFound() throws Exception {
        when(orderService.findById(999L))
            .thenThrow(new ResourceNotFoundException("訂單不存在：999"));

        mockMvc.perform(get("/api/v1/orders/999")
                .header("Authorization", "Bearer " + validJwtToken))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.code").value("RESOURCE_001"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - 建立訂單成功，返回 201")
    void createOrder_ShouldReturn201_WhenRequestValid() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .address("台北市信義區")
            .items(List.of(new OrderItemRequest(1L, 2)))
            .build();

        OrderDTO createdOrder = OrderDTO.builder()
            .id(100L)
            .status("PENDING")
            .amount(new BigDecimal("200"))
            .build();

        when(orderService.create(any())).thenReturn(createdOrder);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())  // 201
            .andExpect(header().exists("Location"))  // Location 頭
            .andExpect(jsonPath("$.data.id").value(100))
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/orders - 請求體驗證失敗，返回 400")
    void createOrder_ShouldReturn400_WhenRequestInvalid() throws Exception {
        // 缺少必填欄位
        String invalidRequest = "{\"userId\": null}";

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.userId").exists());  // 欄位錯誤資訊
    }

    @Test
    @DisplayName("未帶 Token 訪問受保護接口，返回 401")
    void protectedEndpoint_ShouldReturn401_WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/orders/1"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

### 🟠 @DataJpaTest（Repository 層測試）

```java
@DataJpaTest  // 只加載 JPA 相關，使用 H2 記憶體資料庫
@DisplayName("訂單 Repository 測試")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;  // 專為測試設計的 EntityManager

    private Order savedOrder;

    @BeforeEach
    void setUp() {
        // 準備測試數據
        Order order = Order.builder()
            .userId(1L)
            .status(OrderStatus.PAID)
            .amount(new BigDecimal("500"))
            .address("台北市")
            .createdAt(LocalDateTime.now())
            .build();

        savedOrder = entityManager.persistAndFlush(order);
    }

    @Test
    @DisplayName("根據 ID 查詢訂單")
    void findById_ShouldReturnOrder_WhenExists() {
        Optional<Order> found = orderRepository.findById(savedOrder.getId());

        assertThat(found).isPresent()
                         .get()
                         .extracting(Order::getUserId, Order::getStatus)
                         .containsExactly(1L, OrderStatus.PAID);
    }

    @Test
    @DisplayName("根據用戶 ID 查詢訂單列表")
    void findByUserId_ShouldReturnOrders() {
        // 再建立一個同用戶的訂單
        Order order2 = Order.builder()
            .userId(1L)
            .status(OrderStatus.PENDING)
            .amount(new BigDecimal("200"))
            .build();
        entityManager.persistAndFlush(order2);

        List<Order> orders = orderRepository.findByUserId(1L);

        assertThat(orders).hasSize(2)
                          .extracting(Order::getUserId)
                          .containsOnly(1L);
    }

    @Test
    @DisplayName("根據狀態和金額範圍查詢")
    void findByStatusAndAmountBetween() {
        List<Order> orders = orderRepository
            .findByStatusAndAmountBetween(
                OrderStatus.PAID,
                new BigDecimal("100"),
                new BigDecimal("1000")
            );

        assertThat(orders).isNotEmpty()
                          .allMatch(o -> o.getStatus() == OrderStatus.PAID)
                          .allMatch(o ->
                              o.getAmount().compareTo(new BigDecimal("100")) >= 0 &&
                              o.getAmount().compareTo(new BigDecimal("1000")) <= 0
                          );
    }

    @Test
    @DisplayName("保存訂單 - 驗證自動填充欄位")
    void save_ShouldAutoFillAuditFields() {
        Order newOrder = Order.builder()
            .userId(2L)
            .status(OrderStatus.PENDING)
            .amount(new BigDecimal("300"))
            .build();

        Order saved = orderRepository.save(newOrder);
        entityManager.flush();
        entityManager.clear();  // 清除一級緩存，確保從 DB 讀取

        Order fromDb = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getCreatedAt()).isNotNull();
        assertThat(fromDb.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("分頁查詢")
    void findAll_WithPagination() {
        // 建立更多測試數據
        for (int i = 0; i < 15; i++) {
            entityManager.persist(Order.builder()
                .userId(1L)
                .status(OrderStatus.PAID)
                .amount(new BigDecimal(100 + i))
                .build());
        }
        entityManager.flush();

        Page<Order> page = orderRepository.findByUserId(
            1L, PageRequest.of(0, 10, Sort.by("amount").descending())
        );

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(16);
        // 驗證排序（金額由大到小）
        assertThat(page.getContent())
            .extracting(Order::getAmount)
            .isSortedAccordingTo(Comparator.reverseOrder());
    }
}
```

---

### 🔴 @SpringBootTest（整合測試）

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;  // 真實 HTTP 客戶端

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        authToken = "Bearer " + generateTestToken();
    }

    @Test
    @DisplayName("完整訂單建立流程")
    void createOrderFullFlow() {
        // 1. 建立訂單
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .address("台北市信義區")
            .items(List.of(new OrderItemRequest(1L, 2)))
            .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ApiResponse> createResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/v1/orders",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ApiResponse.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getHeaders().getLocation()).isNotNull();

        // 2. 查詢剛建立的訂單
        Long orderId = extractOrderId(createResponse.getHeaders().getLocation());
        ResponseEntity<ApiResponse> getResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/v1/orders/" + orderId,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiResponse.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 3. 驗證資料庫中的數據
        Optional<Order> savedOrder = orderRepository.findById(orderId);
        assertThat(savedOrder).isPresent()
                              .get()
                              .extracting(Order::getStatus)
                              .isEqualTo(OrderStatus.PENDING);
    }
}
```

---

## 5️⃣ Testcontainers（真實環境整合測試）

---

### 🔵 核心原理

```
問題：
  @DataJpaTest 使用 H2 記憶體資料庫
  但 H2 語法和 MySQL 不完全兼容！
  在 H2 通過的測試，在真實 MySQL 可能失敗！

Testcontainers 的解決方式：
  在測試執行時，自動啟動 Docker 容器（真實的 MySQL）
  測試完成後自動銷毀容器
  → 測試環境和生產環境完全一致！

前提：
  本地/CI 環境需要有 Docker
```

---

### 🟡 整合 MySQL + Redis

```java
// 方式一：每個測試類獨立容器（隔離性好，但慢）
@SpringBootTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test")
        .withInitScript("sql/init.sql");  // 初始化 SQL

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    // 動態設定數據源（Container 啟動後才知道端口）
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.redis.host",          redis::getHost);
        registry.add("spring.redis.port",
            () -> redis.getMappedPort(6379));
    }
}

// 方式二：共享容器（所有測試類共用，速度快）✅ 推薦

// 1. 定義基礎類（所有整合測試繼承它）
@SpringBootTest
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    // static + @Container → 所有測試共用同一個容器
    @Container
    protected static final MySQLContainer<?> MYSQL =
        new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);  // 開啟容器複用（更快！）

    @Container
    protected static final GenericContainer<?> REDIS =
        new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Container
    protected static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
            .withReuse(true);

    static {
        // Ryuk 容器管理（自動清理）
        Startables.deepStart(MYSQL, REDIS, KAFKA).join();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url",      MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);

        // Redis
        registry.add("spring.redis.host",
            REDIS::getHost);
        registry.add("spring.redis.port",
            () -> REDIS.getMappedPort(6379));

        // Kafka
        registry.add("spring.kafka.bootstrap-servers",
            KAFKA::getBootstrapServers);
    }
}

// 2. 繼承基礎類
@Testcontainers
class OrderServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderService    orderService;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("完整訂單建立 + 查詢流程（真實 MySQL）")
    void createAndQueryOrder_WithRealMySQL() {
        // Act
        CreateOrderRequest request = CreateOrderRequest.builder()
            .userId(1L)
            .address("台北市")
            .items(List.of(new OrderItemRequest(1L, 3)))
            .build();

        Order created = orderService.create(request);

        // 從真實 MySQL 查詢
        Order fromDb = orderRepository.findById(created.getId()).orElseThrow();

        assertThat(fromDb.getUserId()).isEqualTo(1L);
        assertThat(fromDb.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("Redis 緩存測試（真實 Redis）")
    void getOrder_ShouldUseCache_WithRealRedis() {
        // 第一次查詢（從 DB，並寫入 Redis）
        Order order = orderService.findById(1L);

        // 第二次查詢（應該從 Redis 取）
        Order cached = orderService.findById(1L);

        // 驗證 DB 只被查詢了一次
        verify(orderRepository, times(1)).findById(1L);
        assertThat(cached).isEqualTo(order);
    }
}
```

---

### 🟠 Kafka 整合測試

```java
@Testcontainers
class OrderEventIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Autowired
    private OrderEventConsumer orderEventConsumer;

    @Test
    @DisplayName("訂單事件 Kafka 生產者/消費者整合測試")
    void orderEvent_ShouldBeConsumedSuccessfully() throws Exception {
        // 使用 CountDownLatch 等待消費者處理
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<OrderCreatedEvent> receivedEvent = new AtomicReference<>();

        // Mock 消費者的處理邏輯（或驗證副作用）
        doAnswer(invocation -> {
            receivedEvent.set(invocation.getArgument(0));
            latch.countDown();
            return null;
        }).when(orderEventConsumer).handleOrderCreated(any());

        // 發送事件
        OrderCreatedEvent event = new OrderCreatedEvent(1L, 1L, new BigDecimal("500"));
        kafkaTemplate.send("order-created", event);

        // 等待消費者處理（最多 10 秒）
        boolean consumed = latch.await(10, TimeUnit.SECONDS);

        assertThat(consumed).isTrue();
        assertThat(receivedEvent.get().getOrderId()).isEqualTo(1L);
    }
}
```

---

### 🔴 Flyway 與測試資料管理

```java
// 使用 @Sql 執行測試 SQL
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Sql("/sql/insert-test-orders.sql")  // 測試前執行 SQL
    @Sql(scripts = "/sql/clean-orders.sql",
         executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)  // 測試後清理
    void findByUserId_ShouldReturnOrders() {
        List<Order> orders = orderRepository.findByUserId(1L);
        assertThat(orders).hasSize(3);
    }
}
```

```sql
-- src/test/resources/sql/insert-test-orders.sql
INSERT INTO orders (id, user_id, status, amount, created_at)
VALUES (1, 1, 'PAID',    500.00, '2024-01-01 10:00:00'),
       (2, 1, 'PENDING', 300.00, '2024-01-02 10:00:00'),
       (3, 1, 'PAID',    200.00, '2024-01-03 10:00:00'),
       (4, 2, 'PAID',    150.00, '2024-01-04 10:00:00');
```

---

## 6️⃣ TDD（測試驅動開發）

---

### 🔵 Red-Green-Refactor 循環

```
TDD 三步驟：

1. 🔴 Red（寫一個失敗的測試）
   先寫測試，此時生產代碼還不存在 → 測試必然失敗

2. 🟢 Green（寫最少的代碼讓測試通過）
   不追求完美，只求讓測試通過（可以先 hardcode！）

3. 🔵 Refactor（重構）
   在測試的保護下，改善代碼品質
   重構後重新執行測試，確保仍然通過

好處：
✅ 強迫思考 API 設計（先想使用方式）
✅ 測試覆蓋率自然很高
✅ 重構有安全網（測試保護）
✅ 設計更簡潔（只實作需要的功能）
```

---

### 🟡 TDD 完整實戰

```
需求：實作一個購物車服務
  - 可以加入商品
  - 可以移除商品
  - 可以計算總金額
  - 商品數量不能為負數
  - 同一商品加入多次應該累加數量
```

```java
// ═══════════════════════════════════════
// 第一輪：加入商品功能
// ═══════════════════════════════════════

// Step 1 🔴 Red - 寫失敗的測試
class ShoppingCartTest {

    @Test
    @DisplayName("加入商品後，購物車應有一個商品")
    void addItem_ShouldHaveOneItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), 1);

        assertThat(cart.getItemCount()).isEqualTo(1);
    }
}
// ❌ 此時 ShoppingCart 類不存在 → 編譯失敗（Red）

// Step 2 🟢 Green - 寫最少代碼
public class ShoppingCart {
    private List<CartItem> items = new ArrayList<>();

    public void addItem(String productId, String name,
                        BigDecimal price, int quantity) {
        items.add(new CartItem(productId, name, price, quantity));
    }

    public int getItemCount() {
        return items.size();
    }
}
// ✅ 測試通過（Green）

// Step 3 🔵 Refactor - 改善代碼
// 暫時沒有需要重構的地方

// ═══════════════════════════════════════
// 第二輪：計算總金額
// ═══════════════════════════════════════

// Step 1 🔴 Red
@Test
@DisplayName("計算購物車總金額")
void calculateTotal_ShouldReturnCorrectAmount() {
    ShoppingCart cart = new ShoppingCart();
    cart.addItem("P001", "iPhone",  new BigDecimal("30000"), 1);
    cart.addItem("P002", "AirPods", new BigDecimal("6000"),  2);

    assertThat(cart.getTotal())
        .isEqualByComparingTo(new BigDecimal("42000")); // 30000 + 6000*2
}
// ❌ getTotal() 方法不存在（Red）

// Step 2 🟢 Green
public BigDecimal getTotal() {
    return items.stream()
        .map(item -> item.getPrice()
            .multiply(new BigDecimal(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
}
// ✅ 測試通過（Green）

// ═══════════════════════════════════════
// 第三輪：數量不能為負數
// ═══════════════════════════════════════

// Step 1 🔴 Red
@Test
@DisplayName("加入商品數量為負數時，應拋出例外")
void addItem_ShouldThrowException_WhenQuantityNegative() {
    ShoppingCart cart = new ShoppingCart();

    assertThatThrownBy(() ->
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("商品數量不能小於 1");
}
// ❌ 目前不會拋出例外（Red）

// Step 2 🟢 Green
public void addItem(String productId, String name,
                    BigDecimal price, int quantity) {
    if (quantity < 1) {
        throw new IllegalArgumentException("商品數量不能小於 1");
    }
    items.add(new CartItem(productId, name, price, quantity));
}
// ✅ 測試通過（Green）

// ═══════════════════════════════════════
// 第四輪：同一商品累加數量
// ═══════════════════════════════════════

// Step 1 🔴 Red
@Test
@DisplayName("加入相同商品時，應累加數量而非新增")
void addItem_ShouldAccumulateQuantity_WhenSameProduct() {
    ShoppingCart cart = new ShoppingCart();
    cart.addItem("P001", "iPhone", new BigDecimal("30000"), 1);
    cart.addItem("P001", "iPhone", new BigDecimal("30000"), 2);

    assertThat(cart.getItemCount()).isEqualTo(1);  // 仍然是 1 種商品
    assertThat(cart.getTotal())
        .isEqualByComparingTo(new BigDecimal("90000"));  // 30000 * 3
}
// ❌ 目前會加入兩個商品（Red）

// Step 2 🟢 Green
public void addItem(String productId, String name,
                    BigDecimal price, int quantity) {
    if (quantity < 1) {
        throw new IllegalArgumentException("商品數量不能小於 1");
    }

    // 查找是否已有此商品
    Optional<CartItem> existing = items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst();

    if (existing.isPresent()) {
        existing.get().addQuantity(quantity); // 累加數量
    } else {
        items.add(new CartItem(productId, name, price, quantity));
    }
}
// ✅ 測試通過（Green）

// Step 3 🔵 Refactor
// 可以將查找邏輯提取為私有方法
private Optional<CartItem> findItem(String productId) {
    return items.stream()
        .filter(item -> item.getProductId().equals(productId))
        .findFirst();
}

// ═══════════════════════════════════════
// 最終完整的測試類
// ═══════════════════════════════════════

class ShoppingCartTest {

    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("加入商品後，購物車應有一個商品")
    void addItem_ShouldHaveOneItem() {
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), 1);
        assertThat(cart.getItemCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("計算購物車總金額")
    void calculateTotal_ShouldReturnCorrectAmount() {
        cart.addItem("P001", "iPhone",  new BigDecimal("30000"), 1);
        cart.addItem("P002", "AirPods", new BigDecimal("6000"),  2);
        assertThat(cart.getTotal())
            .isEqualByComparingTo(new BigDecimal("42000"));
    }

    @Test
    @DisplayName("加入商品數量為負數時，應拋出例外")
    void addItem_ShouldThrowException_WhenQuantityNegative() {
        assertThatThrownBy(() ->
            cart.addItem("P001", "iPhone", new BigDecimal("30000"), -1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("商品數量不能小於 1");
    }

    @Test
    @DisplayName("加入相同商品時，應累加數量而非新增")
    void addItem_ShouldAccumulateQuantity_WhenSameProduct() {
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), 1);
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), 2);
        assertThat(cart.getItemCount()).isEqualTo(1);
        assertThat(cart.getTotal())
            .isEqualByComparingTo(new BigDecimal("90000"));
    }

    @Test
    @DisplayName("移除商品後，購物車應為空")
    void removeItem_ShouldEmptyCart() {
        cart.addItem("P001", "iPhone", new BigDecimal("30000"), 1);
        cart.removeItem("P001");
        assertThat(cart.getItemCount()).isEqualTo(0);
        assertThat(cart.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("清空購物車")
    void clear_ShouldRemoveAllItems() {
        cart.addItem("P001", "iPhone",  new BigDecimal("30000"), 1);
        cart.addItem("P002", "AirPods", new BigDecimal("6000"),  1);
        cart.clear();
        assertThat(cart.getItemCount()).isEqualTo(0);
    }
}
```

---

### 🔴 測試覆蓋率

```xml
<!-- JaCoCo 測試覆蓋率（Maven 配置）-->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <!-- 設定覆蓋率門檻（未達到則建置失敗）-->
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>  <!-- 80% 行覆蓋率 -->
                            </limit>
                            <limit>
                                <counter>BRANCH</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>  <!-- 70% 分支覆蓋率 -->
                            </limit>
                        </limits>
                    </rule>
                </rules>
                <!-- 排除不需要測試的類 -->
                <excludes>
                    <exclude>**/config/**</exclude>
                    <exclude>**/dto/**</exclude>
                    <exclude>**/*Application.class</exclude>
                    <exclude>**/exception/**</exclude>
                </excludes>
            </configuration>
        </execution>
    </executions>
</plugin>
```

```
覆蓋率指標：

行覆蓋率（Line Coverage）：
  每一行程式碼是否被執行過

分支覆蓋率（Branch Coverage）：
  if/else 的每個分支是否都被測試過
  更嚴格，更能找到邊界條件的問題

覆蓋率目標：
  核心業務邏輯：> 80%
  工具類：     > 70%
  Controller： > 60%（通常用整合測試覆蓋）

⚠️ 覆蓋率不是越高越好！
  100% 覆蓋率但測試品質差（沒有 Assert）= 無意義
  重要的是測試邏輯是否正確，邊界條件是否覆蓋

執行：
  mvn test                          # 執行測試
  mvn test jacoco:report            # 生成覆蓋率報告
  target/site/jacoco/index.html     # 查看報告
```

---

### 🟠 測試最佳實踐

```java
// ✅ 1. 測試方法命名清晰（三段式）
// methodName_StateUnderTest_ExpectedBehavior
void createOrder_WhenInsufficientStock_ShouldThrowException() { }
void getUser_WhenUserExists_ShouldReturnUser() { }
void deleteOrder_WhenOrderNotFound_ShouldReturn404() { }

// ✅ 2. AAA 結構（Arrange-Act-Assert）
@Test
void someTest() {
    // Arrange（準備）
    User user = User.builder().id(1L).name("John").build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    // Act（執行）
    UserDTO result = userService.findById(1L);

    // Assert（驗證）
    assertThat(result.getName()).isEqualTo("John");
}

// ✅ 3. 一個測試只測一件事
// ❌ 不好（測試多件事）
@Test
void testEverything() {
    // 測試建立
    // 測試更新
    // 測試刪除
}

// ✅ 好（每個測試一件事）
@Test void createOrder_ShouldSucceed() { }
@Test void updateOrder_ShouldSucceed() { }
@Test void deleteOrder_ShouldSucceed() { }

// ✅ 4. 使用 @DisplayName 寫人類可讀的描述
@Test
@DisplayName("當用戶餘額不足時，支付應該失敗並返回 INSUFFICIENT_BALANCE 錯誤碼")
void pay_ShouldFail_WhenInsufficientBalance() { }

// ✅ 5. 測試邊界條件
// 0、負數、null、空字串、最大值、最小值

// ✅ 6. 不要在測試中寫業務邏輯
// ❌ 不好（在測試中重複計算）
@Test
void calculateTotal() {
    BigDecimal price    = new BigDecimal("100");
    int        quantity = 3;
    BigDecimal expected = price.multiply(new BigDecimal(quantity)); // 重複業務邏輯！
    assertThat(service.calculate(price, quantity)).isEqualTo(expected);
}

// ✅ 好（直接使用預期值）
@Test
void calculateTotal() {
    assertThat(service.calculate(new BigDecimal("100"), 3))
        .isEqualByComparingTo(new BigDecimal("300")); // 直接寫預期結果
}
```

---

## 📝 測試 完整總結

| 主題 | 核心要點 |
|------|---------|
| **測試金字塔** | 單元(70%) > 整合(20%) > E2E(10%)，越下層越快 |
| **FIRST 原則** | Fast / Independent / Repeatable / Self-validating / Timely |
| **JUnit 5** | @Test / @BeforeEach / @Nested / @ParameterizedTest |
| **AssertJ** | 流式斷言，比 JUnit 原生更可讀，支援軟斷言 |
| **參數化測試** | @CsvSource / @ValueSource / @MethodSource 減少重複代碼 |
| **Mock** | 完全模擬，隔離依賴 |
| **Stub** | 設定 Mock 方法的返回值 |
| **Spy** | 包裝真實對象，只 Mock 部分方法 |
| **verify()** | 驗證方法調用次數和順序 |
| **ArgumentCaptor** | 捕獲方法調用的參數，驗證傳入值 |
| **@WebMvcTest** | 只加載 Web 層，用 MockMvc 測試 Controller |
| **@DataJpaTest** | 只加載 JPA 層，用 H2 測試 Repository |
| **@SpringBootTest** | 完整應用上下文，真實整合測試 |
| **Testcontainers** | 用 Docker 啟動真實 MySQL/Redis/Kafka，測試更可靠 |
| **TDD** | Red（失敗）→ Green（通過）→ Refactor（重構）循環 |
| **覆蓋率** | JaCoCo，核心業務 > 80%，但品質比數字更重要 |
| **命名規範** | methodName_StateUnderTest_ExpectedBehavior |
| **AAA 結構** | Arrange（準備）/ Act（執行）/ Assert（驗證）|
