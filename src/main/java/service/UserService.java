/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import db.HikariCPManager;
import db.entity.Order;
import db.entity.OrderDetail;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import utils.Functional;
import utils.StrUtil;

/**
 *
 * @author tangyue
 * @version $Id: UserService.java, v 0.1 2018-05-22 16:01 tangyue Exp $$
 */
public class UserService {

    private static HikariCPManager hikariCPM = HikariCPManager.getInstance();

    private static final String QUERY_USER_INFO = "SELECT nick_name nickName, url\n" +
            "FROM dish_weixin_account\n" +
            "WHERE id = ?";


    private static final String QUERY_USER_ORDER = "SELECT order_num num, dish_price price, dish_pay pay, pay_status, create_time\n" +
            "FROM dish_order\n" +
            "WHERE user_id = ? order by id desc " +
            "LIMIT ?, ?";

    private static final String QUERY_USER_ORDER_DETAIL = "SELECT dish_name foodName, dish_quantity quantity, " +
            " dish_price price, dish_discount_price oldPrice, dish_icon icon\n" +
            "FROM dish_order_detail\n" +
            "WHERE order_num = ? ";


    private static final String INSERT_USER_ORDER = "INSERT INTO dish_order " +
            "(order_num, user_id, merchant_id, desk_num, buyer_name, buyer_phone, buyer_address, " +
            "dish_amount, dish_price, dish_pay, sale_status, pay_method, pay_status, create_time, update_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_USER_ORDER_DETAIL = "INSERT INTO dish_order_detail " +
            "(order_num, dish_id, dish_name, dish_quantity, dish_price, dish_discount_price, dish_icon, create_time, update_time) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";


    private static final String QUERY_DISH = "SELECT * FROM dish_food where dish_status = 1 and ";
    /**
     * 获取微信用户信息
     * @param userId
     * @param resultHandler
     */
    public void queryUserInfo(Long userId, Handler<AsyncResult<JsonObject>> resultHandler){
        hikariCPM.queryOne(userId, QUERY_USER_INFO)
                .map(option -> option.orElse(null))
                .setHandler(resultHandler);
    }

    /**
     * 分页查询用户历史订单
     * @param page
     * @param limit
     * @param resultHandler
     */
    public void queryUserOrderPage(Long userId, int page, int limit, Handler<AsyncResult<List<JsonObject>>> resultHandler){

        JsonArray params = new JsonArray().add(userId).add(hikariCPM.calcPage(page, limit)).add(limit);

        hikariCPM.queryMany(params, QUERY_USER_ORDER)
                .compose(orders -> {

                    List<Future<JsonObject>> futures = orders.stream().map(o -> {
                        Future<JsonObject> future = Future.future();
                        getOrderDetails(o, future);
                        return future;
                    }).collect(Collectors.toList());
                    return Functional.allOfFutures(futures);
                }).setHandler(resultHandler);
    }


    /**
     * 通过订单号查询订单详情
     * @param order
     * @return
     */
    private Future<JsonObject> getOrderDetails(JsonObject order, Handler<AsyncResult<JsonObject>> resultHandler){

        JsonArray jsonArray = new JsonArray().add(order.getString("num"));
        return hikariCPM.queryMany(jsonArray, QUERY_USER_ORDER_DETAIL)
                .map(detail -> order.put("items", detail))
                .setHandler(resultHandler);
    }


    /**
     * 提交订单
     * @param order
     * @param resultHandler
     */
    public void addOrder(Order order, Handler<AsyncResult<Void>> resultHandler){

        StringBuffer sb = new StringBuffer(QUERY_DISH).append("id in (");
        List<Long> foodIds = new ArrayList<>();
        order.getDetails().stream().forEach(detail -> {
            foodIds.add(detail.getDishId());
            order.setAmount(order.getAmount() + detail.getDishQuantity());
        });
        String condition = StrUtil.join(foodIds, ",");
        sb.append(condition).append(")");

        hikariCPM.queryOne(order.getUserId(), QUERY_USER_INFO)
                .map(option -> option.orElse(null))
                .compose(u -> {

                    order.setBuyerName(u.getString("nickName"));
                    order.setAddress("");
                    order.setPhone("");
                    return hikariCPM.queryAll(sb.toString())
                            .compose(s -> {

                                Future<Void> future = Future.future();
                                if (Objects.nonNull(s)) {
                                    Iterator<JsonObject> foods = s.iterator();
                                    List<OrderDetail> orderDetails = order.getDetails();
                                    while (foods.hasNext()){
                                        JsonObject food = foods.next();
                                        for (OrderDetail detail : orderDetails){
                                            if (detail.getDishId().equals(food.getLong("id"))){
                                                detail.setDishName(food.getString("dish_name"));
                                                detail.setDishPrice(new BigDecimal(food.getDouble("dish_price")));
                                                detail.setDishIcon(food.getString("dish_icon"));
                                                detail.setDishDiscountPrice(new BigDecimal(food.getDouble("dish_discount_price")));
                                                detail.setCreateTime(order.getCreateTime());
                                                detail.setUpdateTime(order.getUpdateTime());
                                                order.setPrice(order.getPrice().add(detail.getDishPrice()));
                                            }
                                        }
                                    }
                                    order.setDetails(orderDetails);
                                    createOrder(order, future.completer());
                                }
                                return future;
                            });
                }).setHandler(resultHandler);
    }

    /**
     * 创建订单相关
     * @param order
     * @param resultHandler
     */
    private void createOrder(Order order, Handler<AsyncResult<Void>> resultHandler){

        JsonArray params = new JsonArray().add(order.getOrderNum())
                .add(order.getUserId())
                .add(order.getMerchantId())
                .add(order.getDeskNum())
                .add(order.getBuyerName())
                .add(order.getPhone())
                .add(order.getAddress())
                .add(order.getAmount())
                .add(order.getPrice().doubleValue())
                .add(order.getPay().doubleValue())
                .add(order.getSaleStatus())
                .add(order.getPayMethod())
                .add(order.getPayStatus())
                .add(order.getCreateTime().toInstant())
                .add(order.getUpdateTime().toInstant());
        hikariCPM.executeNoResult(params, INSERT_USER_ORDER)
            .compose(s -> {

                Future<Void> future = Future.future();
                List<OrderDetail> details = order.getDetails();
                if (Objects.nonNull(details)) {
                    List<JsonArray> jsonArrays = new ArrayList<>();
                    for (OrderDetail detail : details){

                        JsonArray jsonArray = new JsonArray()
                                .add(order.getOrderNum())
                                .add(detail.getDishId())
                                .add(detail.getDishName())
                                .add(detail.getDishQuantity())
                                .add(detail.getDishPrice().doubleValue())
                                .add(detail.getDishDiscountPrice().doubleValue())
                                .add(detail.getDishIcon())
                                .add(detail.getCreateTime().toInstant())
                                .add(detail.getUpdateTime().toInstant());
                        jsonArrays.add(jsonArray);
                    }
                    future = hikariCPM.batchParams(jsonArrays, INSERT_USER_ORDER_DETAIL);
                }
                return future;
            }).setHandler(resultHandler);

    }
}
