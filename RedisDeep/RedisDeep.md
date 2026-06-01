# 🔴 Redis 深度 完整深入教學

---

## 🗺️ 學習路線圖

```
Redis 深度
├── 1️⃣ 五大資料結構 + 底層編碼
│   ├── String / List / Hash / Set / ZSet
│   └── ZipList / SkipList / Dict / IntSet
├── 2️⃣ 持久化機制
│   ├── RDB（快照）
│   ├── AOF（追加日誌）
│   └── 混合模式（推薦）
├── 3️⃣ 記憶體淘汰策略（8 種）
├── 4️⃣ 分散式鎖
│   ├── SET NX + Lua Script
│   └── Redisson（生產推薦）
├── 5️⃣ 緩存三大問題
│   ├── 緩存穿透
│   ├── 緩存擊穿
│   └── 緩存雪崩
├── 6️⃣ Redis Cluster 架構
│   ├── 16384 槽位分配
│   └── 一致性 Hash
├── 7️⃣ 效能優化
│   ├── Pipeline
│   └── Lua Script
└── 8️⃣ 熱 Key / 大 Key 處理
```

---

## 1️⃣ 五大資料結構 + 底層編碼

---

### 🔵 為什麼要了解底層編碼？

```
Redis 的每種資料結構，會根據「元素數量」和「元素大小」
自動選擇不同的底層編碼（encoding）
→ 小數據用壓縮結構（省記憶體）
→ 大數據用效能結構（速度快）

面試常問：
  "ZSet 底層是什麼？"
  → 少量用 ZipList，大量用 SkipList + Dict
  
了解底層 → 才能解釋為什麼 Redis 這麼快、這麼省記憶體
```

---

### 🟡 String（字串）

```
底層編碼（三種）：

int：
  整數值，且範圍在 long 內
  如：SET counter 100
  → 直接用 long 存，不需要字串物件

embstr（嵌入式字串）：
  字串長度 ≤ 44 bytes
  RedisObject + SDS 一次性分配連續記憶體
  → 記憶體局部性好，只需一次 malloc

raw（普通 SDS）：
  字串長度 > 44 bytes
  RedisObject 和 SDS 分開分配
  → 兩次 malloc

SDS（Simple Dynamic String）結構：
  ┌──────┬──────┬──────┬─────────────────┐
  │ len  │ alloc│ flags│      buf[]       │
  │（已用）│（容量）│（類型）│   實際字串內容    │
  └──────┴──────┴──────┴─────────────────┘

  vs C 字串的優勢：
  ✅ O(1) 取得長度（直接讀 len，不用遍歷）
  ✅ 二進制安全（可以存 \0）
  ✅ 預分配空間，減少記憶體重分配次數
```

```java
// String 常用場景
@Autowired
private StringRedisTemplate redisTemplate;

// 1. 緩存（最常用）
redisTemplate.opsForValue().set("user:1", json, Duration.ofMinutes(30));
String cached = redisTemplate.opsForValue().get("user:1");

// 2. 計數器（原子操作）
Long count = redisTemplate.opsForValue().increment("article:100:views");
redisTemplate.opsForValue().increment("api:rate:user:1", 1);

// 3. 分散式 Session
redisTemplate.opsForValue().set(
    "session:" + sessionId,
    userJson,
    Duration.ofHours(2)
);

// 4. 冪等性控制（SET NX）
Boolean isNew = redisTemplate.opsForValue()
    .setIfAbsent("idempotency:" + requestId, "1", Duration.ofHours(24));
if (!Boolean.TRUE.equals(isNew)) {
    return "重複請求";
}
```

---

### 🟠 List（列表）

```
底層編碼：

listpack（JDK 7.0+ 替代 ZipList）：
  元素數量 ≤ 128 個 且 每個元素 ≤ 64 bytes
  緊湊的連續記憶體結構

quicklist（預設）：
  雙向鏈表，每個節點是一個 listpack
  兼顧記憶體和效能

操作複雜度：
  LPUSH / RPUSH：O(1)  → 頭尾插入
  LPOP / RPOP：  O(1)  → 頭尾刪除
  LINDEX：       O(n)  → 按索引查詢（需遍歷）
  LRANGE：       O(n)  → 範圍查詢
```

```java
// List 常用場景

// 1. 消息佇列（簡單版）
// 生產者
redisTemplate.opsForList().leftPush("task:queue", taskJson);

// 消費者（阻塞等待，BLPOP）
List<String> result = redisTemplate.opsForList()
    .rightPop("task:queue", Duration.ofSeconds(30)); // 阻塞 30 秒

// 2. 最新動態列表（朋友圈 / 訊息流）
// 只保留最新 100 條
redisTemplate.opsForList().leftPush("user:1:feed", postJson);
redisTemplate.opsForList().trim("user:1:feed", 0, 99); // 只保留 0~99

// 3. 操作歷史（最近瀏覽）
redisTemplate.opsForList().leftPush("user:1:history", productId);
redisTemplate.opsForList().trim("user:1:history", 0, 9); // 只保留最近 10 條
List<String> history = redisTemplate.opsForList().range("user:1:history", 0, -1);
```

