# 🎨 前端深度 完整深入教學

---

## 🗺️ 學習路線圖

```
前端深度
├── 1️⃣ 瀏覽器渲染原理
│   ├── 關鍵渲染路徑
│   ├── 重排（Reflow）vs 重繪（Repaint）
│   └── 事件循環（Event Loop）
├── 2️⃣ JavaScript 核心原理
│   ├── 原型鏈 / 繼承
│   ├── 閉包（Closure）
│   ├── 作用域 / 變數提升
│   └── 非同步（Promise / async-await）
├── 3️⃣ TypeScript 深度
│   ├── 類型系統
│   ├── 泛型
│   └── 實用工具類型
├── 4️⃣ React 核心原理
│   ├── Virtual DOM / Diff 算法
│   ├── Fiber 架構
│   ├── Hooks 深度
│   └── 狀態管理
├── 5️⃣ Vue 核心原理
│   ├── 響應式系統（Vue 2 vs Vue 3）
│   ├── 虛擬 DOM / Diff
│   └── Composition API
└── 6️⃣ 前端效能優化
    ├── 載入效能
    ├── 渲染效能
    └── 網路效能
```

---

## 1️⃣ 瀏覽器渲染原理

---

### 🔵 關鍵渲染路徑（Critical Rendering Path）

```
瀏覽器收到 HTML 後的渲染流程：

① 解析 HTML → 建構 DOM Tree
   <html>
     <body>
       <div id="app">Hello</div>
     </body>
   </html>
   → DOM Tree（節點樹）

② 解析 CSS → 建構 CSSOM Tree
   div { color: red; font-size: 16px; }
   → CSSOM Tree（樣式規則樹）

③ DOM + CSSOM → 合成 Render Tree
   只包含可見節點（display:none 不在其中）
   每個節點帶有對應的樣式

④ Layout（佈局 / 重排）
   計算每個節點的幾何位置和大小
   → 輸出：每個元素的盒子模型（x, y, width, height）

⑤ Paint（繪製 / 重繪）
   按照 Render Tree 和 Layout 結果
   將像素繪製到圖層（Layer）上

⑥ Composite（合成）
   將多個圖層合成到螢幕上
   GPU 加速層（transform / opacity）→ 只需 Composite，跳過 Layout + Paint

渲染阻塞資源：
  CSS：阻塞渲染（需要等 CSSOM 建構完成）
  JS（沒有 defer/async）：阻塞 DOM 解析 + 渲染

最佳化：
  ✅ CSS 放 <head>（越早建構 CSSOM 越好）
  ✅ JS 放 <body> 底部 或使用 defer / async
  ✅ 關鍵 CSS 內嵌（Critical CSS），非關鍵 CSS 異步加載
```

---

### 🟡 重排（Reflow）vs 重繪（Repaint）

```
重排（Reflow）= Layout 重新計算
  觸發條件（影響幾何屬性）：
  - 添加/刪除 DOM 元素
  - 改變 width / height / margin / padding / border
  - 改變字體大小
  - 改變 display 屬性
  - 視窗 resize
  → 代價極高！會觸發整個 Layout 過程

重繪（Repaint）= Paint 重新繪製
  觸發條件（只影響外觀，不影響幾何）：
  - 改變 color / background-color / visibility
  - 改變 border-color
  → 代價較高，但比重排輕

合成（Composite）= 只在 GPU 合成層
  只觸發 Composite，完全不觸發 Layout + Paint！
  - transform（translate / scale / rotate）
  - opacity
  → 代價最低！✅ 動畫優先使用這兩個屬性

效能建議：
  ❌ 觸發重排的動畫（使用 top / left 移動元素）
  ✅ 使用 transform: translate() 移動元素（GPU 加速）

  ❌ 多次操作 DOM（每次都可能觸發重排）
  ✅ 使用 DocumentFragment 批量操作
     或 innerHTML 一次性更新

  ❌ 在迴圈中讀取 offsetWidth（強制同步佈局）
  for (let i = 0; i < elements.length; i++) {
      elements[i].style.width = container.offsetWidth + 'px'; // 讀寫交替！
  }
  ✅ 先讀後寫
  const containerWidth = container.offsetWidth; // 先讀
  for (let i = 0; i < elements.length; i++) {
      elements[i].style.width = containerWidth + 'px'; // 再批量寫
  }
```

