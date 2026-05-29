# 🟤 Elasticsearch 完整深入教學

---

## 🗺️ 學習路線圖

```
Elasticsearch
├── 1️⃣ 核心原理
│   ├── 倒排索引（Inverted Index）
│   ├── 分散式架構（Cluster / Node / Shard）
│   └── 文件讀寫流程
├── 2️⃣ 資料模型
│   ├── Index / Document / Field
│   ├── Mapping（欄位類型定義）
│   └── 分析器（Analyzer）
├── 3️⃣ DSL 查詢語法
│   ├── 全文查詢（match / multi_match）
│   ├── 精確查詢（term / terms / range）
│   ├── 複合查詢（bool）
│   ├── 聚合查詢（aggregation）
│   └── 高亮 / 排序 / 分頁
├── 4️⃣ Spring Boot 整合
│   ├── Spring Data Elasticsearch
│   ├── ElasticsearchRestTemplate
│   └── 完整電商搜索實戰
└── 5️⃣ 生產實戰
    ├── 效能調優
    ├── 索引設計最佳實踐
    └── ELK Stack 日誌系統
```

---

## 1️⃣ 核心原理

---

### 🔵 倒排索引（Inverted Index）⭐ 最重要

```
傳統資料庫的查詢方式（正向索引）：
  SELECT * FROM articles WHERE content LIKE '%Java%'
  → 逐行掃描所有文章，找包含 "Java" 的行
  → 全表掃描，極慢！

Elasticsearch 的倒排索引：
  先對文字進行分詞（Tokenization）
  再建立「詞 → 文件列表」的映射

範例文件：
  Doc 1: "Java 是一門優秀的程式語言"
  Doc 2: "Python 和 Java 都很流行"
  Doc 3: "Java 的 JVM 非常強大"

分詞後：
  Doc 1 → [Java, 優秀, 程式, 語言]
  Doc 2 → [Python, Java, 流行]
  Doc 3 → [Java, JVM, 強大]

倒排索引（Term → 文件 + 位置）：
  ┌──────────┬────────────────────────────────┐
  │  Term    │  Posting List（文件 ID + 頻率）  │
  ├──────────┼────────────────────────────────┤
  │  Java    │  Doc1(1), Doc2(1), Doc3(1)     │
  │  優秀    │  Doc1(1)                        │
  │  程式    │  Doc1(1)                        │
  │  語言    │  Doc1(1)                        │
  │  Python  │  Doc2(1)                        │
  │  流行    │  Doc2(1)                        │
  │  JVM     │  Doc3(1)                        │
  │  強大    │  Doc3(1)                        │
  └──────────┴────────────────────────────────┘

搜索 "Java"：
  直接查倒排索引 → Doc1, Doc2, Doc3 → 毫秒級！
  不需要掃描任何文件內容！

相關性評分（BM25 算法）：
  TF（Term Frequency）：詞在文件中出現的頻率（越高越相關）
  IDF（Inverse Document Frequency）：詞在所有文件中的稀有度（越稀有越重要）
  "Java" 在每篇文章都有 → IDF 較低
  "JVM"  只在 Doc3 有   → IDF 較高
  搜索 "JVM"，Doc3 分數最高！
```

---

### 🟡 分散式架構

```
Elasticsearch 集群架構：

┌─────────────────────────────────────────────────────────┐
│                      ES Cluster                          │
│                                                         │
│  ┌────────────────┐  ┌────────────────┐  ┌───────────┐  │
│  │   Node 1       │  │   Node 2       │  │  Node 3   │  │
│  │  (Master)      │  │  (Data)        │  │  (Data)   │  │
│  │                │  │                │  │           │  │
│  │  Shard 0 (主)  │  │  Shard 1 (主)  │  │ Shard 2(主)│ │
│  │  Shard 1 (副)  │  │  Shard 2 (副)  │  │ Shard 0(副)│ │
│  │  Shard 2 (副)  │  │  Shard 0 (副)  │  │ Shard 1(副)│ │
│  └────────────────┘  └────────────────┘  └───────────┘  │
└─────────────────────────────────────────────────────────┘

核心概念：
  Cluster（集群）：多個 Node 組成，對外是一個整體
  Node（節點）：集群中的一台服務器
    Master Node：管理集群狀態（Index 的創建/刪除、Node 的加入/離開）
    Data Node：儲存數據、執行查詢
    Coordinating Node：接收請求，分發到各 Data Node，聚合結果

  Index（索引）：類比 MySQL 的「表」
  Document（文件）：類比 MySQL 的「行」，JSON 格式
  Field（欄位）：類比 MySQL 的「列」

  Shard（分片）：Index 被切成多個 Shard，分散到不同 Node
    主分片（Primary Shard）：接收寫入請求
    副本分片（Replica Shard）：主分片的備份，可以承接讀請求

分片數設定（重要！）：
  ⚠️ 主分片數在建立 Index 時確定，之後不能修改！
  一般規則：每個分片大小控制在 10GB~50GB
  3 個 Node → 3 個主分片（每個 Node 一個）

容錯性：
  Node 1 宕機 → Shard 0 的副本（在 Node 3）自動升為主分片
  集群繼續正常運行！
```

