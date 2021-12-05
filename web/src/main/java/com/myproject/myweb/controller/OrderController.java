package com.myproject.myweb.controller;

import com.myproject.myweb.domain.OrderStatus;
import com.myproject.myweb.dto.order.OrderItemDto;
import com.myproject.myweb.dto.order.OrderResponseDto;
import com.myproject.myweb.dto.user.CustomerResponseDto;
import com.myproject.myweb.exception.ItemStockException;
import com.myproject.myweb.service.CartService;
import com.myproject.myweb.service.ItemService;
import com.myproject.myweb.service.OrderService;
import com.myproject.myweb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final ItemService itemService;
    private final MessageSource messageSource;


    @PostMapping("/payment/ready")
    public String readyPayment(@RequestParam(value = "customer_id") Long customerId,
                               @RequestParam(value = "item_id") List<Long> itemIds,
                               @RequestParam(value = "count", required = false) String count, // item
                               @RequestParam(value = "coupon", required = false) String couponId, // item?
                               @RequestParam(value = "cart_id", required = false) String cartId, // cart
                               HttpSession session){

        Boolean orderImpossible = orderService.orderImpossible(customerId);
        String redirectUrl;
        if(cartId == null) {
            redirectUrl = "redirect:/item/detail/" + itemIds.get(0);
        }else{
            redirectUrl = "redirect:/cart/detail/" + cartId;
        }

        if(orderImpossible) {
            orderRedirectAttributes("OrderAlreadyInProgress");
            return redirectUrl;
            // 주문 페이지 따로 생기기 전까지는 item detail 또는 cart detail 페이지로
        }

        Long orderId;
        try {
            if (cartId != null) {
                // 장바구니에서 주문 (단건, 복수건) >> user exception >> common handler home redirect
                orderId = cartService.order(customerId, itemIds);
                session.setAttribute("order", "cart");
            } else {
                // 상품 단건 바로 주문 >> user or item exception >> common handler home redirect
                orderId = orderService.order(customerId, itemIds.get(0), Integer.parseInt(count), couponId); // 여러 상품들 >> 하나의 주문서 생성
                session.setAttribute("order", "direct");
            }

        }catch (ItemStockException e){
            String msg = messageSource.getMessage(e.getMessage(), new String[]{e.getArgs()[1]}, Locale.getDefault());
            itemService.stockNotice(Long.valueOf(e.getArgs()[0]));
            orderRedirectAttributes(msg);
            return redirectUrl;
        }

        try {
            String paymentUrl = paymentService.ready(customerId, orderId);
            if (paymentUrl != null) return "redirect:" + paymentUrl;

        }catch (WebClientResponseException e){
            log.error("카카오결제 준비 에러 = " + e.getResponseBodyAsString() + " >> 주문 삭제");
            e.printStackTrace();
            orderService.remove(orderId);
        }

        return redirectUrl;
    }

    @RequestMapping("/payment/cancel")
    public String paymentCancel(@RequestParam(value = "orderId") Long orderId, HttpSession session){
        // status QUIT_PAYMENT 확인하기
        String orderKind = (String) session.getAttribute("order");
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId, orderKind);
        orderService.remove(orderId); // 결제 중 취소이기에 배달 완료 익셉션 처리 NO
        orderRedirectAttributes("PaymentCancel");
        return "redirect:" + url;
        // 일단 item 하나일 경우 상품 상세 페이지, 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/payment/fail")
    public String orderFail(@RequestParam(value = "orderId") Long orderId, HttpSession session){
        // status QUIT_PAYMENT 확인하기
        String orderKind = (String) session.getAttribute("order");
        String url = orderService.getRedirectUrlByItemOneOrMany(orderId, orderKind);
        orderService.remove(orderId); // 결제 중 취소, 배달 완료 익셉션 처리 NO
        orderRedirectAttributes("PaymentFailed");
        return "redirect:" + url;
        // 일단 item 하나일 경우 상품 상세 페이지, 여러 개일 경우 장바구니 페이지로
    }

    @RequestMapping("/payment/approve") //GET
    public String approvePayment(@RequestParam(value = "pg_token") String pg_token, HttpSession session){
        CustomerResponseDto customer = (CustomerResponseDto) session.getAttribute("customer");
        OrderResponseDto order = orderService.findOrderReady(customer.getId());

        List<Long> itemIds = order.getOrderItems().stream()
                .map(OrderItemDto::getItemId)
                .collect(Collectors.toList());

        String orderKind = (String) session.getAttribute("order");
        try {
            paymentService.approve(customer.getId(), order.getId(), pg_token);

            orderService.updateOrderStatus(order.getId(), OrderStatus.COMP); // 결제완료된 orderId exception은 없을 가능성이 높아 common handler로만 처리

            if (orderKind.equals("cart")) cartService.remove(customer.getCartId(), itemIds);

            return "redirect:/order/detail/" + order.getId();

        }catch (WebClientResponseException e) {
            // 400 Bad Request Error etc...
            log.error(e.getResponseBodyAsString());

            orderRedirectAttributes("PaymentFailed");
            if (orderKind.equals("cart")) return "redirect:/cart/detail/" + customer.getCartId();
            return "redirect:/item/detail/" + order.getOrderItems().get(0).getItemId();
        }
    }

    @RequestMapping("/cancel/{orderId}")
    public String orderCancel(@PathVariable(value = "orderId") Long orderId){
        String msg = "";
        try{
            paymentService.cancel(orderId); // orderService.cancel() 까지 호출, exception은 common으로 해결
            msg = "OrderCancelComplete";

        }catch (IllegalStateException e){
            if(e.getMessage().equals("DeliveryAlreadyCompletedException")) { // 배달완료 exception 처리
                msg = e.getMessage();
            }
            log.error(e.getMessage());

        }catch (WebClientResponseException e){
            log.error("주문 취소 중 결제 취소 에러 발생 >> " + e.getResponseBodyAsString());
            msg = "PaymentCancelFailed";
        }

        orderRedirectAttributes(msg);
        return "redirect:/order/list";
    }

    private void orderRedirectAttributes(String msg) {
        RedirectAttributes attributes = new RedirectAttributesModelMap();
        attributes.addAttribute("msg", msg);
    }

    @GetMapping("/list") // 페이징 처리
    public String list(@RequestParam(value = "msg", required = false) String msg, Model model){
        if(msg != null) model.addAttribute("msg", messageSource.getMessage(msg, null, Locale.getDefault()));
        return "order/list";
    }

    @GetMapping("/list/api")
    @ResponseBody
    public ItemService.ListByPaging<OrderResponseDto> listApi(HttpSession session,
                                                              Pageable pageable){
        CustomerResponseDto customer = (CustomerResponseDto) session.getAttribute("customer");
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber()-1, pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));
        return orderService.findByCustomerAndPaging(customer.getId(), pageRequest);
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        OrderResponseDto order = orderService.findById(id); // list에서만 호출가능한 것이 아니라 common exception handler로
        model.addAttribute("order", order);
        return "order/detail";
    }

}
