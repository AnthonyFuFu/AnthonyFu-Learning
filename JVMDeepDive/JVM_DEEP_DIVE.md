# 🔥 JVM 深度原理 完整深入教學

---

## 🗺️ 學習路線圖

```
JVM 深度原理
├── 1️⃣ JVM 整體架構
├── 2️⃣ 記憶體模型（Runtime Data Areas）
│   ├── 程序計數器
│   ├── 虛擬機棧
│   ├── 本地方法棧
│   ├── 堆（Heap）
│   └── 方法區 / Metaspace
├── 3️⃣ 類加載機制
│   ├── 類加載流程（7步驟）
│   ├── 雙親委派模型
│   └── 打破雙親委派
├── 4️⃣ GC 垃圾回收
│   ├── 如何判斷對象死亡
│   ├── 四大垃圾回收算法
│   ├── 分代收集理論
│   └── 六大 GC 收集器
├── 5️⃣ Java 記憶體模型（JMM）
│   ├── happens-before 原則
│   ├── volatile 底層原理
│   └── 三大問題：原子性、可見性、有序性
└── 6️⃣ JVM 性能調優
    ├── 常用 JVM 參數
    ├── GC Log 分析
    └── OOM 排查流程
```

---

## 1️⃣ JVM 整體架構

```
┌─────────────────────────────────────────────────────────────┐
│                        JVM 架構                              │
│                                                             │
│  .class 文件 ──→ ┌─────────────────┐                       │
│                  │   類加載子系統    │  （載入、驗證、準備、    │
│                  │  ClassLoader    │   解析、初始化）        │
│                  └────────┬────────┘                       │
│                           ↓                                │
│  ┌────────────────────────────────────────────────────┐    │
│  │              運行時數據區（記憶體模型）                │    │
│  │                                                    │    │
│  │  ┌──────────┐  ┌──────────┐  ┌─────────────────┐  │    │
│  │  │ 程序計數器 │  │ 虛擬機棧  │  │   本地方法棧     │  │    │
│  │  │  (PC)    │  │  Stack  │  │  Native Stack   │  │    │
│  │  │ 線程私有  │  │ 線程私有  │  │   線程私有       │  │    │
│  │  └──────────┘  └──────────┘  └─────────────────┘  │    │
│  │  ┌─────────────────────────────────────────────┐   │    │
│  │  │                  堆 (Heap)                   │   │    │
│  │  │     ┌───────────────┐  ┌─────────────────┐  │   │    │
│  │  │     │   新生代       │  │     老年代        │  │   │    │
│  │  │     │ Eden│S0│  S1  │  │    Old Gen      │  │   │    │
│  │  │     └───────────────┘  └─────────────────┘  │   │    │
│  │  │                   線程共享                    │   │    │
│  │  └─────────────────────────────────────────────┘   │    │
│  │  ┌─────────────────────────────────────────────┐   │    │
│  │  │           方法區 / Metaspace                  │   │    │
│  │  │    類資訊、常量池、靜態變數、JIT 編譯後的代碼     │   │    │
│  │  │                   線程共享                    │   │    │
│  │  └─────────────────────────────────────────────┘   │    │
│  └────────────────────────────────────────────────────┘    │
│                           ↓                                │
│  ┌────────────────┐  ┌────────────────────────────────┐   │
│  │  執行引擎       │  │         本地方法庫               │   │
│  │ ├─ 直譯器      │  │      Native Method Library      │   │
│  │ ├─ JIT 編譯器  │  │                                │   │
│  │ └─ GC         │  │                                │   │
│  └────────────────┘  └────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2️⃣ 記憶體模型（Runtime Data Areas）

---

### 🔵 程序計數器（Program Counter Register）

```
特性：
✅ 線程私有（每個線程一個）
✅ 唯一不會發生 OOM 的區域
✅ 大小固定，極小

作用：記錄當前線程正在執行的字節碼指令地址

為什麼需要？
┌─────────────────────────────────────────┐
│  線程 A 執行到第 15 行   → PC = 15       │
│  ↓ CPU 時間片耗盡，切換到線程 B           │
│  線程 B 執行到第 8 行    → PC = 8        │
│  ↓ 切換回線程 A                          │
│  線程 A 從 PC = 15 繼續執行 ✅           │
│  （如果沒有 PC，就不知道要從哪繼續！）     │
└─────────────────────────────────────────┘

執行 Native 方法時：PC = undefined（未定義）
```

---

### 🟡 虛擬機棧（VM Stack）

```
特性：
✅ 線程私有（每個線程一個棧）
✅ 生命週期與線程相同
❗ 可能拋出 StackOverflowError / OOM

結構：每個方法調用對應一個「棧幀（Stack Frame）」

┌─────────────────────────────────────────┐
│             虛擬機棧                      │
│  ┌─────────────────────────────────┐    │
│  │  棧幀 3：method_c()  ← 棧頂（當前執行）│
│  │  ├─ 局部變數表                    │    │
│  │  ├─ 運算元棧                     │    │
│  │  ├─ 動態連結                     │    │
│  │  └─ 方法返回地址                  │    │
│  ├─────────────────────────────────┤    │
│  │  棧幀 2：method_b()              │    │
│  ├─────────────────────────────────┤    │
│  │  棧幀 1：method_a()  ← 棧底       │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

