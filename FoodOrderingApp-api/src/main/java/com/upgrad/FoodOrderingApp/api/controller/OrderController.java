package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private ItemService itemService;

    //Get details of the coupon by coupon name
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> getCouponByCouponName(
            @PathVariable("coupon_name") final String couponName,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, CouponNotFoundException
    {
        String[] bearerToken = authorization.split("Bearer ");
        CustomerEntity customerEntity = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = customerService.getCustomer(bearerToken[1]);
        }

        CouponEntity couponEntity = orderService.getCouponByCouponName(couponName);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse()
                .id(UUID.fromString(couponEntity.getUuid()))
                .couponName(couponEntity.getCouponName())
                .percent(couponEntity.getPercent());
        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.OK);
    }

    //Lists all past orders by customer
    //List all past orders by customer valid access token
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> getCustomerOrders(
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException
    {
        String[] bearerToken = authorization.split("Bearer ");
        CustomerEntity customerEntity = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = customerService.getCustomer(bearerToken[1]);
        }

        // Get all orders by customer
        List<OrderEntity> orderEntityList = orderService.getOrdersByCustomers(customerEntity);

        // Create response
        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();
        if(orderEntityList!=null){

        for (OrderEntity orderEntity : orderEntityList) {
            OrderListCoupon orderListCoupon = null;
            //Handling null value for coupon_id
            //Included this logic considering that not all orders will have a coupon_id
            if(orderEntity.getCoupon()==null) {
                orderListCoupon = new OrderListCoupon()
                        .id(null)
                        .couponName(null)
                        .percent(null);
            } else {
                orderListCoupon = new OrderListCoupon()
                        .id(UUID.fromString(orderEntity.getCoupon().getUuid()))
                        .couponName(orderEntity.getCoupon().getCouponName())
                        .percent(orderEntity.getCoupon().getPercent());
            }
            //payment
//            OrderListPayment orderListPayment = new OrderListPayment()
//                    .id(UUID.fromString(orderEntity.getPayment().getUuid()))
//                    .paymentName(orderEntity.getPayment().getPaymentName());

            OrderListCustomer orderListCustomer = new OrderListCustomer()
                    .id(UUID.fromString(orderEntity.getCustomer().getUuid()))
                    .firstName(orderEntity.getCustomer().getFirstName())
                    .lastName(orderEntity.getCustomer().getLastName())
                    .emailAddress(orderEntity.getCustomer().getEmail())
                    .contactNumber(orderEntity.getCustomer().getContactNum());

            OrderListAddressState orderListAddressState = new OrderListAddressState()
                    .id(UUID.fromString(orderEntity.getAddress().getState().getUuid()))
                    .stateName(orderEntity.getAddress().getState().getStateName());

            OrderListPayment orderListPayment = new OrderListPayment()
                    .id(UUID.fromString(orderEntity.getPayment().getUuid()))
                    .paymentName(orderEntity.getPayment().getPaymentName());

            OrderListAddress orderListAddress = new OrderListAddress()
                    .id(UUID.fromString(orderEntity.getAddress().getUuid()))
                    .flatBuildingName(orderEntity.getAddress().getFlatBuilNumber())
                    .locality(orderEntity.getAddress().getLocality())
                    .city(orderEntity.getAddress().getCity())
                    .pincode(orderEntity.getAddress().getPinCode())
                    .state(orderListAddressState);

            OrderList orderList = new OrderList()
                    .id(UUID.fromString(orderEntity.getUuid()))
                    .bill(new BigDecimal(orderEntity.getBill()))
                    .coupon(orderListCoupon)
                    .discount(new BigDecimal(orderEntity.getDiscount()))
                    .date(orderEntity.getDate().toString())
                    .payment(orderListPayment)
                    .customer(orderListCustomer)
                    .address(orderListAddress);

            for (OrderItemEntity orderItemEntity : itemService.getItemsByOrder(orderEntity)) {

                ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem()
                        .id(UUID.fromString(orderItemEntity.getItem().getUuid()))
                        .itemName(orderItemEntity.getItem().getItemName())
                        .itemPrice(orderItemEntity.getItem().getPrice())
                        .type(ItemQuantityResponseItem.TypeEnum.fromValue(orderItemEntity.getItem().getType().getValue()));

                ItemQuantityResponse itemQuantityResponse = new ItemQuantityResponse()
                        .item(itemQuantityResponseItem)
                        .quantity(orderItemEntity.getQuantity())
                        .price(orderItemEntity.getPrice());

                orderList.addItemQuantitiesItem(itemQuantityResponse);
            }

            customerOrderResponse.addOrdersItem(orderList);
        }}

        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
    }

    //Creating/Saving new order by customer
    @CrossOrigin
    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> saveOrder(
            final SaveOrderRequest saveOrderRequest,
            @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, CouponNotFoundException,
            AddressNotFoundException, PaymentMethodNotFoundException,
            RestaurantNotFoundException, ItemNotFoundException, SaveOrderException {
        String[] bearerToken = authorization.split("Bearer ");
        CustomerEntity customerEntity = null;
        if(bearerToken.length==1){
            throw new AuthorizationFailedException("ATHR-005","Use valid authorization format <Bearer accessToken>");
        } else {
            customerEntity = customerService.getCustomer(bearerToken[1]);
        }

        //Ensuring no fields are empty except coupon_id and discount
        /*Included this logic considering that not all orders will have a coupon_id and discount
         but other values are required
        */
        if(saveOrderRequest.getAddressId()==null||saveOrderRequest.getItemQuantities().get(0).getItemId()==null||
                saveOrderRequest.getItemQuantities().get(0).getPrice()==null||
                saveOrderRequest.getItemQuantities().get(0).getQuantity()==null||
        saveOrderRequest.getRestaurantId()==null||saveOrderRequest.getPaymentId()==null||saveOrderRequest.getBill()==null){
            throw new SaveOrderException("SOR-001","No field should be empty except Coupon_Id and discount");
        }

        final OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUuid(UUID.randomUUID().toString());
        if(saveOrderRequest.getCouponId()!=null) {
            orderEntity.setCoupon(orderService.getCouponByCouponId(saveOrderRequest.getCouponId().toString()));
        }

        // Allowing null value for discount
        // Defaulting to 0.00 if no value entered
        // Included this logic considering that not all orders will have a discount
        if(saveOrderRequest.getDiscount()!=null) {
            orderEntity.setDiscount(saveOrderRequest.getDiscount().doubleValue());
        } else {
            orderEntity.setDiscount(0.00);
        }

        orderEntity.setCustomer(customerEntity);
        CustomerEntity loggedInCustomer = customerEntity;
        //System.out.println(addressService.getAddressByAddressUuid(saveOrderRequest.getAddressId()).getUuid());
        AddressEntity tempAddressEntity = addressService.getAddressByUUID(saveOrderRequest.getAddressId(),loggedInCustomer);
        //System.out.println(addressService.getCustomerAddressByAddressId(tempAddressEntity).getId());
        if(addressService.getCustomerAddressByAddressId(tempAddressEntity)==null){
            throw new AuthorizationFailedException("ATHR-004","You are not authorized to view/update/delete any one else's address");
        }
        orderEntity.setAddress(addressService.getAddressByUUID(saveOrderRequest.getAddressId(),loggedInCustomer));
        orderEntity.setPayment(paymentService.getPaymentMethod(saveOrderRequest.getPaymentId().toString()));
        orderEntity.setRestaurant(restaurantService.restaurantByUUID(saveOrderRequest.getRestaurantId().toString()));
        orderEntity.setDate(new Date());
        orderEntity.setBill(saveOrderRequest.getBill().doubleValue());
        OrderEntity savedOrderEntity = orderService.saveOrder(orderEntity);

        for (ItemQuantity itemQuantity : saveOrderRequest.getItemQuantities()) {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrder(savedOrderEntity);
            orderItemEntity.setItem(itemService.getItemByUUID(itemQuantity.getItemId().toString()));
            orderItemEntity.setQuantity(itemQuantity.getQuantity());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderItemEntity.setPrice(itemQuantity.getPrice());
            orderService.saveOrderItem(orderItemEntity);
        }

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse()
                .id(savedOrderEntity.getUuid()).status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }
}