---

### 🔴 Hash（雜湊）

```
底層編碼：

listpack：
  元素數量 ≤ 128 個 且 每個 field/value ≤ 64 bytes

hashtable：
  超過閾值自動轉換
  拉鏈法解決 Hash 衝突（和 Java HashMap 類似）
  負載因子 > 1 時觸發 rehash（漸進式，不阻塞）

漸進式 rehash：
  同時維護 ht[0]（舊）和 ht[1]（新）
  每次讀寫操作，順便遷移一個桶
  遷移完成後，ht[0] = ht[1]
  → 避免一次性 rehash 導致服務卡頓！
```

```java
// Hash 常用場景

// 1. 存儲物件（避免 JSON 序列化開銷）
Map<String, String> userFields = new HashMap<>();
userFields.put("name",  "Alice");
userFields.put("email", "alice@example.com");
userFields.put("age",   "25");
redisTemplate.opsForHash().putAll("user:1", userFields);

// 單獨更新某個欄位（不需要讀整個物件）
redisTemplate.opsForHash().put("user:1", "age", "26");

// 讀取特定欄位
String name = (String) redisTemplate.opsForHash().get("user:1", "name");

// 2. 購物車（Hash field = productId, value = quantity）
redisTemplate.opsForHash().increment("cart:user:1", "product:100", 1);
redisTemplate.opsForHash().increment("cart:user:1", "product:200", 2);
Map<Object, Object> cart = redisTemplate.opsForHash().entries("cart:user:1");
// 刪除商品
redisTemplate.opsForHash().delete("cart:user:1", "product:100");

// 3. 計數器集合
redisTemplate.opsForHash().increment("article:100:stats", "views",   1);
redisTemplate.opsForHash().increment("article:100:stats", "likes",   1);
redisTemplate.opsForHash().increment("article:100:stats", "comments",1);
```

---

### 🟢 Set（集合）

```
底層編碼：

intset：
  所有元素都是整數 且 元素數量 ≤ 512
  有序整數陣列，二分搜尋

hashtable：
  超過閾值自動轉換
  value 都為 null

特點：
  元素不重複
  支援集合運算（交集 / 並集 / 差集）
  時間複雜度 O(1) 查詢
```

```java
// Set 常用場景

// 1. 點讚（去重）
redisTemplate.opsForSet().add("article:100:likes", userId.toString());
Long likeCount = redisTemplate.opsForSet().size("article:100:likes");
Boolean hasLiked = redisTemplate.opsForSet().isMember("article:100:likes", userId.toString());
// 取消點讚
redisTemplate.opsForSet().remove("article:100:likes", userId.toString());

// 2. 共同好友（交集）
Set<String> friends1   = redisTemplate.opsForSet().members("user:1:friends");
Set<String> friends2   = redisTemplate.opsForSet().members("user:2:friends");
Set<String> commonFriends = redisTemplate.opsForSet()
    .intersect("user:1:friends", "user:2:friends");

// 3. 抽獎（隨機取元素）
// 不移除（可重複中獎）
String winner = redisTemplate.opsForSet().randomMember("lottery:pool");
// 移除（不可重複中獎）
String winner2 = redisTemplate.opsForSet().pop("lottery:pool");

// 4. UV 統計（獨立訪客，用 HyperLogLog 更省記憶體）
redisTemplate.opsForHyperLogLog().add("page:home:uv", userId.toString());
Long uv = redisTemplate.opsForHyperLogLog().size("page:home:uv");
// HyperLogLog：記憶體固定 12KB，誤差 < 0.81%（不需要精確時使用）
```

---

### 🟣 ZSet（有序集合）⭐ 最重要

```
底層編碼：

listpack：
  元素數量 ≤ 128 個 且 每個元素 ≤ 64 bytes

skiplist + dict（跳表 + 字典）：
  超過閾值自動轉換

為什麼要兩個結構？
  dict：   member → score（O(1) 查詢某個 member 的 score）
  skiplist：按 score 排序（O(log n) 範圍查詢）

跳表（SkipList）結構：

level 3: 1 ──────────────────────────────→ 9
level 2: 1 ──────────→ 5 ──────────────→ 9
level 1: 1 → 2 → 3 → 5 → 6 → 7 → 8 → 9

查詢從最高層開始，快速跳過不需要的節點
時間複雜度：O(log n)

為什麼用跳表而不是紅黑樹？
  ✅ 範圍查詢更簡單（跳表葉節點連結）
  ✅ 實作更簡單（紅黑樹複雜）
  ✅ 並發友好（跳表局部修改，紅黑樹旋轉影響範圍大）
```

