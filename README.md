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

## 資料處裡流程說明

基於 Spring Boot 的多層架構設計

1. **請求入口 (Controller 層)**
- 系統分為前台(`/user/*`)和後台(`/admin/*`)兩個主要入口
- 前台處理一般用戶操作，如購物車、訂單、用戶資料等
- 後台處理管理員操作，如商品管理、訂單管理、促銷管理等
- 所有請求都會先經過 SecurityConfig 進行身份驗證

2. **資料轉換 (DTO 層)**
- 請求資料 (Request DTO):
   - 接收前端傳來的資料，如 LoginRequest、CreateOrderRequest 等
   - 進行基本的資料驗證和格式轉換
- 響應資料 (Response DTO):
   - 將處理結果轉換為前端所需格式
   - 依據不同需求提供不同詳細程度的資料(如基本資料、詳細資料等)

3. **業務邏輯 (Service 層)**
- 實作核心業務邏輯，如：
   - UserService: 處理用戶註冊、資料更新等
   - OrderService: 處理訂單創建、狀態更新等
   - ProductService: 處理商品資訊維護
   - PromotionService: 處理促銷活動邏輯

4. **資料存取 (Repository 層)**
- 負責與資料庫的直接互動
- 處理各種實體的 CRUD 操作
- 提供資料查詢功能
- 確保資料的持久化

5. **資料模型 (Model 層)**
- 定義了系統中的核心實體：
   - 用戶相關：User、Role
   - 商品相關：Product、Category、Review
   - 訂單相關：Order、OrderItem、OrderStatus
   - 購物車：ShoppingCart
   - 促銷相關：Promotion、ProductPromotion、OrderPromotion

6. **錯誤處理**
- GlobalExceptionHandler 統一處理系統異常
- BusinessException 處理業務邏輯異常
- 確保系統能夠優雅地處理各種錯誤情況

7. **安全機制**
- SecurityConfig 配置安全規則
- BCryptConfig 處理密碼加密
- JwtConfig 和 JwtUtil 處理令牌相關邏輯

典型的資料處理流程如下：
1. 請求進入 Controller
2. Controller 將請求數據轉換為 DTO
3. 調用相應的 Service 處理業務邏輯
4. Service 通過 Repository 訪問數據庫
5. 將處理結果轉換為 Response DTO
6. 返回給客戶端

整體架構遵循了關注點分離原則，每一層都有其特定的職責，使系統更容易維護和擴展。同時通過 DTO 模式實現了前後端數據的解耦，提高了系統的靈活性。



