# 🟡 演算法與資料結構 完整深入教學

---

## 🗺️ 學習路線圖

```
演算法與資料結構
├── 1️⃣ 複雜度分析
│   ├── 時間複雜度
│   └── 空間複雜度
├── 2️⃣ 陣列 / 字串
│   ├── 雙指針
│   └── 滑動視窗
├── 3️⃣ 鏈表
│   ├── 反轉鏈表
│   ├── 環檢測
│   └── 合併鏈表
├── 4️⃣ 二元樹
│   ├── DFS（前中後序）
│   ├── BFS（層序遍歷）
│   └── 常見題型
├── 5️⃣ 動態規劃
│   ├── 背包問題
│   └── 最長子序列
├── 6️⃣ 排序演算法
│   ├── 快速排序
│   └── 合併排序
└── 7️⃣ HashMap 應用題
```

---

## 1️⃣ 複雜度分析

---

```
常見時間複雜度（由快到慢）：

O(1)        → 常數時間（Hash 查詢、陣列索引）
O(log n)    → 對數時間（二分搜尋、平衡樹查詢）
O(n)        → 線性時間（單次遍歷）
O(n log n)  → 線性對數（快排、合併排序）
O(n²)       → 平方時間（雙層迴圈、暴力比對）
O(2ⁿ)       → 指數時間（暴力枚舉子集）

面試常見複雜度：
  二分搜尋：     O(log n)
  HashMap 操作： O(1) 平均
  快速排序：     O(n log n) 平均，O(n²) 最壞
  合併排序：     O(n log n) 穩定
  BFS / DFS：    O(V + E)（節點 + 邊）
```

---

## 2️⃣ 陣列 / 字串

---

### 🔵 雙指針（Two Pointers）

```
核心思想：
  用兩個指針（左右 or 快慢）
  在一次遍歷中完成需要兩次遍歷才能完成的事
  時間複雜度從 O(n²) 降到 O(n)
```

```java
// ════════════════════════════════
// 題目 1：兩數之和（已排序陣列）
// 輸入：nums = [2, 7, 11, 15], target = 9
// 輸出：[0, 1]（nums[0] + nums[1] = 9）
// ════════════════════════════════
public int[] twoSum(int[] nums, int target) {
    int left = 0, right = nums.length - 1;

    while (left < right) {
        int sum = nums[left] + nums[right];

        if (sum == target) {
            return new int[]{left, right};
        } else if (sum < target) {
            left++;   // 和太小，左指針右移（增大和）
        } else {
            right--;  // 和太大，右指針左移（減小和）
        }
    }
    return new int[]{-1, -1};
}
// 時間：O(n)，空間：O(1)

// ════════════════════════════════
// 題目 2：反轉字串
// 輸入：["h","e","l","l","o"]
// 輸出：["o","l","l","e","h"]
// ════════════════════════════════
public void reverseString(char[] s) {
    int left = 0, right = s.length - 1;

    while (left < right) {
        char temp   = s[left];
        s[left]     = s[right];
        s[right]    = temp;
        left++;
        right--;
    }
}
// 時間：O(n)，空間：O(1)

// ════════════════════════════════
// 題目 3：三數之和（找所有不重複的三元組使和為 0）
// 輸入：nums = [-1, 0, 1, 2, -1, -4]
// 輸出：[[-1,-1,2],[-1,0,1]]
// ════════════════════════════════
public List<List<Integer>> threeSum(int[] nums) {
    List<List<Integer>> result = new ArrayList<>();
    Arrays.sort(nums); // 先排序！

    for (int i = 0; i < nums.length - 2; i++) {
        // 跳過重複的 i（去重）
        if (i > 0 && nums[i] == nums[i - 1]) continue;
        // 最小值已經大於 0，後面不可能有解
        if (nums[i] > 0) break;

        int left  = i + 1;
        int right = nums.length - 1;

        while (left < right) {
            int sum = nums[i] + nums[left] + nums[right];

            if (sum == 0) {
                result.add(Arrays.asList(nums[i], nums[left], nums[right]));
                // 跳過重複的 left 和 right
                while (left < right && nums[left]  == nums[left + 1])  left++;
                while (left < right && nums[right] == nums[right - 1]) right--;
                left++;
                right--;
            } else if (sum < 0) {
                left++;
            } else {
                right--;
            }
        }
    }
    return result;
}
// 時間：O(n²)，空間：O(1)
```

