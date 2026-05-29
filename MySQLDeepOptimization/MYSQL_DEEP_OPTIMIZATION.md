# 🟣 MySQL 深度優化 完整深入教學

---

## 🗺️ 學習路線圖

```
MySQL 深度優化
├── 1️⃣ 索引原理
│   ├── B+樹索引結構
│   ├── 聚簇索引 vs 非聚簇索引
│   ├── 聯合索引與最左前綴原則
│   └── 覆蓋索引、索引下推
├── 2️⃣ SQL 執行計劃分析
│   ├── EXPLAIN 詳解
│   ├── 慢查詢日誌
│   └── 常見慢查詢優化
├── 3️⃣ 事務與鎖
│   ├── ACID 原理
│   ├── 事務隔離級別
│   ├── MVCC 多版本並發控制
│   └── 鎖機制（表鎖、行鎖、間隙鎖）
├── 4️⃣ 主從複製與讀寫分離
│   ├── Binlog 複製原理
│   ├── 主從配置
│   └── 讀寫分離實作
└── 5️⃣ 分庫分表
    ├── 垂直切分 vs 水平切分
    ├── ShardingSphere 實戰
    └── 分散式主鍵生成
```

---

## 1️⃣ 索引原理

---

### 🔵 B+樹索引結構

```
為什麼用 B+樹而不是其他結構？

二元搜尋樹（BST）：最壞退化成鏈表 O(n)
紅黑樹：樹高大，磁碟 IO 多
Hash：無法範圍查詢
B 樹：非葉節點也存數據，葉節點之間無連接
B+樹：✅ 最適合資料庫！

B+樹特點：
  ✅ 所有數據存在葉節點（非葉節點只存索引鍵）
  ✅ 葉節點用雙向鏈表串連（支援範圍查詢）
  ✅ 樹高低（通常 3~4 層），磁碟 IO 少
  ✅ 非葉節點可以存更多鍵（磁碟頁空間利用率高）

B+樹結構示意：

                    [30]
                   /    \
              [10,20]   [40,50]
             /  |  \    /  |  \
           [1] [10][20][30][40][50]  ← 葉節點（存完整行資料或主鍵）
            ↕   ↕   ↕   ↕   ↕   ↕
           （雙向鏈表串連，支援範圍查詢）

InnoDB 頁大小預設 16KB
一個非葉節點能存多少鍵？
  假設主鍵 8 bytes，指針 6 bytes，14 bytes/條
  16KB / 14 = 約 1170 個鍵
  
樹高 3 層能存多少行？
  1170 * 1170 * 16（每葉節點約16條） = 約 2000 萬行
  → 千萬行資料，查詢只需 3 次磁碟 IO！
```

---

### 🟡 聚簇索引 vs 非聚簇索引

```
聚簇索引（Clustered Index）：
  InnoDB 的主鍵索引
  葉節點直接存完整行資料
  每張表只能有一個聚簇索引

  主鍵查詢：直接在 B+樹找到葉節點，取出完整數據 ✅ 最快！

  主鍵選擇原則：
  ✅ 使用自增整數（INT / BIGINT AUTO_INCREMENT）
  ✅ 避免使用 UUID（隨機性導致頻繁頁分裂，性能差）
  ✅ 越短越好（影響非聚簇索引的大小）

非聚簇索引（Secondary Index / 二級索引）：
  葉節點存的是「主鍵值」，不是完整行資料

  非主鍵查詢流程：
  ① 在二級索引 B+樹找到主鍵值
  ② 再用主鍵值去聚簇索引查完整行資料（回表！）

  回表（Table Lookup）：
  SELECT * FROM users WHERE name = 'John';
  ① 在 name 索引找到 name='John' 的主鍵 id=5
  ② 再用 id=5 回聚簇索引查完整行資料
  → 兩次 B+樹查詢！

  避免回表 → 使用覆蓋索引（見下方）
```

---

### 🟠 聯合索引與最左前綴原則