---

### 🟠 事件循環（Event Loop）

```
JavaScript 是單線程的，Event Loop 讓它能處理非同步：

┌─────────────────────────────────────┐
│           Call Stack（調用棧）         │
│  執行同步程式碼，LIFO（後進先出）        │
└────────────────┬────────────────────┘
                 │ 棧空了
                 ↓
┌─────────────────────────────────────┐
│        Microtask Queue（微任務佇列）   │
│  Promise.then / MutationObserver    │
│  queueMicrotask                     │
│  優先於 Macrotask！清空後才執行 Macro  │
└────────────────┬────────────────────┘
                 │ 微任務佇列清空
                 ↓
┌─────────────────────────────────────┐
│        Macrotask Queue（宏任務佇列）   │
│  setTimeout / setInterval           │
│  I/O / UI rendering                 │
│  每次只取一個宏任務執行                 │
└─────────────────────────────────────┘

執行順序：
  同步程式碼 → 清空微任務佇列 → 執行一個宏任務
  → 清空微任務佇列 → 執行一個宏任務 → ...
```

```javascript
// 事件循環面試題
console.log('1');                          // 同步

setTimeout(() => console.log('2'), 0);    // 宏任務

Promise.resolve()
  .then(() => console.log('3'))           // 微任務
  .then(() => console.log('4'));          // 微任務

console.log('5');                          // 同步

// 輸出順序：1 → 5 → 3 → 4 → 2
// 分析：
// 同步：1, 5
// 微任務：3（then）, 4（then 的 then）
// 宏任務：2（setTimeout）
```

---

## 2️⃣ JavaScript 核心原理

---

### 🔵 原型鏈（Prototype Chain）

```javascript
// 每個物件都有 __proto__（原型）
// 函式有 prototype 屬性

function Animal(name) {
    this.name = name;
}

Animal.prototype.speak = function() {
    return `${this.name} 發出聲音`;
};

function Dog(name, breed) {
    Animal.call(this, name); // 繼承屬性
    this.breed = breed;
}

// 繼承原型
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;

Dog.prototype.bark = function() {
    return `${this.name} 汪汪叫`;
};

const dog = new Dog('小黑', '拉布拉多');

// 原型鏈：
// dog → Dog.prototype → Animal.prototype → Object.prototype → null
//
// dog.__proto__                   === Dog.prototype
// dog.__proto__.__proto__         === Animal.prototype
// dog.__proto__.__proto__.__proto__ === Object.prototype
// dog.__proto__.__proto__.__proto__.__proto__ === null

console.log(dog.speak()); // 小黑 發出聲音（從 Animal.prototype 找到）
console.log(dog.bark());  // 小黑 汪汪叫（從 Dog.prototype 找到）

// ES6 Class（語法糖，底層還是原型鏈）
class AnimalES6 {
    constructor(name) {
        this.name = name;
    }
    speak() {
        return `${this.name} 發出聲音`;
    }
}

class DogES6 extends AnimalES6 {
    constructor(name, breed) {
        super(name);          // 呼叫父類 constructor
        this.breed = breed;
    }
    bark() {
        return `${this.name} 汪汪叫`;
    }
}
```

---

### 🟡 閉包（Closure）

```javascript
// 閉包 = 函式 + 其詞法環境（可以存取外部函式的變數）

function makeCounter(initialValue = 0) {
    let count = initialValue; // 私有變數（外部無法直接訪問）

    return {
        increment() { return ++count; },
        decrement() { return --count; },
        getCount()  { return count; }
    };
}

const counter = makeCounter(10);
console.log(counter.increment()); // 11
console.log(counter.increment()); // 12
console.log(counter.getCount());  // 12
// count 無法從外部直接訪問！→ 數據私有化

// 閉包的常見陷阱（var vs let）
// ❌ 問題：var 沒有塊級作用域
for (var i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100);
}
// 輸出：3, 3, 3（都是同一個 i，循環已結束）

// ✅ 解法一：let（塊級作用域，每次循環獨立）
for (let i = 0; i < 3; i++) {
    setTimeout(() => console.log(i), 100);
}
// 輸出：0, 1, 2

// ✅ 解法二：IIFE（立即執行函式，每次傳入當前 i）
for (var i = 0; i < 3; i++) {
    ((j) => setTimeout(() => console.log(j), 100))(i);
}
// 輸出：0, 1, 2

// 閉包實際應用：函式柯里化（Currying）
function multiply(a) {
    return function(b) {
        return a * b; // 記住了 a
    };
}

const double = multiply(2);
const triple = multiply(3);
console.log(double(5)); // 10
console.log(triple(5)); // 15
```