---

### 🟡 滑動視窗（Sliding Window）

```
核心思想：
  維護一個可變長度的視窗（子陣列 / 子字串）
  右邊界向右擴展（加入新元素）
  當視窗不滿足條件時，左邊界向右收縮
  整體時間複雜度 O(n)
```

```java
// ════════════════════════════════
// 題目 1：無重複字元的最長子字串
// 輸入："abcabcbb"
// 輸出：3（"abc"）
// ════════════════════════════════
public int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> window = new HashMap<>();
    int left = 0, maxLen = 0;

    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        window.put(c, window.getOrDefault(c, 0) + 1);

        // 視窗中有重複字元 → 收縮左邊界
        while (window.get(c) > 1) {
            char leftChar = s.charAt(left);
            window.put(leftChar, window.get(leftChar) - 1);
            left++;
        }

        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
// 時間：O(n)，空間：O(k)（k = 字符集大小）

// ════════════════════════════════
// 題目 2：長度最小的子陣列（和 ≥ target）
// 輸入：target = 7, nums = [2,3,1,2,4,3]
// 輸出：2（[4,3] 的長度）
// ════════════════════════════════
public int minSubArrayLen(int target, int[] nums) {
    int left = 0, sum = 0;
    int minLen = Integer.MAX_VALUE;

    for (int right = 0; right < nums.length; right++) {
        sum += nums[right]; // 右邊界擴展

        // 滿足條件 → 嘗試收縮左邊界（找更短的）
        while (sum >= target) {
            minLen = Math.min(minLen, right - left + 1);
            sum -= nums[left];
            left++;
        }
    }
    return minLen == Integer.MAX_VALUE ? 0 : minLen;
}
// 時間：O(n)，空間：O(1)

// ════════════════════════════════
// 題目 3：找所有字母異位詞
// 輸入：s = "cbaebabacd", p = "abc"
// 輸出：[0, 6]（s[0..2]="cba", s[6..8]="bac" 是 p 的異位詞）
// ════════════════════════════════
public List<Integer> findAnagrams(String s, String p) {
    List<Integer> result = new ArrayList<>();
    if (s.length() < p.length()) return result;

    int[] pCount = new int[26];
    int[] wCount = new int[26];

    // 初始化 p 的字符統計 和 第一個視窗
    for (int i = 0; i < p.length(); i++) {
        pCount[p.charAt(i) - 'a']++;
        wCount[s.charAt(i) - 'a']++;
    }

    if (Arrays.equals(pCount, wCount)) result.add(0);

    // 滑動視窗
    for (int right = p.length(); right < s.length(); right++) {
        wCount[s.charAt(right) - 'a']++;                    // 加入右邊
        wCount[s.charAt(right - p.length()) - 'a']--;       // 移出左邊
        if (Arrays.equals(pCount, wCount)) {
            result.add(right - p.length() + 1);
        }
    }
    return result;
}
```

---

## 3️⃣ 鏈表

---

### 🔵 反轉鏈表

