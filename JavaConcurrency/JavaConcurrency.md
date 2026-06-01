# ⚡ Java 並發深度 完整深入教學

---

## 🗺️ 學習路線圖

```
Java 並發深度
├── 1️⃣ Thread 生命週期與狀態轉換
├── 2️⃣ synchronized 底層原理
│   ├── Monitor 物件鎖
│   └── 鎖升級：偏向 → 輕量 → 重量
├── 3️⃣ volatile 原理
│   ├── 記憶體屏障
│   └── MESI 緩存一致性協議
├── 4️⃣ AQS 底層原理
│   ├── CLH 等待佇列
│   └── 獨佔 / 共享模式
├── 5️⃣ 鎖進階
│   ├── ReentrantLock
│   ├── ReadWriteLock
│   └── StampedLock
├── 6️⃣ ThreadLocal
│   ├── 底層原理
│   └── 記憶體洩漏問題
├── 7️⃣ 線程池
│   ├── ThreadPoolExecutor 7 個參數
│   ├── 拒絕策略
│   └── 線程池調優
├── 8️⃣ ConcurrentHashMap 底層原理
│   └── JDK 8 CAS + Synchronized
└── 9️⃣ CompletableFuture 異步編排
```

---

## 1️⃣ Thread 生命週期與狀態轉換

---

### 🔵 六種狀態

```
Java Thread 狀態（java.lang.Thread.State）：

NEW           → 剛建立，還沒 start()
RUNNABLE      → 運行中或等待 CPU 調度
BLOCKED       → 等待 synchronized 鎖
WAITING       → 無限期等待（wait() / join() / park()）
TIMED_WAITING → 有限期等待（sleep() / wait(ms) / join(ms)）
TERMINATED    → 執行完畢

狀態轉換圖：

              start()
NEW ─────────────────────────→ RUNNABLE
                                  │    │
               synchronized 鎖被占 │    │ 搶到鎖
                                  ↓    │
                               BLOCKED ┘

              Object.wait()
              Thread.join()        notify() / notifyAll()
              LockSupport.park()   LockSupport.unpark()
RUNNABLE ──────────────────→ WAITING ──────────────────→ RUNNABLE

              Thread.sleep(ms)
              Object.wait(ms)      超時或喚醒
RUNNABLE ──────────────────→ TIMED_WAITING ─────────────→ RUNNABLE

              run() 執行完畢
RUNNABLE ──────────────────→ TERMINATED
```

```java
// 查看線程狀態
Thread t = new Thread(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
});

System.out.println(t.getState()); // NEW

t.start();
System.out.println(t.getState()); // RUNNABLE 或 TIMED_WAITING

t.join();
System.out.println(t.getState()); // TERMINATED
```

---

### 🟡 中斷機制（Interruption）

```java
// ❌ 不能強制停止線程（stop() 已廢棄）
// ✅ 使用中斷機制（協作式）

Thread worker = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        // 業務邏輯
        System.out.println("工作中...");
    }
    System.out.println("線程正常結束");
});

worker.start();
Thread.sleep(100);
worker.interrupt(); // 設定中斷標誌

// 處理 InterruptedException
Thread t = new Thread(() -> {
    try {
        Thread.sleep(10000); // TIMED_WAITING
    } catch (InterruptedException e) {
        // sleep 被中斷 → 清除中斷標誌並拋出異常
        // ⚠️ 這裡要決定：恢復中斷標誌 or 直接結束
        Thread.currentThread().interrupt(); // 恢復中斷標誌（推薦）
        System.out.println("被中斷，結束執行");
    }
});
```

---

## 2️⃣ synchronized 底層原理

---

### 🔵 Monitor 物件鎖

```
每個 Java 物件都有一個 Monitor（監視器鎖）
Monitor 由 C++ 的 ObjectMonitor 實作

ObjectMonitor 結構：
┌─────────────────────────────────────────┐
│             ObjectMonitor               │
│                                         │
│  _owner      → 當前持有鎖的線程          │
│  _count      → 重入次數（ReentrantLock） │
│  _EntryList  → 等待鎖的線程佇列（BLOCKED）│
│  _WaitSet    → 呼叫 wait() 的線程集合    │
└─────────────────────────────────────────┘

synchronized 運作流程：

線程 A 嘗試進入 synchronized：
  ① 查看 _owner 是否為 null
  ② 為 null → 設定 _owner = 線程 A，_count = 1 → 進入
  ③ 不為 null 且是自己 → _count++ → 重入（可重入鎖）
  ④ 不為 null 且是別人 → 進入 _EntryList 等待

線程 A 呼叫 wait()：
  ① _count-- 直到為 0 → 釋放鎖（_owner = null）
  ② 線程 A 進入 _WaitSet

呼叫 notify()：
  ① 從 _WaitSet 取出一個線程 → 移到 _EntryList
  ② 等待重新競爭鎖

位元組碼層面：
  monitorenter → 進入 synchronized
  monitorexit  → 離開 synchronized（正常 + 異常都有）
```

