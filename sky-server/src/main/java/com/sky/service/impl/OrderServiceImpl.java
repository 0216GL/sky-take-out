package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.context.UserContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersCancelDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.MessageMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderVO;
import com.sky.vo.OrderStatisticsVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 用户下单
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        AddressBook addressBook = addressBookMapper.selectById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            throw new RuntimeException("地址信息不存在");
        }

        Long userId = UserContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wapper = new LambdaQueryWrapper<>();
        wapper.eq(ShoppingCart::getUserId, userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.selectList(wapper);
        if(shoppingCartList == null){
            throw new RuntimeException("购物车为空");
        }

        Orders orders  = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(userId);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());

        // 先插入订单，MyBatis-Plus 插入后会将主键回填到 orders.getId()
        orderMapper.insert(orders);
        // 订单号使用雪花ID（唯一），与历史数据补齐规则一致
        orders.setNumber(String.valueOf(orders.getId()));
        orderMapper.updateById(orders);

        for(ShoppingCart shoppingCart : shoppingCartList){
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailMapper.insert(orderDetail);
        }

        shoppingCartMapper.delete(wapper);
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 模拟支付：不调用微信支付，直接将订单置为已支付
     */
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        Long userId = UserContext.getCurrentId();

        // 根据订单号 + 当前用户查询订单
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getNumber, ordersPaymentDTO.getOrderNumber())
                    .eq(Orders::getUserId, userId);
        Orders orders = orderMapper.selectOne(queryWrapper);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 幂等处理：已支付的订单直接返回成功，避免重复支付报错
        if (Orders.PAID.equals(orders.getPayStatus())) {
            return buildMockPaymentVO();
        }

        // 模拟支付成功：更新订单状态为待接单、支付状态为已支付、记录结账时间
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(Orders.TO_BE_CONFIRMED);
        update.setPayStatus(Orders.PAID);
        update.setCheckoutTime(LocalDateTime.now());
        orderMapper.updateById(update);

        Map map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + orders.getNumber());

        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);

        // 同时往 message 表插入一条"待接单"消息，供消息中心展示
        com.sky.entity.Message message = com.sky.entity.Message.builder()
                .content("您有新的待接单订单，订单号：" + orders.getNumber() + "，请及时处理")
                .details("{\"orderId\":" + orders.getId() + "}")
                .type(1)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        messageMapper.insert(message);

        return buildMockPaymentVO();
    }

    /**
     * 用户查看历史订单
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, String status) {
        Long userId = UserContext.getCurrentId();
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        if (status != null && !status.isEmpty() && !"0".equals(status)) {
            queryWrapper.eq(Orders::getStatus, status);
        }
        queryWrapper.orderByDesc(Orders::getOrderTime);

        Page<Orders> pageResult = orderMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
        List<Orders> ordersList = pageResult.getRecords();

        // 批量查询订单明细，按订单ID分组填充，避免N+1查询
        if (ordersList != null && !ordersList.isEmpty()) {
            List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(OrderDetail::getOrderId, orderIds);
            Map<Long, List<OrderDetail>> detailMap = orderDetailMapper.selectList(detailWrapper)
                    .stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
            ordersList.forEach(order -> order.setOrderDetailList(detailMap.get(order.getId())));
        }

        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
    }

    /**
     * 用户查询订单详情（含订单明细）
     * 先校验订单属于当前用户，避免越权查看他人订单
     */
    @Override
    public OrderVO userOrderDetail(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        // 越权校验：只能查看自己的订单
        if (!orders.getUserId().equals(UserContext.getCurrentId())) {
            throw new OrderBusinessException("无权查看该订单");
        }

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(detailWrapper);
        orderVO.setOrderDetailList(orderDetailList);

        // 拼接菜品摘要，如：鱼香肉丝x2,宫保鸡丁x1
        String orderDishes = orderDetailList.stream()
                .map(d -> d.getName() + "x" + d.getNumber())
                .collect(Collectors.joining(","));
        orderVO.setOrderDishes(orderDishes);

        return orderVO;
    }

    /**
     * 用户再来一单：把历史订单的菜品重新加入购物车
     * 思路：查出原订单的明细，转成购物车记录插入（数量、口味、价格都照搬）
     */
    @Override
    public void repetition(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        // 越权校验：只能操作自己的订单
        if (!orders.getUserId().equals(UserContext.getCurrentId())) {
            throw new OrderBusinessException("无权操作该订单");
        }

        // 查出原订单的菜品明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(detailWrapper);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new OrderBusinessException("订单明细为空，无法再来一单");
        }

        Long userId = UserContext.getCurrentId();
        // 把每个明细转成购物车记录重新加入
        for (OrderDetail detail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(detail, shoppingCart);
            shoppingCart.setId(null);          // 生成新的主键
            shoppingCart.setUserId(userId);    // 归属当前用户
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 用户催单：通过 WebSocket 向管理端推送一条催单消息
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }
        // 越权校验：只能催自己的订单
        if (!orders.getUserId().equals(UserContext.getCurrentId())) {
            throw new OrderBusinessException("无权操作该订单");
        }

        // 组装催单消息并推送给管理端页面
        Map map = new HashMap<>();
        map.put("type", 4);                                  // 4 = 催单类型
        map.put("orderId", orders.getId());
        map.put("content", "订单号：" + orders.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 订单分页查询
     */
    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        Page<Orders> pageParam = new Page<>(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ordersPageQueryDTO.getStatus() != null, Orders::getStatus, ordersPageQueryDTO.getStatus())
                    .like(ordersPageQueryDTO.getNumber() != null && !ordersPageQueryDTO.getNumber().isEmpty(),
                            Orders::getNumber, ordersPageQueryDTO.getNumber())
                    .like(ordersPageQueryDTO.getPhone() != null && !ordersPageQueryDTO.getPhone().isEmpty(),
                            Orders::getPhone, ordersPageQueryDTO.getPhone())
                    .ge(ordersPageQueryDTO.getBeginTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getBeginTime())
                    .le(ordersPageQueryDTO.getEndTime() != null, Orders::getOrderTime, ordersPageQueryDTO.getEndTime())
                    .orderByDesc(Orders::getOrderTime);

        orderMapper.selectPage(pageParam, queryWrapper);

        List<Orders> ordersList = pageParam.getRecords();
        // 批量查询订单明细，按订单ID分组填充，避免N+1查询
        if (ordersList != null && !ordersList.isEmpty()) {
            List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.in(OrderDetail::getOrderId, orderIds);
            Map<Long, List<OrderDetail>> detailMap = orderDetailMapper.selectList(detailWrapper)
                    .stream()
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));
            ordersList.forEach(order -> order.setOrderDetailList(detailMap.get(order.getId())));
        }

        return new PageResult(pageParam.getTotal(), pageParam.getRecords());
    }

    /**
     * 查询订单详情（含订单明细）
     */
    @Override
    public OrderVO getByIdWithDetail(Long id) {
        Orders orders = orderMapper.selectById(id);
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(detailWrapper);
        orderVO.setOrderDetailList(orderDetailList);

        // 拼接菜品摘要，如：鱼香肉丝x2,宫保鸡丁x1
        String orderDishes = orderDetailList.stream()
                .map(d -> d.getName() + "x" + d.getNumber())
                .collect(Collectors.joining(","));
        orderVO.setOrderDishes(orderDishes);

        return orderVO;
    }

    /**
     * 接单：将待接单订单置为已接单
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.updateById(orders);
    }

    /**
     * 拒单：仅待接单状态的订单可拒单，已支付则模拟退款
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders ordersDB = orderMapper.selectById(ordersRejectionDTO.getId());
        if (ordersDB == null || !Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException("订单不存在或状态异常，无法拒单");
        }

        // 已支付订单模拟退款：支付状态置为退款
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            ordersDB.setPayStatus(Orders.REFUND);
        }

        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(ordersDB);
    }

    /**
     * 取消订单：仅待付款/待接单/已接单状态可取消，已支付则模拟退款
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.selectById(ordersCancelDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException("订单不存在");
        }

        Integer status = ordersDB.getStatus();
        if (!Orders.PENDING_PAYMENT.equals(status)
                && !Orders.TO_BE_CONFIRMED.equals(status)
                && !Orders.CONFIRMED.equals(status)) {
            throw new OrderBusinessException("订单状态异常，无法取消");
        }

        // 已支付订单模拟退款：支付状态置为退款
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            ordersDB.setPayStatus(Orders.REFUND);
        }

        ordersDB.setStatus(Orders.CANCELLED);
        ordersDB.setCancelReason(ordersCancelDTO.getCancelReason());
        ordersDB.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(ordersDB);
    }

    /**
     * 订单状态统计：待接单、待派送、派送中数量
     */
    @Override
    public OrderStatisticsVO statistics() {
        Long toBeConfirmed = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.TO_BE_CONFIRMED));
        Long confirmed = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.CONFIRMED));
        Long deliveryInProgress = orderMapper.selectCount(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS));

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(Math.toIntExact(toBeConfirmed));
        orderStatisticsVO.setConfirmed(Math.toIntExact(confirmed));
        orderStatisticsVO.setDeliveryInProgress(Math.toIntExact(deliveryInProgress));
        return orderStatisticsVO;
    }

    /**
     * 派送订单：将已接单订单置为派送中
     */
    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.updateById(orders);
    }

    /**
     * 完成订单：将派送中订单置为已完成，并记录送达时间
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.updateById(orders);
    }

    /**
     * 构造模拟的微信支付返回参数
     */
    private OrderPaymentVO buildMockPaymentVO() {

        OrderPaymentVO orderPaymentVO = OrderPaymentVO.builder()
                .nonceStr("MOCK_NONCE")
                .paySign("MOCK_SIGN")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .signType("RSA")
                .packageStr("prepay_id=MOCK_PREPAY_ID")
                .build();

        return orderPaymentVO;
    }
}
