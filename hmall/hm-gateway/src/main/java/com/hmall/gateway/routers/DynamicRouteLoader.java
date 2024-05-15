package com.hmall.gateway.routers;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class DynamicRouteLoader {

    //用于监听路由的api
    @Resource
    private NacosConfigManager nacosConfigManager;
    //要监听得配置文件
    private static final String dataId = "gateway-routes.json";
    //要监听得配置文件得分组
    private static final String group = "DEFAULT_GROUP";
    //用于记录旧路由id的集合
    private final Set<String> routeIds = new HashSet<>();
    //更新路由表的工具api
    @Resource
    private RouteDefinitionWriter writer;

    @PostConstruct
    public void initRoute() throws NacosException {
        //1. 项目启动时先拉取一次配置， 并且添加配置监听器
        String configInfo = nacosConfigManager.getConfigService()
                .getConfigAndSignListener(dataId, group, 5000, new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }

                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //2. 添加监听器,监听到配置变更, 需要去更新路由表
                        updateConfigInfo(configInfo);
                    }
                });
        //3. 第一次读取到配置也要读取到路由表
        updateConfigInfo(configInfo);
    }

    public void updateConfigInfo(String configInfo){
        log.debug("监听到路由配置信息: {}", configInfo);
        //1. 解析配置信息, 转为RouteDefinition
        List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
        //2. 删除旧得路由表
        for (String routeId : routeIds){
            writer.delete(Mono.just(routeId)).subscribe();
        }
        routeIds.clear();
        //3. 更新路由表
        for (RouteDefinition routeDefinition: routeDefinitions){
            //1. 更新路由表
            writer.save(Mono.just(routeDefinition)).subscribe();
            //2. 记录路由表id, 方便下一次更新前删除
            routeIds.add(routeDefinition.getId());
        }
    }
}