```java
// synchronized 四種用法
public class SynchronizedDemo {

    private static final Object LOCK = new Object();

    // 1. 同步實例方法（鎖 this）
    public synchronized void instanceMethod() {
        // 鎖 = this 物件
    }

    // 2. 同步靜態方法（鎖 Class）
    public static synchronized void staticMethod() {
        // 鎖 = SynchronizedDemo.class
    }

    // 3. 同步程式碼區塊（鎖指定物件）
    public void blockMethod() {
        synchronized (LOCK) {
            // 鎖 = LOCK 物件（粒度更細，推薦）
        }
    }

    // 4. 同步 this（等同於方法一）
    public void thisMethod() {
        synchronized (this) { }
    }
}

// ⚠️ 死鎖範例（兩個線程互等對方的鎖）
Object lockA = new Object();
Object lockB = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lockA) {
        Thread.sleep(100);
        synchronized (lockB) { } // 等待 lockB
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lockB) {
        Thread.sleep(100);
        synchronized (lockA) { } // 等待 lockA
    }
});
// 死鎖！t1 等 lockB，t2 等 lockA，互等永遠

// 預防死鎖：固定加鎖順序
// 都先鎖 lockA 再鎖 lockB
```

---

### 🟡 鎖升級機制（JDK 6+ 優化）⭐

```
背景：重量級鎖需要作業系統介入（核心態切換），代價極高
JDK 6 引入鎖升級，根據競爭情況自動升級

物件頭（Object Header）Mark Word（64bit）：

鎖狀態      │ 存儲內容
────────────┼─────────────────────────────────
無鎖        │ hashCode(25) │ age(4) │ 01
偏向鎖      │ threadId(54) │ 01
輕量級鎖    │ 指向棧幀的指針(62) │ 00
重量級鎖    │ 指向 Monitor 的指針(62) │ 10
GC 標記     │ 11

升級流程：

無鎖
  │ 第一個線程進入 synchronized
  ↓
偏向鎖（Biased Lock）
  記錄線程 ID 在 Mark Word
  之後同一個線程進入 → 直接進，不需要 CAS！
  適合：只有一個線程的場景
  │ 其他線程嘗試競爭
  ↓
輕量級鎖（Lightweight Lock）
  CAS 方式嘗試獲取鎖（自旋）
  在線程的棧幀建立 Lock Record，CAS 將 Mark Word 改為指向 Lock Record
  適合：競爭少，線程交替執行（不同時）
  │ 自旋超過次數（預設 10 次，JDK 6 後自適應）
  ↓
重量級鎖（Heavyweight Lock）
  進入核心態，真正的 Monitor 鎖
  未獲得鎖的線程阻塞（不消耗 CPU）
  適合：高競爭、長臨界區

⚠️ 鎖只能升級，不能降級（偏向鎖可以撤銷）
```

---

## 3️⃣ volatile 原理

---

### 🔵 兩大保證

```
volatile 保證：
  ① 可見性：一個線程修改，其他線程立即可見
  ② 有序性：禁止指令重排序（保證 happens-before）

volatile 不保證：
  ❌ 原子性（i++ 非原子操作）
```

```java
// ❌ 沒有 volatile，可能出現可見性問題
private boolean running = true;

Thread worker = new Thread(() -> {
    while (running) { // 線程可能讀取到緩存中的舊值，永遠不停！
    }
});

worker.start();
Thread.sleep(100);
running = false; // 主線程修改，worker 線程可能看不到！

// ✅ 加上 volatile
private volatile boolean running = true;
// 每次讀都從主記憶體讀，每次寫都立即刷新到主記憶體
```

---

### 🟡 記憶體屏障（Memory Barrier）