```java
// 建立聯合索引
CREATE INDEX idx_name_age_city ON users(name, age, city);

// B+樹排序方式：先按 name，name 相同按 age，age 相同按 city

// ✅ 可以使用索引：
SELECT * FROM users WHERE name = 'John';                        // 用 name
SELECT * FROM users WHERE name = 'John' AND age = 25;          // 用 name + age
SELECT * FROM users WHERE name = 'John' AND age = 25 AND city = 'Taipei'; // 全用

// ✅ 範圍查詢（最左前綴必須是等值）：
SELECT * FROM users WHERE name = 'John' AND age > 20;          // name 等值 + age 範圍

// ❌ 無法使用索引（跳過了最左列）：
SELECT * FROM users WHERE age = 25;                            // 跳過 name
SELECT * FROM users WHERE city = 'Taipei';                     // 跳過 name, age

// ❌ 範圍查詢後的列無法用索引：
SELECT * FROM users WHERE name = 'John' AND age > 20 AND city = 'Taipei';
// city 索引失效！（age 已是範圍查詢）

// ✅ 最佳實踐：選擇性高的列放前面
// 選擇性 = 不重複值數量 / 總行數
// gender（男/女）選擇性低 → 放後面
// name（很多不同名字）選擇性高 → 放前面
```

---

### 🔴 覆蓋索引（Covering Index）

```java
// 覆蓋索引：查詢所需的所有欄位都在索引中，不需要回表！

// 建立索引
CREATE INDEX idx_name_age ON users(name, age);

// ✅ 覆蓋索引（只查 name 和 age，都在索引中）
SELECT name, age FROM users WHERE name = 'John';
// EXPLAIN 中 Extra 欄位顯示：Using index ← 覆蓋索引！

// ❌ 需要回表（查了 email，不在索引中）
SELECT name, age, email FROM users WHERE name = 'John';

// ✅ 實際應用：分頁優化
// ❌ 深度分頁（OFFSET 很大時極慢）
SELECT * FROM orders ORDER BY created_at LIMIT 1000000, 10;
// 需要掃描 1000010 行，然後丟棄前 1000000 行！

// ✅ 覆蓋索引 + 延遲關聯
SELECT * FROM orders o
JOIN (
    SELECT id FROM orders ORDER BY created_at LIMIT 1000000, 10
) t ON o.id = t.id;
// 先用覆蓋索引只取主鍵（快），再 JOIN 取完整資料

// ✅ 更好的方案：遊標分頁（記錄上一頁最後一個 ID）
SELECT * FROM orders
WHERE id > 最後一個id
ORDER BY id
LIMIT 10;
```

---

### 🟢 索引下推（Index Condition Pushdown，ICP）

```
MySQL 5.6+ 的優化：

無 ICP：
  ① 儲存引擎根據索引找到主鍵
  ② 回表取完整行資料
  ③ Server 層過濾 WHERE 條件

有 ICP：
  ① 儲存引擎根據索引找到候選行
  ② 在儲存引擎層直接用索引過濾 WHERE 條件（不回表！）
  ③ 只有通過過濾的行才回表

// 範例：
CREATE INDEX idx_name_age ON users(name, age);

SELECT * FROM users WHERE name LIKE 'J%' AND age = 25;

無 ICP：
  先找所有 name LIKE 'J%' 的行（可能很多）→ 全部回表 → Server 過濾 age=25

有 ICP：
  找 name LIKE 'J%' → 在索引層過濾 age=25 → 只有符合的才回表
  → 大幅減少回表次數！

EXPLAIN 中 Extra：Using index condition ← 表示使用了 ICP
```

---

## 2️⃣ SQL 執行計劃分析

---

### 🔵 EXPLAIN 詳解

```sql
EXPLAIN SELECT u.name, o.amount
FROM users u
JOIN orders o ON u.id = o.user_id
WHERE u.status = 'ACTIVE'
  AND o.amount > 100
ORDER BY o.created_at DESC
LIMIT 10;
```

