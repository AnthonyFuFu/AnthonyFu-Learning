# 📐 系統設計（System Design）完整深入教學

---

## 🗺️ 學習路線圖

```
系統設計
├── 1️⃣ 基礎理論（CAP / BASE / ACID）
├── 2️⃣ 分散式系統核心
│   ├── 一致性模型
│   ├── 分散式事務
│   └── 分散式鎖
├── 3️⃣ 高可用設計
│   ├── 限流（Rate Limiting）
│   ├── 熔斷（Circuit Breaker）
│   └── 降級（Fallback）
├── 4️⃣ 高效能設計
│   ├── 分庫分表
│   ├── 讀寫分離
│   └── 緩存設計
└── 5️⃣ 架構設計
    ├── 負載均衡
    ├── 微服務設計原則
    └── 消息隊列使用場景
```

---

## 1️⃣ 基礎理論

---

### 🔵 CAP 理論

CAP 理論指出，一個分散式系統**不可能同時滿足**以下三個特性，最多只能同時滿足兩個：

```
         C（Consistency 一致性）
              △
             / \
            /   \
           /     \
          /  只能  \
         /  選兩個  \
        /           \
       A─────────────P
（Availability）  （Partition Tolerance）
  可用性              分區容錯性
```

| 特性 | 說明 |
|------|------|
| **C（一致性）** | 所有節點在同一時間看到的資料是相同的 |
| **A（可用性）** | 每個請求都能在有限時間內收到回應（不保證是最新資料）|
| **P（分區容錯）** | 當網路分區（節點間通訊失敗）時，系統仍能繼續運作 |

> ⚠️ **重點**：在分散式系統中，**P（分區容錯）幾乎是必選的**（網路故障是常態），所以真正的選擇是 **CP 或 AP**。

#### 實際例子：

| 系統 | 選擇 | 說明 |
|------|------|------|
| **Zookeeper** | CP | 保證強一致性，犧牲可用性 |
| **Eureka** | AP | 保證高可用，犧牲強一致性 |
| **Redis 主從** | AP | 主節點掛掉，從節點繼續服務（可能有舊資料）|
| **MySQL 單機** | CA | 沒有分區，保證一致性和可用性 |

---

### 🟡 ACID vs BASE

#### ACID（傳統資料庫事務）

| 特性 | 說明 | 英文 |
|------|------|------|
| **A** | 原子性：全做或全不做 | Atomicity |
| **C** | 一致性：事務前後資料完整性 | Consistency |
| **I** | 隔離性：事務間互不干擾 | Isolation |
| **D** | 持久性：提交後永久保存 | Durability |

#### BASE（分散式系統的妥協方案）

| 特性 | 說明 | 英文 |
|------|------|------|
| **BA** | 基本可用：允許局部失敗，保證核心可用 | Basically Available |
| **S** | 軟狀態：允許中間狀態存在（資料短暫不一致）| Soft State |
| **E** | 最終一致性：資料最終會達到一致 | Eventually Consistent |

```
ACID  ←───────────────────────────→  BASE
強一致性                              最終一致性
高可靠                                高可用
傳統RDBMS                            NoSQL / 分散式系統
```

---

## 2️⃣ 分散式系統核心

---

### 🔵 一致性模型（從強到弱）

```
強一致性（Strong Consistency）
    │  寫入後立即可讀到最新值
    │  → 效能差，常見於 ZooKeeper
    ↓
線性一致性（Linearizability）
    │  操作看起來是原子的，有全局排序
    │  → Etcd 使用此模型
    ↓
順序一致性（Sequential Consistency）
    │  所有節點看到的操作順序相同
    ↓
因果一致性（Causal Consistency）
    │  有因果關係的操作保持順序
    ↓
最終一致性（Eventual Consistency）
    │  允許暫時不一致，最終一致
    │  → Redis 主從、Eureka 使用此模型
    ↓
弱一致性（Weak Consistency）
    讀到的不一定是最新的
```

---

### 🟠 分散式事務

**問題場景**：訂單服務扣款 + 庫存服務減庫存，如果其中一個失敗怎麼辦？

---

#### 方案一：2PC（Two-Phase Commit，兩階段提交）