---

### 🟠 非同步：Promise / async-await

```javascript
// Promise 三種狀態：pending → fulfilled / rejected

// 手動實作 Promise.all（面試常考）
function promiseAll(promises) {
    return new Promise((resolve, reject) => {
        const results = [];
        let completedCount = 0;

        if (promises.length === 0) {
            resolve([]);
            return;
        }

        promises.forEach((promise, index) => {
            Promise.resolve(promise)
                .then(result => {
                    results[index] = result;
                    completedCount++;
                    if (completedCount === promises.length) {
                        resolve(results); // 全部完成才 resolve
                    }
                })
                .catch(reject); // 任一失敗立即 reject
        });
    });
}

// async / await 錯誤處理
async function fetchUserData(userId) {
    try {
        const user    = await userService.findById(userId);
        const orders  = await orderService.findByUser(userId);
        return { user, orders };
    } catch (error) {
        console.error('查詢失敗', error);
        throw error;
    }
}

// 並行執行（不要串行 await，浪費時間！）
async function fetchParallel(userId) {
    // ❌ 串行（總耗時 = 各自耗時相加）
    const user   = await userService.findById(userId);   // 等 user 完成
    const orders = await orderService.findByUser(userId); // 才開始查 order

    // ✅ 並行（總耗時 = 最慢的那個）
    const [user2, orders2] = await Promise.all([
        userService.findById(userId),
        orderService.findByUser(userId)
    ]);

    return { user: user2, orders: orders2 };
}
```

---

## 3️⃣ TypeScript 深度

---

### 🔵 類型系統

```typescript
// 基本類型
let name:    string  = 'Alice';
let age:     number  = 25;
let active:  boolean = true;
let nothing: null    = null;
let undef:   undefined = undefined;

// 聯合類型（Union）
type ID = string | number;
let id: ID = 'user-123';
id = 456; // 也可以

// 字面量類型（精確值）
type Direction = 'LEFT' | 'RIGHT' | 'UP' | 'DOWN';
type StatusCode = 200 | 201 | 400 | 401 | 403 | 404 | 500;

// 介面（Interface）vs 類型別名（Type Alias）
interface User {
    id:       number;
    name:     string;
    email?:   string;      // 可選屬性
    readonly role: string; // 只讀屬性
}

// interface 可以繼承和合併
interface AdminUser extends User {
    permissions: string[];
}

// 同名 interface 自動合併（Declaration Merging）
interface User { age?: number; }
// User 現在有 id, name, email, role, age

// Type Alias 更靈活（支援聯合 / 交叉類型）
type ApiResponse<T> = {
    success: boolean;
    data:    T;
    message: string;
};

// 交叉類型（Intersection）= 合併
type AdminUser2 = User & { permissions: string[] };
```

---

### 🟡 泛型（Generics）