#### 棧幀內部詳解：

```java
public int calculate(int a, int b) {
    int sum = a + b;
    return sum * 2;
}
```

```
棧幀結構：
┌────────────────────────────────────────────┐
│  局部變數表（Local Variable Table）          │
│  索引 0: this（非靜態方法）                  │
│  索引 1: a = 10                            │
│  索引 2: b = 20                            │
│  索引 3: sum = 0（初始）→ 30（賦值後）       │
├────────────────────────────────────────────┤
│  運算元棧（Operand Stack）                   │
│  執行 a + b 時，a 和 b 先入棧，加法後出棧     │
│  結果 30 入棧，賦值給 sum 後出棧             │
├────────────────────────────────────────────┤
│  動態連結（Dynamic Linking）                 │
│  指向運行時常量池中該方法的引用               │
├────────────────────────────────────────────┤
│  方法返回地址（Return Address）              │
│  記錄調用者的 PC 值，方法結束後回到哪裡       │
└────────────────────────────────────────────┘
```

#### StackOverflowError vs OOM：

```java
// StackOverflowError：棧深度超過限制
public void infinite() {
    infinite(); // 遞迴沒有出口 → StackOverflowError
}

// OOM：建立太多線程
for (int i = 0; i < 100000; i++) {
    new Thread(() -> {
        while(true) {}
    }).start(); // OOM: unable to create new native thread
}

// 調整棧大小
// -Xss512k  每個線程棧大小為 512KB（預設 512k~1M）
```

---

### 🔴 堆（Heap）⭐ 最重要

```
特性：
✅ 線程共享（所有線程都用同一個堆）
✅ JVM 中最大的記憶體區域
✅ GC 主要工作區域
❗ 可能拋出 OOM: Java heap space

堆的結構（分代設計）：

┌──────────────────────────────────────────────────────────┐
│                          堆                               │
│                                                          │
│  ┌──────────────────────────┐  ┌──────────────────────┐  │
│  │        新生代 Young Gen    │  │    老年代 Old Gen      │  │
│  │        （佔堆 1/3）        │  │    （佔堆 2/3）        │  │
│  │                          │  │                      │  │
│  │  ┌────────┬─────┬──────┐ │  │  長期存活的對象         │  │
│  │  │ Eden   │ S0  │  S1  │ │  │  大對象（直接進入）      │  │
│  │  │（8/10） │(1/10)│(1/10)│ │  │                      │  │
│  │  └────────┴─────┴──────┘ │  │                      │  │
│  └──────────────────────────┘  └──────────────────────┘  │
└──────────────────────────────────────────────────────────┘

Eden : S0 : S1  = 8 : 1 : 1
新生代 : 老年代  = 1 : 2

對應 JVM 參數：
-Xms              堆初始大小
-Xmx              堆最大大小
-Xmn              新生代大小
-XX:SurvivorRatio=8   Eden:S0:S1 = 8:1:1
```

#### 對象在堆中的生命週期：

```
新對象建立
    ↓
進入 Eden 區
    ↓
Eden 區滿 → 觸發 Minor GC
    ↓
存活對象（標記年齡 = 1）→ 移到 S0 區
死亡對象 → 回收
    ↓
再次 Minor GC
    ↓
S0 存活 → 移到 S1（年齡 +1）
Eden 存活 → 移到 S1（年齡 = 1）
    ↓
每次 Minor GC，S0 和 S1 互換角色（其中一個永遠為空）
    ↓
年齡 >= 15（預設）→ 晉升到老年代
    ↓
老年代滿 → 觸發 Full GC（Major GC）
```

```java
public void demo() {
    // 1. 短命對象（在 Minor GC 就被回收）
    for (int i = 0; i < 10000; i++) {
        String temp = new String("臨時字串"); // 每次循環後就沒有引用了
    }

    // 2. 長命對象（最終晉升到老年代）
    List<byte[]> cache = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
        cache.add(new byte[1024 * 1024]); // 1MB，每次 Minor GC 都存活
    }

    // 3. 大對象（直接進老年代，不過 Eden）
    byte[] bigObj = new byte[1024 * 1024 * 10]; // 10MB 大對象
    // -XX:PretenureSizeThreshold=5M → 超過 5MB 直接進老年代
}
```

---

### 🟠 方法區 / Metaspace

```
Java 7 及之前：方法區（PermGen 永久代）
Java 8 及之後：Metaspace（元空間）← 使用本地記憶體，不在堆中！

儲存內容：
┌─────────────────────────────────────────────┐
│               方法區 / Metaspace              │
│                                             │
│  ✅ 類的元資訊（欄位、方法、存取修飾符等）       │
│  ✅ 運行時常量池（Runtime Constant Pool）      │
│  ✅ 靜態變數（Static Variables）              │
│  ✅ JIT 編譯後的程式碼快取                     │
└─────────────────────────────────────────────┘

為什麼從 PermGen 改為 Metaspace？
PermGen 問題：
  ❌ 大小固定（-XX:MaxPermSize），容易 OOM
  ❌ GC 效率低
  ❌ 大小難以預測（框架動態生成大量類）

Metaspace 優點：
  ✅ 使用本地記憶體，預設沒有上限
  ✅ 動態調整大小
  ✅ 可用 -XX:MaxMetaspaceSize 設定上限

JVM 參數：
-XX:MetaspaceSize=256m      初始大小
-XX:MaxMetaspaceSize=512m   最大大小
```

