# 🌐 網路基礎 完整深入教學

---

## 🗺️ 學習路線圖

```
網路基礎
├── 1️⃣ TCP / IP 模型
│   ├── 四層 / 七層模型
│   └── 各層協議對應
├── 2️⃣ TCP 深度
│   ├── 三次握手
│   ├── 四次揮手
│   ├── 可靠傳輸機制
│   └── 常見面試題
├── 3️⃣ HTTP 協議演進
│   ├── HTTP 1.0 / 1.1 / 2.0 / 3.0
│   └── 關鍵差異對比
├── 4️⃣ HTTPS 與 TLS
│   ├── TLS 握手流程
│   ├── 對稱 vs 非對稱加密
│   └── 憑證體系
├── 5️⃣ DNS 解析流程
└── 6️⃣ CDN 原理
```

---

## 1️⃣ TCP / IP 模型

---

### 🔵 四層 vs 七層模型對照

```
OSI 七層模型          TCP/IP 四層模型      常見協議
──────────────────────────────────────────────────────
7. 應用層（Application）  ┐
6. 表示層（Presentation） ├→ 應用層          HTTP / HTTPS / FTP
5. 會話層（Session）      ┘                  WebSocket / DNS / SMTP

4. 傳輸層（Transport）    → 傳輸層           TCP / UDP

3. 網路層（Network）      → 網路層           IP / ICMP / ARP

2. 資料鏈路層（Data Link）┐
1. 實體層（Physical）     ┘→ 網路介面層       Ethernet / Wi-Fi

資料封裝過程（從上到下）：
應用層    → HTTP Message（應用資料）
傳輸層    → TCP Segment（加 TCP Header：源/目標埠號）
網路層    → IP Packet（加 IP Header：源/目標 IP）
資料鏈路層 → Ethernet Frame（加 MAC 地址）
實體層    → 0101 二進位位元串
```

---

## 2️⃣ TCP 深度

---

### 🔵 三次握手（Three-Way Handshake）

```
為什麼需要三次握手？
→ 確認雙方的「發送」和「接收」能力都正常

         Client                    Server
           │                         │
           │──── SYN（seq=x）────────→│  Client 發起連接請求
           │                         │  Server 收到，知道 Client 能發送
           │                         │
           │←─ SYN+ACK（seq=y,ack=x+1）│  Server 確認並回應
           │                         │  Client 收到，知道 Server 能發送和接收
           │                         │
           │──── ACK（ack=y+1）──────→│  Client 確認
           │                         │  Server 收到，知道 Client 能接收
           │                         │
           │    [連接建立 ESTABLISHED] │

SYN：SYNchronize（同步）
ACK：ACKnowledge（確認）
seq：序列號（Sequence Number）
ack：確認號（Acknowledgment Number）= 期望收到的下一個序列號

為什麼不是兩次？
  兩次只能確認 Client 能發、Server 能收
  無法確認 Server 能發、Client 能收
  → 無法保證全雙工連接可靠

為什麼不是四次？
  三次已經足夠確認雙方能力
  第三次 ACK 可以順便攜帶數據
  四次是浪費
```

---

### 🟡 四次揮手（Four-Way Handshake）