```
JMM（Java Memory Model）定義四種屏障：

LoadLoad   屏障：前面的 Load 先於後面的 Load
StoreStore 屏障：前面的 Store 先於後面的 Store
LoadStore  屏障：前面的 Load 先於後面的 Store
StoreLoad  屏障：前面的 Store 先於後面的 Load（最強）

volatile 寫的屏障：
  ... 普通操作
  [StoreStore 屏障]
  volatile 寫
  [StoreLoad 屏障]
  ... 後續操作

volatile 讀的屏障：
  ... 前面操作
  volatile 讀
  [LoadLoad 屏障]
  [LoadStore 屏障]
  ... 後續操作

效果：
  volatile 寫之前的所有操作 → 都在 volatile 寫之前完成
  volatile 讀之後的所有操作 → 都在 volatile 讀之後完成
  → 保證有序性！
```

```java
// 雙重檢查鎖定（DCL）為什麼需要 volatile？
public class Singleton {
    // ⚠️ 必須加 volatile！
    private static volatile Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                    // new 操作分三步：
                    // ① 分配記憶體
                    // ② 初始化物件
                    // ③ 將引用指向記憶體地址
                    // 沒有 volatile → JIT 可能重排為 ①③②
                    // 另一個線程看到 instance != null 但物件未初始化完成！
                }
            }
        }
        return instance;
    }
}
// volatile 的 StoreStore 屏障 → 防止 ②③ 重排序
```

---

## 4️⃣ AQS 底層原理 ⭐

---

### 🔵 AQS 架構

```
AQS（AbstractQueuedSynchronizer）是 Java 並發的骨幹！

基於 AQS 實作的類：
  ReentrantLock
  ReentrantReadWriteLock
  Semaphore
  CountDownLatch
  CyclicBarrier

AQS 核心：
  state    - int 型的同步狀態（volatile）
  CLH 佇列 - 雙向鏈表，存放等待線程的 Node

獨佔模式（ReentrantLock）：
  state = 0  → 未鎖定
  state = 1  → 已鎖定
  state = N  → 重入 N 次

共享模式（Semaphore / CountDownLatch）：
  state = N  → 允許 N 個線程同時訪問
```

```
CLH 佇列（等待佇列）：

Head（虛節點）→ Node(T1) → Node(T2) → Node(T3) → null
                 │             │             │
              RUNNING      WAITING        WAITING

獲取鎖流程：
① 嘗試 CAS 修改 state（tryAcquire）
② 成功 → 設定 exclusiveOwnerThread = 當前線程
③ 失敗 → 加入 CLH 佇列尾部（CAS 方式，線程安全）
④ 前驅節點是 Head → 再次嘗試獲取（公平鎖機制）
⑤ 還是失敗 → LockSupport.park() 阻塞自己

釋放鎖流程：
① tryRelease：state-- 直到為 0
② unparkSuccessor：喚醒 CLH 佇列中的第一個等待線程
③ 被喚醒的線程再次嘗試獲取鎖
```

```java
// 自定義鎖（理解 AQS 用）
public class SimpleLock extends AbstractQueuedSynchronizer {

    // 嘗試獲取鎖（獨佔模式）
    @Override
    protected boolean tryAcquire(int acquires) {
        // CAS 將 state 從 0 改為 1
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }

    // 嘗試釋放鎖
    @Override
    protected boolean tryRelease(int releases) {
        if (getState() == 0) {
            throw new IllegalMonitorStateException();
        }
        setExclusiveOwnerThread(null);
        setState(0);
        return true;
    }

    public void lock()   { acquire(1); }
    public void unlock() { release(1); }
}
```

---

## 5️⃣ 鎖進階

---

### 🔵 ReentrantLock