---

### 🟠 文件寫入流程

```
寫入流程（PUT /index/_doc/1）：

① 請求到達任意 Node（協調節點）
② 計算文件應該寫入哪個 Shard：
   shard = hash(document_id) % primary_shard_count
③ 協調節點將請求轉發到對應的主分片所在的 Node
④ 主分片寫入數據
⑤ 並行複製到所有副本分片
⑥ 所有副本確認寫入成功後，返回客戶端成功

寫入過程（主分片）：
  ① 寫入 Memory Buffer（記憶體緩衝）
  ② 每 1 秒 Refresh 一次（將 Buffer 寫入 Segment 文件）
     → 寫入後 1 秒內可以被搜索（準實時 Near Real-Time，NRT）
  ③ 每 30 分鐘或 Translog 過大 → Flush（Segment 落盤）
  ④ 定期 Merge（合併小 Segment 為大 Segment，提升查詢性能）

Translog（事務日誌）：
  每次寫入都會寫 Translog（WAL 機制，防止數據丟失）
  即使 Node 宕機，重啟後可以從 Translog 恢復
```

---

## 2️⃣ 資料模型

---

### 🔵 Mapping（欄位類型定義）

```json
// 建立 Index 並定義 Mapping
PUT /products
{
  "settings": {
    "number_of_shards":   3,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "ik_smart_analyzer": {
          "type":      "custom",
          "tokenizer": "ik_smart"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "long"
      },
      "name": {
        "type":     "text",
        "analyzer": "ik_max_word",
        "fields": {
          "keyword": {
            "type":         "keyword",
            "ignore_above": 256
          }
        }
      },
      "description": {
        "type":     "text",
        "analyzer": "ik_smart"
      },
      "price": {
        "type": "double"
      },
      "category": {
        "type": "keyword"
      },
      "tags": {
        "type": "keyword"
      },
      "stock": {
        "type": "integer"
      },
      "status": {
        "type": "keyword"
      },
      "images": {
        "type": "keyword",
        "index": false
      },
      "location": {
        "type": "geo_point"
      },
      "attributes": {
        "type": "nested",
        "properties": {
          "key":   { "type": "keyword" },
          "value": { "type": "keyword" }
        }
      },
      "created_at": {
        "type":   "date",
        "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
      }
    }
  }
}
```

```
常用欄位類型：

text：    全文搜索（會分詞）
          name: "iPhone 15 Pro"
          → 分詞 → ["iPhone", "15", "Pro"]
          → 可以搜 "iPhone" 找到此文件

keyword： 精確匹配（不分詞）
          category: "手機"
          → 必須搜 "手機" 才能找到，搜 "手" 找不到
          → 適合排序、聚合、精確過濾

text + keyword 雙類型（fields）：
  name 欄位同時支援全文搜索和精確匹配
  name        → text  → 全文搜索（"iPhone"）
  name.keyword → keyword → 排序、聚合（精確匹配）

nested：  嵌套對象（保持對象內欄位的關聯）
          vs object 類型：
          object 的數組會被扁平化（失去關聯）
          nested 保持每個對象的獨立性

index: false  不建立索引（不能被搜索，但可以取回）
              適合：大圖片 URL、不需要搜索的欄位
```

---

### 🟡 分析器（Analyzer）