```java
// ZSet 常用場景

// 1. 排行榜（score = 分數 / 積分）
// 新增或更新分數
redisTemplate.opsForZSet().add("game:leaderboard", "player:Alice", 9500.0);
redisTemplate.opsForZSet().incrementScore("game:leaderboard", "player:Bob", 100.0);

// 取前 10 名（由高到低）
Set<ZSetOperations.TypedTuple<String>> top10 =
    redisTemplate.opsForZSet()
        .reverseRangeWithScores("game:leaderboard", 0, 9);

// 查詢某玩家排名（0-based）
Long rank = redisTemplate.opsForZSet()
    .reverseRank("game:leaderboard", "player:Alice");

// 2. 延遲佇列（score = 執行時間戳）
long executeAt = System.currentTimeMillis() + 30_000; // 30 秒後執行
redisTemplate.opsForZSet().add("delay:queue", taskJson, executeAt);

// 定期輪詢（每秒查詢到期任務）
@Scheduled(fixedRate = 1000)
public void pollDelayQueue() {
    long now = System.currentTimeMillis();
    Set<String> dueTasks = redisTemplate.opsForZSet()
        .rangeByScore("delay:queue", 0, now);
    // 處理到期任務並移除
    dueTasks.forEach(task -> {
        processTask(task);
        redisTemplate.opsForZSet().remove("delay:queue", task);
    });
}

// 3. 時間軸（score = 時間戳）
redisTemplate.opsForZSet().add(
    "user:1:timeline",
    postJson,
    (double) System.currentTimeMillis()
);
// 查詢最新 20 條
Set<String> timeline = redisTemplate.opsForZSet()
    .reverseRange("user:1:timeline", 0, 19);
```

---

## 2️⃣ 持久化機制

---

### 🔵 RDB（Redis Database Backup）

```
RDB = 快照（Snapshot）
  在指定時間點，將記憶體中的資料以二進制格式存到 .rdb 檔案

觸發方式：
  ① save 命令（同步，阻塞 Redis，生產禁用）
  ② bgsave 命令（非同步，fork 子程序，不阻塞）
  ③ 自動觸發（redis.conf 配置）：
     save 900 1     → 900 秒內有 1 個 key 修改
     save 300 10    → 300 秒內有 10 個 key 修改
     save 60 10000  → 60 秒內有 10000 個 key 修改

bgsave 流程（重要！）：
  ① Redis 父程序 fork() 出子程序
  ② 子程序把記憶體快照寫入臨時 .rdb 檔案
  ③ 寫完後替換舊的 .rdb 檔案
  ④ 父程序繼續處理請求（不阻塞！）

Copy-On-Write（寫時複製）：
  fork 後父子程序共享記憶體頁
  父程序修改某頁時，才複製該頁給子程序
  → 大多數時候記憶體不需要複製（節省記憶體）

優點：
  ✅ 文件小，恢復速度快
  ✅ 適合做備份（每天定時 bgsave）
  ✅ 對 Redis 效能影響小

缺點：
  ❌ 兩次快照之間的數據可能丟失（最多丟幾分鐘）
  ❌ fork 時若記憶體很大，fork 本身會短暫阻塞
```

---

### 🟡 AOF（Append Only File）

```
AOF = 追加日誌
  每次寫操作都追加到 .aof 文件（類似 MySQL Binlog）

AOF 同步策略（appendfsync）：
  always：   每次寫操作都 fsync（最安全，最慢）
  everysec： 每秒 fsync 一次（推薦，最多丟 1 秒資料）✅
  no：       由 OS 決定何時 fsync（最快，不可控）

AOF 重寫（rewrite）：
  問題：AOF 文件會越來越大（含大量過期命令）
        SET k 1 → SET k 2 → SET k 3 → 只有最後一條有意義

  解決：AOF 重寫
  ① bgrewriteaof 命令（或自動觸發）
  ② fork 子程序，掃描當前記憶體，生成最小化的 AOF
  ③ 重寫期間的新命令寫入「AOF 重寫緩衝區」
  ④ 子程序完成後，緩衝區追加到新 AOF
  ⑤ 替換舊 AOF 文件

自動觸發配置：
  auto-aof-rewrite-percentage 100  → AOF 比上次增長 100%
  auto-aof-rewrite-min-size 64mb   → AOF 最小 64MB 才觸發

優點：
  ✅ 數據更完整（最多丟 1 秒）
  ✅ 文本格式，可讀性強

缺點：
  ❌ 文件比 RDB 大
  ❌ 恢復速度比 RDB 慢
```

---

### 🟠 混合持久化（推薦 ✅）

