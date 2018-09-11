package server; /**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */

import db.HikariCPManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import lombok.extern.log4j.Log4j2;
import utils.CodeEnum;
import utils.CommonUtil;
import utils.NetworkUtil;
import web.router.DishRouter;
import web.router.MerchantRouter;
import web.router.UserRouter;

/**
 * 客户端程序
 * @author tangyue
 * @version $Id: server.ClientServer.java, v 0.1 2018-03-05 13:51 tangyue Exp $$
 */
@Log4j2
public class ClientServer extends AbstractVerticle {

    private HttpServer server;

    @Override
    public void start() throws Exception {

        initComponents();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create())
                .handler(CorsHandler.create("*")
                        .allowedHeaders(CommonUtil.getAllowedHeaders())
                        .allowedMethods(CommonUtil.getAllowedMethods()));

        router.mountSubRouter("/dish", DishRouter.create());
        router.mountSubRouter("/user", UserRouter.create());
        router.mountSubRouter("/merchant", MerchantRouter.create());

        router.route().failureHandler(f -> {
            f.response().setStatusCode(CodeEnum.SYS_ERROR.getCode()).end(CodeEnum.SYS_ERROR.getMessage());
        });
        Integer port = config().getInteger("serverPort", 8083);
        this.server = vertx.createHttpServer();
        this.server.requestHandler(router :: accept).listen(port);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        HikariCPManager.close();
        this.server.close(res -> log.info("HTTP服务器关闭" + (res.succeeded()?"成功":"失败")));
    }

    private void initComponents(){
        CommonUtil.init(context);
        NetworkUtil.init();
        HikariCPManager.init();
    }



}