```
分析器流程：
原始文本 → Character Filter → Tokenizer → Token Filter → Token 列表

範例：
原始文本："I LOVE Java Programming!"

① Character Filter（字符過濾）：去除 HTML 標籤等
   → "I LOVE Java Programming!"

② Tokenizer（分詞）：按空格、標點切分
   → ["I", "LOVE", "Java", "Programming"]

③ Token Filter（詞語過濾）：
   lowercase → ["i", "love", "java", "programming"]
   stop word → 去除停用詞（"i"）→ ["love", "java", "programming"]
   stemmer   → 詞幹提取 → ["love", "java", "program"]

中文分詞（IK Analyzer 插件）：
  ik_smart：智能模式，較少分詞（粗粒度）
    "中華人民共和國" → ["中華人民共和國"]
  ik_max_word：最細粒度，盡量多分詞
    "中華人民共和國" → ["中華人民共和國", "中華人民", "中華", "人民共和國", ...]

建議：
  index（建立索引）時 → ik_max_word（盡量多分詞，增加召回率）
  search（搜索時）   → ik_smart（智能分詞，提高精確度）
```

```bash
# 測試分析器
GET /products/_analyze
{
  "analyzer": "ik_max_word",
  "text": "Java 程式設計入門到精通"
}

# 輸出：
# ["Java", "程式", "程式設計", "設計", "入門", "到", "精通"]
```

---

## 3️⃣ DSL 查詢語法

---

### 🔵 全文查詢

```json
// 1. match 查詢（最常用，全文搜索）
GET /products/_search
{
  "query": {
    "match": {
      "name": {
        "query":    "蘋果 手機",
        "operator": "and"
      }
    }
  }
}
// operator: "or"（預設）→ 包含任一詞即可
// operator: "and"       → 必須同時包含所有詞

// 2. match_phrase 查詢（短語搜索，詞的順序要一致）
GET /products/_search
{
  "query": {
    "match_phrase": {
      "description": {
        "query": "超長續航",
        "slop":  2
      }
    }
  }
}
// slop: 允許詞之間的最大間隔（0 = 完全相鄰）

// 3. multi_match 查詢（同時搜索多個欄位）
GET /products/_search
{
  "query": {
    "multi_match": {
      "query":  "iPhone",
      "fields": ["name^3", "description^1", "category^2"],
      "type":   "best_fields"
    }
  }
}
// ^3 表示 name 欄位的權重是 description 的 3 倍
// type:
//   best_fields：取最高分的欄位（預設）
//   most_fields：多個欄位都匹配分數更高
//   cross_fields：跨欄位搜索（適合姓名分開存）
```

---

### 🟡 精確查詢

```json
// 1. term 查詢（精確匹配，不分詞）
GET /products/_search
{
  "query": {
    "term": {
      "category": {
        "value": "手機"
      }
    }
  }
}
// ⚠️ term 用於 keyword 欄位
// 不要用 term 查詢 text 欄位（因為 text 已被分詞，無法完全匹配）

// 2. terms 查詢（多值精確匹配，類似 SQL IN）
GET /products/_search
{
  "query": {
    "terms": {
      "category": ["手機", "平板", "電腦"]
    }
  }
}

// 3. range 查詢（範圍查詢）
GET /products/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 1000,
        "lte": 5000
      }
    }
  }
}
// gte = greater than or equal（≥）
// lte = less than or equal（≤）
// gt  = greater than（>）
// lt  = less than（<）

// 日期範圍
GET /products/_search
{
  "query": {
    "range": {
      "created_at": {
        "gte": "now-7d/d",
        "lte": "now/d"
      }
    }
  }
}
// now-7d/d = 7天前（取整天）

// 4. exists 查詢（欄位是否存在）
GET /products/_search
{
  "query": {
    "exists": {
      "field": "discount_price"
    }
  }
}

// 5. wildcard 查詢（通配符）
GET /products/_search
{
  "query": {
    "wildcard": {
      "name.keyword": {
        "value": "iPhone*"
      }
    }
  }
}
// ⚠️ wildcard 效能差，避免在高基數欄位上使用
```

---

### 🔴 複合查詢（Bool Query）⭐ 最重要