```
              協調者（Coordinator）
             /         \
            /           \
       參與者A        參與者B
      （訂單服務）   （庫存服務）

Phase 1 - Prepare（準備階段）：
協調者 → 所有參與者：「你可以提交嗎？」
參與者 → 協調者：「可以 / 不可以」

Phase 2 - Commit（提交階段）：
全部回應「可以」→ 協調者發送 Commit
任一回應「不可以」→ 協調者發送 Rollback
```

| 優點 | 缺點 |
|------|------|
| 實作簡單 | **同步阻塞**：準備階段鎖定資源 |
| 強一致性 | **單點故障**：協調者掛掉全部卡死 |
| | **腦裂問題**：Commit 訊息部分送達 |

---

#### 方案二：TCC（Try-Confirm-Cancel）

```
三個步驟：
┌─────────┐    ┌──────────────┐    ┌────────────────┐
│  Try    │ →  │   Confirm    │ 或 │    Cancel      │
│ 預留資源 │    │ 確認提交資源  │    │ 釋放預留資源    │
└─────────┘    └──────────────┘    └────────────────┘
```

**電商下單範例：**

```
訂單服務：
  Try:     建立訂單（狀態：待確認）
  Confirm: 訂單狀態改為已確認
  Cancel:  刪除訂單

庫存服務：
  Try:     凍結庫存 N 個（可用庫存 -N，凍結庫存 +N）
  Confirm: 真正扣減庫存（凍結庫存 -N）
  Cancel:  解凍庫存（可用庫存 +N，凍結庫存 -N）
```

| 優點 | 缺點 |
|------|------|
| 無長時間鎖定 | 業務侵入性高（需實作三個方法）|
| 可控性強 | 程式碼量大 |
| | 需要保證冪等性 |

---

#### 方案三：SAGA 模式

```
將分散式事務拆成多個本地事務，每個都有對應的補償事務：

正向流程：T1 → T2 → T3 → T4

失敗補償：T1 → T2 → T3 失敗 → C2 → C1
                              ↑（補償事務，撤銷已完成的操作）
```

**範例：**

```
T1: 建立訂單        C1: 取消訂單
T2: 扣減庫存        C2: 恢復庫存
T3: 扣款            C3: 退款
T4: 發送通知        C4: 發送取消通知
```

| 優點 | 缺點 |
|------|------|
| 無鎖定、高效能 | 補償邏輯複雜 |
| 適合長流程業務 | 中間狀態暴露（非強一致）|
| Seata 支援此模式 | 需保證補償事務冪等 |

---

#### 方案四：本地消息表（最終一致性）

```
┌─────────────┐        ┌────────────────┐
│  訂單服務    │        │   庫存服務      │
│             │        │                │
│  ┌────────┐ │        │                │
│  │ 訂單表  │ │        │                │
│  └────────┘ │        │                │
│  ┌────────┐ │  MQ    │                │
│  │消息表   │─┼───────→│  消費消息      │
│  └────────┘ │        │  扣減庫存       │
│  （同一個DB  │        │                │
│   同一事務） │        │                │
└─────────────┘        └────────────────┘
```

**流程：**
1. 訂單服務：在**同一個事務**中寫訂單 + 寫消息表
2. 定時任務掃描未發送的消息，發到 MQ
3. 庫存服務消費 MQ，扣減庫存
4. 消費成功 → 標記消息已完成

---

#### 🏆 方案對比總結

| 方案 | 一致性 | 效能 | 複雜度 | 適用場景 |
|------|--------|------|--------|---------|
| 2PC | 強一致 | ⭐ | 中 | 少量服務，資料庫支援 |
| TCC | 最終一致 | ⭐⭐⭐ | 高 | 對一致性要求高，電商支付 |
| SAGA | 最終一致 | ⭐⭐⭐⭐ | 高 | 長流程業務 |
| 本地消息表 | 最終一致 | ⭐⭐⭐⭐ | 中 | 跨服務異步操作 |

---

### 🔴 分散式鎖

**問題**：多個服務實例同時修改同一份資料怎麼辦？

#### Redis 分散式鎖（推薦）

```java
// 錯誤做法 ❌ - 非原子操作
redisTemplate.opsForValue().setIfAbsent(key, value);
redisTemplate.expire(key, 30, TimeUnit.SECONDS);

// 正確做法 ✅ - 原子操作
String result = redisTemplate.execute((RedisCallback<String>) connection -> {
    return connection.set(
        key.getBytes(),
        value.getBytes(),
        Expiration.seconds(30),
        RedisStringCommands.SetOption.SET_IF_ABSENT
    );
});
```

