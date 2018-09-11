/**
 * chenxitech.cn Inc. Copyright (c) 2017-2018 All Rights Reserved.
 */

import com.google.common.collect.Lists;

import java.util.List;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.log4j.Log4j2;
import server.ClientServer;

/**
 *
 * @author tangyue
 * @version $Id: Main.java, v 0.1 2018-03-05 13:44 tangyue Exp $$
 */
@Log4j2
public class Main {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();
        deploy(vertx, ServiceLauncher.class, new DeploymentOptions());
    }


    public static class ServiceLauncher extends AbstractVerticle {

        @Override
        public void start(Future<Void> startFuture) throws Exception {

            DeploymentOptions clientOpts = new DeploymentOptions().setWorkerPoolSize(4);
            List<Future> futures = Lists.newArrayList();
            futures.add(deploy(vertx, ClientServer.class,clientOpts));
            CompositeFuture.all(
                    futures
            ).setHandler(r -> {
                if (r.succeeded()){
                    startFuture.complete();
                } else {
                    log.error("start future fail: {}", r.cause());
                    startFuture.fail(r.cause());
                }
            });
        }


    }

    private static Future<Void> deploy(Vertx vertx, Class verticle, DeploymentOptions opts){
        Future<Void> done = Future.future();
        String deploymentName = "java-dish:" + verticle.getName();
        JsonObject config = new JsonObject();

        opts.setConfig(config);

        vertx.deployVerticle(deploymentName, opts, r -> {
            if(r.succeeded()){
                log.info("Successfully deployed verticle: {}", deploymentName);
                done.complete();
            }
            else {
                log.error("Failed to deploy verticle: {}", deploymentName);
                log.error("test: {}", r.cause());
                done.fail(r.cause());
            }
        });

        return done;
    }
}