```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": { "name": "手機" }
        }
      ],
      "should": [
        { "term":  { "brand": "Apple" }},
        { "range": { "rating": { "gte": 4.5 }}}
      ],
      "must_not": [
        { "term": { "status": "OFFLINE" }}
      ],
      "filter": [
        { "range": { "price": { "gte": 1000, "lte": 10000 }}},
        { "term":  { "category": "手機" }}
      ],
      "minimum_should_match": 1
    }
  }
}
```

```
bool 查詢四個子句：

must（必須匹配）：
  ✅ 影響相關性分數（BM25）
  條件必須全部滿足（類似 AND）

should（應該匹配）：
  ✅ 影響相關性分數（滿足越多分越高）
  minimum_should_match：最少要滿足幾個 should

must_not（必須不匹配）：
  ❌ 不影響相關性分數（只做過濾）
  條件都不能滿足（類似 NOT）

filter（過濾）：
  ❌ 不影響相關性分數（比 must 快！）
  條件必須滿足
  ✅ 結果會被緩存（性能最好）
  → 適合：價格範圍、分類、狀態等不需要影響相關性的條件

性能建議：
  ① 不需要影響排序的條件 → 放 filter（有緩存，不計算分數）
  ② 需要影響排序的條件 → 放 must / should
  ③ 排除條件 → 放 must_not
```

```json
// 實戰：電商商品搜索
// 搜索："藍牙耳機"，類別：電子產品，價格 500~2000，有庫存，按評分排序
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "multi_match": {
            "query":  "藍牙耳機",
            "fields": ["name^3", "description"]
          }
        }
      ],
      "filter": [
        { "term":  { "category": "電子產品" }},
        { "range": { "price": { "gte": 500, "lte": 2000 }}},
        { "range": { "stock": { "gt": 0 }}}
      ]
    }
  },
  "sort": [
    { "_score":  { "order": "desc" }},
    { "rating":  { "order": "desc" }},
    { "sold_count": { "order": "desc" }}
  ],
  "from": 0,
  "size": 20,
  "highlight": {
    "fields": {
      "name":        { "number_of_fragments": 0 },
      "description": { "fragment_size": 150, "number_of_fragments": 3 }
    },
    "pre_tags":  ["<em>"],
    "post_tags": ["</em>"]
  },
  "_source": ["id", "name", "price", "rating", "images"]
}
```

---

### 🟠 聚合查詢（Aggregation）

```json
// 場景：商品搜索的左側篩選面板（品牌/分類/價格區間）

GET /products/_search
{
  "query": {
    "bool": {
      "must": [{ "match": { "name": "手機" }}]
    }
  },
  "size": 0,
  "aggs": {
    "category_count": {
      "terms": {
        "field": "category",
        "size":  10,
        "order": { "_count": "desc" }
      }
    },
    "brand_count": {
      "terms": {
        "field": "brand",
        "size":  20
      }
    },
    "price_ranges": {
      "range": {
        "field": "price",
        "ranges": [
          { "key": "0-1000",  "from": 0,    "to": 1000  },
          { "key": "1000-3000", "from": 1000, "to": 3000 },
          { "key": "3000-8000", "from": 3000, "to": 8000 },
          { "key": "8000以上",  "from": 8000              }
        ]
      }
    },
    "avg_price": {
      "avg": {
        "field": "price"
      }
    },
    "max_price": {
      "max": {
        "field": "price"
      }
    },
    "price_histogram": {
      "histogram": {
        "field":    "price",
        "interval": 1000,
        "min_doc_count": 1
      }
    },
    "rating_stats": {
      "stats": {
        "field": "rating"
      }
    }
  }
}

// 巢狀聚合（子聚合）：每個分類的平均價格
GET /products/_search
{
  "size": 0,
  "aggs": {
    "by_category": {
      "terms": {
        "field": "category",
        "size":  10
      },
      "aggs": {
        "avg_price": {
          "avg": { "field": "price" }
        },
        "price_range": {
          "stats": { "field": "price" }
        }
      }
    }
  }
}
```

---

### 🟢 巢狀查詢（Nested Query）