```
EXPLAIN 輸出欄位詳解：

┌────┬─────────┬────────────┬──────┬──────────────┬──────────────┬────────┬──────────┬──────┬───────────────────────────┐
│ id │ select_ │   table    │ type │ possible_    │     key      │ key_  │   rows   │filter│          Extra            │
│    │  type   │            │      │    keys      │              │  len  │          │      │                           │
├────┼─────────┼────────────┼──────┼──────────────┼──────────────┼────────┼──────────┼──────┼───────────────────────────┤
│  1 │ SIMPLE  │     u      │ ref  │status_idx    │  status_idx  │  62   │   500    │ 100% │ Using where               │
│  1 │ SIMPLE  │     o      │ ref  │user_id_idx   │ user_id_idx  │   8   │    10    │  33% │ Using index condition     │
└────┴─────────┴────────────┴──────┴──────────────┴──────────────┴────────┴──────────┴──────┴───────────────────────────┘
```

#### type 欄位（效能由好到差）：

```
system  > const > eq_ref > ref > range > index > ALL

system：表中只有一行
const： 主鍵或唯一索引等值查詢（最多一行）
        WHERE id = 1

eq_ref：JOIN 時，被驅動表用主鍵/唯一索引等值查詢
        JOIN users u ON o.user_id = u.id

ref：   非唯一索引等值查詢（可能多行）
        WHERE status = 'ACTIVE'

range： 索引範圍查詢
        WHERE age BETWEEN 20 AND 30
        WHERE id IN (1,2,3)

index： 全索引掃描（比 ALL 好，因為索引比表小）
        只查詢索引中的欄位

ALL：   全表掃描 ← 最差！必須優化！
        沒有使用任何索引
```

#### Extra 欄位常見值：

```
Using index         → ✅ 覆蓋索引，不需要回表
Using where         → Server 層過濾，索引不能完全過濾
Using index condition → ✅ 索引下推（ICP）
Using filesort      → ⚠️ 需要額外排序（沒有用到索引排序）
Using temporary     → ⚠️ 使用臨時表（GROUP BY / DISTINCT 效能差）
Using join buffer   → ⚠️ JOIN 沒有用到索引
```

---

### 🟡 慢查詢日誌

```sql
-- 開啟慢查詢日誌
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 1;        -- 超過 1 秒的 SQL 記錄
SET GLOBAL slow_query_log_file = '/var/log/mysql/slow.log';

-- 也記錄沒有用到索引的查詢
SET GLOBAL log_queries_not_using_indexes = ON;

-- 查看慢查詢日誌
# Time: 2024-01-01T10:00:00
# Query_time: 3.456  Lock_time: 0.001  Rows_sent: 100  Rows_examined: 5000000
SELECT * FROM orders WHERE status = 'PAID' ORDER BY created_at DESC LIMIT 100;

-- 分析工具：mysqldumpslow
mysqldumpslow -s t -t 10 /var/log/mysql/slow.log
# -s t：按查詢時間排序
# -t 10：顯示前 10 條

-- 更強大的工具：pt-query-digest
pt-query-digest /var/log/mysql/slow.log
```

---

### 🔴 常見慢查詢優化案例

