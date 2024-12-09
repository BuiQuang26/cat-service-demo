package com.example.cat_service_demo.configs;

import com.example.cat_service_demo.domain.Cat;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Value("${redis.address}")
    private String redisServerAddress;

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisServerAddress)
                .setPassword(null)
                .setConnectionMinimumIdleSize(10)
                .setConnectionPoolSize(64)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500)
                .setNameMapper(new PrefixNameMapper("cat_service_demo:"));
        config.setThreads(16);
        config.setNettyThreads(32);
        config.setCodec(new JsonJacksonCodec());
        config.setTransportMode(org.redisson.config.TransportMode.NIO);
        return Redisson.create(config);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return properties -> {
            properties.put("hibernate.cache.use_second_level_cache", true);
            properties.put("hibernate.cache.use_query_cache", true);
            properties.put("hibernate.cache.redisson.config", "./redisson-config.yml");
            properties.put("hibernate.cache.redisson.entity.expiration.time_to_live", String.valueOf(10000));
            properties.put("hibernate.cache.redisson.entity.expiration.max_idle_time", String.valueOf(5000));
        };
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        // create "testMap" cache with ttl = 24 minutes and maxIdleTime = 12 minutes
        config.put(CacheNameConstants.CAT_CACHE, new CacheConfig(10*60*1000, 5*60*1000));
        System.out.println(Cat.class.getName());
        config.put(Cat.class.getName(), new CacheConfig(60*1000, 60*1000));
        return new RedissonSpringCacheManager(redissonClient, config);
    }

}