```json
// 查詢 attributes 中 key="顏色" AND value="黑色" 的商品
// 必須用 nested query，否則不同 attribute 的 key/value 會交叉匹配！

GET /products/_search
{
  "query": {
    "nested": {
      "path": "attributes",
      "query": {
        "bool": {
          "must": [
            { "term": { "attributes.key":   "顏色" }},
            { "term": { "attributes.value": "黑色" }}
          ]
        }
      }
    }
  }
}
```

---

## 4️⃣ Spring Boot 整合

---

### 🔵 Spring Data Elasticsearch 整合

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

```yaml
# application.yml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: ${ES_PASSWORD}
    connection-timeout: 5s
    socket-timeout:     30s
```

```java
// ES 文件實體
@Document(indexName = "products", createIndex = true)
@Setting(settingPath = "elasticsearch/product-settings.json")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text,
           analyzer = "ik_max_word",
           searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text,
           analyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Double)
    private Double price;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Float)
    private Float rating;

    @Field(type = FieldType.Long)
    private Long soldCount;

    @Field(type = FieldType.Keyword,
           index = false)
    private String imageUrl;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date,
           format = DateFormat.date_time)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Nested)
    private List<ProductAttribute> attributes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductAttribute {
        @Field(type = FieldType.Keyword)
        private String key;
        @Field(type = FieldType.Keyword)
        private String value;
    }
}
```

```java
// Repository（簡單查詢）
@Repository
public interface ProductRepository
        extends ElasticsearchRepository<ProductDocument, Long> {

    // 方法名自動生成查詢
    List<ProductDocument> findByCategory(String category);
    List<ProductDocument> findByPriceBetween(Double min, Double max);
    List<ProductDocument> findByNameContaining(String name);

    // 自定義查詢
    @Query("{\"bool\": {\"must\": [{\"match\": {\"name\": \"?0\"}}], " +
           "\"filter\": [{\"term\": {\"status\": \"ACTIVE\"}}]}}")
    Page<ProductDocument> searchActiveByName(String name, Pageable pageable);
}
```

