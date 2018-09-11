/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package web.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import service.MerchantService;
import utils.CodeEnum;
import utils.CommonUtil;
import utils.StrUtil;
import web.ApiRouter;

/**
 *
 * @author tangyue
 * @version $Id: MerchantRouter.java, v 0.1 2018-05-22 17:07 tangyue Exp $$
 */
public class MerchantRouter extends ApiRouter {

    private Vertx vertx = CommonUtil.vertx();

    private Router router;

    private MerchantService merchantService;

    public static Router create(){
        return new MerchantRouter().router;
    }

    private MerchantRouter(){
        this.router = Router.router(this.vertx);
        this.router.get("/:merchantId/info").handler(this::merchantInfo);

        this.merchantService = new MerchantService();
    }

    /**
     *
     * @param context
     */
    public void merchantInfo(RoutingContext context){

        String merchantId = context.request().getParam("merchantId");
        if (StrUtil.isBlank(merchantId)){
            serviceUnavailable(context, CodeEnum.SYS_REQUEST);
        } else {
            this.merchantService.queryMerchantInfo(Long.parseLong(merchantId), resultHandlerNonEmpty(context));
        }
    }
}