```
Redis 4.0 引入，結合 RDB 和 AOF 的優點

aof-use-rdb-preamble yes（開啟混合模式）

AOF 重寫時的格式：
  ┌─────────────────┬──────────────────────────┐
  │   RDB 格式快照   │  AOF 格式的增量命令        │
  │（重寫時的記憶體）  │（重寫完成後的新命令）       │
  └─────────────────┴──────────────────────────┘

恢復時：
  ① 先快速載入 RDB 部分（快！）
  ② 再重放 AOF 增量部分（少量）
  → 兼顧速度和完整性

生產建議：
  ✅ 開啟 AOF（appendonly yes）
  ✅ 開啟混合模式（aof-use-rdb-preamble yes）
  ✅ AOF 同步策略用 everysec
  ✅ 定期手動 bgsave 做備份
```

---

## 3️⃣ 記憶體淘汰策略（8 種）

---

```
當 Redis 記憶體達到 maxmemory 上限時，
根據 maxmemory-policy 決定如何騰出空間：

針對設有 TTL（過期時間）的 key：
  volatile-lru    → 淘汰最近最少使用的 key（LRU）
  volatile-lfu    → 淘汰使用頻率最低的 key（LFU）
  volatile-random → 隨機淘汰
  volatile-ttl    → 淘汰 TTL 最短的 key（即將過期）

針對所有 key：
  allkeys-lru     → 從所有 key 中淘汰 LRU ✅（最常用）
  allkeys-lfu     → 從所有 key 中淘汰 LFU
  allkeys-random  → 從所有 key 中隨機淘汰

不淘汰：
  noeviction      → 記憶體滿時直接返回錯誤（預設，生產不推薦）

LRU vs LFU：
  LRU（Least Recently Used）：
    按最近使用時間，淘汰最久沒用的
    問題：偶爾用一次的 key 會佔據位置

  LFU（Least Frequently Used）：
    按使用頻率，淘汰最少用的
    更精準，但實作更複雜

選擇建議：
  作為緩存使用 → allkeys-lru（最常見）
  有明確 TTL  → volatile-lru
  熱點數據明顯 → allkeys-lfu
```

```yaml
# redis.conf 配置
maxmemory 4gb
maxmemory-policy allkeys-lru
```

---

## 4️⃣ 分散式鎖

---

### 🔵 SET NX 實作（基礎版）

```java
// ❌ 錯誤方式一：SETNX + EXPIRE 兩步驟（非原子！）
redisTemplate.opsForValue().setIfAbsent("lock:order", "1"); // 成功
// 如果這裡程序崩潰 → 鎖永遠不會過期！（死鎖）
redisTemplate.expire("lock:order", 30, TimeUnit.SECONDS);

// ✅ 正確方式：SET NX EX 原子命令
@Service
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String LOCK_PREFIX = "lock:";

    public boolean tryLock(String lockKey, String requestId, long expireSeconds) {
        String key = LOCK_PREFIX + lockKey;
        // SET key value NX EX seconds → 原子操作
        Boolean success = redisTemplate.opsForValue()
            .setIfAbsent(key, requestId, Duration.ofSeconds(expireSeconds));
        return Boolean.TRUE.equals(success);
    }

    public boolean unlock(String lockKey, String requestId) {
        String key = LOCK_PREFIX + lockKey;
        // ⚠️ 必須用 Lua Script 保證原子性！
        // 防止誤刪其他請求的鎖
        String script =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

        Long result = redisTemplate.execute(
            new DefaultRedisScript<>(script, Long.class),
            List.of(key),
            requestId
        );
        return Long.valueOf(1L).equals(result);
    }
}

// 使用範例
@Service
@RequiredArgsConstructor
public class OrderService {

    private final RedisDistributedLock lock;

    public void createOrder(Long userId) {
        String lockKey   = "order:user:" + userId;
        String requestId = UUID.randomUUID().toString(); // 唯一標識，防止誤刪

        boolean locked = lock.tryLock(lockKey, requestId, 30);
        if (!locked) {
            throw new BusinessException("系統繁忙，請稍後再試");
        }

        try {
            // 業務邏輯（同一用戶同一時間只能有一個請求進入）
            doCreateOrder(userId);
        } finally {
            lock.unlock(lockKey, requestId); // 釋放鎖
        }
    }
}
```

---

### 🟡 Redisson（生產推薦）⭐