```typescript
// 泛型函式
function identity<T>(value: T): T {
    return value;
}

const str = identity<string>('Hello'); // string
const num = identity<number>(42);      // number
// TypeScript 可以推斷類型，不必明確指定

// 泛型介面
interface Repository<T> {
    findById(id: number): Promise<T>;
    findAll(): Promise<T[]>;
    save(entity: T): Promise<T>;
    delete(id: number): Promise<void>;
}

// 實作
class UserRepository implements Repository<User> {
    async findById(id: number): Promise<User> { /* ... */ }
    async findAll(): Promise<User[]>           { /* ... */ }
    async save(user: User): Promise<User>      { /* ... */ }
    async delete(id: number): Promise<void>    { /* ... */ }
}

// 泛型約束（Constraint）
function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
    return obj[key];
}

const user: User = { id: 1, name: 'Alice', role: 'admin' };
const userName = getProperty(user, 'name');  // string ✅
// getProperty(user, 'invalid');             // ❌ 編譯錯誤！

// 泛型類別
class Stack<T> {
    private items: T[] = [];

    push(item: T): void     { this.items.push(item); }
    pop(): T | undefined    { return this.items.pop(); }
    peek(): T | undefined   { return this.items[this.items.length - 1]; }
    isEmpty(): boolean      { return this.items.length === 0; }
    size(): number          { return this.items.length; }
}

const numberStack = new Stack<number>();
numberStack.push(1);
numberStack.push(2);
console.log(numberStack.pop()); // 2
```

---

### 🟠 實用工具類型（Utility Types）

```typescript
interface User {
    id:       number;
    name:     string;
    email:    string;
    password: string;
    role:     string;
    createdAt: Date;
}

// Partial<T>：所有屬性變為可選（更新請求常用）
type UpdateUserRequest = Partial<User>;
// { id?: number; name?: string; email?: string; ... }

// Required<T>：所有屬性變為必填
type RequiredUser = Required<User>;

// Readonly<T>：所有屬性變為只讀
type ReadonlyUser = Readonly<User>;

// Pick<T, K>：只取部分屬性（響應 DTO 常用）
type UserPublicInfo = Pick<User, 'id' | 'name' | 'role'>;
// { id: number; name: string; role: string; }

// Omit<T, K>：排除部分屬性（排除敏感欄位）
type UserWithoutPassword = Omit<User, 'password'>;
// { id, name, email, role, createdAt } - 沒有 password

// Record<K, V>：key-value 映射
type RolePermissions = Record<string, string[]>;
const permissions: RolePermissions = {
    'admin': ['read', 'write', 'delete'],
    'user':  ['read'],
};

// Exclude<T, U>：從聯合類型中排除
type Status  = 'pending' | 'active' | 'inactive' | 'deleted';
type Visible = Exclude<Status, 'deleted'>;
// 'pending' | 'active' | 'inactive'

// Extract<T, U>：從聯合類型中取交集
type ActiveStatus = Extract<Status, 'active' | 'pending'>;
// 'active' | 'pending'

// ReturnType<T>：取函式返回類型
async function getUser() { return new User(); }
type GetUserReturn = ReturnType<typeof getUser>; // Promise<User>

// Parameters<T>：取函式參數類型
function createOrder(userId: number, items: string[]): void {}
type CreateOrderParams = Parameters<typeof createOrder>;
// [userId: number, items: string[]]

// 實際應用：型別安全的 API 響應
type ApiResponse<T> = {
    success: true;
    data: T;
} | {
    success: false;
    error: string;
    code: string;
};

function handleResponse<T>(response: ApiResponse<T>): T {
    if (response.success) {
        return response.data; // TypeScript 知道這裡有 data
    }
    throw new Error(response.error); // TypeScript 知道這裡有 error
}
```

---

## 4️⃣ React 核心原理

---

### 🔵 Virtual DOM 與 Diff 算法

```
Virtual DOM 是什麼？
  用 JavaScript 物件描述真實 DOM 的輕量副本

  真實 DOM：
  <div id="app">
    <h1>Hello</h1>
    <p>World</p>
  </div>

  Virtual DOM：
  {
    type: 'div',
    props: { id: 'app' },
    children: [
      { type: 'h1', props: {}, children: ['Hello'] },
      { type: 'p',  props: {}, children: ['World'] }
    ]
  }

為什麼需要 Virtual DOM？
  直接操作真實 DOM 代價很高（觸發重排/重繪）
  Virtual DOM 在記憶體中比較差異（Diff）
  只對真實 DOM 做最小量的更新

Diff 算法三大策略：

① 同層比較（只比較同一層級，不跨層級）
  不同類型的元素 → 直接替換整棵子樹
  相同類型的元素 → 更新屬性，繼續比較子元素

② key 屬性（列表優化）
  相同 key → 認為是同一個元素，只更新屬性
  不同 key → 創建新元素，銷毀舊元素

  ❌ 不要用 index 作為 key（列表增刪時會錯誤匹配）
  ✅ 使用唯一且穩定的 id 作為 key

③ 組件類型
  同類型組件 → 更新 props，不重建
  不同類型組件 → 銷毀舊組件，創建新組件
```