```java
// 鏈表節點定義
public class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

// ════════════════════════════════
// 題目 1：反轉整個鏈表（迭代法）
// 輸入：1→2→3→4→5
// 輸出：5→4→3→2→1
// ════════════════════════════════
public ListNode reverseList(ListNode head) {
    ListNode prev = null;
    ListNode curr = head;

    while (curr != null) {
        ListNode next = curr.next; // 暫存下一個節點
        curr.next = prev;          // 反轉指向
        prev = curr;               // prev 前進
        curr = next;               // curr 前進
    }
    return prev; // prev 最終指向新的頭節點
}
// 時間：O(n)，空間：O(1)

// 遞迴法（理解遞迴思維）
public ListNode reverseListRecursive(ListNode head) {
    // 基礎情況：空或只有一個節點
    if (head == null || head.next == null) return head;

    // 遞迴反轉後半部分
    ListNode newHead = reverseListRecursive(head.next);

    // 讓下一個節點指回自己
    head.next.next = head;
    head.next = null;

    return newHead;
}

// ════════════════════════════════
// 題目 2：反轉鏈表的前 N 個節點
// ════════════════════════════════
private ListNode successor = null; // 記錄後繼節點

public ListNode reverseN(ListNode head, int n) {
    if (n == 1) {
        successor = head.next; // 記錄第 n+1 個節點
        return head;
    }
    ListNode newHead = reverseN(head.next, n - 1);
    head.next.next = head;
    head.next = successor; // 連接後繼節點
    return newHead;
}

// ════════════════════════════════
// 題目 3：反轉鏈表區間 [left, right]
// 輸入：1→2→3→4→5，left=2，right=4
// 輸出：1→4→3→2→5
// ════════════════════════════════
public ListNode reverseBetween(ListNode head, int left, int right) {
    // 建立虛擬頭節點，簡化邊界處理
    ListNode dummy = new ListNode(-1);
    dummy.next = head;
    ListNode pre = dummy;

    // pre 移動到 left 的前一個位置
    for (int i = 0; i < left - 1; i++) {
        pre = pre.next;
    }

    ListNode curr = pre.next;
    // 執行 right - left 次「頭插法」
    for (int i = 0; i < right - left; i++) {
        ListNode next = curr.next;
        curr.next     = next.next;
        next.next     = pre.next;
        pre.next      = next;
    }
    return dummy.next;
}
```

---

### 🟡 環檢測（Floyd 龜兔賽跑）

```java
// ════════════════════════════════
// 題目 1：判斷鏈表是否有環
// ════════════════════════════════
public boolean hasCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;        // 慢指針每次走 1 步
        fast = fast.next.next;   // 快指針每次走 2 步

        if (slow == fast) return true; // 相遇 → 有環
    }
    return false; // fast 到達終點 → 無環
}
// 時間：O(n)，空間：O(1)

// ════════════════════════════════
// 題目 2：找環的入口節點
// ════════════════════════════════
public ListNode detectCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    // 第一步：找到相遇點
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) break;
    }

    // 無環
    if (fast == null || fast.next == null) return null;

    // 第二步：一個指針從頭出發，一個從相遇點出發
    // 兩者步速相同，再次相遇處就是環的入口
    // 數學原理：head 到入口的距離 = 相遇點到入口的距離
    slow = head;
    while (slow != fast) {
        slow = slow.next;
        fast = fast.next;
    }
    return slow;
}
```

---

### 🟠 合併鏈表

```java
// ════════════════════════════════
// 題目 1：合併兩個有序鏈表
// 輸入：1→2→4, 1→3→4
// 輸出：1→1→2→3→4→4
// ════════════════════════════════
public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
    ListNode dummy = new ListNode(-1); // 虛擬頭節點
    ListNode curr  = dummy;

    while (l1 != null && l2 != null) {
        if (l1.val <= l2.val) {
            curr.next = l1;
            l1 = l1.next;
        } else {
            curr.next = l2;
            l2 = l2.next;
        }
        curr = curr.next;
    }

    // 接上剩餘部分
    curr.next = (l1 != null) ? l1 : l2;
    return dummy.next;
}
// 時間：O(m+n)，空間：O(1)

// ════════════════════════════════
// 題目 2：找鏈表中間節點（快慢指針）
// 輸入：1→2→3→4→5
// 輸出：3（中間節點）
// ════════════════════════════════
public ListNode middleNode(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;

    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
    }
    // fast 到達末尾時，slow 剛好在中間
    return slow;
}

// ════════════════════════════════
// 題目 3：刪除鏈表倒數第 N 個節點
// 輸入：1→2→3→4→5，n=2
// 輸出：1→2→3→5
// ════════════════════════════════
public ListNode removeNthFromEnd(ListNode head, int n) {
    ListNode dummy = new ListNode(-1);
    dummy.next = head;
    ListNode fast = dummy;
    ListNode slow = dummy;

    // fast 先走 n+1 步
    for (int i = 0; i <= n; i++) {
        fast = fast.next;
    }

    // fast 和 slow 同步走，直到 fast 到達末尾
    while (fast != null) {
        fast = fast.next;
        slow = slow.next;
    }

    // slow 的下一個就是要刪除的節點
    slow.next = slow.next.next;
    return dummy.next;
}
```