```java
// Redisson 解決了 SET NX 的問題：
// ✅ 看門狗機制（自動續期，防止業務還沒完成鎖就過期）
// ✅ 可重入鎖（同一線程可以多次獲取）
// ✅ 公平鎖
// ✅ 紅鎖（RedLock，多節點一致性）

// pom.xml
// <dependency>
//     <groupId>org.redisson</groupId>
//     <artifactId>redisson-spring-boot-starter</artifactId>
//     <version>3.24.3</version>
// </dependency>

@Service
@RequiredArgsConstructor
public class OrderService {

    private final RedissonClient redissonClient;

    // 基本使用
    public void createOrder(Long userId) {
        RLock lock = redissonClient.getLock("lock:order:user:" + userId);

        boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
        // tryLock(waitTime, leaseTime, unit)
        // waitTime:  最多等待 3 秒獲取鎖
        // leaseTime: 鎖定最長 30 秒（-1 = 啟用看門狗）

        if (!locked) {
            throw new BusinessException("系統繁忙，請稍後再試");
        }

        try {
            doCreateOrder(userId);
        } finally {
            if (lock.isHeldByCurrentThread()) { // 確保是自己持有的鎖
                lock.unlock();
            }
        }
    }

    // 看門狗機制（leaseTime = -1 時自動啟用）
    public void longRunningTask() {
        RLock lock = redissonClient.getLock("lock:long:task");
        lock.lock(); // 不指定過期時間，看門狗每 10 秒自動續期 30 秒
        try {
            // 耗時很長的任務，不用擔心鎖過期
            processLargeData();
        } finally {
            lock.unlock();
        }
    }

    // 紅鎖（RedLock）- 多個 Redis 節點
    public void redLockExample() {
        RLock lock1 = redissonClient1.getLock("lock:order");
        RLock lock2 = redissonClient2.getLock("lock:order");
        RLock lock3 = redissonClient3.getLock("lock:order");

        // 需要多數節點（3/5）成功才算獲取到鎖
        RLock redLock = redissonClient.getRedLock(lock1, lock2, lock3);

        redLock.lock();
        try {
            // 即使某個 Redis 節點宕機，鎖仍然有效
        } finally {
            redLock.unlock();
        }
    }
}
```

```
看門狗（Watchdog）原理：

lock.lock()（不設過期時間）
  ↓
默認鎖定 30 秒（lockWatchdogTimeout）
  ↓
啟動後台線程（每 10 秒檢查一次）
  ↓
如果鎖仍被持有 → 自動延長過期時間到 30 秒
  ↓
業務完成，unlock() → 取消看門狗

效果：
  業務執行 5 秒 → 鎖還有 25 秒，不需要續期
  業務執行 25 秒 → 看門狗在第 10s、20s 各續期一次
  業務異常崩潰 → 看門狗線程也消失 → 鎖 30 秒後自動過期 ✅
```

---

## 5️⃣ 緩存三大問題

---

### 🔴 緩存穿透（Cache Penetration）

```
問題：
  查詢一個「不存在的 key」（如：id = -1）
  Redis 沒有 → 打到 DB → DB 也沒有
  大量這樣的請求 → DB 被打垮！
  （可能是惡意攻擊）

解決方案：

① 緩存空值（最簡單）
  查詢結果為 null → 也存入 Redis（value = 空字串）
  設置短 TTL（如：5 分鐘）
  缺點：大量不同的 id → 佔用大量 Redis 記憶體

② 布隆過濾器（Bloom Filter）⭐ 推薦
  預先把所有有效 id 加入布隆過濾器
  請求到來先查布隆過濾器
  不在 → 直接返回（不查 DB）
  在 → 查 Redis → 查 DB
  
  布隆過濾器特性：
  ✅ 說「不存在」→ 100% 不存在
  ⚠️ 說「存在」→ 可能誤判（有 Hash 碰撞）
  記憶體極省（1億個元素約 125MB）
```

```java
// 方案一：緩存空值
public User getUser(Long id) {
    String key = "user:" + id;
    String cached = redisTemplate.opsForValue().get(key);

    if (cached != null) {
        if ("null".equals(cached)) {
            return null; // 緩存了空值
        }
        return JSON.parseObject(cached, User.class);
    }

    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
        // 緩存空值，TTL 短一些
        redisTemplate.opsForValue().set(key, "null", Duration.ofMinutes(5));
    } else {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(user),
            Duration.ofMinutes(30));
    }
    return user;
}

// 方案二：布隆過濾器（Redisson）
@Component
public class UserBloomFilter {

    @Autowired
    private RedissonClient redissonClient;

    private RBloomFilter<Long> bloomFilter;

    @PostConstruct
    public void init() {
        bloomFilter = redissonClient.getBloomFilter("bloom:user");
        // 預計 1 億個元素，誤判率 0.01%
        bloomFilter.tryInit(100_000_000L, 0.001);

        // 初始化：把所有有效 userId 加入布隆過濾器
        userRepository.findAllIds().forEach(bloomFilter::add);
    }

    public boolean mightExist(Long userId) {
        return bloomFilter.contains(userId);
    }

    // 新增用戶時同步更新
    public void addUser(Long userId) {
        bloomFilter.add(userId);
    }
}

// 使用
public User getUser(Long id) {
    if (!bloomFilter.mightExist(id)) {
        return null; // 布隆過濾器說不存在 → 直接返回
    }
    // 正常查詢流程...
}
```

---

### 🟡 緩存擊穿（Cache Breakdown）

```
問題：
  一個「熱點 key」突然過期
  大量請求同時打到 DB（同一時刻）
  DB 壓力驟增

解決方案：

① 互斥鎖（Mutex Lock）
  只讓一個請求重建緩存
  其他請求等待或返回舊資料

② 邏輯過期（不設 TTL，手動控制）
  緩存永不過期
  value 中加入 expire_time 欄位
  讀到過期時，非同步更新緩存（立即返回舊資料）
```