#### 字串常量池：

```java
// 字串常量池（String Pool）在 Java 8+ 移到了堆中！
String s1 = "Hello";             // 在字串常量池
String s2 = "Hello";             // 從常量池取同一個
String s3 = new String("Hello"); // 在堆中新建對象

System.out.println(s1 == s2); // true（同一個常量池引用）
System.out.println(s1 == s3); // false（s3 是堆中新對象）

// intern() - 手動放入常量池
String s4 = s3.intern();
System.out.println(s1 == s4); // true

// 整數常量池（-128 ~ 127）
Integer i1 = 127;
Integer i2 = 127;
System.out.println(i1 == i2); // true（常量池範圍內）

Integer i3 = 128;
Integer i4 = 128;
System.out.println(i3 == i4); // false（超出常量池範圍，新建對象）
```

---

## 3️⃣ 類加載機制

---

### 🔵 類加載的完整流程（7 個步驟）

```
.class 文件
    │
    ▼
┌─────────┐    ┌─────────┐    ┌─────────┐
│  載入    │ →  │  驗證    │ →  │  準備    │
│ Loading  │    │  Verify  │    │ Prepare  │
└─────────┘    └─────────┘    └────┬────┘
                                   │
              ┌────────────────────┘
              ▼
         ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
         │  解析    │ →  │  初始化  │ →  │  使用    │ →  │  卸載    │
         │ Resolve  │    │  Init    │    │   Use    │    │ Unload  │
         └─────────┘    └─────────┘    └─────────┘    └─────────┘
              ↑──────連接（Linking）──────┘
```

#### 每個階段詳解：

```java
public class Demo {
    public static int value = 100;
    public static final String NAME = "Java";

    static {
        System.out.println("靜態初始化塊執行！");
        value = 200;
    }
}
```

```
① 載入（Loading）：
   ✅ 根據類的全限定名找到 .class 文件
   ✅ 將二進制字節流加載到記憶體
   ✅ 在方法區建立類的元資訊
   ✅ 在堆中建立 java.lang.Class 對象

② 驗證（Verification）：
   ✅ 文件格式驗證（魔數 CAFEBABE 開頭？）
   ✅ 元數據驗證（是否繼承了 final 類？）
   ✅ 字節碼驗證（指令是否合法？）
   ✅ 符號引用驗證（引用的類/方法存在嗎？）

③ 準備（Preparation）：
   ✅ 為靜態變數分配記憶體，設置「零值」（不是初始值！）

   value = 0    ← 零值（不是 100！）
   NAME = null  ← 零值（但 final 常量例外！）

   ⚠️ 例外：static final 基本類型/字串常量 → 直接賦值
   NAME = "Java" ← final 常量直接賦值（編譯期常量）

④ 解析（Resolution）：
   ✅ 將常量池中的符號引用替換為直接引用
   符號引用：com/example/Demo（字串符號）
   直接引用：記憶體地址（真實指針）

⑤ 初始化（Initialization）：
   ✅ 執行類的靜態代碼（<clinit> 方法）
   ✅ 按照代碼順序執行靜態賦值和靜態代碼塊

   value = 100  ← 靜態賦值
   System.out.println("靜態初始化塊執行！") ← 靜態代碼塊
   value = 200  ← 靜態代碼塊中的賦值

   最終 value = 200

⑥ 使用（Using）：正常使用類和對象

⑦ 卸載（Unloading）：類不再使用，GC 回收
```

#### 初始化的觸發條件（主動引用）：

```java
// ✅ 以下情況會觸發類初始化：
Demo demo = new Demo();          // 1. new 對象
int v = Demo.value;              // 2. 訪問靜態變數（非 final 常量）
Demo.staticMethod();             // 3. 調用靜態方法
Class.forName("com.example.Demo"); // 4. 反射
new Child();                     // 5. 子類初始化時，父類先初始化

// ❌ 以下情況「不會」觸發初始化（被動引用）：
int v = Demo.NAME;               // 訪問 final 常量 → 不觸發
Demo[] arr = new Demo[10];       // 建立陣列 → 不觸發
```

---

### 🟡 雙親委派模型（Parent Delegation Model）

```
類加載器層次結構：

              ┌─────────────────────┐
              │  Bootstrap ClassLoader │  ← 啟動類加載器
              │  （C++ 實現，Java 看不到）│    載入 JDK 核心類
              │  路徑：jre/lib/rt.jar  │    String, Object, ...
              └──────────┬──────────┘
                         │  parent
              ┌──────────┴──────────┐
              │  Extension ClassLoader │  ← 擴展類加載器
              │  路徑：jre/lib/ext/*.jar│    載入 JDK 擴展類
              └──────────┬──────────┘
                         │  parent
              ┌──────────┴──────────┐
              │  Application ClassLoader│ ← 應用程序類加載器
              │  路徑：classpath        │   載入你寫的類
              └──────────┬──────────┘
                         │  parent
              ┌──────────┴──────────┐
              │   Custom ClassLoader  │  ← 自定義類加載器
              └─────────────────────┘
```

