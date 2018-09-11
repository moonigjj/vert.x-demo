/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package web.router;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import service.DishService;
import utils.CodeEnum;
import utils.CommonUtil;
import utils.StrUtil;
import web.ApiRouter;

/**
 *
 * @author tangyue
 * @version $Id: DishRouter.java, v 0.1 2018-05-15 13:33 tangyue Exp $$
 */
public class DishRouter extends ApiRouter {

    private Vertx vertx = CommonUtil.vertx();

    private Router router;

    private DishService dishService;

    public static Router create(){
        return new DishRouter().router;
    }

    private DishRouter(){
        this.router = Router.router(this.vertx);
        this.router.get("/foods").handler(this::dishFoods);
        this.router.get("/:foodId/info").handler(this::dishInfo);
        this.router.get("/types").handler(this::dish);

        this.dishService = new DishService();
    }

    /**
     * 获取菜单列表
     * @param context
     */
    private void dishFoods(RoutingContext context){

        dishService.queryAllFood(resultHandler(context, Json::encodePrettily));
    }

    private void dishInfo(RoutingContext context){
        String foodId = context.request().getParam("foodId");
        if (StrUtil.isBlank(foodId)){
            serviceUnavailable(context, CodeEnum.SYS_REQUEST);
        } else {
            dishService.queryFoodInfo(Long.parseLong(foodId), resultHandlerNonEmpty(context));
        }
    }

    /**
     * 获取商品分类列表
     * @param context
     */
    private void dish(RoutingContext context){

        dishService.queryAllType(resultHandler(context, Json::encodePrettily));
    }
}