```java
// ReentrantLock vs synchronized 對比

// synchronized：
// ✅ 簡單易用
// ❌ 不能中斷等待
// ❌ 不能設定超時
// ❌ 只支援非公平鎖

// ReentrantLock：
// ✅ 可中斷等待
// ✅ 可設定超時
// ✅ 支援公平鎖 / 非公平鎖
// ✅ 可以綁定多個 Condition
// ❌ 需要手動 unlock()（必須在 finally！）

ReentrantLock lock = new ReentrantLock(true); // true = 公平鎖

// 標準使用方式
lock.lock();
try {
    // 臨界區
} finally {
    lock.unlock(); // ⚠️ 必須在 finally！
}

// 可中斷等待
try {
    lock.lockInterruptibly(); // 等待時可以被 interrupt() 中斷
    try {
        // 臨界區
    } finally {
        lock.unlock();
    }
} catch (InterruptedException e) {
    System.out.println("等待鎖被中斷");
}

// 超時等待
boolean acquired = lock.tryLock(3, TimeUnit.SECONDS);
if (acquired) {
    try {
        // 臨界區
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("3 秒內未獲得鎖，放棄");
}

// Condition（類比 wait / notify，但更靈活）
ReentrantLock lock       = new ReentrantLock();
Condition      notEmpty  = lock.newCondition();
Condition      notFull   = lock.newCondition();

// 生產者
lock.lock();
try {
    while (queue.isFull()) {
        notFull.await(); // 等待「不滿」條件
    }
    queue.add(item);
    notEmpty.signal(); // 通知「不空」條件
} finally {
    lock.unlock();
}

// 消費者
lock.lock();
try {
    while (queue.isEmpty()) {
        notEmpty.await(); // 等待「不空」條件
    }
    item = queue.poll();
    notFull.signal(); // 通知「不滿」條件
} finally {
    lock.unlock();
}
```

---

### 🟡 ReadWriteLock（讀寫鎖）

```java
// 場景：讀多寫少（如：緩存）
// 多個讀可以同時進行（讀讀不互斥）
// 寫的時候排他（讀寫互斥、寫寫互斥）

ReadWriteLock rwLock  = new ReentrantReadWriteLock();
Lock          readLock  = rwLock.readLock();
Lock          writeLock = rwLock.writeLock();

// 快取範例
private final Map<String, Object> cache   = new HashMap<>();
private final ReadWriteLock       rwLock2 = new ReentrantReadWriteLock();

public Object get(String key) {
    rwLock2.readLock().lock();
    try {
        return cache.get(key); // 多個線程可以同時讀
    } finally {
        rwLock2.readLock().unlock();
    }
}

public void put(String key, Object value) {
    rwLock2.writeLock().lock();
    try {
        cache.put(key, value); // 寫的時候排他
    } finally {
        rwLock2.writeLock().unlock();
    }
}

// ⚠️ 讀寫鎖的問題：寫鎖可能飢餓（一直有讀鎖，寫鎖永遠等不到）
// 解決：StampedLock（樂觀讀）
```

---

### 🟠 StampedLock（JDK 8，效能最強）

```java
// StampedLock 特點：
// ✅ 樂觀讀（不加鎖，讀完驗證是否有寫入）
// ✅ 讀取效能比 ReadWriteLock 更高
// ❌ 不支援重入
// ❌ 不支援 Condition

StampedLock stampedLock = new StampedLock();
private double x, y;

// 樂觀讀（最快，不加鎖）
public double distanceFromOrigin() {
    long stamp = stampedLock.tryOptimisticRead(); // 獲取樂觀讀票據
    double cx = x, cy = y;                        // 讀取數據

    if (!stampedLock.validate(stamp)) {           // 驗證期間有沒有寫入
        // 有寫入 → 升級為悲觀讀鎖
        stamp = stampedLock.readLock();
        try {
            cx = x;
            cy = y;
        } finally {
            stampedLock.unlockRead(stamp);
        }
    }
    return Math.sqrt(cx * cx + cy * cy);
}

// 寫鎖
public void move(double deltaX, double deltaY) {
    long stamp = stampedLock.writeLock();
    try {
        x += deltaX;
        y += deltaY;
    } finally {
        stampedLock.unlockWrite(stamp);
    }
}
```

---

## 6️⃣ ThreadLocal

---

### 🔵 底層原理

```
ThreadLocal 解決的問題：
  每個線程都需要有自己獨立的變數副本
  如：數據庫連線、用戶上下文、MDC 日誌追蹤

底層結構：

每個 Thread 物件內部有一個 ThreadLocalMap：
  Thread.threadLocals = ThreadLocalMap

ThreadLocalMap 是一個特殊的 HashMap：
  key   = ThreadLocal 物件（弱引用）
  value = 存入的值

Thread1.threadLocals:
  [ThreadLocal@001] → "User-Alice"
  [ThreadLocal@002] → Connection@A

Thread2.threadLocals:
  [ThreadLocal@001] → "User-Bob"
  [ThreadLocal@002] → Connection@B

每個線程有自己的 Map → 自然隔離！
```