```java
// 方案一：互斥鎖
public Product getProduct(Long id) {
    String key     = "product:" + id;
    String lockKey = "lock:rebuild:" + id;

    // 1. 先查緩存
    String cached = redisTemplate.opsForValue().get(key);
    if (cached != null) {
        return JSON.parseObject(cached, Product.class);
    }

    // 2. 緩存未命中，嘗試獲取鎖
    String requestId = UUID.randomUUID().toString();
    boolean locked = Boolean.TRUE.equals(
        redisTemplate.opsForValue()
            .setIfAbsent(lockKey, requestId, Duration.ofSeconds(3))
    );

    try {
        if (locked) {
            // 3. 獲得鎖 → 重建緩存
            // 再查一次（雙重檢查，防止其他線程已經重建）
            cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return JSON.parseObject(cached, Product.class);
            }

            Product product = productRepository.findById(id).orElseThrow();
            redisTemplate.opsForValue().set(key, JSON.toJSONString(product),
                Duration.ofMinutes(30));
            return product;
        } else {
            // 4. 未獲得鎖 → 短暫等待後重試
            Thread.sleep(50);
            return getProduct(id); // 遞迴重試
        }
    } finally {
        // 5. 釋放鎖（Lua 原子操作）
        if (locked) {
            unlock(lockKey, requestId);
        }
    }
}

// 方案二：邏輯過期（熱點數據，永不過期）
@Data
public class CacheWrapper<T> {
    private T      data;
    private LocalDateTime expireTime; // 邏輯過期時間（存在 value 裡）
}

public Product getProductWithLogicalExpire(Long id) {
    String key    = "product:" + id;
    String cached = redisTemplate.opsForValue().get(key);

    CacheWrapper<Product> wrapper =
        JSON.parseObject(cached, new TypeReference<CacheWrapper<Product>>() {});

    // 未過期 → 直接返回
    if (LocalDateTime.now().isBefore(wrapper.getExpireTime())) {
        return wrapper.getData();
    }

    // 已過期 → 非同步重建，先返回舊資料
    String lockKey = "lock:rebuild:" + id;
    boolean locked = Boolean.TRUE.equals(
        redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", Duration.ofSeconds(3))
    );

    if (locked) {
        // 非同步重建緩存（不阻塞當前請求）
        CompletableFuture.runAsync(() -> {
            try {
                Product product = productRepository.findById(id).orElseThrow();
                CacheWrapper<Product> newWrapper = new CacheWrapper<>();
                newWrapper.setData(product);
                newWrapper.setExpireTime(LocalDateTime.now().plusMinutes(30));
                redisTemplate.opsForValue()
                    .set(key, JSON.toJSONString(newWrapper));
            } finally {
                redisTemplate.delete(lockKey);
            }
        }, executor);
    }

    return wrapper.getData(); // 立即返回舊資料，不讓用戶等待
}
```

---

### 🟠 緩存雪崩（Cache Avalanche）

```
問題：
  大量 key 在同一時間過期（如：夜間批次初始化緩存，TTL 全設 30 分鐘）
  → 30 分鐘後全部過期 → 大量請求同時打到 DB → 雪崩！

另一種場景：
  Redis 服務直接宕機 → 所有請求打到 DB

解決方案：

① TTL 隨機化（防止同時過期）
  TTL = 基礎時間 + random(0, 600) 秒
  讓過期時間分散，避免同時失效

② 永不過期 + 非同步更新（邏輯過期）
  見上方「緩存擊穿方案二」

③ Redis 高可用（防止宕機）
  Redis Sentinel（哨兵模式，自動主從切換）
  Redis Cluster（集群模式，分片 + 高可用）

④ 多級緩存（降低 Redis 壓力）
  本地緩存（Caffeine）→ Redis → DB
  即使 Redis 宕機，本地緩存仍可服務

⑤ 熔斷降級（服務保護）
  檢測到 DB 壓力過大 → 熔斷 → 返回預設值
  Sentinel / Resilience4j 實作
```

```java
// ① TTL 隨機化
private static final Random RANDOM = new Random();

public void cacheProduct(Long id, Product product) {
    int baseTtl    = 30 * 60;              // 30 分鐘（秒）
    int jitter     = RANDOM.nextInt(600);  // 隨機 0~10 分鐘
    int actualTtl  = baseTtl + jitter;

    redisTemplate.opsForValue().set(
        "product:" + id,
        JSON.toJSONString(product),
        Duration.ofSeconds(actualTtl)
    );
}

// ④ 多級緩存（Caffeine + Redis）
@Service
public class ProductCacheService {

    private final Cache<Long, Product> localCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.MINUTES) // 本地緩存 5 分鐘
        .build();

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Product getProduct(Long id) {
        // L1：本地緩存（毫秒級，零網路開銷）
        Product product = localCache.getIfPresent(id);
        if (product != null) return product;

        // L2：Redis 緩存
        String cached = redisTemplate.opsForValue().get("product:" + id);
        if (cached != null) {
            product = JSON.parseObject(cached, Product.class);
            localCache.put(id, product); // 回填本地緩存
            return product;
        }

        // L3：資料庫
        product = productRepository.findById(id).orElseThrow();
        redisTemplate.opsForValue().set("product:" + id,
            JSON.toJSONString(product), Duration.ofMinutes(30));
        localCache.put(id, product);
        return product;
    }
}
```