```
為什麼需要四次揮手？
→ TCP 是全雙工的，關閉需要雙方各自關閉自己的發送方向

         Client                    Server
           │                         │
           │──── FIN（seq=u）────────→│  Client 說：我不發數據了
           │                         │  [Client → FIN_WAIT_1]
           │                         │
           │←──── ACK（ack=u+1）─────│  Server 說：好的，收到了
           │                         │  [Client → FIN_WAIT_2]
           │                         │  但 Server 可能還有數據要發！
           │                         │  繼續發送剩餘數據...
           │                         │
           │←──── FIN（seq=v）────────│  Server 說：我也不發了
           │                         │  [Server → LAST_ACK]
           │                         │
           │──── ACK（ack=v+1）──────→│  Client 說：好的，收到了
           │                         │  [Client → TIME_WAIT]
           │                         │  [等待 2MSL 後進入 CLOSED]
           │                         │  [Server 收到 ACK → CLOSED]

為什麼不是三次？
  Server 收到 Client 的 FIN 後
  可能還有數據沒發完（半關閉狀態）
  所以 ACK 和 FIN 不能合併
  → 必須四次

TIME_WAIT 等待 2MSL（Maximum Segment Lifetime）的原因：
  MSL = 最大報文生存時間（通常 60 秒）
  等待 2MSL = 120 秒

  原因一：確保最後一個 ACK 能到達 Server
    如果最後的 ACK 丟失，Server 會重發 FIN
    Client 在 TIME_WAIT 期間可以重發 ACK

  原因二：讓舊連接的殘留報文消失
    防止新連接收到上一個連接的遲到報文

TIME_WAIT 過多的問題：
  高並發短連接場景（如爬蟲、壓測）
  大量 TIME_WAIT 佔用端口和記憶體

解決方案：
  net.ipv4.tcp_tw_reuse = 1（允許複用 TIME_WAIT 端口）
  net.ipv4.tcp_fin_timeout = 30（縮短 FIN_WAIT_2 超時）
```

---

### 🟠 TCP 可靠傳輸機制

```
TCP 如何保證可靠傳輸？

① 序列號（Sequence Number）
  每個位元組都有編號
  接收方按序列號排序，解決亂序問題

② 確認應答（ACK）
  接收方收到數據後回 ACK
  ACK 號 = 期望收到的下一個序列號

③ 超時重傳（Retransmission）
  發送方等待 ACK 超時 → 重新發送
  RTT（Round Trip Time）動態計算超時時間

④ 滑動視窗（Sliding Window）
  不需要每次都等 ACK，可以連續發多個包
  視窗大小 = 接收方緩衝區剩餘空間（流量控制）

  發送方視窗：
  ┌────────┬────────────────┬────────────┐
  │已發已確認 │  已發未確認     │  未發送     │
  │         │ ← 視窗大小 →  │            │
  └────────┴────────────────┴────────────┘

⑤ 流量控制（Flow Control）
  接收方告知發送方自己的接收視窗大小
  防止發送太快，接收方緩衝區溢出

⑥ 壅塞控制（Congestion Control）
  防止網路本身過載（不是接收方問題）
  四個算法：
  慢啟動      → 初始視窗小，指數增長
  擁塞避免    → 超過閾值，線性增長
  快速重傳    → 收到 3 個重複 ACK，立即重傳
  快速恢復    → 快速重傳後，不降到 1，降到閾值/2
```

---

### 🔴 TCP 面試高頻題

```
Q1：TCP 和 UDP 的區別？

TCP                        UDP
─────────────────────────────────────────
連接導向（三次握手）         無連接
可靠傳輸（ACK + 重傳）       不可靠（可能丟包）
有序傳輸                    無序
流量控制 + 壅塞控制          無控制機制
速度較慢                    速度快
適合：HTTP / FTP / Email    適合：視訊 / 遊戲 / DNS

Q2：TCP 如何處理半連接攻擊（SYN Flood）？
  攻擊：大量偽造 IP 的 SYN 請求
  → 服務器發 SYN+ACK 但永遠等不到第三次 ACK
  → 半連接佇列被塞滿 → 正常連接無法建立

  防禦：
  SYN Cookie：不存儲半連接狀態，用 Hash 驗證第三次 ACK
  縮短 SYN 超時時間
  限制同一 IP 的 SYN 速率

Q3：Keep-Alive 是什麼？
  TCP Keep-Alive：
    長時間沒有數據傳輸時，定期發探測包
    確認對方還存活
    linux 預設 7200 秒才開始探測

  HTTP Keep-Alive：
    複用同一個 TCP 連接處理多個 HTTP 請求
    避免每次 HTTP 請求都三次握手
    HTTP 1.1 預設開啟
```

---

## 3️⃣ HTTP 協議演進

---

### 🔵 HTTP 各版本對比

