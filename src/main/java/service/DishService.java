/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package service;

import java.util.List;
import java.util.stream.Collectors;

import db.HikariCPManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author tangyue
 * @version $Id: DishService.java, v 0.1 2018-05-15 13:35 tangyue Exp $$
 */
public class DishService {

    private static HikariCPManager hikariCPM = HikariCPManager.getInstance();

    private static final String QUERY_ALL_TYPE_SQL = "SELECT id as typeId, type_name as typeName FROM dish_type";

    private static final String QUERY_ALL_FOOD_SQL = "SELECT id as foodId, dish_name name, dish_price price, dish_discount_price oldPrice, dish_icon icon, remark\n" +
            "FROM dish_food\n" +
            "WHERE deleted = 1";

    private static final String QUERY_FOOD_INFO_SQL = "SELECT id as foodId, dish_name name, dish_price price, dish_discount_price oldPrice, dish_icon icon, remark\n" +
            "FROM dish_food\n" +
            "WHERE deleted = 1 and id = ?";

    /**
     * 返回菜品分类
     * @param resultHandler
     */
    public void queryAllType(Handler<AsyncResult<List<JsonObject>>> resultHandler){
        hikariCPM.queryAll(QUERY_ALL_TYPE_SQL)
            .map(list -> list.stream()
                    .collect(Collectors.toList()))
        .setHandler(resultHandler);
    }

    /**
     * 返回菜品
     * @param resultHandler
     */
    public void queryAllFood(Handler<AsyncResult<List<JsonObject>>> resultHandler){

        hikariCPM.queryAll(QUERY_ALL_FOOD_SQL)
                .map(list -> list.stream()
                        .collect(Collectors.toList()))
                .setHandler(resultHandler);
    }

    /**
     * 返回菜品详细信息
     * @param resultHandler
     */
    public void queryFoodInfo(Long foodId, Handler<AsyncResult<JsonObject>> resultHandler){

        hikariCPM.queryOne(foodId, QUERY_FOOD_INFO_SQL)
                .map(option -> option.orElse(null))
                .setHandler(resultHandler);
    }
}
