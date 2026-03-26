# 📒 Catalog Service — Low-Level Design

## 1. Class Diagram

```mermaid
classDiagram
    class ProductService {
        +createProduct(CreateProductRequest) ProductResponse
        +getProduct(id) ProductResponse
        +getProductBySku(skuCode) ProductResponse
        +getProductsByCategory(categoryId, Pageable) Page
        +getProductsBySeller(sellerId, Pageable) Page
        +searchProducts(keyword, Pageable) Page
        +getFeaturedProducts(limit) List
        +updateProduct(id, UpdateProductRequest) ProductResponse
        +deleteProduct(id) void
    }

    class CategoryService {
        +createCategory(CreateCategoryRequest) CategoryResponse
        +getRootCategories() List
        +getCategory(id) CategoryResponse
        +getSubcategories(parentId) List
    }

    class BrandService {
        +createBrand(name, logoUrl, desc) BrandResponse
        +getAllBrands() List
        +getBrand(id) BrandResponse
    }

    class SkuGeneratorService {
        -AtomicLong counter
        +generateSku(categorySlug) String
    }

    class Product {
        -String id
        -String skuCode
        -String name
        -String description
        -BigDecimal mrp
        -BigDecimal sellingPrice
        -BigDecimal discountPercentage
        -String unit
        -BigDecimal weight
        -Category category
        -Brand brand
        -String sellerId
        -List~ProductVariant~ variants
        -List~ProductImage~ images
        -boolean active
        -boolean featured
        -String hsnCode
        -BigDecimal gstPercentage
        -BigDecimal avgRating
        -Integer reviewCount
    }

    class Category {
        -String id
        -String name
        -String slug
        -String description
        -String imageUrl
        -Category parent
        -List~Category~ children
        -Integer level
        -Integer sortOrder
        -boolean active
    }

    class Brand {
        -String id
        -String name
        -String slug
        -String logoUrl
        -boolean active
    }

    class ProductVariant {
        -String id
        -Product product
        -String variantSkuCode
        -String size
        -String color
        -BigDecimal variantWeight
        -BigDecimal price
    }

    class ProductImage {
        -String id
        -Product product
        -String imageUrl
        -String altText
        -Integer sortOrder
        -boolean isPrimary
    }

    ProductService --> Product
    ProductService --> CategoryService
    ProductService --> BrandService
    ProductService --> SkuGeneratorService
    Product *-- ProductVariant
    Product *-- ProductImage
    Product --> Category
    Product --> Brand
    Category --> Category : parent-child
```

## 2. Category Hierarchy (Self-Referencing Tree)

```mermaid
graph TB
    ROOT["Catalog Root"]
    
    GROC["🥦 Groceries<br>level=0"]
    ELEC["📱 Electronics<br>level=0"]
    FASH["👕 Fashion<br>level=0"]
    HOME["🏠 Home & Kitchen<br>level=0"]

    FRUITS["Fruits & Vegetables<br>level=1"]
    DAIRY["Dairy & Bread<br>level=1"]
    SNACKS["Snacks & Beverages<br>level=1"]
    PHONES["Smartphones<br>level=1"]
    LAPTOPS["Laptops<br>level=1"]

    FRESH_FRUITS["Fresh Fruits<br>level=2"]
    FRESH_VEG["Fresh Vegetables<br>level=2"]
    EXOTIC["Exotic Fruits<br>level=2"]
    MILK["Milk<br>level=2"]
    CURD["Curd & Yogurt<br>level=2"]

    ROOT --> GROC & ELEC & FASH & HOME
    GROC --> FRUITS & DAIRY & SNACKS
    ELEC --> PHONES & LAPTOPS
    FRUITS --> FRESH_FRUITS & FRESH_VEG & EXOTIC
    DAIRY --> MILK & CURD
```

## 3. SKU Generation

```
Format: IWOS-{CATEGORY_PREFIX}-{SEQUENCE}

Examples:
  IWOS-FRU-000001   → Fruits category, 1st product
  IWOS-ELE-000042   → Electronics, 42nd product
  IWOS-DAI-000007   → Dairy, 7th product

Category prefix: First 3 chars of category slug (uppercase)
Sequence: AtomicLong counter (thread-safe)
```

## 4. ER Diagram

```mermaid
erDiagram
    PRODUCTS {
        uuid id PK
        varchar sku_code UK
        varchar name
        text description
        decimal mrp
        decimal selling_price
        decimal discount_percentage
        varchar unit
        decimal weight
        uuid category_id FK
        uuid brand_id FK
        varchar seller_id
        boolean active
        boolean featured
        varchar hsn_code
        decimal gst_percentage
        varchar search_keywords
        decimal avg_rating
        int review_count
        timestamp created_at
        timestamp updated_at
        bigint version
    }

    CATEGORIES {
        uuid id PK
        varchar name
        varchar slug UK
        text description
        varchar image_url
        uuid parent_id FK
        int level
        int sort_order
        boolean active
    }

    BRANDS {
        uuid id PK
        varchar name UK
        varchar slug UK
        varchar logo_url
        text description
        boolean active
    }

    PRODUCT_VARIANTS {
        uuid id PK
        uuid product_id FK
        varchar variant_sku_code UK
        varchar size
        varchar color
        decimal variant_weight
        decimal price
        boolean active
    }

    PRODUCT_IMAGES {
        uuid id PK
        uuid product_id FK
        varchar image_url
        varchar alt_text
        int sort_order
        boolean is_primary
    }

    PRODUCTS }|--|| CATEGORIES : belongs_to
    PRODUCTS }|--o| BRANDS : has_brand
    PRODUCTS ||--o{ PRODUCT_VARIANTS : has_variants
    PRODUCTS ||--o{ PRODUCT_IMAGES : has_images
    CATEGORIES ||--o{ CATEGORIES : has_children
```

## 5. Search Index Sync (Catalog → OpenSearch)

```mermaid
sequenceDiagram
    participant CAT as Catalog Service
    participant KAFKA as Kafka
    participant SRCH as Search Service
    participant OS as OpenSearch

    CAT->>KAFKA: ProductCreatedEvent {id, sku, name, price, category}
    KAFKA->>SRCH: Consume event
    SRCH->>SRCH: Map to ProductDocument
    SRCH->>OS: PUT /products/_doc/{id}
    OS-->>SRCH: Indexed ✅

    Note over CAT: Product price updated
    CAT->>KAFKA: ProductUpdatedEvent {id, newPrice}
    KAFKA->>SRCH: Consume event
    SRCH->>OS: PUT /products/_doc/{id} (partial update)

    Note over CAT: Product deactivated
    CAT->>KAFKA: ProductDeletedEvent {id}
    KAFKA->>SRCH: Consume event
    SRCH->>OS: DELETE /products/_doc/{id}
```