```java
// ElasticsearchRestTemplate（複雜查詢，推薦！）
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchService {

    private final ElasticsearchRestTemplate esTemplate;
    private final ProductRepository         productRepository;

    // ========================= 搜索 =========================

    public SearchResult<ProductDocument> search(ProductSearchRequest request) {

        // 1. 建立 bool query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 全文搜索（影響相關性分數）
        if (StringUtils.hasText(request.getKeyword())) {
            boolQuery.must(
                QueryBuilders.multiMatchQuery(request.getKeyword(),
                    "name", "description", "brand")
                    .field("name", 3.0f)          // name 欄位權重 x3
                    .field("description", 1.0f)
                    .field("brand", 2.0f)
                    .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
            );
        }

        // 過濾條件（不影響相關性分數，有緩存）
        if (StringUtils.hasText(request.getCategory())) {
            boolQuery.filter(
                QueryBuilders.termQuery("category", request.getCategory())
            );
        }

        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            RangeQueryBuilder priceRange = QueryBuilders.rangeQuery("price");
            if (request.getMinPrice() != null)
                priceRange.gte(request.getMinPrice());
            if (request.getMaxPrice() != null)
                priceRange.lte(request.getMaxPrice());
            boolQuery.filter(priceRange);
        }

        if (request.getBrands() != null && !request.getBrands().isEmpty()) {
            boolQuery.filter(
                QueryBuilders.termsQuery("brand", request.getBrands())
            );
        }

        // 只顯示有庫存的
        if (Boolean.TRUE.equals(request.getInStockOnly())) {
            boolQuery.filter(QueryBuilders.rangeQuery("stock").gt(0));
        }

        boolQuery.mustNot(
            QueryBuilders.termQuery("status", "OFFLINE")
        );

        // 2. 建立 NativeSearchQuery
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
            .withQuery(boolQuery);

        // 3. 排序
        String sortBy = request.getSortBy();
        if ("price_asc".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        } else if ("price_desc".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
        } else if ("sales".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("soldCount").order(SortOrder.DESC));
        } else if ("rating".equals(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort("rating").order(SortOrder.DESC));
        } else {
            // 預設：相關性分數 → 評分 → 銷量
            queryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
            queryBuilder.withSort(SortBuilders.fieldSort("rating").order(SortOrder.DESC));
            queryBuilder.withSort(SortBuilders.fieldSort("soldCount").order(SortOrder.DESC));
        }

        // 4. 分頁
        queryBuilder.withPageable(
            PageRequest.of(request.getPage(), request.getSize())
        );

        // 5. 高亮
        if (StringUtils.hasText(request.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field(new HighlightBuilder.Field("name")
                    .numOfFragments(0))              // 0 = 返回完整欄位
                .field(new HighlightBuilder.Field("description")
                    .fragmentSize(150)
                    .numOfFragments(3))
                .preTags("<em>")
                .postTags("</em>");
            queryBuilder.withHighlightBuilder(highlightBuilder);
        }

        // 6. 聚合（用於左側篩選面板）
        queryBuilder
            .addAggregation(AggregationBuilders.terms("by_category")
                .field("category").size(20))
            .addAggregation(AggregationBuilders.terms("by_brand")
                .field("brand").size(50))
            .addAggregation(AggregationBuilders.range("by_price")
                .field("price")
                .addRange("0-500",   0,    500)
                .addRange("500-1000",  500,  1000)
                .addRange("1000-3000", 1000, 3000)
                .addRange("3000+",     3000, Double.MAX_VALUE));

        // 7. 只取需要的欄位（節省網路傳輸）
        queryBuilder.withSourceFilter(
            new FetchSourceFilter(
                new String[]{"id", "name", "price", "category",
                             "brand", "rating", "soldCount", "imageUrl", "stock"},
                null
            )
        );

        // 8. 執行查詢
        SearchHits<ProductDocument> searchHits =
            esTemplate.search(queryBuilder.build(), ProductDocument.class);

        // 9. 處理結果
        return buildSearchResult(searchHits, request);
    }

    private SearchResult<ProductDocument> buildSearchResult(
            SearchHits<ProductDocument> searchHits,
            ProductSearchRequest request) {

        // 處理高亮
        List<ProductDocument> products = searchHits.stream()
            .map(hit -> {
                ProductDocument product = hit.getContent();
                // 替換高亮欄位
                if (!hit.getHighlightFields().isEmpty()) {
                    List<String> nameHighlight =
                        hit.getHighlightField("name");
                    if (!nameHighlight.isEmpty()) {
                        product.setName(nameHighlight.get(0));
                    }
                }
                return product;
            })
            .collect(Collectors.toList());

        // 處理聚合結果
        Map<String, List<AggBucket>> aggregations = new HashMap<>();

        if (searchHits.hasAggregations()) {
            Aggregations aggs = searchHits.getAggregations();

            // 分類聚合
            Terms categoryTerms = aggs.get("by_category");
            aggregations.put("categories",
                categoryTerms.getBuckets().stream()
                    .map(b -> new AggBucket(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList()));

            // 品牌聚合
            Terms brandTerms = aggs.get("by_brand");
            aggregations.put("brands",
                brandTerms.getBuckets().stream()
                    .map(b -> new AggBucket(b.getKeyAsString(), b.getDocCount()))
                    .collect(Collectors.toList()));
        }

        return SearchResult.<ProductDocument>builder()
            .items(products)
            .total(searchHits.getTotalHits())
            .page(request.getPage())
            .size(request.getSize())
            .aggregations(aggregations)
            .build();
    }

    // ========================= 索引管理 =========================

    // 批量索引（同步 DB 數據到 ES）
    public void bulkIndex(List<ProductDocument> products) {
        productRepository.saveAll(products);
        log.info("批量索引 {} 個商品", products.size());
    }

    // 更新單個文件
    public void updateProduct(ProductDocument product) {
        productRepository.save(product);
    }

    // 刪除文件
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    // 完全重建索引（全量同步）
    @Async
    public void reindexAll() {
        log.info("開始全量重建索引...");
        int page = 0, size = 1000;
        long total = 0;

        while (true) {
            List<Product> products = productRepository
                .findAllActive(PageRequest.of(page, size));
            if (products.isEmpty()) break;

            List<ProductDocument> docs = products.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());

            productRepository.saveAll(docs);
            total += docs.size();
            page++;
            log.info("已索引 {} 個商品...", total);
        }

        log.info("全量重建索引完成，共 {} 個商品", total);
    }

    private ProductDocument toDocument(Product product) {
        return ProductDocument.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice().doubleValue())
            .category(product.getCategory())
            .brand(product.getBrand())
            .stock(product.getStock())
            .rating(product.getRating())
            .soldCount(product.getSoldCount())
            .imageUrl(product.getMainImage())
            .status(product.getStatus().name())
            .createdAt(product.getCreatedAt())
            .build();
    }
}
```