---

## 4️⃣ 二元樹

---

### 🔵 DFS（深度優先搜尋）

```java
// 樹節點定義
public class TreeNode {
    int val;
    TreeNode left, right;
    TreeNode(int val) { this.val = val; }
}

// ════════════════════════════════
// 前序遍歷：根 → 左 → 右
// ════════════════════════════════
public List<Integer> preorderTraversal(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    preorder(root, result);
    return result;
}

private void preorder(TreeNode node, List<Integer> result) {
    if (node == null) return;
    result.add(node.val);          // 根
    preorder(node.left,  result);  // 左
    preorder(node.right, result);  // 右
}

// ════════════════════════════════
// 中序遍歷：左 → 根 → 右（BST 的中序 = 升序！）
// ════════════════════════════════
private void inorder(TreeNode node, List<Integer> result) {
    if (node == null) return;
    inorder(node.left,  result);   // 左
    result.add(node.val);          // 根
    inorder(node.right, result);   // 右
}

// ════════════════════════════════
// 後序遍歷：左 → 右 → 根
// ════════════════════════════════
private void postorder(TreeNode node, List<Integer> result) {
    if (node == null) return;
    postorder(node.left,  result); // 左
    postorder(node.right, result); // 右
    result.add(node.val);          // 根
}

// ════════════════════════════════
// 題目 1：二元樹的最大深度
// ════════════════════════════════
public int maxDepth(TreeNode root) {
    if (root == null) return 0;

    int leftDepth  = maxDepth(root.left);
    int rightDepth = maxDepth(root.right);

    return Math.max(leftDepth, rightDepth) + 1;
}
// 時間：O(n)，空間：O(h)（h = 樹高）

// ════════════════════════════════
// 題目 2：判斷是否為對稱二元樹
// ════════════════════════════════
public boolean isSymmetric(TreeNode root) {
    return isMirror(root.left, root.right);
}

private boolean isMirror(TreeNode left, TreeNode right) {
    if (left == null && right == null) return true;
    if (left == null || right == null) return false;

    return left.val == right.val
        && isMirror(left.left,  right.right)
        && isMirror(left.right, right.left);
}

// ════════════════════════════════
// 題目 3：路徑總和（是否存在根到葉子的路徑，其和等於 target）
// ════════════════════════════════
public boolean hasPathSum(TreeNode root, int targetSum) {
    if (root == null) return false;

    // 到達葉子節點，判斷剩餘值是否等於葉子節點值
    if (root.left == null && root.right == null) {
        return targetSum == root.val;
    }

    return hasPathSum(root.left,  targetSum - root.val)
        || hasPathSum(root.right, targetSum - root.val);
}

// ════════════════════════════════
// 題目 4：二元樹的最低公共祖先（LCA）
// ════════════════════════════════
public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
    // 基礎情況：空節點 或 找到 p 或 q
    if (root == null || root == p || root == q) return root;

    // 在左右子樹中分別尋找
    TreeNode left  = lowestCommonAncestor(root.left,  p, q);
    TreeNode right = lowestCommonAncestor(root.right, p, q);

    // 左右都找到 → 當前節點就是 LCA
    if (left != null && right != null) return root;

    // 只有一側找到 → 返回那一側
    return left != null ? left : right;
}
```

---

### 🟡 BFS（廣度優先搜尋 / 層序遍歷）

```java
// ════════════════════════════════
// 題目 1：二元樹的層序遍歷
// 輸入：    3
//          / \
//         9  20
//            / \
//           15   7
// 輸出：[[3],[9,20],[15,7]]
// ════════════════════════════════
public List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;

    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
        int  levelSize = queue.size(); // 當前層的節點數
        List<Integer> level = new ArrayList<>();

        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);

            if (node.left  != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        result.add(level);
    }
    return result;
}
// 時間：O(n)，空間：O(n)

// ════════════════════════════════
// 題目 2：二元樹的右視圖
// 輸出每層最右邊的節點
// 輸出：[3, 20, 7]
// ════════════════════════════════
public List<Integer> rightSideView(TreeNode root) {
    List<Integer> result = new ArrayList<>();
    if (root == null) return result;

    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);

    while (!queue.isEmpty()) {
        int levelSize = queue.size();
        for (int i = 0; i < levelSize; i++) {
            TreeNode node = queue.poll();
            // 每層最後一個節點加入結果
            if (i == levelSize - 1) result.add(node.val);
            if (node.left  != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
    }
    return result;
}
```