```sql
-- ❌ 案例1：對索引欄位做函數操作（索引失效！）
SELECT * FROM orders WHERE YEAR(created_at) = 2024;

-- ✅ 優化：改寫為範圍查詢
SELECT * FROM orders
WHERE created_at >= '2024-01-01'
  AND created_at < '2025-01-01';

---

-- ❌ 案例2：隱式類型轉換（索引失效！）
-- user_id 欄位是 VARCHAR，但傳入整數
SELECT * FROM users WHERE user_id = 123;
-- MySQL 自動轉換：CAST(user_id AS INT) = 123 → 全表掃描！

-- ✅ 優化：類型要一致
SELECT * FROM users WHERE user_id = '123';

---

-- ❌ 案例3：OR 條件（可能索引失效）
SELECT * FROM users WHERE name = 'John' OR email = 'john@example.com';
-- name 和 email 分別有索引，但 OR 可能導致全表掃描

-- ✅ 優化：改用 UNION
SELECT * FROM users WHERE name = 'John'
UNION
SELECT * FROM users WHERE email = 'john@example.com';

---

-- ❌ 案例4：SELECT *（查詢不需要的欄位）
SELECT * FROM orders WHERE user_id = 1;

-- ✅ 優化：只查需要的欄位（可以利用覆蓋索引）
SELECT id, amount, status FROM orders WHERE user_id = 1;

---

-- ❌ 案例5：JOIN 過多或缺少索引
SELECT * FROM orders o
JOIN users u ON o.user_id = u.id  -- u.id 有主鍵索引 ✅
JOIN products p ON o.product_id = p.id  -- p.id 有主鍵索引 ✅
JOIN categories c ON p.category_id = c.id;  -- category_id 沒有索引！❌

-- ✅ 優化：為 JOIN 欄位建立索引
ALTER TABLE products ADD INDEX idx_category_id (category_id);

---

-- ❌ 案例6：深度分頁（OFFSET 很大）
SELECT * FROM orders LIMIT 1000000, 10;  -- 掃描 100萬行！

-- ✅ 優化：遊標分頁
SELECT * FROM orders WHERE id > :lastId ORDER BY id LIMIT 10;
```

---

## 3️⃣ 事務與鎖

---

### 🔵 事務隔離級別

```
SQL 標準定義的四種隔離級別（由低到高）：

隔離級別              臟讀   不可重複讀   幻讀
────────────────────────────────────────────────
READ UNCOMMITTED      ✅     ✅          ✅
READ COMMITTED        ❌     ✅          ✅
REPEATABLE READ（預設）❌     ❌          ✅（InnoDB 用 Gap Lock 解決）
SERIALIZABLE          ❌     ❌          ❌

問題定義：
  臟讀（Dirty Read）：
    讀到其他事務未提交的修改
    事務 A 修改了一行但未提交 → 事務 B 讀到了 → A 回滾 → B 讀到的是無效數據！

  不可重複讀（Non-Repeatable Read）：
    同一事務中，兩次讀取同一行，結果不同
    事務 A 讀取 name='John' → 事務 B 修改 name='Jane' 並提交 → 事務 A 再讀取 → 'Jane'

  幻讀（Phantom Read）：
    同一事務中，兩次查詢返回的行數不同
    事務 A 查詢 age>20 的用戶有 5 個 → 事務 B 插入一個 age=25 的用戶 → 事務 A 再查詢 → 6 個！

MySQL 預設隔離級別：REPEATABLE READ
```

```sql
-- 查看隔離級別
SELECT @@transaction_isolation;

-- 設定隔離級別
SET SESSION transaction_isolation = 'READ-COMMITTED';
```

---

### 🟡 MVCC（Multi-Version Concurrency Control）

```
MVCC 是 InnoDB 實現高並發的核心機制！

核心思想：
  讀不加鎖，寫不阻塞讀
  通過保存數據的多個版本，讓讀操作看到歷史版本

InnoDB 每行數據隱藏三個欄位：
  ┌──────────────────────────────────────────────────────┐
  │  row_id  │  trx_id（事務ID）  │  roll_pointer  │ 數據 │
  │（可選）    │  最後修改此行的事務 │  指向undo log   │      │
  └──────────────────────────────────────────────────────┘

Undo Log（撤銷日誌）：
  保存行的歷史版本鏈

  當前版本：name='Jane' (trx_id=100)
       ↓ roll_pointer
  歷史版本：name='John' (trx_id=50)
       ↓ roll_pointer
  更早版本：name='Joe'  (trx_id=10)

ReadView（一致性視圖）：
  事務開始時，創建一個快照（ReadView）
  記錄：
    m_ids：當前活躍（未提交）的事務 ID 列表
    min_trx_id：活躍事務的最小 ID
    max_trx_id：下一個事務的 ID（尚未分配）
    creator_trx_id：當前事務的 ID

版本可見性判斷（讀取哪個版本）：
  如果 trx_id == creator_trx_id → 自己修改的，可見 ✅
  如果 trx_id < min_trx_id → 已提交的，可見 ✅
  如果 trx_id >= max_trx_id → 還沒開始的事務，不可見 ❌
  如果 min_trx_id <= trx_id < max_trx_id：
    在 m_ids 中 → 未提交，不可見 ❌
    不在 m_ids 中 → 已提交，可見 ✅

REPEATABLE READ：
  事務開始時建立 ReadView，整個事務期間用同一個 ReadView
  → 保證同一事務兩次讀取結果相同！

READ COMMITTED：
  每次 SELECT 都建立新的 ReadView
  → 可以讀到其他事務已提交的修改（不可重複讀）
```