**完整實作（含鎖續期）：**

```java
@Component
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_VALUE = UUID.randomUUID().toString();

    public boolean tryLock(String key, long timeout) {
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(key, LOCK_VALUE, timeout, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    public void unlock(String key) {
        String luaScript =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "   return redis.call('del', KEYS[1]) " +
            "else " +
            "   return 0 " +
            "end";

        redisTemplate.execute(
            new DefaultRedisScript<>(luaScript, Long.class),
            Collections.singletonList(key),
            LOCK_VALUE
        );
    }
}
```

> ⚠️ **為什麼用 Lua 腳本釋放鎖？**
> 因為「判斷 value 是否是自己的」和「刪除 key」這兩步必須是**原子操作**，否則可能刪掉別人的鎖！

#### Redisson（生產推薦）

```java
RLock lock = redisson.getLock("myLock");
try {
    if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
        // 業務邏輯
    }
} finally {
    lock.unlock();
}
```

---

## 3️⃣ 高可用設計

---

### 🔵 限流（Rate Limiting）

**目的**：保護系統不被突發流量打垮

#### 算法一：固定窗口算法

```
時間窗口 1分鐘，允許 100 次請求

[0s ─────────────────── 60s] → 最多 100 次
[60s ────────────────── 120s] → 最多 100 次

❌ 問題：臨界點突破
59s 來 100 次 + 61s 來 100 次 = 2 秒內 200 次！
```

#### 算法二：滑動窗口算法

```
當前時間往前推 1 分鐘的窗口是動態的：
現在是 90s，窗口是 [30s ~ 90s]
現在是 91s，窗口是 [31s ~ 91s]  ← 窗口滑動
```

#### 算法三：令牌桶（Token Bucket）✅ 推薦

```
令牌桶
┌─────────────────────────┐
│  🪙 🪙 🪙 🪙 🪙          │  ← 以固定速率生成令牌
│  容量：100 個令牌        │
└─────────────────────────┘
        ↓ 每次請求消耗 1 個令牌

優點：允許突發流量（桶中存有令牌時可以立即消費）
```

#### 算法四：漏桶（Leaky Bucket）

```
請求進來 →  ┌─────┐
           │桶    │ → 以固定速率處理請求
突發流量    │     │
           └──┬──┘
              │ 固定速率輸出

優點：輸出速率穩定
缺點：無法應對突發流量
```

#### Java 實作（Guava RateLimiter）：

```java
import com.google.common.util.concurrent.RateLimiter;

@RestController
public class OrderController {

    private final RateLimiter rateLimiter = RateLimiter.create(10.0);

    @PostMapping("/order")
    public ResponseEntity<?> createOrder() {
        if (!rateLimiter.tryAcquire()) {
            return ResponseEntity.status(429)
                .body("Too Many Requests，請稍後再試");
        }
        return ResponseEntity.ok("訂單創建成功");
    }
}
```

#### Redis + Lua 分散式限流：

```java
public boolean isAllowed(String userId, int maxRequest, int windowSeconds) {
    String key = "rate_limit:" + userId;
    long now = System.currentTimeMillis();
    long windowStart = now - windowSeconds * 1000L;

    String luaScript =
        "redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[1]) " +
        "local count = redis.call('ZCARD', KEYS[1]) " +
        "if count < tonumber(ARGV[2]) then " +
        "   redis.call('ZADD', KEYS[1], ARGV[3], ARGV[3]) " +
        "   redis.call('EXPIRE', KEYS[1], ARGV[4]) " +
        "   return 1 " +
        "else " +
        "   return 0 " +
        "end";

    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(luaScript, Long.class),
        Collections.singletonList(key),
        String.valueOf(windowStart),
        String.valueOf(maxRequest),
        String.valueOf(now),
        String.valueOf(windowSeconds)
    );

    return Long.valueOf(1).equals(result);
}
```

---

### 🟡 熔斷（Circuit Breaker）

**比喻**：像家裡的電路保險絲，電流過大時自動斷路，防止整個電路燒毀。