#### 雙親委派流程：

```
當 AppClassLoader 要載入 "com.example.Demo" 時：

AppClassLoader：
    └→ 先委派給 ExtClassLoader
        └→ 再委派給 BootstrapClassLoader
            └→ Bootstrap 嘗試載入 → 找不到
        ← Bootstrap 返回「找不到」
        ExtClassLoader 嘗試載入 → 找不到
    ← Ext 返回「找不到」
    AppClassLoader 自己載入 → 找到了！✅

當要載入 "java.lang.String" 時：
AppClassLoader → ExtClassLoader → Bootstrap
Bootstrap 在 rt.jar 找到了！✅ 使用 Bootstrap 載入的 String
```

#### 為什麼要有雙親委派？

```java
// 安全性：你自己寫了一個 java.lang.String
package java.lang;
public class String {
    static {
        System.out.println("我入侵了 JVM！"); // 惡意代碼
    }
}
// 雙親委派保護：Bootstrap 從 rt.jar 找到了真正的 String.class
// 你的惡意 String 永遠不會被載入！✅
```

#### 自定義 ClassLoader：

```java
public class CustomClassLoader extends ClassLoader {

    private final String classPath;

    public CustomClassLoader(String classPath) {
        this.classPath = classPath;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] classData = loadClassData(name);
        if (classData == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, classData, 0, classData.length);
    }

    private byte[] loadClassData(String className) {
        String path = classPath + "/" +
                      className.replace('.', '/') + ".class";
        try (InputStream is  = new FileInputStream(path);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();

        } catch (IOException e) {
            return null;
        }
    }
}
```

#### 打破雙親委派：

```java
// 打破方式：重寫 loadClass() 方法（不調用 super）
public class BreakParentDelegation extends ClassLoader {

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        // 對自己的類，直接自己載入（打破委派）
        if (name.startsWith("com.example")) {
            return findClass(name);   // 跳過父加載器！
        }
        // 其他類還是走雙親委派（JDK 核心類必須走）
        return super.loadClass(name);
    }
}

// 打破雙親委派的典型案例：
// 1. JDBC（SPI 機制）
// 2. Tomcat（隔離不同 Web 應用的類）
// 3. OSGi（模塊化框架）
```

---

## 4️⃣ GC 垃圾回收

---

### 🔵 如何判斷對象死亡

#### 方式一：引用計數法（Java 不用！）

```java
// 引用計數法的致命缺陷：循環引用！
public class Node {
    public Node next;
}

Node a = new Node(); // a 引用計數 = 1
Node b = new Node(); // b 引用計數 = 1
a.next = b;          // b 引用計數 = 2
b.next = a;          // a 引用計數 = 2
a = null;            // a 引用計數 = 1（還有 b.next 指向它）
b = null;            // b 引用計數 = 1（還有 a.next 指向它）
// 兩個對象互相引用，引用計數永遠不為 0 → 記憶體洩漏 ❌
```

#### 方式二：可達性分析（Java 使用！）

```
GC Roots（根節點）：
  ✅ 虛擬機棧中的引用（局部變數）
  ✅ 方法區中的靜態變數引用
  ✅ 方法區中的常量引用
  ✅ 本地方法棧中的引用
  ✅ 被 synchronized 持有的對象

                 GC Root 1    GC Root 2
                    │              │
                    ↓              ↓
                  對象 A         對象 C
                    │              │
                    ↓              ↓
                  對象 B         對象 D

                  對象 E ←→ 對象 F （循環引用，但沒有 GC Root 到達）

結論：
✅ A、B、C、D → 可達 → 存活
❌ E、F → 不可達 → 可以回收（即使互相引用）
```

#### 四種引用類型：

```java
// 1. 強引用（Strong Reference）- 最常見
Object obj = new Object(); // GC 永遠不回收
obj = null; // 斷開引用後才可能被回收

// 2. 軟引用（Soft Reference）- 記憶體不足時回收
SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]);
byte[] data = softRef.get(); // 可能為 null（已被回收）
// 用途：做緩存（記憶體夠就保留，不夠就回收）

// 3. 弱引用（Weak Reference）- 下次 GC 就回收
WeakReference<Object> weakRef = new WeakReference<>(new Object());
// 只要 GC 執行，就會被回收
// 用途：WeakHashMap（防止記憶體洩漏的 Map）

// 4. 虛引用（Phantom Reference）- 形同虛設
ReferenceQueue<Object> queue = new ReferenceQueue<>();
PhantomReference<Object> phantomRef =
    new PhantomReference<>(new Object(), queue);
phantomRef.get(); // 永遠返回 null！
// 用途：對象被回收時得到通知

// 引用強弱：強 > 軟 > 弱 > 虛
```

---

### 🟡 四大垃圾回收算法

#### 算法一：標記-清除（Mark-Sweep）