---

## 5️⃣ 動態規劃（Dynamic Programming）

---

```
動態規劃解題框架：
  ① 定義狀態（dp[i] 代表什麼？）
  ② 找狀態轉移方程（dp[i] 怎麼從前面的狀態得到？）
  ③ 確定初始值（dp[0] / dp[1] 是多少？）
  ④ 確定遍歷順序（從小到大還是從大到小？）
```

---

### 🔵 基礎 DP

```java
// ════════════════════════════════
// 題目 1：爬樓梯（Fibonacci 變形）
// 每次可以爬 1 或 2 階，到達 n 階有幾種方法？
// ════════════════════════════════
public int climbStairs(int n) {
    if (n <= 2) return n;

    // dp[i] = 到達第 i 階的方法數
    int[] dp = new int[n + 1];
    dp[1] = 1;
    dp[2] = 2;

    for (int i = 3; i <= n; i++) {
        dp[i] = dp[i - 1] + dp[i - 2]; // 從 i-1 爬 1 步 或從 i-2 爬 2 步
    }
    return dp[n];
}
// 時間：O(n)，空間：O(n)

// 空間優化（只需要前兩個值）
public int climbStairsOptimized(int n) {
    if (n <= 2) return n;
    int prev2 = 1, prev1 = 2;
    for (int i = 3; i <= n; i++) {
        int curr = prev1 + prev2;
        prev2 = prev1;
        prev1 = curr;
    }
    return prev1;
}
// 時間：O(n)，空間：O(1)

// ════════════════════════════════
// 題目 2：打家劫舍（不能搶相鄰的房子）
// 輸入：[2,7,9,3,1]
// 輸出：12（2+9+1）
// ════════════════════════════════
public int rob(int[] nums) {
    if (nums.length == 1) return nums[0];

    // dp[i] = 搶前 i 個房子能獲得的最大金額
    int[] dp = new int[nums.length];
    dp[0] = nums[0];
    dp[1] = Math.max(nums[0], nums[1]);

    for (int i = 2; i < nums.length; i++) {
        // 搶第 i 個：dp[i-2] + nums[i]
        // 不搶第 i 個：dp[i-1]
        dp[i] = Math.max(dp[i - 1], dp[i - 2] + nums[i]);
    }
    return dp[nums.length - 1];
}
```

---

### 🟡 背包問題

```java
// ════════════════════════════════
// 0/1 背包問題
// n 個物品，每個有重量 weight[i] 和價值 value[i]
// 背包容量為 W，求能裝的最大價值
// ════════════════════════════════
public int knapsack(int W, int[] weight, int[] value) {
    int n = weight.length;
    // dp[i][j] = 前 i 個物品，背包容量為 j 時的最大價值
    int[][] dp = new int[n + 1][W + 1];

    for (int i = 1; i <= n; i++) {
        for (int j = 0; j <= W; j++) {
            // 裝不下第 i 個物品
            if (j < weight[i - 1]) {
                dp[i][j] = dp[i - 1][j];
            } else {
                // 不裝 vs 裝
                dp[i][j] = Math.max(
                    dp[i - 1][j],
                    dp[i - 1][j - weight[i - 1]] + value[i - 1]
                );
            }
        }
    }
    return dp[n][W];
}
// 時間：O(n×W)，空間：O(n×W)

// ════════════════════════════════
// 題目：分割等和子集（背包的應用）
// 輸入：[1,5,11,5]
// 輸出：true（[1,5,5] 和 [11] 等和）
// ════════════════════════════════
public boolean canPartition(int[] nums) {
    int sum = Arrays.stream(nums).sum();
    if (sum % 2 != 0) return false; // 和為奇數，不可能分割

    int target = sum / 2;
    // dp[j] = 是否能從數組中選出若干數，使其和恰好為 j
    boolean[] dp = new boolean[target + 1];
    dp[0] = true; // 選 0 個數，和為 0

    for (int num : nums) {
        // ⚠️ 從大到小遍歷（0/1 背包，每個物品只用一次）
        for (int j = target; j >= num; j--) {
            dp[j] = dp[j] || dp[j - num];
        }
    }
    return dp[target];
}
```