---

### 🟠 鎖機制

#### 表鎖 vs 行鎖：

```
表鎖（Table Lock）：
  鎖定整張表，粒度大，並發低
  MyISAM 使用表鎖
  InnoDB 的 LOCK TABLES 命令

行鎖（Row Lock）：
  InnoDB 預設，鎖定具體的行，並發高
  只有使用到索引才能用行鎖！
  沒有索引 → 升級為表鎖！❌

⚠️ 重要：InnoDB 行鎖是加在索引上的，不是行本身！
```

#### 三種行鎖：

```
Record Lock（記錄鎖）：
  鎖定一條具體的索引記錄
  WHERE id = 5 → 鎖定 id=5 這條記錄

Gap Lock（間隙鎖）：
  鎖定索引記錄之間的間隙（不包含記錄本身）
  WHERE id BETWEEN 5 AND 10 → 鎖定 (5, 10) 之間的空隙
  防止其他事務在這個間隙插入新行！→ 解決幻讀！

Next-Key Lock（臨鍵鎖）：
  = Record Lock + Gap Lock
  鎖定一條記錄及其之前的間隙
  InnoDB 預設使用 Next-Key Lock
  WHERE id = 5 → 鎖定 (-∞, 5] 的 Next-Key Lock
```

```sql
-- 死鎖範例：
-- 事務 A                    事務 B
BEGIN;                       BEGIN;
UPDATE orders SET status='PAID' WHERE id=1;   -- 鎖定 id=1
                             UPDATE orders SET status='PAID' WHERE id=2;   -- 鎖定 id=2
UPDATE orders SET status='SHIPPED' WHERE id=2; -- 等待 id=2（被 B 鎖住）
                             UPDATE orders SET status='SHIPPED' WHERE id=1; -- 等待 id=1（被 A 鎖住）
-- 死鎖！MySQL 自動選一個事務回滾

-- 避免死鎖的方法：
-- 1. 固定加鎖順序（總是先鎖 id=1 再鎖 id=2）
-- 2. 縮短事務持有鎖的時間
-- 3. 使用 SELECT ... FOR UPDATE 在事務開始時就鎖定

-- 查看死鎖信息
SHOW ENGINE INNODB STATUS;
```

---

### 🔴 樂觀鎖 vs 悲觀鎖

```java
// 悲觀鎖：假設一定會衝突，先鎖住再操作
@Transactional
public void deductStock(Long productId, Integer quantity) {
    // SELECT ... FOR UPDATE（排他鎖，阻塞其他讀寫）
    Product product = productMapper.selectByIdForUpdate(productId);
    if (product.getStock() < quantity) {
        throw new InsufficientStockException();
    }
    productMapper.deductStock(productId, quantity);
}
// Mapper.xml：SELECT * FROM products WHERE id=#{id} FOR UPDATE

// 樂觀鎖：假設不會衝突，提交時檢查（版本號/CAS）
// 表中加 version 欄位
@Transactional
public boolean deductStock(Long productId, Integer quantity, Integer version) {
    int rows = productMapper.deductStockWithVersion(
        productId, quantity, version
    );
    if (rows == 0) {
        // 版本不匹配（被其他事務修改了）→ 重試
        return false;
    }
    return true;
}
// SQL：UPDATE products
//      SET stock = stock - #{quantity}, version = version + 1
//      WHERE id = #{id} AND version = #{version} AND stock >= #{quantity}

// 什麼時候用哪個？
// 悲觀鎖：衝突頻繁、不能接受重試（如秒殺扣庫存）
// 樂觀鎖：衝突少、可以接受重試（如一般訂單更新）
```