```
HTTP 1.0（1996）：
  每次請求建立新的 TCP 連接，請求完成後斷開
  ❌ 每次都要三次握手，開銷大
  ❌ 無法並行請求

HTTP 1.1（1997，目前最廣泛）：
  ✅ Keep-Alive：TCP 連接可複用（預設開啟）
  ✅ Pipeline：可以連續發多個請求，不等回應
  ❌ 隊頭阻塞（Head-of-Line Blocking）：
     雖然可以發多個請求，但響應必須按順序返回
     第一個請求很慢 → 後面的請求都要等
  ❌ Header 每次都要完整發送（無壓縮）
  ❌ 只支援請求-響應模式（服務器不能主動推送）

HTTP 2.0（2015）：
  ✅ 多路複用（Multiplexing）：
     一個 TCP 連接，多個 Stream 並行傳輸
     每個 HTTP 請求是一個 Stream
     Stream 之間互不阻塞（徹底解決隊頭阻塞！）
  ✅ Header 壓縮（HPACK 算法）：
     首次發送完整 Header，之後只發差異部分
  ✅ Server Push：
     服務器可以主動推送資源（如 HTML 請求時順帶推送 CSS/JS）
  ✅ 二進位格式：比 1.x 的文本格式更高效
  ❌ 底層還是 TCP，TCP 層的隊頭阻塞仍存在
     （一個 TCP 包丟失 → 所有 Stream 都要等重傳）

HTTP 3.0（2022，基於 QUIC）：
  ✅ 底層換成 UDP + QUIC（Quick UDP Internet Connections）
  ✅ 徹底解決 TCP 層的隊頭阻塞
     每個 Stream 獨立，一個 Stream 的丟包不影響其他 Stream
  ✅ 0-RTT / 1-RTT 握手（比 TLS over TCP 更快建連）
  ✅ 連接遷移：網路切換（WiFi → 4G）不需要重建連接
  ❌ 基於 UDP，某些防火牆/中間件會屏蔽
  ❌ 部署複雜，普及率仍在提升中
```

---

### 🟡 HTTP 狀態碼快速記憶

```
1xx  資訊性     100 Continue
2xx  成功       200 OK / 201 Created / 204 No Content
3xx  重定向     301 永久 / 302 臨時 / 304 Not Modified
4xx  客戶端錯誤  400 Bad Request / 401 未認證 / 403 無權限
                404 Not Found / 429 Too Many Requests
5xx  服務器錯誤  500 Internal Error / 502 Bad Gateway
                503 Service Unavailable / 504 Gateway Timeout
```

---

### 🟠 HTTP 常用 Header

```
請求 Header：
  Host：           請求的域名（必填）
  Authorization：  認證（Bearer JWT）
  Content-Type：   請求體類型（application/json）
  Accept：         客戶端接受的響應類型
  User-Agent：     客戶端資訊
  Cache-Control：  快取控制
  If-None-Match：  ETag 快取驗證
  If-Modified-Since：時間快取驗證

響應 Header：
  Content-Type：   響應體類型
  Cache-Control：  快取策略（max-age=3600）
  ETag：           資源版本標識（用於快取驗證）
  Last-Modified：  最後修改時間
  Location：       重定向地址（201 / 301 / 302）
  Set-Cookie：     設定 Cookie
  Access-Control-Allow-Origin：跨域設定（CORS）

快取控制：
  Cache-Control: no-cache       → 每次都要驗證（可用快取但要確認）
  Cache-Control: no-store       → 完全不快取
  Cache-Control: max-age=3600   → 快取 3600 秒，不需要重新請求
  Cache-Control: must-revalidate → 過期後必須重新驗證
  ETag: "abc123"                → 資源版本號
  If-None-Match: "abc123"       → 驗證快取是否有效
  304 Not Modified              → 快取有效，用本地快取

瀏覽器快取流程：
  第一次請求 → 服務器返回資源 + ETag
  第二次請求 → 帶上 If-None-Match: ETag值
  資源未變動 → 服務器返回 304（無需傳送資源本體，省頻寬！）
  資源已變動 → 服務器返回 200 + 新資源 + 新 ETag
```

---

## 4️⃣ HTTPS 與 TLS

