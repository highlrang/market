<br/>  
## 쇼핑몰 프로젝트
<br/>  

#### 프로젝트 설명

일반적인 쇼핑몰 기능을 갖춘 프로젝트로 판매자와 구매자로 사용자를 구분하여 기능을 구현했습니다.
판매자의 기능으로는 상품 등록, 수정, 삭제, 리스트뷰 및 디테일뷰, 알림 기능을 구현하였고,
구매자의 기능으로는 상품 리스트뷰 및 디테일뷰, 상품 구매 및 결제, 장바구니, 주문 리스트뷰 및 디테일뷰, 쿠폰 기능을 구현했습니다.


-----------------------------------------------------------
<br/>  

#### JPA

+ 연관관계를 통한 모델링 최적화
+ 도메인 메서드를 통한 코드 통일성 및 데이터 일관성 유지

```java
@Entity
@NoArgsConstructor
@Getter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    public void setCustomer(Customer customer) {
        this.customer = customer;
        customer.setCart(this);
    }

    public void addCartItem(CartItem cartItem){ // Cart 최초 생성 시 + 추가로 장바구니 상품 추가 시
        List<Long> itemIds = cartItems.stream().map(c -> c.getItem().getId()).collect(Collectors.toList());
        if(itemIds.contains(cartItem.getItem().getId())) {
            throw new IllegalStateException("CartItemAlreadyExistedInCart");
        }
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    public static Cart createCart(Customer customer, CartItem cartItem){ // 생성자
        Cart cart = new Cart();
        cart.setCustomer(customer);
        cart.addCartItem(cartItem);
        return cart;
    }
    ...
    
    public int getTotalPrice(){
        return cartItems.stream().mapToInt(CartItem::getTotalPrice).sum();
    }
}
```

-----------------------------------------------------------
<br/>  

#### REST API

+ 리스트 페이징) Controller

```java
@GetMapping("/list/api")
@ResponseBody
public ItemService.ListByPaging<OrderResponseDto> listApi(HttpSession session, Pageable pageable){
    CustomerResponseDto customer = (CustomerResponseDto) session.getAttribute("customer");
    return orderService.findByCustomerAndPaging(customer.getId(), pageable);
}
```

+ 리스트 페이징) Jquery Ajax

```java
$(function(){
    var size=$(this).val();
    var data={
        page:"1",
        size:size
    };

    $.ajax({
        type:'get',
        url:'/order/list/api',
        data:data,

        success: function(data){
            console.log(data);
            $("#now_page").val("1");
            $("#total_page").val(data["totalPage"]);

            orderListChange(data);
            pagination("1",size,data["totalPage"]);
        },
        
        error: function(request,status,error){
            console.log(request,status,error);
        }
    });
}
```

+ kakaopay 결제
    + WebClient로 kakaopay api 규격에 맞춰 요청

```java
@Transactional
public String ready(Long customerId, Long orderId) throws WebClientResponseException {
    Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("OrderNotFoundException"));
    OrderResponseDto orderResponseDto = new OrderResponseDto(order);

    String myHost = "http://127.0.0.1/order/payment";
    String kakaopayUrl = "https://kapi.kakao.com/v1/payment/ready";

    MultiValueMap<String, String> parameterMap = getParameterMap(customerId, orderId);
    parameterMap.add("item_name", orderResponseDto.getOrderItemsName());
    parameterMap.add("quantity", String.valueOf(orderResponseDto.getTotalCount()));
    parameterMap.add("total_amount", String.valueOf(orderResponseDto.getTotalPrice()));
    parameterMap.add("tax_free_amount", "0");
        
    parameterMap.add("approval_url", myHost + "/approve");
    parameterMap.add("cancel_url", myHost + "/cancel?orderId=" + orderId);
    parameterMap.add("fail_url", myHost + "/fail?orderId=" + orderId);

    Mono<PaymentReadyDto> mono = webClient
                .mutate()
                .baseUrl(kakaopayUrl)
                .defaultHeaders(httpHeader -> {
                    httpHeader.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    httpHeader.set("Authorization", "KakaoAK 39e0b2a37b36d82e2289cee9827048e2");
                })
                .build()
                .post()
                .body(BodyInserters.fromFormData(parameterMap))
                .retrieve()
                .bodyToMono(PaymentReadyDto.class);

    PaymentReadyDto paymentReadyDto = mono.block();
    this.saveTid(orderId, paymentReadyDto.getTid());
    return paymentReadyDto.getNext_redirect_pc_url();
}

```


-----------------------------------------------------------
<br/>  

#### 멀티 모듈 프로젝트

+ build.gradle
    + core 모듈에 entity와 Repository 생성
    + web 모듈에 core 모듈을 의존성 jar로 추가
    + 여러 모듈에서 entity를 공유 가능

```java
project(':web'){
    dependencies{
        compile project(':core')
    }
}
```

-----------------------------------------------------------
<br/>  

#### Scheduler를 이용한 일괄 작업 자동화

+ 오래된 쿠폰과 판매자 알림 삭제 작업

```java
@Component
@RequiredArgsConstructor
public class RemoveScheduler {
    private final CouponRepository couponRepository;
    private final SellerNoticeRepository sellerNoticeRepository;
    
    @Scheduled(cron = "0 0 1 * * *") // 매월 1일
    public void couponRemove(){
        List<Coupon> coupons = couponRepository.findAllByExpirationDateBefore(LocalDateTime.now());
        couponRepository.deleteAllInBatch(coupons);
    }
    
    @Scheduled(cron = "0 0 2 * * *")
    public void sellerNoticeRemove(){
        List<SellerNotice> sellerNoticeList = sellerNoticeRepository.findAllByDatetimeBefore(LocalDateTime.now().minusYears(1));
        sellerNoticeRepository.deleteAllInBatch();
    }
}
```


-----------------------------------------------------------  
<br/>  

#### AWS 배포

+ .travis.yml을 통해 travis에서 CI된 코드를 빌드
+ 프로젝트 jar 파일과 배포 스크립트를 묶어 AWS S3에 전달
+ AWS Codedeploy에서 S3에 업로드된 파일(appspec.yml, start.sh 등)을 읽어 AWS EC2에 배포
+ <http://ec2-13-124-45-59.ap-northeast-2.compute.amazonaws.com>

<br/><br/>  
