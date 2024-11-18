# iLive 電商平台

## 專案簡介
iLive 是一個提供完整購物體驗的電商平台系統，包含前台購物功能及後台管理系統。

## 技術需求
- JDK 17
- Spring Boot 3.x
- MariaDB
- Maven 3.x
- Spring Security
- JWT Token
- BCrypt 加密

## 專案架構
```
src
└── main
    ├── java
    │   └── fcu
    │       └── ilive
    │           ├── config                          # 設定檔目錄
    │           │   ├── SecurityConfig.java         # Spring Security 設定
    │           │   ├── BCryptConfig.java          # 密碼加密設定
    │           │   └── JwtConfig.java             # JWT Token 設定
    │           │
    │           ├── controller                      # API 控制器目錄
    │           │   ├── user                       # 前台 API 控制器
    │           │   │   ├── AuthController.java    # 認證相關 API (/auth/**)
    │           │   │   ├── UserController.java    # 使用者相關 API (/users/**)
    │           │   │   ├── CartController.java    # 購物車相關 API (/cart/**)
    │           │   │   └── OrderController.java   # 訂單相關 API (/orders/**)
    │           │   └── admin                      # 後台 API 控制器
    │           │       ├── ProductController.java    # 商品管理 API (/admin/products/**)
    │           │       ├── OrderController.java      # 訂單管理 API (/admin/orders/**)
    │           │       ├── PromotionController.java  # 促銷管理 API (/admin/promotions/**)
    │           │       └── AdminController.java      # 管理員相關 API (/admin/users/**)
    │           │
    │           ├── model                          # 資料模型目錄
    │           │   ├── user                       # 使用者相關模型
    │           │   │   ├── User.java             # 使用者實體
    │           │   │   └── Role.java             # 角色實體
    │           │   ├── product                    # 商品相關模型
    │           │   │   ├── Product.java          # 商品實體
    │           │   │   ├── Category.java         # 分類實體
    │           │   │   └── Review.java           # 評價實體
    │           │   ├── order                      # 訂單相關模型
    │           │   │   ├── Order.java            # 訂單實體
    │           │   │   ├── OrderItem.java        # 訂單項目實體
    │           │   │   └── OrderStatus.java      # 訂單狀態實體
    │           │   ├── cart                       # 購物車相關模型
    │           │   │   └── ShoppingCart.java     # 購物車實體
    │           │   └── promotion                  # 促銷相關模型
    │           │       ├── Promotion.java         # 促銷活動實體
    │           │       ├── ProductPromotion.java  # 商品促銷實體
    │           │       └── OrderPromotion.java    # 訂單促銷實體
    │           │
    │           ├── repository                     # 資料存取層目錄
    │           │   ├── user                       # 使用者相關資料存取
    │           │   │   ├── UserRepository.java    # 使用者資料操作
    │           │   │   └── RoleRepository.java    # 角色資料操作
    │           │   ├── product                    # 商品相關資料存取
    │           │   │   ├── ProductRepository.java # 商品資料操作
    │           │   │   └── CategoryRepository.java # 分類資料操作
    │           │   ├── order                      # 訂單相關資料存取
    │           │   │   ├── OrderRepository.java   # 訂單資料操作
    │           │   │   └── OrderItemRepository.java # 訂單項目資料操作
    │           │   └── promotion                  # 促銷相關資料存取
    │           │       └── PromotionRepository.java # 促銷資料操作
    │           │
    │           ├── service                        # 服務層目錄
    │           │   ├── user                       # 使用者相關服務
    │           │   │   ├── UserService.java      # 使用者服務實作
    │           │   │   └── AuthService.java      # 認證服務實作
    │           │   ├── product                    # 商品相關服務
    │           │   │   └── ProductService.java   # 商品服務實作
    │           │   ├── order                      # 訂單相關服務
    │           │   │   └── OrderService.java     # 訂單服務實作
    │           │   └── promotion                  # 促銷相關服務
    │           │       └── PromotionService.java  # 促銷服務實作
    │           │
    │           ├── dto                            # 資料傳輸物件目錄
    │           │   ├── request                    # 請求物件
    │           │   │   ├── auth                  # 認證相關請求
    │           │   │   │   ├── LoginRequest.java # 登入請求
    │           │   │   │   ├── RegisterRequest.java # 註冊請求
    │           │   │   │   └── PasswordResetRequest.java # 重設密碼請求
    │           │   │   ├── user                  # 使用者相關請求
    │           │   │   │   ├── UpdateProfileRequest.java  # 更新個人資料請求
    │           │   │   │   └── ChangePasswordRequest.java # 修改密碼請求
    │           │   │   ├── product               # 商品相關請求
    │           │   │   │   ├── CreateProductRequest.java  # 建立商品請求
    │           │   │   │   ├── UpdateProductRequest.java  # 更新商品請求
    │           │   │   │   └── ProductSearchRequest.java  # 商品搜尋請求
    │           │   │   ├── order                 # 訂單相關請求
    │           │   │   │   ├── CreateOrderRequest.java    # 建立訂單請求
    │           │   │   │   ├── UpdateOrderRequest.java    # 更新訂單請求
    │           │   │   │   └── OrderQueryRequest.java     # 訂單查詢請求   
    │           │   │   └── promotion             # 促銷相關請求
    │           │   │       ├── CreatePromotionRequest.java # 建立促銷活動請求
    │           │   │       └── UpdatePromotionRequest.java # 更新促銷活動請求
    │           │   └── response                   # 回應物件
    │           │       ├── auth                  # 認證相關回應
    │           │       │   ├── LoginResponse.java # 登入回應
    │           │       │   ├── JwtResponse.java # JWT令牌回應
    │           │       │   └── UserTokenResponse.java # 使用者令牌回應   
    │           │       ├── user                  # 使用者相關回應
    │           │       │   ├── UserProfileResponse.java # 使用者資料回應
    │           │       │   └── UserDetailsResponse.java # 使用者詳細資料回應  
    │           │       ├── product               # 商品相關回應
    │           │       │   ├── ProductResponse.java # 商品基本資料回應
    │           │       │   ├── ProductDetailResponse.java # 商品詳細資料回應
    │           │       │   └── ProductPageResponse.java # 商品分頁資料回應    
    │           │       ├── order                 # 訂單相關回應
    │           │       │   ├── OrderResponse.java # 訂單基本資料回應
    │           │       │   ├── OrderDetailResponse.java # 訂單詳細資料回應
    │           │       │   └── OrderStatusResponse.java # 訂單狀態回應
    │           │       └── promotion             # 促銷相關回應
    │           │           ├── PromotionResponse.java # 促銷活動基本資料回應
    │           │           └── PromotionDetailResponse.java # 促銷活動詳細資料回應
    │           │
    │           ├── constant                       # 常數定義目錄
    │           │   └── OrderStatusEnum.java      # 訂單狀態列舉
    │           │
    │           ├── util                          # 工具類目錄
    │           │   └── JwtUtil.java             # JWT 工具類
    │           │
    │           ├── exception                      # 例外處理目錄
    │           │   ├── GlobalExceptionHandler.java # 全局例外處理
    │           │   └── BusinessException.java     # 業務邏輯例外
    │           │
    │           └── ILiveApplication.java          # 應用程式進入點
    │
    └── resources                                  # 資源檔案目錄
        └── application.properties                 # 基本配置檔
        
```