---

### 🔵 對稱加密 vs 非對稱加密

```
對稱加密（AES / ChaCha20）：
  加密和解密用同一把金鑰
  ✅ 速度快（適合大量資料）
  ❌ 金鑰如何安全傳遞？（雞生蛋的問題）

非對稱加密（RSA / ECDSA）：
  公鑰（Public Key）：任何人都可以拿到，用來加密
  私鑰（Private Key）：只有自己保留，用來解密
  ✅ 解決金鑰傳遞問題
  ❌ 速度慢（只適合小量資料，如金鑰交換）

HTTPS 的聰明做法：
  用「非對稱加密」安全地交換「對稱加密金鑰」
  之後通訊全用「對稱加密」（速度快）
  → 兼顧安全和效能！
```

---

### 🟡 TLS 1.3 握手流程（簡化版）

```
         Client                      Server
           │                           │
           │─── Client Hello ─────────→│
           │    （支援的 TLS 版本）        │
           │    （支援的加密套件列表）      │
           │    （Client 隨機數）         │
           │                           │
           │←── Server Hello ──────────│
           │    （選定的加密套件）         │
           │    （Server 隨機數）         │
           │←── Certificate ───────────│
           │    （Server 的 SSL 憑證）    │
           │←── Server Key Exchange ───│
           │    （ECDHE 公鑰參數）        │
           │←── Server Hello Done ─────│
           │                           │
           │  Client 驗證憑證（CA 鏈）    │
           │  生成 Pre-Master Secret    │
           │─── Client Key Exchange ──→│
           │    （用 Server 公鑰加密的     │
           │      Pre-Master Secret）   │
           │                           │
           │  雙方各自計算 Master Secret  │
           │  （Client隨機數+Server隨機數  │
           │    +Pre-Master Secret）    │
           │                           │
           │─── Change Cipher Spec ───→│  之後用對稱加密
           │─── Finished ─────────────→│
           │←── Change Cipher Spec ────│
           │←── Finished ──────────────│
           │                           │
           │   [TLS 握手完成，開始傳輸]    │

TLS 1.3 改進（比 TLS 1.2 更快更安全）：
  ✅ 握手從 2-RTT 降到 1-RTT（更快建連）
  ✅ 0-RTT 恢復（重連不需要完整握手）
  ✅ 移除不安全的加密算法（RSA 金鑰交換、RC4 等）
  ✅ 強制前向保密（Forward Secrecy）
     → 即使私鑰洩漏，過去的通訊也無法解密

前向保密（Forward Secrecy）：
  每次連接生成臨時金鑰（ECDHE）
  連接結束後金鑰銷毀
  → 攻擊者錄下加密流量，未來即使拿到服務器私鑰也無法解密
```

---

### 🟠 SSL 憑證體系（CA 信任鏈）

```
憑證頒發流程：
  ① 網站生成公私鑰對
  ② 向 CA（Certificate Authority）提交 CSR（含公鑰 + 域名）
  ③ CA 驗證域名所有權（DNS 記錄 / 文件驗證）
  ④ CA 用自己的私鑰對憑證簽名
  ⑤ 網站部署憑證

驗證流程（瀏覽器）：
  ① 收到服務器憑證
  ② 查看憑證是哪個 CA 簽發的
  ③ 用 CA 的公鑰驗證憑證簽名
  ④ 驗證域名是否匹配
  ⑤ 驗證憑證是否過期 / 是否被吊銷

信任鏈（Chain of Trust）：
  Root CA（根憑證，預裝在操作系統）
      ↓ 簽名
  Intermediate CA（中間憑證）
      ↓ 簽名
  End-entity Certificate（網站憑證）

常見憑證類型：
  DV（Domain Validation）：只驗證域名所有權，最快（分鐘級）
  OV（Organization Validation）：驗證企業身份（天級）
  EV（Extended Validation）：最嚴格的驗證（週級，瀏覽器顯示公司名）

免費憑證：Let's Encrypt（DV，90天自動續期）

HSTS（HTTP Strict Transport Security）：
  服務器告訴瀏覽器：這個域名只能用 HTTPS
  Strict-Transport-Security: max-age=31536000; includeSubDomains
  → 防止 SSL Stripping 攻擊
```

