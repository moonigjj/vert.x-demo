/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package service;

import db.HikariCPManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 *
 * @author tangyue
 * @version $Id: Merchant.java, v 0.1 2018-05-22 16:20 tangyue Exp $$
 */
public class MerchantService {

    private static HikariCPManager hikariCPM = HikariCPManager.getInstance();

    private static final String QUERY_MERCHANT_INFO = "SELECT mer_name name, province, city,address, mer_dec remark, mer_phone phone, mer_url url, mer_begin_time time\n" +
            "FROM merchants\n" +
            "WHERE id = ?";

    /**
     * 查询餐厅信息
     * @param merchantId
     * @param resultHandler
     */
    public void queryMerchantInfo(Long merchantId, Handler<AsyncResult<JsonObject>> resultHandler){
        hikariCPM.queryOne(merchantId, QUERY_MERCHANT_INFO)
                .map(option -> option.orElse(null))
                .setHandler(resultHandler);
    }
}