## 專案架構
```
src
└── main
    ├── java
    │   └── fcu
    │       └── ilive
    │           ├── config                          # 設定檔目錄
    │           │   ├── SecurityConfig.java         # Spring Security 與 密碼加密設定 設定
    │           │   └── JwtConfig.java              # JWT Token 設定
    │           │
    │           ├── controller                      # API 控制器目錄
    │           │   ├── user                        # 前台 API 控制器
    │           │   │   ├── AuthController.java     # 認證相關 API (/auth/**)
    │           │   │   ├── UserController.java     # 使用者相關 API (/users/**)
    │           │   │   ├── CartController.java     # 購物車相關 API (/cart/**)
    │           │   │   └── OrderController.java    # 訂單相關 API (/orders/**)
    │           │   └── admin                       # 後台 API 控制器
    │           │       ├── AdminAuthController.java   # 管理員認證 API (/admin/auth/**)
    │           │       ├── AdminUserController.java   # 管理員用戶管理 API (/admin/users/**)
    │           │       ├── ProductController.java     # 商品管理 API (/admin/products/**)
    │           │       ├── OrderController.java       # 訂單管理 API (/admin/orders/**)
    │           │       └── PromotionController.java   # 促銷管理 API (/admin/promotions/**)
    │           │
    │           ├── model                           # 資料模型目錄
    │           │   ├── user                        # 使用者相關模型
    │           │   │   └── User.java               # 使用者實體
    │           │   ├── admin                       # 管理員相關模型
    │           │   │   ├── Admin.java              # 管理員實體
    │           │   │   ├── AdminRoles.java         # 管理員角色實體  
    │           │   │   ├── AdminPermission.java    # 管理員權限實體
    │           │   │   ├── AdminRolePermission.java # 角色權限關聯
    │           │   │   └── AdminUserRole.java      # 管理員角色關聯
    │           │   ├── product                     # 商品相關模型
    │           │   │   ├── Product.java            # 商品實體
    │           │   │   ├── Category.java           # 分類實體
    │           │   │   └── Review.java             # 評價實體
    │           │   ├── order                       # 訂單相關模型
    │           │   │   ├── Order.java              # 訂單實體
    │           │   │   ├── OrderItem.java          # 訂單項目實體
    │           │   │   └── OrderStatus.java        # 訂單狀態實體
    │           │   ├── cart                        # 購物車相關模型
    │           │   │   ├── CartItems.java          # 訂單項目實體    
    │           │   │   └── ShoppingCart.java       # 購物車實體
    │           │   └── promotion                   # 促銷相關模型
    │           │       ├── Promotion.java          # 促銷活動實體
    │           │       ├── ProductPromotion.java   # 商品促銷實體
    │           │       └── OrderPromotion.java     # 訂單促銷實體
    │           │
    │           ├── repository                      # 資料存取層目錄
    │           │   ├── user                        # 使用者相關資料存取
    │           │   │   ├── UserRepository.java     # 使用者資料操作
    │           │   │   └── AdminRolesRepository.java # 管理員角色資料操作
    │           │   ├── admin                       # 管理員相關資料存取
    │           │   │   ├── AdminRepository.java    # 管理員資料操作
    │           │   │   ├── AdminRolePermissionRepository.java # 角色權限關聯資料操作
    │           │   │   └── AdminUserRoleRepository.java # 管理員角色關聯資料操作
    │           │   ├── product                     # 商品相關資料存取
    │           │   │   ├── ProductRepository.java  # 商品資料操作
    │           │   │   └── CategoryRepository.java # 分類資料操作
    │           │   ├── order                       # 訂單相關資料存取
    │           │   │   ├── OrderRepository.java    # 訂單資料操作
    │           │   │   └── OrderItemRepository.java # 訂單項目資料操作
    │           │   └── promotion                   # 促銷相關資料存取
    │           │       └── PromotionRepository.java # 促銷資料操作
    │           │
    │           ├── service                         # 服務層目錄
    │           │   ├── user                        # 使用者相關服務
    │           │   │   ├── UserService.java        # 使用者服務實作
    │           │   │   └── AuthService.java        # 認證服務實作
    │           │   ├── admin                       # 管理員相關服務
    │           │   │   ├── AdminService.java       # 管理員服務實作
    │           │   │   ├── AdminAuthService.java   # 管理員認證服務實作
    │           │   │   └── AdminPermissionService.java # 管理員權限服務實作
    │           │   ├── product                     # 商品相關服務
    │           │   │   └── ProductService.java     # 商品服務實作
    │           │   ├── order                       # 訂單相關服務
    │           │   │   └── OrderService.java       # 訂單服務實作
    │           │   └── promotion                   # 促銷相關服務
    │           │       └── PromotionService.java   # 促銷服務實作
    │           │
    │           ├── constant                        # 常數定義目錄
    │           │   ├── OrderStatusEnum.java        # 訂單狀態列舉
    │           │   └── AdminPermissionEnum.java    # 管理員權限列舉
    │           │
    │           ├── util                            # 工具類目錄
    │           │   └── JwtUtil.java                # JWT 工具類
    │           │
    │           ├── exception                       # 例外處理目錄
    │           │   ├── GlobalExceptionHandler.java # 全局例外處理
    │           │   └── BusinessException.java      # 業務邏輯例外
    │           │
    │           └── ILiveApplication.java           # 應用程式進入點
    │
    └── resources                                   # 資源檔案目錄
        └── application.properties                  # 基本配置檔
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
- 最後更新：2024/11/29

## 注意事項
1. 開發時請遵循 RESTful API 設計原則
2. 確保程式碼遵循 Java 代碼規範
3. 提交前進行完整測試
4. 資料庫連結獨立不上傳至git

## 授權資訊
© 2024 FCU iLive Team. All Rights Reserved.