```
狀態機：

        失敗率 > 閾值
  閉合 ──────────────→ 斷開
（正常）                （熔斷）
  ↑                      │
  │     半開 ←───────────┘
  │    （探測）  等待超時
  │       │
  └───────┘
   測試請求成功
```

| 狀態 | 說明 |
|------|------|
| **Closed（閉合）** | 正常狀態，請求正常通過，統計失敗率 |
| **Open（斷開）** | 熔斷狀態，直接返回錯誤，不請求下游 |
| **Half-Open（半開）** | 允許少量請求測試，成功則恢復 Closed |

#### Resilience4j 實作：

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      inventoryService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
```

```java
@Service
public class OrderService {

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackDeductStock")
    public String deductStock(Long productId, Integer quantity) {
        return inventoryClient.deduct(productId, quantity);
    }

    public String fallbackDeductStock(Long productId, Integer quantity, Exception e) {
        log.error("庫存服務熔斷，productId={}", productId);
        return "庫存服務暫時不可用，訂單已排隊處理";
    }
}
```

---

### 🔴 降級（Fallback）策略

```
降級策略                    使用場景
─────────────────────────────────────────
返回預設值         →  搜索結果顯示熱門商品
讀取緩存           →  商品詳情從 Redis 讀取
異步處理           →  下單失敗放入 MQ 重試
返回靜態頁面       →  活動頁掛了返回靜態版本
直接返回錯誤       →  非核心功能直接告知不可用
```

---

## 4️⃣ 高效能設計

---

### 🔵 分庫分表

**什麼時候需要？**
> MySQL 單表超過 **1000 萬行** 或 **2GB** 時，查詢效能明顯下降，需要考慮分表。

#### 垂直切分 vs 水平切分

```
垂直分庫（按業務）：                  水平分表（按數據量）：
┌─────────────────┐                 ┌──────────────────────────────┐
│   訂單數據庫      │                 │  order_0   │  order_1        │
│  - order        │                 │ id % 2 = 0 │ id % 2 = 1      │
│  - order_item   │                 ├────────────┼─────────────────┤
└─────────────────┘                 │  id 0,2,4  │   id 1,3,5      │
┌─────────────────┐                 └──────────────────────────────┘
│   用戶數據庫      │
│  - user         │
│  - user_address │
└─────────────────┘
```

#### 常見分片策略：

```
Hash 取模：  userId % 4 → 均勻分布，但擴容難
Range 範圍：  id 1~100萬 → shard0，101~200萬 → shard1
一致性 Hash：  擴容方便，但實作複雜
```

#### ShardingSphere 配置範例：

```yaml
spring:
  shardingsphere:
    datasource:
      names: db0, db1
    rules:
      sharding:
        tables:
          t_order:
            actual-data-nodes: db$->{0..1}.t_order_$->{0..1}
            database-strategy:
              standard:
                sharding-column: user_id
                sharding-algorithm-name: db-inline
            table-strategy:
              standard:
                sharding-column: order_id
                sharding-algorithm-name: table-inline
        sharding-algorithms:
          db-inline:
            type: INLINE
            props:
              algorithm-expression: db$->{user_id % 2}
          table-inline:
            type: INLINE
            props:
              algorithm-expression: t_order_$->{order_id % 2}
```

---

### 🟡 緩存設計（三大問題）

#### 問題一：緩存穿透（Cache Penetration）

```
惡意請求不存在的 ID → Redis 沒有 → MySQL 也沒有 → 每次都打到 DB

解決方案：
1. 緩存空值（存 null，設短過期時間）
2. 布隆過濾器（Bloom Filter）← 推薦
```

```java
RBloomFilter<Long> bloomFilter = redisson.getBloomFilter("product:ids");
bloomFilter.tryInit(1000000L, 0.01);
bloomFilter.add(productId);

public Product getProduct(Long id) {
    if (!bloomFilter.contains(id)) {
        return null;
    }
    // 查 Redis → 查 DB
}
```

#### 問題二：緩存雪崩（Cache Avalanche）

```
大量緩存同時過期 → 所有請求打到 DB → DB 崩潰

解決方案：
1. 過期時間加隨機數（30分鐘 + Random(0~5分鐘)）
2. 熱點數據設為永不過期（邏輯過期）
3. Redis 叢集，避免單點
4. 熔斷降級保護 DB
```

```java
long expireTime = 1800 + new Random().nextInt(300);
redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
```

#### 問題三：緩存擊穿（Cache Breakdown）

```
熱點 Key 過期的瞬間，大量並發請求同時湧入 DB