---

### 🟡 DB 與 ES 數據同步方案

```
數據同步策略：

① 同步雙寫（簡單，但有一致性問題）
  @Transactional
  public Product createProduct(ProductRequest request) {
      Product product = productRepo.save(request);    // 寫 DB
      esService.index(toDocument(product));            // 寫 ES
      return product;
  }
  問題：ES 寫失敗但 DB 成功 → 數據不一致！

② 異步雙寫（解耦，最終一致性）
  @Transactional
  public Product createProduct(ProductRequest request) {
      Product product = productRepo.save(request);
      // 事務提交後，異步同步到 ES
      eventPublisher.publishEvent(new ProductCreatedEvent(product));
      return product;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Async
  public void syncToEs(ProductCreatedEvent event) {
      esService.index(toDocument(event.getProduct()));
  }

③ Canal（監聽 MySQL Binlog）← 生產推薦
  MySQL Binlog → Canal → MQ（Kafka/RabbitMQ）→ ES Sync Service → ES
  完全解耦，任何 DB 變更都能同步
  即使 ES Sync Service 掛了，MQ 會保留消息
```

```java
// Canal 消費者（監聽 MySQL 變更同步 ES）
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductCanalConsumer {

    private final ProductSearchService searchService;

    @RabbitListener(queues = "canal.product.changes")
    public void handleProductChange(CanalMessage message) {
        switch (message.getEventType()) {
            case INSERT, UPDATE -> {
                ProductDocument doc = convertToDocument(message.getData());
                searchService.updateProduct(doc);
                log.info("同步商品到 ES：{}", doc.getId());
            }
            case DELETE -> {
                Long productId = extractId(message.getData());
                searchService.deleteProduct(productId);
                log.info("從 ES 刪除商品：{}", productId);
            }
        }
    }
}
```

---

## 5️⃣ 生產實戰

---

### 🔵 索引設計最佳實踐

```
Index 命名規範：
  ❌ products（不要用單一索引名）
  ✅ products_v1（帶版本號）
     products_v2（升級 Mapping 時建立新版本）
  
  使用別名（Alias）：
  products → 指向 products_v1
  
  重建索引時：
  ① 建立新的 products_v2
  ② 將數據從 v1 複製到 v2（reindex API）
  ③ 將別名 products 指向 v2（零停機！）
  ④ 刪除 products_v1

Index 別名操作：
POST /_aliases
{
  "actions": [
    { "add":    { "index": "products_v2", "alias": "products" }},
    { "remove": { "index": "products_v1", "alias": "products" }}
  ]
}

分片設計：
  根據數據量估算：每個分片 10GB~50GB
  1億商品 × 約1KB/文件 = 約100GB
  → 3~5 個主分片
  → 副本數：1（生產）或 2（高可用）

冷熱數據分離（ILM 索引生命週期管理）：
  Hot（最近7天）：SSD 磁碟，3副本
  Warm（7天~30天）：HDD 磁碟，1副本，只讀
  Cold（30天以上）：壓縮，1副本
  Delete（90天後）：自動刪除
```

---

### 🟡 效能調優

```yaml
# 索引設定調優
PUT /products/_settings
{
  "index": {
    "refresh_interval": "30s",
    "number_of_replicas": 0
  }
}
# 批量導入時：
# ① 關閉副本（number_of_replicas: 0）
# ② 增大 refresh_interval（降低刷新頻率）
# 導入完成後再恢復

# JVM 調優（elasticsearch.yml 旁的 jvm.options）
# 設定堆記憶體為物理記憶體的 50%，且不超過 32GB
# -Xms16g
# -Xmx16g
# ⚠️ 不超過 32GB！超過會關閉 Compressed OOPs，反而浪費記憶體
```