---

### 🟠 最長子序列

```java
// ════════════════════════════════
// 題目 1：最長遞增子序列（LIS）
// 輸入：[10,9,2,5,3,7,101,18]
// 輸出：4（[2,3,7,101]）
// ════════════════════════════════
public int lengthOfLIS(int[] nums) {
    int n = nums.length;
    // dp[i] = 以 nums[i] 結尾的最長遞增子序列長度
    int[] dp = new int[n];
    Arrays.fill(dp, 1); // 每個元素本身就是長度 1 的子序列

    int maxLen = 1;
    for (int i = 1; i < n; i++) {
        for (int j = 0; j < i; j++) {
            if (nums[j] < nums[i]) { // nums[i] 可以接在 nums[j] 後面
                dp[i] = Math.max(dp[i], dp[j] + 1);
            }
        }
        maxLen = Math.max(maxLen, dp[i]);
    }
    return maxLen;
}
// 時間：O(n²)，空間：O(n)

// ════════════════════════════════
// 題目 2：最長公共子序列（LCS）
// 輸入："abcde" 和 "ace"
// 輸出：3（"ace"）
// ════════════════════════════════
public int longestCommonSubsequence(String text1, String text2) {
    int m = text1.length(), n = text2.length();
    // dp[i][j] = text1 前 i 個字符 和 text2 前 j 個字符的最長公共子序列
    int[][] dp = new int[m + 1][n + 1];

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (text1.charAt(i - 1) == text2.charAt(j - 1)) {
                // 末尾字符相同 → 公共子序列長度 + 1
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                // 末尾字符不同 → 取捨一個末尾字符的最大值
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[m][n];
}
// 時間：O(m×n)，空間：O(m×n)

// ════════════════════════════════
// 題目 3：最小編輯距離
// 輸入："horse" → "ros"
// 輸出：3（替換 h→r, 刪除 r, 刪除 e）
// ════════════════════════════════
public int minDistance(String word1, String word2) {
    int m = word1.length(), n = word2.length();
    // dp[i][j] = word1 前 i 個字符 轉換為 word2 前 j 個字符的最少操作數
    int[][] dp = new int[m + 1][n + 1];

    // 初始化：空字串轉換需要 i 或 j 次操作
    for (int i = 0; i <= m; i++) dp[i][0] = i;
    for (int j = 0; j <= n; j++) dp[0][j] = j;

    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1]; // 字符相同，不需要操作
            } else {
                dp[i][j] = Math.min(
                    dp[i - 1][j] + 1,     // 刪除 word1[i]
                    Math.min(
                        dp[i][j - 1] + 1, // 插入 word2[j]
                        dp[i - 1][j - 1] + 1 // 替換
                    )
                );
            }
        }
    }
    return dp[m][n];
}
```

---

## 6️⃣ 排序演算法

---

### 🔵 快速排序（QuickSort）

```java
// 平均 O(n log n)，最壞 O(n²)（已排序時）
// 原地排序，空間 O(log n)（遞迴棧）
// 不穩定排序

public void quickSort(int[] nums, int left, int right) {
    if (left >= right) return;

    int pivotIndex = partition(nums, left, right);
    quickSort(nums, left,          pivotIndex - 1);
    quickSort(nums, pivotIndex + 1, right);
}

private int partition(int[] nums, int left, int right) {
    // 隨機選基準值（避免最壞情況）
    int randomIndex = left + (int)(Math.random() * (right - left + 1));
    swap(nums, randomIndex, right);

    int pivot = nums[right];
    int i     = left - 1; // i 指向小於 pivot 的區域末尾

    for (int j = left; j < right; j++) {
        if (nums[j] <= pivot) {
            i++;
            swap(nums, i, j);
        }
    }
    swap(nums, i + 1, right); // 把 pivot 放到正確位置
    return i + 1;
}

private void swap(int[] nums, int i, int j) {
    int temp = nums[i];
    nums[i]  = nums[j];
    nums[j]  = temp;
}

// 使用
int[] nums = {3, 6, 8, 10, 1, 2, 1};
quickSort(nums, 0, nums.length - 1);
// 結果：[1, 1, 2, 3, 6, 8, 10]
```

