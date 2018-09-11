/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */
package weixin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.log4j.Log4j2;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import utils.CommonUtil;
import web.ApiRouter;

/**
 *
 * @author tangyue
 * @version $Id: WechatController.java, v 0.1 2018-02-28 16:51 tangyue Exp $$
 */
@Log4j2
public class WechatOauthRouter extends ApiRouter {

    private WxMpService wxMpService;

    private Vertx vertx = CommonUtil.vertx();

    private Router router;

    public static Router create(){
        return new WechatOauthRouter().router;
    }

    private WechatOauthRouter(){
        this.wxMpService = new WxMpServiceImpl();
        this.router = Router.router(this.vertx);

        this.router.post();
    }

    /**
     * 微信公众号
     * 微信授权获取openid
     * @param context
     */
    private void authorize(RoutingContext context){

        try {
            this.wxMpService.oauth2buildAuthorizationUrl("",
                    WxConsts.OAuth2Scope.SNSAPI_USERINFO,
                    URLEncoder.encode("","utf-8")
                    );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