---

### 🟡 Fiber 架構（React 16+）

```
Fiber 解決的問題：
  React 15 的 Stack Reconciler：
  → 遞迴同步更新 DOM，一旦開始無法中斷
  → 大量更新時，主線程被長時間佔用
  → 動畫卡頓 / 用戶輸入無響應

Fiber 的解決方式：
  將渲染工作拆成小單元（Fiber Node）
  每個 Fiber Node 對應一個 React 元素
  利用 requestIdleCallback 在瀏覽器空閒時執行
  可以中斷、恢復、重用工作

Fiber 兩個階段：

① Reconciliation（協調，可中斷）：
  遍歷 Fiber Tree，找出需要更新的節點
  標記副作用（新增 / 更新 / 刪除）
  可以被更高優先級的工作中斷

② Commit（提交，不可中斷）：
  將 Reconciliation 的結果一次性應用到真實 DOM
  執行 useEffect / useLayoutEffect

優先級調度（Concurrent Mode）：
  不同類型的更新有不同優先級
  用戶輸入（最高優先級）→ 立即響應
  動畫 → 高優先級
  數據加載 → 低優先級
  → 高優先級任務可以打斷低優先級任務
```

---

### 🟠 Hooks 深度

```javascript
// ════════════════════════════════
// useState - 狀態管理
// ════════════════════════════════
function Counter() {
    const [count, setCount] = useState(0);

    // ✅ 使用函式更新（基於前一個值）
    const increment = () => setCount(prev => prev + 1);

    // ❌ 直接用 count（閉包陷阱，並發模式下可能不正確）
    const wrongIncrement = () => setCount(count + 1);

    return <button onClick={increment}>{count}</button>;
}

// ════════════════════════════════
// useEffect - 副作用處理
// ════════════════════════════════
function UserProfile({ userId }) {
    const [user, setUser] = useState(null);

    useEffect(() => {
        let cancelled = false; // 防止競態條件

        async function fetchUser() {
            const data = await userService.findById(userId);
            if (!cancelled) { // 組件未卸載才更新狀態
                setUser(data);
            }
        }

        fetchUser();

        // 清理函式（組件卸載或 userId 改變時執行）
        return () => { cancelled = true; };

    }, [userId]); // 依賴陣列：userId 改變時重新執行

    return <div>{user?.name}</div>;
}

// ════════════════════════════════
// useCallback / useMemo - 效能優化
// ════════════════════════════════
function ProductList({ products, onAddToCart }) {
    // useMemo：快取計算結果（只在依賴改變時重新計算）
    const expensiveProducts = useMemo(() => {
        return products.filter(p => p.price > 1000);
    }, [products]); // products 改變才重新計算

    // useCallback：快取函式（只在依賴改變時創建新函式）
    const handleAddToCart = useCallback((productId) => {
        onAddToCart(productId);
    }, [onAddToCart]); // onAddToCart 改變才創建新函式

    return (
        <ul>
            {expensiveProducts.map(product => (
                <ProductItem
                    key={product.id}
                    product={product}
                    onAdd={handleAddToCart}
                />
            ))}
        </ul>
    );
}

// ════════════════════════════════
// 自定義 Hook（Custom Hook）
// ════════════════════════════════
function useFetch<T>(url: string) {
    const [data,    setData]    = useState<T | null>(null);
    const [loading, setLoading] = useState(true);
    const [error,   setError]   = useState<Error | null>(null);

    useEffect(() => {
        let cancelled = false;

        setLoading(true);
        setError(null);

        fetch(url)
            .then(res => res.json())
            .then(data => {
                if (!cancelled) {
                    setData(data);
                    setLoading(false);
                }
            })
            .catch(err => {
                if (!cancelled) {
                    setError(err);
                    setLoading(false);
                }
            });

        return () => { cancelled = true; };
    }, [url]);

    return { data, loading, error };
}

// 使用自定義 Hook
function UserList() {
    const { data: users, loading, error } = useFetch<User[]>('/api/users');

    if (loading) return <Spinner />;
    if (error)   return <ErrorMessage error={error} />;

    return (
        <ul>
            {users?.map(user => <li key={user.id}>{user.name}</li>)}
        </ul>
    );
}
```

