/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package weixin;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.util.SignUtils;

import java.util.Map;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.log4j.Log4j2;
import utils.CodeEnum;
import utils.CommonUtil;
import utils.XMLUtil;
import web.ApiRouter;
import web.model.ReturnModel;


/**
 *
 * @author tangyue
 * @version $Id: WechatPayRouter.java, v 0.1 2018-05-14 15:55 tangyue Exp $$
 */
@Log4j2
public class WechatPayRouter extends ApiRouter {

    private WxPayConfig payConfig;

    private WxPayService payService;

    private Vertx vertx = CommonUtil.vertx();

    private Router router;


    private WechatPayRouter(WxPayConfig payConfig, WxPayService payService){
        this.payConfig = new WxPayConfig();
        this.payService = payService;

        this.router = Router.router(this.vertx);
    }

    /**
     * 返回前台H5调用JS支付所需要的参数，公众号支付调用此接口
     * @param context
     */
    public void getJSSDKPayInfo(RoutingContext context){

        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();

        ReturnModel returnModel = new ReturnModel();
        WxPayUnifiedOrderRequest prepayInfo = WxPayUnifiedOrderRequest.newBuilder()
                .openid(request.getParam(WechatConstant.openid))
                .outTradeNo(request.getParam(WechatConstant.out_trade_no))
                .totalFee(Integer.parseInt(request.getParam(WechatConstant.total_fee)))
                .body(request.getParam(WechatConstant.body))
                .tradeType(request.getParam(WechatConstant.trade_type))
                .spbillCreateIp(request.getParam(WechatConstant.spbill_create_ip))
                .notifyUrl("") // 填写通知回调地址
                .build();

        try {
            Map<String, String> payInfo = this.payService.createOrder(prepayInfo);
            returnModel.setSuc(true);
            returnModel.setResult(payInfo);
        } catch (WxPayException e) {
            returnModel.setResult(false);
            returnModel.setReason(e.getErrCodeDes());
            log.error(e.getErrCodeDes());
        }
        response.setStatusCode(CodeEnum.SYS_SUC.getCode()).end(Json.encodePrettily(returnModel));
    }

    /**
     * 微信通知支付结果的回调地址，notify_url
     * @param context
     */
    public void getJSSDKCallbackData(RoutingContext context){

        HttpServerResponse response = context.response();
        try {
            synchronized (this) {
                Map<String, String> kvm = XMLUtil.parseXmlStringToMap(context.getBodyAsString());
                if (SignUtils.checkSign(kvm, null, this.payConfig.getMchKey())) {
                    if (kvm.get("result_code").equals("SUCCESS")) {
                        //TODO(user) 微信服务器通知此回调接口支付成功后，通知给业务系统做处理
                        log.info("out_trade_no: " + kvm.get("out_trade_no") + " pay SUCCESS!");
                        response.end("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[ok]]></return_msg></xml>");
                    } else {
                        log.error("out_trade_no: "
                                + kvm.get("out_trade_no") + " result_code is FAIL");
                        response.end(
                                "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[result_code is FAIL]]></return_msg></xml>");
                    }
                } else {
                    response.end(
                            "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[check signature FAIL]]></return_msg></xml>");
                    log.error("out_trade_no: " + kvm.get("out_trade_no")
                            + " check signature FAIL");
                }
            }
        } catch (Exception e){
            log.error(e);
        }
    }
}