---

## 4️⃣ 主從複製與讀寫分離

---

### 🔵 Binlog 複製原理

```
主從複製架構：

┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   Master     │        │    Slave 1   │        │    Slave 2   │
│              │        │              │        │              │
│  ┌────────┐  │        │  ┌────────┐  │        │  ┌────────┐  │
│  │ Binlog │──┼──────→ │  │ Relay  │  │        │  │ Relay  │  │
│  │（二進位 │  │  I/O   │  │  Log   │  │        │  │  Log   │  │
│  │ 日誌）  │  │  Thread│  └────┬───┘  │        │  └────────┘  │
│  └────────┘  │        │       │SQL   │        │              │
│              │        │       ↓Thread│        │              │
│  寫操作      │        │  ┌────────┐  │        │  讀操作      │
│  INSERT      │        │  │ 重放   │  │        │  SELECT      │
│  UPDATE      │        │  │ SQL    │  │        │              │
│  DELETE      │        │  └────────┘  │        │              │
└──────────────┘        └──────────────┘        └──────────────┘

複製流程：
① Master 執行 SQL，寫入 Binlog
② Slave 的 I/O Thread 連接 Master，讀取 Binlog
③ Slave 將 Binlog 寫入本地 Relay Log（中繼日誌）
④ Slave 的 SQL Thread 重放 Relay Log 中的 SQL

Binlog 格式：
  STATEMENT：記錄 SQL 語句（小，但可能不一致）
  ROW：記錄每行的實際變化（大，但準確）← 推薦
  MIXED：混合模式

主從延遲問題：
  Master 寫入後立即查 Slave 可能看到舊數據！
  解決方案：
  ① 寫後讀：寫操作後的讀請求，路由到 Master
  ② 半同步複製：Master 等至少一個 Slave 確認收到才返回
  ③ 並行複製：Slave 多線程重放（MySQL 5.6+）
```

---

### 🟡 讀寫分離實作（Spring Boot）

```yaml
# application.yml
spring:
  datasource:
    master:
      url: jdbc:mysql://master-host:3306/mydb
      username: root
      password: password
      driver-class-name: com.mysql.cj.jdbc.Driver
    slave1:
      url: jdbc:mysql://slave1-host:3306/mydb
      username: readonly
      password: password
      driver-class-name: com.mysql.cj.jdbc.Driver
    slave2:
      url: jdbc:mysql://slave2-host:3306/mydb
      username: readonly
      password: password
      driver-class-name: com.mysql.cj.jdbc.Driver
```

```java
// 自定義讀寫分離路由
public enum DataSourceType {
    MASTER, SLAVE
}

// ThreadLocal 存放當前數據源類型
public class DataSourceContextHolder {
    private static final ThreadLocal<DataSourceType> CONTEXT_HOLDER =
        new ThreadLocal<>();

    public static void setDataSourceType(DataSourceType type) {
        CONTEXT_HOLDER.set(type);
    }

    public static DataSourceType getDataSourceType() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}

// 動態數據源路由
@Component
public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        DataSourceType type = DataSourceContextHolder.getDataSourceType();
        return type == null ? DataSourceType.MASTER : type;
    }
}

// AOP 自動切換數據源
@Aspect
@Component
@Order(1)  // 優先於事務切面
public class DataSourceAspect {

    // @Transactional(readOnly=true) → Slave
    // @Transactional → Master
    @Around("@annotation(transactional)")
    public Object around(ProceedingJoinPoint point,
                         Transactional transactional) throws Throwable {
        try {
            if (transactional.readOnly()) {
                DataSourceContextHolder.setDataSourceType(DataSourceType.SLAVE);
            } else {
                DataSourceContextHolder.setDataSourceType(DataSourceType.MASTER);
            }
            return point.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}

// 使用
@Service
public class OrderService {

    @Transactional  // → Master（寫操作）
    public Order createOrder(OrderRequest request) { ... }

    @Transactional(readOnly = true)  // → Slave（讀操作）
    public Order getOrder(Long id) { ... }
}

// 生產環境推薦：使用 ShardingSphere 或 MyBatis-Plus 的讀寫分離功能
```