---

## 6️⃣ Redis Cluster 架構

---

### 🔵 16384 槽位（Slot）

```
Redis Cluster 不使用一致性 Hash，使用槽位分配：

集群將 key 空間分為 16384 個 slot（0 ~ 16383）
每個 key 根據 CRC16(key) % 16384 計算所屬 slot
每個 Master 節點負責一部分 slot

3 主 3 從範例：
  Master1：slot 0 ~ 5460
  Master2：slot 5461 ~ 10922
  Master3：slot 10923 ~ 16383
  Slave1：Master1 的副本
  Slave2：Master2 的副本
  Slave3：Master3 的副本

請求路由：
  客戶端發送 GET order:123
  計算 CRC16("order:123") % 16384 = 7890
  → 屬於 Master2 的範圍
  → 如果連的不是 Master2，返回 MOVED 7890 master2_ip:port
  → 客戶端重新連到 Master2

Hash Tag（強制同槽）：
  多個 key 加上相同的 {} 標記
  MSET {user:1}:name "Alice" {user:1}:email "alice@example.com"
  → {} 內容相同 → 計算相同 slot → 在同一個節點
  → 可以使用 MSET / MGET 等多 key 命令

節點故障處理：
  Master1 宕機
  → Slave1 等待 cluster-node-timeout（預設 15s）
  → Slave1 發起選舉，其他 Master 投票
  → Slave1 升為新 Master
  → 繼續服務
```

```yaml
# application.yml（Spring Boot 連接 Redis Cluster）
spring:
  redis:
    cluster:
      nodes:
        - redis-node1:6379
        - redis-node2:6379
        - redis-node3:6379
        - redis-node4:6379
        - redis-node5:6379
        - redis-node6:6379
      max-redirects: 3  # 最多重定向次數
    lettuce:
      cluster:
        refresh:
          adaptive: true          # 自動感知集群拓撲變化
          period: 30s
```

---

## 7️⃣ 效能優化

---

### 🔵 Pipeline（批量命令）

```java
// 問題：N 個命令 = N 次網路往返（RTT）
// Pipeline：N 個命令一次性發送，一次網路往返

// ❌ 沒有 Pipeline（假設 RTT = 1ms）
for (int i = 0; i < 1000; i++) {
    redisTemplate.opsForValue().set("key:" + i, "value:" + i);
}
// 總耗時 ≈ 1000ms（1000 次網路往返）

// ✅ 使用 Pipeline
redisTemplate.executePipelined(connection -> {
    for (int i = 0; i < 1000; i++) {
        connection.stringCommands().set(
            ("key:" + i).getBytes(),
            ("value:" + i).getBytes()
        );
    }
    return null;
});
// 總耗時 ≈ 數毫秒（1 次網路往返）

// ⚠️ Pipeline 不保證原子性！
// 每個命令都可能成功或失敗
// 需要原子性 → 用 Lua Script 或 Transaction
```

---

### 🟡 Lua Script（原子操作）

```java
// Lua Script 在 Redis 中是原子執行的
// 適合：需要多個命令組合成一個原子操作的場景

// 範例：限流（令牌桶）- 原子判斷並計數
String rateLimitScript =
    "local key     = KEYS[1] " +
    "local limit   = tonumber(ARGV[1]) " +
    "local expire  = tonumber(ARGV[2]) " +
    "local current = tonumber(redis.call('get', key) or '0') " +
    "if current >= limit then " +
    "    return 0 " +  // 超過限制
    "else " +
    "    redis.call('incr', key) " +
    "    if current == 0 then " +
    "        redis.call('expire', key, expire) " +
    "    end " +
    "    return 1 " +  // 允許通過
    "end";

public boolean isAllowed(String userId, int limit, int windowSeconds) {
    String key = "rate:limit:" + userId;
    Long result = redisTemplate.execute(
        new DefaultRedisScript<>(rateLimitScript, Long.class),
        List.of(key),
        String.valueOf(limit),
        String.valueOf(windowSeconds)
    );
    return Long.valueOf(1L).equals(result);
}
```

---

## 8️⃣ 熱 Key / 大 Key 處理

---

### 🔵 熱 Key（Hot Key）