---

## 5️⃣ DNS 解析流程

---

### 🔵 完整解析過程

```
瀏覽器輸入 www.example.com 的解析過程：

① 瀏覽器快取
   有快取 → 直接用
   沒有 → 繼續

② 作業系統快取（/etc/hosts 文件）
   有記錄 → 直接用
   沒有 → 繼續

③ 本地 DNS 解析器（通常是路由器或 ISP 的 DNS）
   有快取 → 直接回應
   沒有 → 開始遞迴查詢

④ 查詢根域名服務器（Root DNS Server）
   問："誰管 .com？"
   → 返回 .com TLD 服務器的地址

⑤ 查詢 TLD 服務器（Top-Level Domain）
   問："誰管 example.com？"
   → 返回 example.com 的權威 DNS 服務器地址

⑥ 查詢權威 DNS 服務器（Authoritative DNS）
   問："www.example.com 的 IP 是多少？"
   → 返回 IP 地址（如 93.184.216.34）

⑦ 本地 DNS 解析器快取結果（根據 TTL）
   → 返回給客戶端

⑧ 瀏覽器快取 → 建立 TCP 連接 → 發送 HTTP 請求

全程時間：通常 < 100ms（快取命中時 < 1ms）

DNS 記錄類型：
  A     → 域名 → IPv4 地址
  AAAA  → 域名 → IPv6 地址
  CNAME → 域名 → 另一個域名（別名）
  MX    → 郵件服務器地址
  TXT   → 文字記錄（SPF / DKIM / 域名驗證）
  NS    → 名稱服務器

TTL（Time To Live）：
  DNS 記錄的快取有效期
  短 TTL（60s）→ 改 IP 時快速生效，但查詢頻繁
  長 TTL（86400s = 1天）→ 查詢少，但改 IP 時需等較長時間生效

DNS 負載均衡：
  同一個域名配置多個 A 記錄（不同 IP）
  DNS 輪詢返回不同 IP → 達到簡單的負載均衡
  缺點：不考慮服務器健康狀態
```

---

## 6️⃣ CDN 原理

---

### 🔵 CDN 架構與工作流程

```
CDN（Content Delivery Network）內容分發網路

問題：
  用戶在台灣，服務器在美國
  → 每次請求都要跨越太平洋，延遲高（100ms+）

CDN 解決方式：
  在全球各地部署「邊緣節點」（Edge Node / POP）
  靜態資源快取到距用戶最近的節點
  用戶請求 → 就近的邊緣節點回應 → 延遲低（10ms 以內）

CDN 請求流程：

① 用戶瀏覽器請求 static.example.com/image.jpg
② DNS 查詢 → CDN 廠商的 GSLB（全球負載均衡）
③ GSLB 根據用戶 IP 地理位置，返回最近邊緣節點的 IP
④ 瀏覽器請求最近的邊緣節點

  快取命中（Cache Hit）：
  邊緣節點有快取 → 直接返回（毫秒級）

  快取未命中（Cache Miss）：
  邊緣節點 → 回源（Origin Server 源站）→ 取得資源
  → 快取到邊緣節點 → 返回給用戶
  下次相同請求 → 直接從邊緣節點返回

CDN 適合的資源：
  ✅ 靜態資源：圖片 / 視頻 / JS / CSS / 字型
  ✅ 大文件下載（軟體安裝包）
  ❌ 動態 API 響應（每次結果不同，無法快取）
  ❌ 用戶個人化內容（購物車 / 訂單狀態）
```

---

### 🟡 CDN 快取策略