```
執行前：
┌──┬──┬──┬──┬──┬──┬──┬──┐
│A▼│  │B▼│  │  │C▼│  │D▼│   ▼ = 存活
└──┴──┴──┴──┴──┴──┴──┴──┘

執行後：
┌──┬──┬──┬──┬──┬──┬──┬──┐
│A │空│B │空│空│C │空│D │
└──┴──┴──┴──┴──┴──┴──┴──┘

缺點：
❌ 記憶體碎片化（空閒空間不連續）
❌ 下次分配大對象時找不到連續空間
```

#### 算法二：標記-整理（Mark-Compact）

```
執行後：
┌──┬──┬──┬──┬──┬──┬──┬──┐
│A │B │C │D │空│空│空│空│   ← 整理到左邊，右邊連續空閒
└──┴──┴──┴──┴──┴──┴──┴──┘

優點：✅ 沒有碎片
缺點：❌ 移動對象需要更新所有引用，效能較差（STW 時間長）
適用：老年代（G1/Serial Old）
```

#### 算法三：複製算法（Copying）

```
執行前（From 區）：              To 區（空）：
┌──┬──┬──┬──┐              ┌──┬──┬──┬──┐
│A▼│  │B▼│  │              │空│空│空│空│
└──┴──┴──┴──┘              └──┴──┴──┴──┘

執行後（From 清空）：           To 區（已整理）：
┌──┬──┬──┬──┐              ┌──┬──┬──┬──┐
│空│空│空│空│              │A │B │空│空│
└──┴──┴──┴──┘              └──┴──┴──┴──┘

優點：✅ 沒有碎片、速度快
缺點：❌ 浪費一半記憶體
適用：新生代（Eden + S0/S1 就是複製算法！）
```

#### 算法四：分代收集（Generational Collection）⭐

```
理論依據（弱分代假說）：
✅ 大多數對象朝生夕死（新生代）
✅ 少數對象長期存活（老年代）

新生代（Young Gen）→ 複製算法
  原因：存活率低（10%~20%），複製的對象少，速度快

老年代（Old Gen）→ 標記-清除 或 標記-整理
  原因：存活率高，複製算法太浪費空間

觸發 GC 的條件：
  Minor GC：Eden 區滿時觸發（回收新生代）
  Major GC：老年代空間不足時觸發（回收老年代）
  Full GC：Minor + Major，整個堆都回收（速度最慢！盡量避免）
```

---

### 🔴 六大 GC 收集器 ⭐

```
收集器發展歷程：
Serial → ParNew → Parallel Scavenge
                         ↓
             Serial Old / Parallel Old / CMS
                         ↓
                        G1
                         ↓
                    ZGC / Shenandoah（Java 11+）
```

#### 收集器一：Serial GC（序列）

```
特點：單線程，GC 時 STW（Stop The World）
Young Gen：複製算法
Old Gen：標記-整理

GC 期間：
  業務線程：████████████│等待│████████████
  GC 線程：            │GC  │

適用：小型應用（Client 模式），記憶體小於 100MB
JVM 參數：-XX:+UseSerialGC
```

#### 收集器二：ParNew GC（並行）

```
特點：Serial 的多線程版，GC 時仍 STW，但多個 GC 線程同時工作
Young Gen：複製算法
Old Gen：配合 CMS 使用

JVM 參數：-XX:+UseParNewGC
```

#### 收集器三：Parallel Scavenge GC（吞吐量優先）

```
目標：最大化吞吐量（Throughput = 運行用戶代碼時間 / 總時間）
適用：後台計算型應用（批次處理、科學計算）

JVM 參數：
-XX:+UseParallelGC
-XX:MaxGCPauseMillis=200    最大停頓時間目標
-XX:GCTimeRatio=99          吞吐量目標（99% 時間用在業務）
-XX:+UseAdaptiveSizePolicy  自動調整各區大小
```

#### 收集器四：CMS GC（低延遲，已廢棄）

```
目標：最短 GC 停頓時間（低延遲）
Old Gen：標記-清除算法

CMS 執行過程（4 個階段）：

階段 1：初始標記（Initial Mark）
  STW！但很短
  只標記 GC Roots 直接關聯的對象

階段 2：並發標記（Concurrent Mark）
  ✅ 與業務線程並發執行！不 STW

階段 3：重新標記（Remark）
  STW！比初始標記稍長
  修正並發標記期間業務線程變動的標記

階段 4：並發清除（Concurrent Sweep）
  ✅ 與業務線程並發執行！不 STW

時間軸：
業務線程：████│停│███████████│停│███████████
GC 線程：     │初│==並發標記==│重│==並發清除==

缺點：
❌ 標記-清除 → 記憶體碎片化
❌ 並發執行佔用 CPU 資源
❌ 浮動垃圾

JVM 參數：-XX:+UseConcMarkSweepGC（Java 14 已移除）
```

#### 收集器五：G1 GC（Garbage First）⭐ Java 9+ 預設