---

### 🔴 狀態管理

```javascript
// ════════════════════════════════
// Context API（輕量狀態共享）
// ════════════════════════════════
const UserContext = createContext<User | null>(null);

function UserProvider({ children }) {
    const [user, setUser] = useState<User | null>(null);

    const login  = async (credentials) => {
        const user = await authService.login(credentials);
        setUser(user);
    };
    const logout = () => setUser(null);

    return (
        <UserContext.Provider value={{ user, login, logout }}>
            {children}
        </UserContext.Provider>
    );
}

// 自定義 Hook 封裝
function useUser() {
    const context = useContext(UserContext);
    if (!context) throw new Error('useUser 必須在 UserProvider 內使用');
    return context;
}

// ════════════════════════════════
// Zustand（輕量狀態管理，推薦）
// ════════════════════════════════
import { create } from 'zustand';

interface CartState {
    items:      CartItem[];
    addItem:    (item: CartItem) => void;
    removeItem: (id: string) => void;
    clearCart:  () => void;
    total:      () => number;
}

const useCartStore = create<CartState>((set, get) => ({
    items: [],

    addItem: (item) => set(state => ({
        items: [...state.items, item]
    })),

    removeItem: (id) => set(state => ({
        items: state.items.filter(item => item.id !== id)
    })),

    clearCart: () => set({ items: [] }),

    total: () => get().items.reduce((sum, item) =>
        sum + item.price * item.quantity, 0
    ),
}));

// 使用
function Cart() {
    const { items, removeItem, total } = useCartStore();

    return (
        <div>
            {items.map(item => (
                <CartItem
                    key={item.id}
                    item={item}
                    onRemove={() => removeItem(item.id)}
                />
            ))}
            <div>總計：{total()}</div>
        </div>
    );
}
```

---

## 5️⃣ Vue 核心原理

---

### 🔵 響應式系統對比（Vue 2 vs Vue 3）

```javascript
// Vue 2：Object.defineProperty（有缺陷）
const data = { count: 0, items: [] };

Object.defineProperty(data, 'count', {
    get() {
        // 依賴收集（Watcher 訂閱）
        return value;
    },
    set(newVal) {
        value = newVal;
        // 觸發更新（通知所有 Watcher）
    }
});

// Vue 2 的缺陷：
// ❌ 無法偵測屬性新增（必須用 Vue.set）
//    data.newProp = 'value' → 不響應！
//    Vue.set(data, 'newProp', 'value') → ✅

// ❌ 無法偵測陣列 index 直接修改
//    data.items[0] = 'new' → 不響應！
//    需要用 push/pop/splice 等變異方法

// Vue 3：Proxy（完全解決 Vue 2 的問題）
const reactiveData = new Proxy(data, {
    get(target, key, receiver) {
        // 依賴追蹤
        track(target, key);
        return Reflect.get(target, key, receiver);
    },
    set(target, key, value, receiver) {
        const result = Reflect.set(target, key, value, receiver);
        // 觸發更新
        trigger(target, key);
        return result;
    },
    // 還可以攔截 deleteProperty、has 等操作
    deleteProperty(target, key) {
        const result = Reflect.deleteProperty(target, key);
        trigger(target, key);
        return result;
    }
});

// Vue 3 的優勢：
// ✅ 偵測屬性新增/刪除
// ✅ 偵測陣列 index 修改
// ✅ 惰性響應（深層屬性只在訪問時才響應化）
// ✅ 效能更好（不需要初始化時遍歷所有屬性）
```

---

### 🟡 Composition API