---

## 5️⃣ 分庫分表

---

### 🔵 什麼時候需要分庫分表？

```
單表問題：
  MySQL 單表資料量超過 1000 萬行 → 查詢效能下降
  MySQL 單庫連線數、磁碟 IO 成為瓶頸

垂直切分（按業務）：
  ┌─────────────────┐       ┌─────────────────┐
  │   訂單數據庫      │       │   用戶數據庫      │
  │  - orders       │       │  - users         │
  │  - order_items  │       │  - addresses     │
  └─────────────────┘       └─────────────────┘
  優點：業務清晰，解耦
  缺點：跨庫 JOIN 困難，需要分散式事務

水平切分（按數據量）：
  ┌──────────────────┐  ┌──────────────────┐
  │  orders_0         │  │  orders_1         │
  │  user_id % 2 = 0  │  │  user_id % 2 = 1  │
  │  id: 2,4,6,8...   │  │  id: 1,3,5,7...   │
  └──────────────────┘  └──────────────────┘
  優點：每個表數據量減半，效能提升
  缺點：跨表查詢困難，分散式事務複雜
```

---

### 🟡 ShardingSphere 實戰

```yaml
# application.yml（ShardingSphere 5.x）
spring:
  shardingsphere:
    datasource:
      names: db0, db1
      db0:
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:mysql://host0:3306/order_db0
        username: root
        password: password
      db1:
        type: com.zaxxer.hikari.HikariDataSource
        url: jdbc:mysql://host1:3306/order_db1
        username: root
        password: password

    rules:
      sharding:
        tables:
          t_order:
            # 實際物理表：db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1
            actual-data-nodes: db$->{0..1}.t_order_$->{0..1}

            # 分庫策略（按 user_id 決定去哪個庫）
            database-strategy:
              standard:
                sharding-column: user_id
                sharding-algorithm-name: db-inline

            # 分表策略（按 order_id 決定去哪個表）
            table-strategy:
              standard:
                sharding-column: order_id
                sharding-algorithm-name: table-inline

            # 分散式主鍵生成
            key-generate-strategy:
              column: order_id
              key-generator-name: snowflake

        sharding-algorithms:
          db-inline:
            type: INLINE
            props:
              algorithm-expression: db$->{user_id % 2}
          table-inline:
            type: INLINE
            props:
              algorithm-expression: t_order_$->{order_id % 2}

        key-generators:
          snowflake:
            type: SNOWFLAKE

    props:
      sql-show: true   # 顯示實際執行的 SQL（調試用）
```

---

### 🔴 分散式主鍵生成（雪花算法）

```
雪花算法（Snowflake）：Twitter 發明的分散式 ID 生成方案

64 位 Long 型 ID 結構：
┌──────────────────────────────────────────────────────────────┐
│ 0 │ 41位時間戳 │ 10位機器ID │ 12位序列號 │
│符號│（毫秒）    │ (5位DC+5位機器) │ (4096/ms)  │
└──────────────────────────────────────────────────────────────┘

時間戳：41位，可用約 69 年
機器ID：10位，最多 1024 個節點
序列號：12位，每毫秒最多生成 4096 個 ID

特點：
✅ 全局唯一
✅ 趨勢遞增（適合做主鍵，避免 B+樹頻繁頁分裂）
✅ 不依賴資料庫（高效能）
✅ 包含時間戳（可以從 ID 推算大概生成時間）

缺點：
❌ 依賴系統時鐘（時鐘回撥會導致 ID 重複！）
❌ 需要配置不同機器的 workerId
```

