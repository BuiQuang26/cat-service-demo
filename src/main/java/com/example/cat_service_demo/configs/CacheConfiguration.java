package com.example.cat_service_demo.configs;

import com.example.cat_service_demo.domain.Cat;
import org.hibernate.cache.jcache.ConfigSettings;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.Kryo5Codec;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Value("${redis.address}")
    private String[] redisServerAddress;

    @Value("${redis.cluster}")
    private Boolean redisCluster;

    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        if (redisCluster) {
            config.useClusterServers()
                    .setMasterConnectionPoolSize(64)
                    .setMasterConnectionMinimumIdleSize(10)
                    .setIdleConnectionTimeout(10000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500)
                    .addNodeAddress(redisServerAddress)
                    .setNameMapper(new PrefixNameMapper("cat_service_demo:"));
        } else {
            config.useSingleServer()
                    .setAddress(redisServerAddress[0])
                    .setConnectionMinimumIdleSize(10)
                    .setConnectionPoolSize(64)
                    .setIdleConnectionTimeout(10000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000)
                    .setRetryAttempts(3)
                    .setRetryInterval(1500)
                    .setNameMapper(new PrefixNameMapper("cat_service_demo:"));
        }
        config.setThreads(16);
        config.setNettyThreads(32);
        config.setCodec(new Kryo5Codec());
        config.setTransportMode(org.redisson.config.TransportMode.NIO);
        return Redisson.create(config);
    }

    @Bean
    public javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration(RedissonClient redissonClient) {
        MutableConfiguration<Object, Object> jcacheConfig = new MutableConfiguration<>();
        jcacheConfig.setStatisticsEnabled(true);
        jcacheConfig.setExpiryPolicyFactory(
                CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 20))
        );
        return RedissonConfiguration.fromInstance(redissonClient, jcacheConfig);
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager cacheManager) {
        return properties -> properties.put(ConfigSettings.CACHE_MANAGER, cacheManager);
    }

    @Bean
    public JCacheManagerCustomizer cacheManagerCustomizer(javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration) {
        return cm -> {
            createCache(cm, CacheNameConstants.CAT_CACHE, jcacheConfiguration);
            createCache(cm, Cat.class.getName(), jcacheConfiguration);
            // jhipster-needle-redis-add-entry
        };
    }

    private void createCache(
            javax.cache.CacheManager cm,
            String cacheName,
            javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration
    ) {
        javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        } else {
            cm.createCache(cacheName, jcacheConfiguration);
        }
    }
}