```java
// ThreadLocal 使用
public class UserContext {

    private static final ThreadLocal<UserInfo> CURRENT_USER =
        new ThreadLocal<>();

    public static void setUser(UserInfo user) {
        CURRENT_USER.set(user);
    }

    public static UserInfo getUser() {
        return CURRENT_USER.get();
    }

    // ⚠️ 必須清除！否則線程池中的線程會殘留舊資料
    public static void clear() {
        CURRENT_USER.remove();
    }
}

// 在 Filter 中使用
@Component
public class UserContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {
        try {
            UserInfo user = extractUserFromToken(request);
            UserContext.setUser(user);
            chain.doFilter(request, response);
        } finally {
            UserContext.clear(); // ⚠️ 必須在 finally 清除！
        }
    }
}

// 業務層直接取用，不需要傳參
@Service
public class OrderService {
    public Order createOrder(OrderRequest request) {
        UserInfo currentUser = UserContext.getUser(); // 直接取得當前用戶
        // ...
    }
}
```

---

### 🟡 記憶體洩漏問題 ⭐

```
為什麼會洩漏？

ThreadLocalMap 的 Entry：
  key   = WeakReference<ThreadLocal>（弱引用）
  value = Object（強引用）

當 ThreadLocal 物件沒有外部強引用時：
  GC 會回收 ThreadLocal 物件（key 變為 null）
  但 value 仍有強引用，GC 無法回收！
  → 記憶體洩漏！

                   GC 可以回收
ThreadLocal ref ────X───→ ThreadLocal 物件
                             ↑ 弱引用
Thread → ThreadLocalMap → Entry
                             ↓ 強引用（洩漏！）
                           value 物件（無法回收）

在線程池場景更嚴重：
  線程池的線程長期存活
  → 線程的 ThreadLocalMap 長期存在
  → value 永遠不會被 GC 回收
  → 記憶體一直洩漏！

解決方案：
  ✅ 使用完後立即呼叫 remove()（最重要！）
  ✅ 在 try-finally 中呼叫 remove()
  ✅ ThreadLocal 宣告為 static final（確保有強引用）
```

---

## 7️⃣ 線程池

---

### 🔵 ThreadPoolExecutor 7 個參數 ⭐

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    5,                                    // ① corePoolSize：核心線程數（長期保留）
    20,                                   // ② maximumPoolSize：最大線程數
    60L,                                  // ③ keepAliveTime：非核心線程空閒存活時間
    TimeUnit.SECONDS,                     // ④ unit：時間單位
    new LinkedBlockingQueue<>(200),       // ⑤ workQueue：等待佇列
    new ThreadFactoryBuilder()            // ⑥ threadFactory：線程工廠（命名！）
        .setNameFormat("order-pool-%d")
        .build(),
    new ThreadPoolExecutor.CallerRunsPolicy() // ⑦ handler：拒絕策略
);
```

```
線程池工作流程：

提交任務
  │
  ├→ 核心線程數 < corePoolSize？
  │   是 → 創建新核心線程執行
  │
  ├→ 佇列未滿？
  │   是 → 加入等待佇列
  │
  ├→ 線程數 < maximumPoolSize？
  │   是 → 創建非核心線程執行
  │
  └→ 執行拒絕策略！

四種拒絕策略：

AbortPolicy（預設）：
  拋出 RejectedExecutionException
  ❌ 不推薦（會讓調用方崩潰）

CallerRunsPolicy（推薦）：
  由提交任務的線程自己執行
  ✅ 起到降速效果，不丟任務

DiscardPolicy：
  靜默丟棄任務
  ❌ 任務丟失，難以排查

DiscardOldestPolicy：
  丟棄佇列中最老的任務
  ❌ 可能丟失重要任務
```

```java
// ❌ 不要用 Executors 工廠方法！
Executors.newFixedThreadPool(10);
// 使用 LinkedBlockingQueue（無界佇列！）
// 任務堆積 → OOM！

Executors.newCachedThreadPool();
// 最大線程數 Integer.MAX_VALUE
// 任務多時創建大量線程 → OOM！

// ✅ 自定義線程池參數

// 計算建議值：
// CPU 密集型（加密/計算）：corePoolSize = CPU 核心數 + 1
// IO 密集型（資料庫/網路）：corePoolSize = CPU 核心數 × 2

int cpuCores = Runtime.getRuntime().availableProcessors();

