package com.sample.crawler.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private Integer redisPort;
    
	@Bean
	public JedisConnectionFactory connectionFactory() {
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
	    return new JedisConnectionFactory(redisStandaloneConfiguration);
	}

	@Bean
	RedisTemplate<Object, Object> redisTemplate() {
		final RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory());
		template.setKeySerializer(new StringRedisSerializer());
		return template;
	}
}