```
核心思想：
  不再固定劃分新生代/老年代區域
  將整個堆分成多個大小相等的 Region（區域）

堆結構：
┌───┬───┬───┬───┬───┬───┬───┬───┐
│ E │ E │ O │ E │ S │ O │ E │ H │
├───┼───┼───┼───┼───┼───┼───┼───┤
│ O │ S │ E │ O │ E │ H │ O │ E │
└───┴───┴───┴───┴───┴───┴───┴───┘
E = Eden   S = Survivor   O = Old   H = Humongous（大對象）

特點：
✅ Region 可以動態分配角色
✅ 優先回收垃圾最多的 Region（Garbage First！）
✅ 可預測的停頓時間（-XX:MaxGCPauseMillis=200）
✅ 整合了複製 + 標記-整理，沒有碎片

G1 執行過程：
① Young GC：Eden 滿了，回收新生代 Region（STW，複製算法）
② Concurrent Marking：並發標記，標記整個堆的存活對象
③ Mixed GC：同時回收新生代 + 部分老年代
④ Full GC（緊急情況）：G1 來不及回收時，單線程，非常慢！要避免

JVM 參數：
-XX:+UseG1GC                           開啟 G1（Java 9+ 預設）
-XX:MaxGCPauseMillis=200               最大停頓目標（毫秒）
-XX:G1HeapRegionSize=16m               Region 大小（1MB~32MB，2的冪次）
-XX:G1NewSizePercent=5                 新生代最小佔比
-XX:G1MaxNewSizePercent=60             新生代最大佔比
-XX:InitiatingHeapOccupancyPercent=45  堆使用率超過 45% 開始並發標記
```

#### 收集器六：ZGC（Java 15 正式）⭐

```
目標：超低延遲（停頓時間 < 10ms，甚至 < 1ms！）
最大支援：16TB 的堆！

核心技術：染色指針（Colored Pointers）+ 讀屏障（Load Barrier）

ZGC 移動對象時幾乎全程並發！
業務線程訪問舊地址時，讀屏障自動轉發到新地址

對比：
┌──────────┬────────────┬─────────────┐
│ 收集器    │ 最大停頓時間  │ 適用場景      │
├──────────┼────────────┼─────────────┤
│ Serial   │ 幾百ms~幾秒 │ 小型應用      │
│ Parallel │ 幾百ms      │ 後台批次      │
│ G1       │ <200ms     │ 大多數應用    │
│ ZGC      │ <10ms      │ 超低延遲系統   │
└──────────┴────────────┴─────────────┘

JVM 參數：-XX:+UseZGC
Java 21 ZGC 進一步優化，停頓時間可達 < 1ms！
```

---

## 5️⃣ Java 記憶體模型（JMM）

---

### 🔵 JMM 核心概念

```
問題：多核 CPU 下，每個核心都有自己的快取（L1/L2 Cache）
      當多個線程讀寫同一個變數時，可能看到不一樣的值！

主記憶體（Main Memory）：所有線程共享，存放所有變數
本地記憶體（Working Memory）：每個線程私有的快取

┌──────────────────────────────────────────────────────┐
│                    主記憶體（共享）                      │
│          int x = 0;    boolean flag = false;          │
└──────────────┬──────────────────┬────────────────────┘
               │                  │
    ┌──────────┴──────┐  ┌────────┴────────┐
    │   線程 A 的工作記憶體 │  │   線程 B 的工作記憶體 │
    │   x = 0 (副本)   │  │   x = 0 (副本)   │
    └─────────────────┘  └─────────────────┘

線程 A 修改 x = 1：
  1. 在工作記憶體中改為 x = 1
  2. 寫回主記憶體（不一定立即！）

線程 B 讀取 x：
  如果 B 的工作記憶體中是舊副本 x = 0
  線程 B 看到的還是 0！→ 可見性問題
```

---

### 🟡 三大問題：原子性、可見性、有序性

```java
// 1. 原子性問題
int count = 0;
// count++ 不是原子操作！分解為三步：
// ① 讀取 count（load）
// ② count + 1（add）
// ③ 寫回 count（store）
// 多線程並發 count++ 會發生數據丟失！

// 2. 可見性問題
boolean flag = false;
// 線程 A：flag = true; → 寫入工作記憶體，未同步到主記憶體
// 線程 B：while(!flag) {} → 一直讀取工作記憶體的 flag = false → 死循環！

// 3. 有序性問題（指令重排序）
// 例：double-checked locking（DCL）的問題
public class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();
                    // new Singleton() 分解為三步：
                    // ① 分配記憶體空間
                    // ② 初始化對象
                    // ③ instance 指向記憶體地址
                    // 指令重排後可能是 ① → ③ → ②
                    // 另一個線程看到 instance != null，但對象還沒初始化完！
                }
            }
        }
        return instance;
    }
}
```

---

### 🟠 volatile 底層原理

```java
// ✅ 加 volatile 解決可見性和有序性（但不保證原子性！）
public class Singleton {
    private static volatile Singleton instance; // ← volatile 關鍵字

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton(); // ✅ volatile 禁止重排序
                }
            }
        }
        return instance;
    }
}
```