```java
// 批量寫入（Bulk API）- 提升寫入效能
@Service
public class BulkIndexService {

    private final ElasticsearchRestTemplate esTemplate;

    public void bulkIndex(List<ProductDocument> products) {
        // 每批 1000 個，避免單次請求太大
        int batchSize = 1000;
        for (int i = 0; i < products.size(); i += batchSize) {
            List<ProductDocument> batch = products.subList(
                i, Math.min(i + batchSize, products.size())
            );

            List<IndexQuery> queries = batch.stream()
                .map(product -> new IndexQueryBuilder()
                    .withId(product.getId().toString())
                    .withObject(product)
                    .build())
                .collect(Collectors.toList());

            esTemplate.bulkIndex(queries, ProductDocument.class);
            log.info("批量索引進度：{}/{}", i + batch.size(), products.size());
        }

        // 強制刷新（讓數據立即可搜索）
        esTemplate.indexOps(ProductDocument.class).refresh();
    }
}
```

```
查詢優化：

✅ 使用 filter 代替 must（有緩存，不計算分數）
✅ 避免深度分頁（from + size > 10000 很慢）
   改用 search_after（基於上一頁最後一條記錄）
✅ 只取需要的欄位（_source filtering）
✅ 避免通配符查詢（wildcard）在高基數欄位
✅ 避免對 text 欄位排序（用 keyword 欄位排序）

深度分頁 → search_after：
GET /products/_search
{
  "query": { "match_all": {} },
  "sort": [
    { "created_at": "desc" },
    { "_id": "desc" }
  ],
  "size": 20,
  "search_after": ["2024-01-15T10:30:00", "12345"]
}
```

---

### 🔴 ELK Stack 日誌系統建置

```
完整 ELK 日誌流程回顧：

Spring Boot（JSON 日誌）
    ↓ stdout / 文件
Filebeat（輕量日誌採集）
    ↓ beats 協議
Logstash（解析、過濾、轉換）
    ↓ HTTP / 客戶端
Elasticsearch（儲存、索引）
    ↓
Kibana（搜索、視覺化、告警）

日誌 Index 命名規範：
  {service}-logs-{date}
  order-service-logs-2024.01.15
  user-service-logs-2024.01.15

常用 Kibana KQL 查詢：
  log_level: "ERROR"
  trace_id: "abc123"
  service: "order-service" AND duration_ms > 1000
  log_message: *OutOfMemoryError*

ILM（Index Lifecycle Management）：
  Hot  → 7天，SSD，可讀寫
  Warm → 30天，HDD，只讀（forceMerge 到 1 個 Segment）
  Cold → 90天，壓縮
  Delete → 自動刪除
```

```json
// 建立日誌 ILM Policy
PUT /_ilm/policy/logs-policy
{
  "policy": {
    "phases": {
      "hot": {
        "min_age": "0ms",
        "actions": {
          "rollover": {
            "max_age":  "1d",
            "max_size": "50gb"
          }
        }
      },
      "warm": {
        "min_age": "7d",
        "actions": {
          "shrink":     { "number_of_shards": 1 },
          "forcemerge": { "max_num_segments": 1 },
          "allocate":   { "require": { "data": "warm" }}
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "allocate": { "require": { "data": "cold" }}
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

---

## 📝 Elasticsearch 總結

| 主題 | 核心要點 |
|------|---------|
| **倒排索引** | 詞 → 文件列表，O(1) 查詢，BM25 相關性評分 |
| **分散式架構** | Cluster/Node/Shard，主副本，自動容錯 |
| **Mapping** | text（分詞）/ keyword（不分詞），設計好不能輕易修改 |
| **分析器** | ik_max_word（索引時）/ ik_smart（搜索時）|
| **match** | 全文搜索，分詞後匹配，影響相關性分數 |
| **term** | 精確匹配，不分詞，用於 keyword 欄位 |
| **bool query** | must / should / must_not / filter 四個子句 |
| **filter** | 不影響分數，有緩存，效能最好，放過濾條件 |
| **聚合** | terms / range / histogram，用於統計和篩選面板 |
| **數據同步** | Canal + MQ 監聽 Binlog，最終一致性 |
| **分頁** | 深度分頁用 search_after，避免 from > 10000 |
| **批量寫入** | Bulk API，每批 1000 條，暫時關閉副本和 refresh |
| **ILM** | Hot → Warm → Cold → Delete，自動管理日誌索引 |