解決方案：
1. 互斥鎖（同一時間只有一個請求重建緩存）
2. 邏輯過期（不設 TTL，在 value 中存過期時間）
```

```java
public Product getProductWithMutex(Long id) {
    String cacheKey = "product:" + id;
    Product product = getFromCache(cacheKey);
    if (product != null) return product;

    String lockKey = "lock:product:" + id;
    boolean locked = tryLock(lockKey);

    try {
        if (locked) {
            product = productMapper.selectById(id);
            setCache(cacheKey, product, 30, TimeUnit.MINUTES);
        } else {
            Thread.sleep(50);
            return getProductWithMutex(id);
        }
    } finally {
        if (locked) unlock(lockKey);
    }

    return product;
}
```

---

## 5️⃣ 架構設計

---

### 🔵 負載均衡

| 算法 | 說明 | 適用場景 |
|------|------|---------|
| **輪詢（Round Robin）** | 依序分配 | 服務性能相近 |
| **加權輪詢** | 按性能分配比例 | 服務性能不均 |
| **最少連接** | 分給連接數最少的 | 請求處理時間不均 |
| **IP Hash** | 根據 IP 固定分配 | 需要 Session 黏滯 |
| **隨機** | 隨機選擇 | 簡單場景 |

---

### 🟡 微服務設計原則

| 原則 | 說明 |
|------|------|
| **單一職責** | 每個服務只做一件事 |
| **服務自治** | 每個服務有自己的資料庫 |
| **接口穩定** | API 版本向後兼容 |
| **失敗設計** | 設計時假設依賴服務會失敗 |
| **冪等設計** | 重複請求不產生副作用 |

#### 冪等性實作：

```java
@PostMapping("/order")
public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
    String idempotencyKey = "order:idem:" + request.getRequestId();

    Boolean isFirst = redisTemplate.opsForValue()
        .setIfAbsent(idempotencyKey, "processing", 24, TimeUnit.HOURS);

    if (!Boolean.TRUE.equals(isFirst)) {
        return ResponseEntity.ok(getOrderResult(request.getRequestId()));
    }

    Order order = orderService.create(request);
    redisTemplate.opsForValue().set(idempotencyKey, order.getId().toString());
    return ResponseEntity.ok(order);
}
```

---

### 🟠 消息隊列使用場景

```
1. 非同步解耦：
   訂單服務 → [MQ] → 通知服務（不需要同步等待）

2. 流量削峰：
   秒殺：10萬請求 → [MQ緩衝] → 服務慢慢消費

3. 最終一致性：
   本地消息表 → [MQ] → 其他服務（分散式事務）

4. 事件驅動：
   用戶下單事件 → [MQ] → 積分服務/推薦服務/物流服務
```

---

## 🎯 系統設計面試答題框架

```
Step 1：明確需求
  ├── 功能需求：支持哪些功能？
  └── 非功能需求：QPS、並發量、可用性要求

Step 2：估算規模
  ├── 每日 DAU 是多少？
  ├── 讀寫比例？
  └── 需要多少台服務器？

Step 3：高層設計
  └── 畫出核心架構圖

Step 4：深入設計
  ├── 資料庫設計
  ├── 核心流程設計
  └── 關鍵問題解決方案

Step 5：討論瓶頸
  └── 如何擴展？如何解決單點問題？
```

---

## 📝 系統設計總結

| 主題 | 核心知識點 |
|------|-----------|
| **CAP/BASE** | 分散式不可能三角，BASE 是妥協方案 |
| **分散式事務** | 2PC（強一致）/ TCC / SAGA / 本地消息表（最終一致）|
| **分散式鎖** | Redis + Lua 保證原子性，Redisson Watch Dog 自動續期 |
| **限流** | 令牌桶（允許突發）/ 漏桶（穩定輸出）/ 滑動窗口 |
| **熔斷** | Closed → Open → Half-Open 狀態機 |
| **緩存** | 穿透（布隆過濾器）/ 雪崩（隨機 TTL）/ 擊穿（互斥鎖）|
| **分庫分表** | 垂直切分（按業務）/ 水平切分（按數據量）|