```
volatile 底層原理（兩個機制）：

機制 1：記憶體屏障（Memory Barrier）
  在 volatile 寫操作後插入 StoreStore + StoreLoad 屏障
  在 volatile 讀操作前插入 LoadLoad + LoadStore 屏障
  → 禁止指令重排序！

機制 2：MESI 緩存一致性協議
  volatile 變數寫入時，強制刷新到主記憶體
  其他 CPU 核心的快取行標記為無效
  → 其他線程讀取時，必須從主記憶體重新載入

volatile 能做什麼：
  ✅ 保證可見性（修改後立即對其他線程可見）
  ✅ 保證有序性（禁止指令重排序）
  ❌ 不保證原子性！（count++ 仍然不安全）
```

```java
// volatile 的正確使用場景：

// ✅ 場景1：狀態標誌（單寫多讀）
public class Server {
    private volatile boolean running = true;

    public void stop() {
        running = false; // 一個線程寫
    }

    public void run() {
        while (running) { // 其他線程讀
            // 處理請求...
        }
    }
}

// ✅ 場景2：雙重檢查鎖定（DCL）
private static volatile Singleton instance;

// ❌ 不適合的場景：複合操作（需要 Atomic 類或 synchronized）
volatile int count = 0;
count++; // ❌ 不安全！需要 AtomicInteger.incrementAndGet()
```

---

### 🔴 happens-before 原則

```
happens-before 是 JMM 保證可見性和有序性的核心規則！

定義：如果操作 A happens-before 操作 B，
     則 A 的結果對 B 可見，且 A 在 B 之前執行

Java 中的 8 條 happens-before 規則：

1. 程序順序規則（Program Order Rule）
   同一個線程中，按代碼順序，前面的操作 HB 後面的操作

2. 監視器鎖規則（Monitor Lock Rule）
   unlock 操作 HB 後續的 lock 操作
   synchronized 解鎖後，其他線程加鎖能看到解鎖前的修改

3. volatile 規則（Volatile Variable Rule）
   volatile 寫 HB 後續對該變數的 volatile 讀

4. 線程啟動規則（Thread Start Rule）
   Thread.start() HB 新線程的任何操作

5. 線程終止規則（Thread Termination Rule）
   線程的任何操作 HB Thread.join() 返回

6. 線程中斷規則（Thread Interruption Rule）
   Thread.interrupt() HB 被中斷線程檢測到中斷

7. 對象終結規則（Finalizer Rule）
   對象初始化 HB finalize() 方法

8. 傳遞性規則（Transitivity）
   A HB B，B HB C → A HB C
```

```java
// happens-before 實際範例
public class HappensBeforeDemo {
    private int value = 0;
    private volatile boolean ready = false;

    // 線程 A
    public void writer() {
        value = 42;      // 操作①
        ready = true;    // 操作②（volatile 寫）
    }

    // 線程 B
    public void reader() {
        if (ready) {                         // 操作③（volatile 讀）
            System.out.println(value);       // 操作④
        }
    }

    // 分析：
    // ① HB ②（程序順序規則）
    // ② HB ③（volatile 規則）
    // ③ HB ④（程序順序規則）
    // 傳遞性：① HB ④
    // ✅ 線程 B 讀到 ready=true 時，一定能看到 value=42！
}
```

---

## 6️⃣ JVM 性能調優

---

### 🔵 常用 JVM 參數速查表

```bash
# ===== 堆記憶體設定 =====
-Xms2g                    # 堆初始大小（生產環境建議與 Xmx 相同，避免動態擴容）
-Xmx2g                    # 堆最大大小
-Xmn512m                  # 新生代大小（G1 不需要設定）
-XX:MetaspaceSize=256m    # Metaspace 初始大小
-XX:MaxMetaspaceSize=512m # Metaspace 最大大小

# ===== 棧設定 =====
-Xss512k                  # 每個線程的棧大小（預設 512k~1m）

# ===== GC 選擇 =====
-XX:+UseG1GC              # 使用 G1（Java 9+ 預設）
-XX:+UseZGC               # 使用 ZGC（超低延遲）
-XX:+UseParallelGC        # 使用 Parallel（吞吐量優先）

# ===== G1 調優 =====
-XX:MaxGCPauseMillis=200  # GC 最大停頓目標
-XX:G1HeapRegionSize=16m  # Region 大小

# ===== GC 日誌（Java 9+ 新格式）=====
-Xlog:gc*:file=/logs/gc.log:time,level,tags:filecount=10,filesize=100m

# ===== 診斷 =====
-XX:+HeapDumpOnOutOfMemoryError         # OOM 時自動 dump
-XX:HeapDumpPath=/logs/heapdump.hprof   # dump 文件路徑

# ===== JIT 編譯 =====
-XX:+TieredCompilation                  # 分層編譯（預設開啟）
-XX:CompileThreshold=10000              # 方法調用多少次後 JIT 編譯
```

---

### 🟡 GC Log 分析

```bash
# Java 11+ GC 日誌格式（-Xlog:gc*）：

[2.457s][info][gc,start] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
[2.461s][info][gc      ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
                                 200M->10M(512M) 4.123ms
                                 ↑      ↑   ↑      ↑
                              回收前  回收後  堆大小  停頓時間

# 關鍵指標解讀：
# Young GC 停頓 > 100ms  → 新生代太小，需要調大
# Full GC 頻繁           → 老年代太小 或 記憶體洩漏
# Humongous 對象多        → 大對象頻繁分配，需調整 Region 大小
```

---