## 主要功能
### 前台功能
1. 會員功能
    - 註冊/登入
    - 個人資料管理
    - 訂單查詢
    - 購物車管理

2. 商品功能
    - 商品瀏覽與搜尋
    - 商品分類查詢
    - 商品評價與評論

3. 訂單功能
    - 購物車管理
    - 訂單建立
    - 付款處理
    - 訂單狀態查詢

### 後台功能
1. 管理者權限
    - 管理者帳號管理
    - 權限設定
    - 角色管理

2. 商品管理
    - 商品上架/下架
    - 庫存管理
    - 分類管理

3. 訂單管理
    - 訂單處理
    - 出貨管理
    - 退換貨處理

## 安裝步驟
1. 環境準備
```bash
# 確認 Java 版本
java -version

# 確認 Maven 版本
mvn -version
```

2. 資料庫設定
- 建立 MariaDB 資料庫
- 修改 application.properties 資料庫連線設定

3. 專案建置
```bash
# 建置專案
mvn clean install

# 運行專案
mvn spring-boot:run
```

## 開發文件
- API文件：見專案內 API 文件
- 系統需求規格書：見專案內文件

## 版本資訊
- 版本：1.0.0
- 最後更新：2024/11/27

## 注意事項
1. 開發時請遵循 RESTful API 設計原則
2. 確保程式碼遵循 Java 代碼規範
3. 提交前進行完整測試
4. 資料庫連結獨立不上傳至git

## 授權資訊
© 2024 FCU iLive Team. All Rights Reserved.