// 統一管理線程池（放在配置類）
@Configuration
public class ThreadPoolConfig {

    @Bean("orderTaskExecutor")
    public ThreadPoolExecutor orderTaskExecutor() {
        return new ThreadPoolExecutor(
            10, 50, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadFactoryBuilder()
                .setNameFormat("order-task-%d")
                .setDaemon(false)
                .build(),
            (r, executor) -> {
                // 自定義拒絕策略：記錄日誌 + 告警
                log.error("線程池已滿！佇列大小：{}，活躍線程：{}",
                    executor.getQueue().size(),
                    executor.getActiveCount());
                // 降級處理：同步執行
                r.run();
            }
        );
    }

    // Spring 異步任務線程池（@Async 使用）
    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(
            new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

### 🟡 線程池監控

```java
// 監控線程池狀態（定期打印）
@Scheduled(fixedRate = 30000)
public void monitorThreadPool() {
    log.info("線程池狀態 - 核心：{}, 活躍：{}, 最大：{}, " +
             "完成：{}, 佇列：{}, 任務總數：{}",
        executor.getCorePoolSize(),
        executor.getActiveCount(),
        executor.getMaximumPoolSize(),
        executor.getCompletedTaskCount(),
        executor.getQueue().size(),
        executor.getTaskCount()
    );
}
```

---

## 8️⃣ ConcurrentHashMap 底層原理

---

### 🔵 JDK 7 vs JDK 8

```
JDK 7（Segment 分段鎖）：
  16 個 Segment，每個 Segment 是一個小 HashMap
  鎖粒度 = Segment（同一 Segment 的操作互斥）
  最多 16 個線程並行

JDK 8（CAS + Synchronized，更細粒度）：
  結構：Node 陣列 + 鏈表 / 紅黑樹
  鎖粒度 = 單個桶（Node 陣列的一格）
  最多 N 個線程並行（N = 陣列長度）
```

```
JDK 8 put 流程：

① 計算 key 的 hash 值
② 定位到陣列中的桶（index = hash & (n-1)）
③ 桶為空？
   → CAS 插入新 Node（不加鎖，速度快）
④ 桶不為空，且 hash == MOVED（-1）？
   → 協助其他線程完成擴容
⑤ 桶不為空，且正常 Node？
   → synchronized 鎖定這個桶的頭節點
   → 插入鏈表 or 紅黑樹

關鍵設計：
  只鎖定單個桶的頭節點（鎖粒度極細）
  不同桶的操作完全並行（高吞吐量）
  CAS 操作用於空桶插入（無鎖）
```

```java
// size() 的實現（不是簡單計數，而是 LongAdder）
// ConcurrentHashMap 使用分段計數（CounterCell 陣列）
// 避免高並發下 size 計數的競爭

// 常用方法
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// putIfAbsent（原子操作）
map.putIfAbsent("key", 0);

// computeIfAbsent（初始化複雜對象）
map.computeIfAbsent("key", k -> new ArrayList<>()).add("value");

// compute（原子更新）
map.compute("count", (k, v) -> v == null ? 1 : v + 1);

// merge（合併）
map.merge("count", 1, Integer::sum);

// ⚠️ 注意：複合操作不是原子的！
// ❌ 非原子（check-then-act）
if (!map.containsKey("key")) {
    map.put("key", value);
}
// ✅ 使用 putIfAbsent 或 computeIfAbsent
map.putIfAbsent("key", value);
```

---

## 9️⃣ CompletableFuture 異步編排

---

### 🔵 核心 API

```java
// CompletableFuture 是 Future 的增強版
// 支援異步編排、鏈式調用、組合多個異步任務

// ─── 建立異步任務 ───

// 有返回值
CompletableFuture<User> userFuture = CompletableFuture.supplyAsync(
    () -> userService.findById(1L),
    executor  // 指定線程池（不指定用 ForkJoinPool.commonPool）
);

// 無返回值
CompletableFuture<Void> voidFuture = CompletableFuture.runAsync(
    () -> notificationService.send("通知"),
    executor
);

// ─── 鏈式調用 ───

CompletableFuture<OrderDTO> result = CompletableFuture
    // 1. 查詢用戶
    .supplyAsync(() -> userService.findById(userId), executor)

    // 2. 用用戶資訊查詢訂單（上一步的結果作為輸入）
    .thenApply(user -> orderService.findByUser(user))

    // 3. 轉換為 DTO
    .thenApply(order -> OrderDTO.from(order))

    // 4. 異常處理
    .exceptionally(ex -> {
        log.error("查詢失敗", ex);
        return OrderDTO.empty();
    });

// ─── 組合多個異步任務 ───

// 兩個任務都完成才繼續（AND 關係）
CompletableFuture<User>    userFuture2  = userService.asyncFindById(userId);
CompletableFuture<List<Order>> orderFuture = orderService.asyncFindByUser(userId);

CompletableFuture<UserOrderVO> combined = userFuture2
    .thenCombine(orderFuture, (user, orders) -> {
        return new UserOrderVO(user, orders);
    });

// 任意一個完成就繼續（OR 關係）
CompletableFuture<String> result2 = primaryFuture.applyToEither(
    fallbackFuture,
    response -> response
);

// 等待所有任務完成
CompletableFuture<Void> allDone = CompletableFuture.allOf(
    future1, future2, future3
);
allDone.join(); // 阻塞等待所有完成

// 等待任意一個完成
CompletableFuture<Object> anyDone = CompletableFuture.anyOf(
    future1, future2, future3
);
```

```java
// 實戰：並行查詢多個服務（串行 vs 並行）

// ❌ 串行（總耗時 = 各服務耗時之和）
public OrderDetailVO getOrderDetail(Long orderId) {
    Order    order   = orderService.findById(orderId);     // 100ms
    User     user    = userService.findById(order.getUserId()); // 50ms
    Product  product = productService.findById(order.getProductId()); // 80ms
    // 總計：230ms
    return new OrderDetailVO(order, user, product);
}

// ✅ 並行（總耗時 = 最慢服務的耗時）
public OrderDetailVO getOrderDetailParallel(Long orderId) {
    Order order = orderService.findById(orderId); // 必須先查

    // 並行查詢 user 和 product
    CompletableFuture<User> userFuture3 = CompletableFuture
        .supplyAsync(() -> userService.findById(order.getUserId()), executor);

    CompletableFuture<Product> productFuture = CompletableFuture
        .supplyAsync(() -> productService.findById(order.getProductId()), executor);

    // 等待兩個都完成
    CompletableFuture.allOf(userFuture3, productFuture).join();

    // 獲取結果（已完成，get() 不會阻塞）
    User    user    = userFuture3.get();
    Product product = productFuture.get();
    // 總計：max(50ms, 80ms) = 80ms（節省 150ms！）

    return new OrderDetailVO(order, user, product);
}

// 帶超時的異步任務（JDK 9+）
CompletableFuture<String> withTimeout = future
    .orTimeout(3, TimeUnit.SECONDS)          // 超時拋出 TimeoutException
    .exceptionally(ex -> "預設返回值");

// 或
CompletableFuture<String> withTimeout2 = future
    .completeOnTimeout("預設值", 3, TimeUnit.SECONDS); // 超時返回預設值
```

---

## 📝 Java 並發深度 總結

| 主題 | 核心要點 |
|------|---------|
| **Thread 狀態** | NEW / RUNNABLE / BLOCKED / WAITING / TIMED_WAITING / TERMINATED |
| **synchronized** | Monitor 物件鎖，EntryList + WaitSet，可重入 |
| **鎖升級** | 無鎖 → 偏向鎖 → 輕量級鎖（CAS）→ 重量級鎖，只升不降 |
| **volatile** | 可見性 + 有序性（不保證原子性），記憶體屏障防止重排序 |
| **AQS** | state + CLH 佇列，是 ReentrantLock / Semaphore 的骨幹 |
| **ReentrantLock** | 可中斷 / 可超時 / 公平鎖 / Condition，必須 finally unlock |
| **ReadWriteLock** | 讀讀不互斥，讀寫 / 寫寫互斥，讀多寫少場景 |
| **StampedLock** | 樂觀讀（不加鎖），效能最強，不支援重入 |
| **ThreadLocal** | 每個線程獨立副本，key 是弱引用，用完必須 remove() |
| **線程池** | 7 個參數要熟背，禁用 Executors，推薦 CallerRunsPolicy |
| **ConcurrentHashMap** | JDK 8：CAS（空桶）+ Synchronized（桶頭節點），鎖粒度極細 |
| **CompletableFuture** | 異步編排，thenApply / thenCombine / allOf，並行查詢節省時間 |