```
問題：
  某個 key 被大量請求訪問（如：秒殺商品詳情）
  → 該 key 所在的 Redis 節點 CPU 飆高
  → 單點成為瓶頸

發現方法：
  ① redis-cli --hotkeys（Redis 4.0+）
  ② monitor 命令（生產慎用，會影響效能）
  ③ Proxy 層統計（如：Twemproxy）
  ④ 客戶端埋點（記錄 key 訪問頻率）

解決方案：

① 本地緩存（最有效）
  熱 Key 直接緩存在應用本地（Caffeine）
  不需要訪問 Redis！
  缺點：各實例數據可能不一致（短暫）

② Key 分片（打散熱點）
  將一個熱 Key 複製成 N 份：
  hot:product:100:0 ~ hot:product:100:9
  讀取時隨機選一份：
  int shard = ThreadLocalRandom.current().nextInt(10);
  String key = "hot:product:100:" + shard;
  
  寫入時更新所有分片（或異步更新）

③ Redis Cluster 讀寫分離
  對熱 Key 進行讀擴展（讀從節點）
```

---

### 🟡 大 Key（Big Key）

```
什麼是大 Key？
  String 類型 > 10KB
  List / Hash / Set / ZSet 元素數量 > 10000 個

問題：
  ① 佔用大量記憶體
  ② 查詢耗時（網路傳輸慢）
  ③ 刪除大 Key 會阻塞 Redis（DEL 是同步的！）
  ④ 主從同步時，大 Key 的 RDB 傳輸耗時

發現方法：
  redis-cli --bigkeys                    # 掃描大 key
  redis-cli -c DEBUG OBJECT key          # 查看 key 大小
  MEMORY USAGE keyname                   # 精確記憶體佔用

解決方案：

① 拆分大 Key
  Hash 有 10 萬個 field → 拆成 100 個 Hash，每個 1000 個 field
  big:hash:key → hash:key:0 ~ hash:key:99
  分片規則：field 的 hash % 100

② 壓縮 Value
  使用 Snappy / LZ4 壓縮存儲
  適合：value 是重複性高的 JSON

③ 刪除大 Key 用 UNLINK（非同步刪除）
  DEL key    → 同步刪除（會阻塞！）
  UNLINK key → 非同步刪除（先斷開引用，再後台清理） ✅
  
  或分批刪除（List / Hash / ZSet）：
  HSCAN → 每次刪除 100 個 field → 重複直到清空
```

```java
// 安全刪除大 Key（分批）
public void deleteLargeHash(String key) {
    ScanOptions options = ScanOptions.scanOptions().count(100).build();
    Cursor<Map.Entry<Object, Object>> cursor =
        redisTemplate.opsForHash().scan(key, options);

    while (cursor.hasNext()) {
        List<Object> fields = new ArrayList<>();
        for (int i = 0; i < 100 && cursor.hasNext(); i++) {
            fields.add(cursor.next().getKey());
        }
        redisTemplate.opsForHash().delete(key, fields.toArray());
    }
    redisTemplate.delete(key); // 最後刪除 key 本身
}

// UNLINK（非同步刪除）
redisTemplate.unlink("large:key");  // Spring Data Redis 支援
```

---

## 📝 Redis 深度 總結

| 主題 | 核心要點 |
|------|---------|
| **String 編碼** | int / embstr（≤44B）/ raw，底層是 SDS |
| **List 編碼** | listpack（小）→ quicklist（大）|
| **Hash 編碼** | listpack（小）→ hashtable，漸進式 rehash |
| **Set 編碼** | intset（純整數）→ hashtable |
| **ZSet 編碼** | listpack（小）→ skiplist + dict，跳表支援範圍查詢 |
| **跳表 vs 紅黑樹** | 跳表範圍查詢更簡單，實作更容易，並發友好 |
| **RDB** | 二進制快照，bgsave fork 子程序，恢復快 |
| **AOF** | 追加命令日誌，everysec 最多丟 1 秒，支援重寫 |
| **混合持久化** | RDB 快照 + AOF 增量，推薦生產使用 |
| **淘汰策略** | 作為緩存用 allkeys-lru，有 TTL 用 volatile-lru |
| **分散式鎖** | SET NX + Lua 釋放，生產用 Redisson（看門狗機制）|
| **緩存穿透** | 布隆過濾器 / 緩存空值 |
| **緩存擊穿** | 互斥鎖重建 / 邏輯過期（熱點數據）|
| **緩存雪崩** | TTL 隨機化 / 多級緩存 / Redis 高可用 |
| **Cluster** | 16384 槽位，CRC16 取模路由，MOVED 重定向 |
| **Pipeline** | 批量命令一次網路往返，提升吞吐量 |
| **Lua Script** | 原子操作，適合限流 / 分散式鎖釋放 |
| **熱 Key** | 本地緩存 / Key 分片打散熱點 |
| **大 Key** | UNLINK 非同步刪除，SCAN 分批刪除，避免阻塞 |