---

### 🟡 合併排序（MergeSort）

```java
// 穩定 O(n log n)，空間 O(n)
// 適合鏈表排序、外部排序（數據量超過記憶體）

public void mergeSort(int[] nums, int left, int right) {
    if (left >= right) return;

    int mid = left + (right - left) / 2; // 避免溢出
    mergeSort(nums, left,    mid);
    mergeSort(nums, mid + 1, right);
    merge(nums, left, mid, right);
}

private void merge(int[] nums, int left, int mid, int right) {
    // 複製到臨時陣列
    int[] temp = Arrays.copyOfRange(nums, left, right + 1);

    int i = 0;                    // 左半部分指針
    int j = mid - left + 1;      // 右半部分指針
    int k = left;                 // 原陣列指針

    while (i <= mid - left && j <= right - left) {
        if (temp[i] <= temp[j]) {
            nums[k++] = temp[i++];
        } else {
            nums[k++] = temp[j++];
        }
    }

    while (i <= mid - left)  nums[k++] = temp[i++];
    while (j <= right - left) nums[k++] = temp[j++];
}
```

---

### 🟠 排序演算法對比

```
演算法        時間複雜度（平均）  時間複雜度（最壞）  空間      穩定
─────────────────────────────────────────────────────────────────
氣泡排序      O(n²)             O(n²)             O(1)      ✅
選擇排序      O(n²)             O(n²)             O(1)      ❌
插入排序      O(n²)             O(n²)             O(1)      ✅
快速排序      O(n log n)        O(n²)             O(log n)  ❌
合併排序      O(n log n)        O(n log n)        O(n)      ✅
堆積排序      O(n log n)        O(n log n)        O(1)      ❌
計數排序      O(n + k)          O(n + k)          O(k)      ✅

Java Arrays.sort()：
  基本型別 → 雙軸快速排序（DualPivotQuicksort）
  物件型別 → TimSort（合併排序 + 插入排序的混合）
```

---

## 7️⃣ HashMap 應用題

---

```java
// ════════════════════════════════
// 題目 1：兩數之和（未排序版本）
// 輸入：nums = [2,7,11,15], target = 9
// 輸出：[0,1]
// ════════════════════════════════
public int[] twoSum(int[] nums, int target) {
    // key = 數值, value = 索引
    Map<Integer, Integer> map = new HashMap<>();

    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];

        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    return new int[]{};
}
// 時間：O(n)，空間：O(n)

// ════════════════════════════════
// 題目 2：字母異位詞分組
// 輸入：["eat","tea","tan","ate","nat","bat"]
// 輸出：[["bat"],["nat","tan"],["ate","eat","tea"]]
// ════════════════════════════════
public List<List<String>> groupAnagrams(String[] strs) {
    // key = 排序後的字串（異位詞排序後相同）
    Map<String, List<String>> map = new HashMap<>();

    for (String str : strs) {
        char[] chars = str.toCharArray();
        Arrays.sort(chars);
        String key = new String(chars);

        map.computeIfAbsent(key, k -> new ArrayList<>()).add(str);
    }
    return new ArrayList<>(map.values());
}
// 時間：O(n × k log k)（k = 最長字串長度）

// ════════════════════════════════
// 題目 3：和為 K 的子陣列（前綴和 + HashMap）
// 輸入：nums = [1,1,1], k = 2
// 輸出：2（共 2 個子陣列和為 2）
// ════════════════════════════════
public int subarraySum(int[] nums, int k) {
    // prefixSumCount[sum] = 前綴和為 sum 的次數
    Map<Integer, Integer> prefixSumCount = new HashMap<>();
    prefixSumCount.put(0, 1); // 前綴和為 0 出現 1 次（空子陣列）

    int count = 0, prefixSum = 0;

    for (int num : nums) {
        prefixSum += num;

        // 如果存在 prefixSum - k 的前綴和
        // 說明從那個位置到當前位置的子陣列和為 k
        count += prefixSumCount.getOrDefault(prefixSum - k, 0);

        prefixSumCount.put(prefixSum,
            prefixSumCount.getOrDefault(prefixSum, 0) + 1);
    }
    return count;
}
// 時間：O(n)，空間：O(n)

// ════════════════════════════════
// 題目 4：LRU 緩存（LinkedHashMap 實作）
// ════════════════════════════════
class LRUCache extends LinkedHashMap<Integer, Integer> {
    private final int capacity;

    public LRUCache(int capacity) {
        // accessOrder = true：按訪問順序排序（最近訪問的在最後）
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        // 當 size > capacity 時，移除最久未使用的 entry（最前面的）
        return size() > capacity;
    }
}

// 手動實作 LRU（雙向鏈表 + HashMap，面試版）
class LRUCacheManual {
    private final int capacity;
    private final Map<Integer, Node> map = new HashMap<>();

    // 虛擬頭尾節點（簡化邊界處理）
    private final Node head = new Node(0, 0);
    private final Node tail = new Node(0, 0);

    private static class Node {
        int key, val;
        Node prev, next;
        Node(int key, int val) { this.key = key; this.val = val; }
    }

    public LRUCacheManual(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        Node node = map.get(key);
        moveToHead(node); // 訪問後移到頭部（最近使用）
        return node.val;
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            Node node = map.get(key);
            node.val = value;
            moveToHead(node);
        } else {
            Node newNode = new Node(key, value);
            map.put(key, newNode);
            addToHead(newNode);

            if (map.size() > capacity) {
                Node removed = removeTail(); // 移除最久未使用的
                map.remove(removed.key);
            }
        }
    }

    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private Node removeTail() {
        Node node = tail.prev;
        removeNode(node);
        return node;
    }
}
```

