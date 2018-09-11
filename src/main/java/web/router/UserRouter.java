/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package web.router;

import java.util.Date;
import java.util.Objects;

import db.entity.Order;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import service.UserService;
import utils.CodeEnum;
import utils.CommonUtil;
import utils.DateUtil;
import utils.SnowflakeIdWorker;
import utils.StrUtil;
import web.ApiRouter;

/**
 * 用户相关请求接口
 * @author tangyue
 * @version $Id: UserRouter.java, v 0.1 2018-05-16 14:32 tangyue Exp $$
 */
public class UserRouter extends ApiRouter {

    private Vertx vertx = CommonUtil.vertx();

    private Router router;

    private UserService userService;

    public static Router create(){
        return new UserRouter().router;
    }

    private UserRouter(){
        this.router = Router.router(this.vertx);
        this.router.get("/info/:userId").handler(this::getUserInfo);
        this.router.get("/order/:userId").handler(this::getOrder);
        this.router.post("/order/add").handler(this::submitOrder);

        this.userService = new UserService();
    }

    /**
     * 获取用户信息
     * @param context
     */
    private void getUserInfo(RoutingContext context){
        String userId = context.request().getParam("userId");
        if (StrUtil.isBlank(userId)){
            serviceUnavailable(context, CodeEnum.SYS_REQUEST);
        } else {

            this.userService.queryUserInfo(Long.parseLong(userId), resultHandlerNonEmpty(context));
        }
    }

    /**
     * 获取用户订单列表
     * @param context
     */
    private void getOrder(RoutingContext context){
        String userId = context.request().getParam("userId");
        String pageNum = context.request().getParam("pageNum");
        String pageSize = context.request().getParam("pageSize");
        if (StrUtil.isBlank(userId)){
            serviceUnavailable(context, CodeEnum.SYS_REQUEST);
        } else {

            Integer page = StrUtil.isNumber(pageNum) ? Integer.parseInt(pageNum) : 0;
            Integer size = StrUtil.isNumber(pageSize) ? Integer.parseInt(pageSize) : 10;
            this.userService.queryUserOrderPage(Long.parseLong(userId), page, size, resultHandlerNonEmpty(context));
        }
    }

    /**
     * 提交订单
     * @param context
     */
    private void submitOrder(RoutingContext context){

        JsonObject jsonObject = context.getBodyAsJson();
        Order order = jsonObject.mapTo(Order.class);
        if (Objects.isNull(order.getUserId()) || Objects.isNull(order.getDetails())
                || Objects.isNull(order.getPay()) || Objects.isNull(order.getPayMethod())){
            serviceUnavailable(context, CodeEnum.SYS_REQUEST);
        } else {
            Date now = new Date();
            order.setPayStatus(2);
            order.setCreateTime(now);
            order.setUpdateTime(now);
            order.setOrderNum(DateUtil.formatDate("yyyyMMdd") + SnowflakeIdWorker.getInstance().nextId());
            this.userService.addOrder(order, resultVoidHandler(context));
        }
    }
}