```
快取控制（Cache-Control Header）：
  Cache-Control: public, max-age=31536000, immutable
  → 公開快取（CDN 可以快取）
  → 快取 1 年
  → immutable：資源不會改變，瀏覽器不需要驗證

版本化資源（避免快取污染）：
  ❌ /static/app.js         → 更新後用戶可能拿到舊版本
  ✅ /static/app.abc123.js  → Hash 值隨內容改變
                               → 內容改變 → URL 改變 → CDN 重新快取

CDN 快取清除（Cache Purge / Invalidation）：
  當源站資源更新時，需要清除 CDN 快取
  ① 手動 Purge（CDN 控制台 / API）
  ② URL 版本化（最優雅，不需要 Purge）
  ③ 設定短 TTL（簡單但增加回源頻率）

CDN 安全功能：
  DDoS 防護    → 流量在 CDN 節點被過濾吸收
  WAF          → Web 應用防火牆（SQL 注入 / XSS 過濾）
  HTTPS 加速  → CDN 在邊緣節點終止 TLS，減少 TLS 握手次數
  IP 封鎖      → 封鎖惡意 IP

知名 CDN 廠商：
  Cloudflare / AWS CloudFront / Akamai
  阿里雲 CDN / 騰訊雲 CDN
```

---

## 📝 網路基礎 面試題速查

```
Q：TCP 三次握手為什麼不是兩次？
A：兩次只能確認「Client 能發 + Server 能收」
   無法確認「Server 能發 + Client 能收」
   三次才能確保全雙工雙向通訊都正常

Q：四次揮手為什麼不是三次？
A：TCP 是全雙工，關閉需要雙方各自關閉發送方向
   Server 收到 FIN 後可能還有數據要發（半關閉狀態）
   所以 ACK 和 FIN 不能合併 → 需要四次

Q：TIME_WAIT 太多怎麼辦？
A：縮短 TIME_WAIT 時間 / 開啟 SO_REUSEADDR / tcp_tw_reuse

Q：HTTP 和 HTTPS 的區別？
A：HTTPS = HTTP + TLS 加密
   HTTP：明文傳輸，443 → 80 埠
   HTTPS：加密傳輸，預設 443 埠
   HTTPS 確保機密性（加密）/ 完整性（防竄改）/ 身份驗證（憑證）

Q：HTTP 2.0 解決了什麼問題？
A：解決了 HTTP 1.1 的隊頭阻塞（HOL Blocking）
   通過多路複用，一個連接並行傳輸多個 Stream
   同時提供 Header 壓縮、Server Push

Q：DNS 解析的完整流程？
A：瀏覽器快取 → OS 快取 → 本地 DNS
   → 根域名服務器 → TLD 服務器 → 權威 DNS
   → 返回 IP → 快取

Q：HTTPS 一定安全嗎？
A：不一定
   HTTPS 確保傳輸安全，但不保證：
   ① 服務器端程式碼安全（SQL 注入等）
   ② 憑證不被偽造（需要 CA 體系可信）
   ③ 用戶端安全（被植入根憑證可被中間人攻擊）
```

---

## 📝 網路基礎 總結

| 主題 | 核心要點 |
|------|---------|
| **TCP 三次握手** | 確保雙向通訊可靠，不能兩次（無法確認 Server 能發）|
| **TCP 四次揮手** | 全雙工各自關閉，不能三次（半關閉狀態）|
| **TIME_WAIT** | 等待 2MSL 確保 ACK 到達 + 舊報文消失 |
| **TCP vs UDP** | TCP 可靠有序，UDP 快速無序，按場景選擇 |
| **HTTP 1.1** | Keep-Alive 複用連接，但有隊頭阻塞 |
| **HTTP 2.0** | 多路複用解決應用層隊頭阻塞，Header 壓縮 |
| **HTTP 3.0** | 基於 QUIC(UDP)，徹底解決 TCP 層隊頭阻塞 |
| **TLS 握手** | 非對稱加密交換金鑰，對稱加密傳輸資料 |
| **前向保密** | 臨時金鑰（ECDHE），即使私鑰洩漏過去通訊也安全 |
| **DNS 流程** | 瀏覽器→OS→本地DNS→根→TLD→權威DNS |
| **CDN** | 邊緣節點就近服務，GSLB 根據 IP 路由到最近節點 |
| **快取策略** | URL 版本化 + 長 TTL，動態內容不快取 |