### 🔴 OOM 排查完整流程

```
OOM 類型診斷：

1. java.lang.OutOfMemoryError: Java heap space
   → 堆記憶體不足

2. java.lang.OutOfMemoryError: GC overhead limit exceeded
   → GC 時間佔比超過 98%，但回收不到 2%

3. java.lang.OutOfMemoryError: Metaspace
   → 方法區不足（類太多、動態生成類）

4. java.lang.OutOfMemoryError: Direct buffer memory
   → NIO 直接記憶體不足（Netty、NIO 框架）

5. java.lang.StackOverflowError
   → 棧溢出（遞迴過深）

6. java.lang.OutOfMemoryError: unable to create new native thread
   → 線程數超過 OS 限制
```

```bash
# 排查工具

# 1. jps - 列出 JVM 進程
jps -l
# 輸出：12345 com.example.Application

# 2. jmap - 記憶體分析
jmap -heap 12345                                    # 查看堆使用情況
jmap -dump:format=b,file=heapdump.hprof 12345       # 生成 heap dump
jmap -histo 12345 | head -20                        # 查看對象統計 Top 20

# 3. jstack - 線程分析
jstack 12345 > thread_dump.txt
grep -A 5 "BLOCKED" thread_dump.txt                 # 查找 BLOCKED 線程（死鎖）

# 4. jstat - GC 統計（每 1000ms 打印一次，共 10 次）
jstat -gcutil 12345 1000 10
# 輸出：
# S0     S1    E      O      M     CCS   YGC   YGCT  FGC   FGCT  GCT
# 0.00  98.55  89.45  23.18  96.50  91.38  15    0.234  0    0.000  0.234
# ↑S0   ↑S1   ↑Eden  ↑Old   ↑Meta  ↑Class ↑Young次數  ↑Full次數

# 5. jinfo - 查看 JVM 參數
jinfo -flags 12345
```

```java
// 常見記憶體洩漏場景：

// ❌ 1. 靜態集合持有大量對象（永遠不清理）
public class CacheManager {
    private static Map<String, Object> cache = new HashMap<>();

    public void add(String key, Object value) {
        cache.put(key, value); // 不斷累積，永不回收
    }
}

// ✅ 修復：使用弱引用或設定最大大小
private static Map<String, Object> cache = new WeakHashMap<>();
// 或使用 Caffeine 緩存（有大小限制）

// ❌ 2. 未關閉的資源
public void badCode() throws Exception {
    InputStream is = new FileInputStream("file.txt");
    // 忘記 close()！
}

// ✅ 修復：try-with-resources
public void goodCode() throws Exception {
    try (InputStream is = new FileInputStream("file.txt")) {
        // 自動關閉
    }
}

// ❌ 3. 非靜態內部類持有外部類引用
public class Outer {
    private byte[] bigData = new byte[1024 * 1024]; // 1MB

    // 非靜態內部類，隱式持有 Outer 的引用！
    class Inner {
        void doSomething() {}
    }
}

// ✅ 修復：改為靜態內部類
static class Inner { ... }
```

#### OOM 排查實戰案例：

```
案例：電商系統 Full GC 頻繁，每隔 30 分鐘一次

症狀：
  - Full GC 停頓 3~5 秒
  - GC 後老年代記憶體沒有明顯下降

排查過程：

Step 1：觀察 GC 情況
  jstat -gcutil <pid> 5000
  發現：Old 區使用率緩慢上升，最終觸發 Full GC

Step 2：生成 Heap Dump
  jmap -dump:format=b,file=heap.hprof <pid>

Step 3：MAT 分析
  發現：一個 HashMap 持有大量 Session 對象（每個 1KB）
  總計：50 萬個 Session 對象，500MB！

Step 4：定位程式碼
  發現：Session 管理器使用靜態 HashMap 儲存 Session
  但 Session 過期後只是標記 expired=true，沒有從 Map 中移除！

Step 5：修復
  改用 Caffeine Cache（有過期自動移除）：
  Cache<String, Session> sessionCache = Caffeine.newBuilder()
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .maximumSize(100_000)
      .build();

Step 6：效果
  Full GC 消失，老年代穩定在 30% 以下
```

---

## 📝 JVM 深度原理 總結

| 模組 | 核心要點 |
|------|---------|
| **記憶體模型** | 堆（新生代+老年代）/ 棧（線程私有）/ 方法區（Metaspace）|
| **堆結構** | Eden:S0:S1=8:1:1，新生代:老年代=1:2 |
| **類加載** | 載入→驗證→準備→解析→初始化，雙親委派保證安全 |
| **GC 判斷** | 可達性分析（GC Roots），4 種引用強弱 |
| **GC 算法** | 複製（新生代）/ 標記整理（老年代）|
| **GC 收集器** | G1（預設）/ ZGC（超低延遲）|
| **JMM** | 主記憶體 + 工作記憶體，三大問題：原子性/可見性/有序性 |
| **volatile** | 記憶體屏障 + MESI，保證可見性和有序性，不保證原子性 |
| **happens-before** | 8 大規則，保證多線程可見性的核心 |
| **調優** | jmap/jstat/jstack 三件套，Heap Dump 分析 MAT |