```java
// Spring Boot 中使用雪花算法
@Component
public class SnowflakeIdGenerator {

    private final long workerId;
    private final long datacenterId;
    private long sequence        = 0L;
    private long lastTimestamp   = -1L;

    // 各部分位數
    private static final long WORKER_ID_BITS     = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS      = 12L;

    // 最大值
    private static final long MAX_WORKER_ID      = ~(-1L << WORKER_ID_BITS);      // 31
    private static final long MAX_DATACENTER_ID  = ~(-1L << DATACENTER_ID_BITS);  // 31
    private static final long MAX_SEQUENCE       = ~(-1L << SEQUENCE_BITS);       // 4095

    // 移位
    private static final long WORKER_ID_SHIFT     = SEQUENCE_BITS;               // 12
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS; // 17
    private static final long TIMESTAMP_SHIFT     = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS; // 22

    // 起始時間（2024-01-01 00:00:00 UTC）
    private static final long EPOCH = 1704067200000L;

    public SnowflakeIdGenerator(
            @Value("${app.snowflake.worker-id:1}") long workerId,
            @Value("${app.snowflake.datacenter-id:1}") long datacenterId) {
        this.workerId     = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 時鐘回撥處理
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                "時鐘回撥，拒絕生成 ID，回撥時間：" + (lastTimestamp - timestamp) + " ms"
            );
        }

        if (timestamp == lastTimestamp) {
            // 同一毫秒內，序列號遞增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                // 序列號溢出，等待下一毫秒
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId     << WORKER_ID_SHIFT)
            | sequence;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
```

---

## 📊 MySQL 調優檢查清單

```
建表規範：
✅ 必須有主鍵，且用自增整數（BIGINT UNSIGNED AUTO_INCREMENT）
✅ 禁止使用 TEXT/BLOB（放在額外表中）
✅ NOT NULL 欄位設定預設值
✅ 時間欄位用 DATETIME，不用 TIMESTAMP（2038 問題）
✅ 金額欄位用 DECIMAL，不用 FLOAT（精度問題）

索引規範：
✅ JOIN 欄位必須建索引
✅ WHERE 常用欄位建索引
✅ 聯合索引：選擇性高的欄位放前面
✅ 禁止在索引欄位上做函數操作
✅ 單表索引不超過 5 個

SQL 規範：
✅ 禁止 SELECT *
✅ 禁止深度分頁（OFFSET > 10000）
✅ WHERE 條件必須有索引欄位
✅ 分頁用遊標方式（WHERE id > lastId）
✅ 大批量操作分批執行（每批 1000 條）
✅ 禁止在事務中做遠程調用
```

---

## 📝 MySQL 深度優化 總結

| 主題 | 核心要點 |
|------|---------|
| **B+樹** | 葉節點存數據，雙向鏈表，3\~4 層可存千萬行 |
| **聚簇索引** | 主鍵即索引，葉節點存完整行，用自增整數 |
| **回表** | 二級索引查到主鍵，再去聚簇索引取完整行 |
| **覆蓋索引** | 所需欄位都在索引中，避免回表，Extra=Using index |
| **最左前綴** | 聯合索引從最左列開始匹配，範圍查詢後的列失效 |
| **EXPLAIN** | type=ALL 必須優化，Extra=Using filesort 要關注 |
| **MVCC** | 多版本並發控制，ReadView + Undo Log，讀不加鎖 |
| **間隙鎖** | 防止幻讀，鎖定索引間隙，REPEATABLE READ 預設開啟 |
| **主從複製** | Binlog → Relay Log → SQL Thread 重放，注意主從延遲 |
| **雪花算法** | 64位趨勢遞增ID，時間戳+機器ID+序列號 |