```javascript
// Options API（Vue 2 風格）
// 問題：相關邏輯分散在 data / methods / computed / watch
export default {
    data() {
        return { count: 0, user: null };
    },
    methods: {
        increment() { this.count++; },
        async fetchUser() { /* 邏輯 */ }
    },
    computed: {
        doubleCount() { return this.count * 2; }
    },
    watch: {
        count(newVal) { console.log(newVal); }
    }
};

// Composition API（Vue 3）
// 優勢：相關邏輯集中在一起，更好的可複用性
import { ref, reactive, computed, watch, onMounted } from 'vue';

// 可複用的邏輯（類比 React Custom Hook）
function useCounter(initialValue = 0) {
    const count = ref(initialValue);

    const doubleCount = computed(() => count.value * 2);

    function increment() { count.value++; }
    function decrement() { count.value--; }
    function reset()     { count.value = initialValue; }

    watch(count, (newVal, oldVal) => {
        console.log(`count 從 ${oldVal} 變為 ${newVal}`);
    });

    return { count, doubleCount, increment, decrement, reset };
}

function useUser(userId) {
    const user    = ref(null);
    const loading = ref(false);
    const error   = ref(null);

    async function fetchUser() {
        loading.value = true;
        try {
            user.value = await userService.findById(userId);
        } catch (e) {
            error.value = e;
        } finally {
            loading.value = false;
        }
    }

    onMounted(fetchUser);

    return { user, loading, error, refetch: fetchUser };
}

// 在組件中使用
export default {
    setup() {
        const { count, doubleCount, increment } = useCounter(0);
        const { user, loading } = useUser(1);

        return { count, doubleCount, increment, user, loading };
    }
};

// <script setup> 語法糖（Vue 3.2+，更簡潔）
```

---

## 6️⃣ 前端效能優化

---

### 🔵 載入效能

```
代碼分割（Code Splitting）：
  ❌ 一個巨大的 bundle.js（首屏加載所有代碼）
  ✅ 按路由分割，只加載當前頁面需要的代碼

// React（React.lazy）
const ProductPage = React.lazy(() => import('./pages/ProductPage'));
const OrderPage   = React.lazy(() => import('./pages/OrderPage'));

function App() {
    return (
        <Suspense fallback={<LoadingSpinner />}>
            <Routes>
                <Route path="/products" element={<ProductPage />} />
                <Route path="/orders"   element={<OrderPage />} />
            </Routes>
        </Suspense>
    );
}

// Vite / Webpack 動態 import
const module = await import('./heavy-module.js');

Tree Shaking（去除未使用的代碼）：
  ✅ 使用 ES Module（import/export）
  Webpack / Rollup / Vite 自動移除未引用的代碼

  ❌ import * as _ from 'lodash';     // 引入整個 lodash
  ✅ import { debounce } from 'lodash'; // 只引入需要的函式

資源優化：
  圖片：
    ✅ WebP 格式（比 JPEG 小 25~34%）
    ✅ 圖片懶加載（<img loading="lazy">）
    ✅ 響應式圖片（<picture> / srcset）
    ✅ 雪碧圖（Sprite）合併小圖示

  字型：
    ✅ 只加載需要的字重和字符集
    ✅ font-display: swap（先用系統字型，字型加載完再替換）
```

---

### 🟡 渲染效能

```javascript
// React 效能優化

// ① React.memo（避免不必要的重渲染）
const ProductItem = React.memo(function ProductItem({ product, onAdd }) {
    // 只有 product 或 onAdd 改變時才重渲染
    return (
        <div>
            <span>{product.name}</span>
            <button onClick={() => onAdd(product.id)}>加入購物車</button>
        </div>
    );
}, (prevProps, nextProps) => {
    // 自定義比較（返回 true = 不重渲染）
    return prevProps.product.id    === nextProps.product.id &&
           prevProps.product.price === nextProps.product.price;
});

// ② 虛擬化長列表（react-window）
import { FixedSizeList } from 'react-window';

function VirtualList({ items }) {
    const Row = ({ index, style }) => (
        <div style={style}>
            {items[index].name}
        </div>
    );

    return (
        <FixedSizeList
            height={600}       // 可視區域高度
            width={400}
            itemCount={items.length}
            itemSize={50}      // 每行高度
        >
            {Row}
        </FixedSizeList>
    );
    // 無論 items 有多少筆，只渲染可視區域的 DOM 節點
}

// ③ 防抖（debounce）/ 節流（throttle）
// 防抖：最後一次觸發後 delay ms 才執行（搜索輸入）
function useDebounce<T>(value: T, delay: number): T {
    const [debouncedValue, setDebouncedValue] = useState(value);

    useEffect(() => {
        const timer = setTimeout(() => setDebouncedValue(value), delay);
        return () => clearTimeout(timer);
    }, [value, delay]);

    return debouncedValue;
}

// 節流：固定時間間隔最多執行一次（滾動事件）
function throttle(fn: Function, limit: number) {
    let lastTime = 0;
    return function(...args) {
        const now = Date.now();
        if (now - lastTime >= limit) {
            lastTime = now;
            fn.apply(this, args);
        }
    };
}
```