---

## 📝 演算法解題思路速查

```
拿到題目，先問自己：

陣列 / 字串：
  有序？→ 二分搜尋 or 雙指針
  找子陣列 / 子字串？→ 滑動視窗
  需要計數 / 去重？→ HashMap

鏈表：
  需要找中點 / 判斷環？→ 快慢指針
  需要處理頭節點？→ 建虛擬頭節點

樹：
  需要對每個節點做判斷？→ DFS（遞迴）
  需要按層處理？→ BFS（Queue）
  BST？→ 中序遍歷是有序的

動態規劃：
  求最大 / 最小 / 計數 / 是否可行？→ 考慮 DP
  有最優子結構？→ 定義狀態，找轉移方程

排序：
  需要排序後處理？→ 先排序 O(n log n)
  Top K 問題？→ 優先隊列（堆積）O(n log k)
```

---

## 📝 演算法與資料結構 總結

| 類型 | 演算法 | 時間複雜度 | 關鍵思路 |
|------|--------|-----------|---------|
| **雙指針** | 兩數之和 / 三數之和 / 反轉字串 | O(n) | 左右指針逼近，排序後使用 |
| **滑動視窗** | 最長無重複子字串 / 最小子陣列 | O(n) | 右擴左縮，維護視窗狀態 |
| **鏈表** | 反轉 / 環檢測 / 找中點 | O(n) | 虛擬頭節點 / 快慢指針 |
| **樹 DFS** | 最大深度 / 路徑和 / LCA | O(n) | 遞迴，想清楚返回值的意義 |
| **樹 BFS** | 層序遍歷 / 右視圖 | O(n) | Queue，每層用 levelSize 控制 |
| **DP 基礎** | 爬樓梯 / 打家劫舍 | O(n) | 定義狀態，找轉移方程 |
| **背包 DP** | 0/1 背包 / 分割等和子集 | O(n×W) | 二維 → 一維，從大到小遍歷 |
| **序列 DP** | LIS / LCS / 編輯距離 | O(n²) | 以 i 結尾 / 前 i 個字符的最優值 |
| **快速排序** | 分治 + 分割 | O(n log n) 平均 | 隨機基準，Partition 原地交換 |
| **合併排序** | 分治 + 合併 | O(n log n) 穩定 | 拆分到單元素，再合併 |
| **HashMap** | 兩數之和 / 子陣列和 / LRU | O(n) | 以空間換時間，記錄出現過的值 |
| **前綴和** | 子陣列和為 K | O(n) | prefixSum[i] - prefixSum[j] = 區間和 |