---

### 🟠 網路效能

```
HTTP 快取策略：
  靜態資源（JS/CSS/圖片）：
    Cache-Control: public, max-age=31536000, immutable
    搭配 Content Hash 命名（app.abc123.js）
    → 長期快取，內容改變 URL 改變

  HTML 文件：
    Cache-Control: no-cache
    → 每次驗證，確保總是最新的 HTML

預加載（Preload / Prefetch）：
  // 關鍵資源提前加載（當前頁面需要）
  <link rel="preload" href="/fonts/font.woff2" as="font" crossorigin>
  <link rel="preload" href="/api/critical-data" as="fetch" crossorigin>

  // 下一個頁面可能需要（空閒時加載）
  <link rel="prefetch" href="/pages/product.chunk.js">

  // DNS 預解析
  <link rel="dns-prefetch" href="//api.example.com">
  <link rel="preconnect" href="//api.example.com">

Service Worker（離線快取 / PWA）：
  攔截 fetch 請求
  快取資源到 Cache Storage
  網路斷開時仍能提供服務

// 簡單的 Service Worker 快取策略
self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request).then(cached => {
            // 有快取 → 返回快取（同時更新快取）
            if (cached) {
                // Stale-While-Revalidate 策略
                fetch(event.request).then(response => {
                    caches.open('v1').then(cache => {
                        cache.put(event.request, response);
                    });
                });
                return cached;
            }
            // 沒快取 → 請求網路並快取
            return fetch(event.request).then(response => {
                const cloned = response.clone();
                caches.open('v1').then(cache => {
                    cache.put(event.request, cloned);
                });
                return response;
            });
        })
    );
});
```

---

## 📝 前端深度 總結

| 主題 | 核心要點 |
|------|---------|
| **關鍵渲染路徑** | HTML→DOM / CSS→CSSOM → Render Tree → Layout → Paint → Composite |
| **重排 vs 重繪** | 重排代價最高，transform/opacity 只觸發 Composite |
| **Event Loop** | 同步 → 微任務（Promise）→ 宏任務（setTimeout）|
| **原型鏈** | 物件透過 `__proto__` 串連，ES6 Class 是語法糖 |
| **閉包** | 函式記住外部作用域，實現私有變數和函式工廠 |
| **Promise** | pending → fulfilled/rejected，鏈式 .then，all/race |
| **TypeScript 泛型** | 類型參數化，`<T>` 讓程式碼同時通用又型別安全 |
| **Utility Types** | Partial/Omit/Pick/Record 等，減少重複型別定義 |
| **Virtual DOM** | JS 物件描述 DOM，Diff 後最小化真實 DOM 操作 |
| **Fiber** | 可中斷的協調，優先級調度，解決主線程卡頓 |
| **React Hooks** | useState/useEffect/useCallback/useMemo/自定義 Hook |
| **Vue 3 Proxy** | 解決 Vue 2 無法偵測屬性新增/陣列 index 修改的問題 |
| **Composition API** | 邏輯集中，可複用，類比 React Custom Hook |
| **Code Splitting** | React.lazy + import() 按路由分割，減少首屏 bundle |
| **React.memo** | 避免子組件不必要重渲染，搭配 useCallback 使用 |
| **虛擬化列表** | react-window 只渲染可視區域，處理萬筆資料 |
| **HTTP 快取** | 靜態資源長期快取 + Hash 命名，HTML 用 no-cache |
