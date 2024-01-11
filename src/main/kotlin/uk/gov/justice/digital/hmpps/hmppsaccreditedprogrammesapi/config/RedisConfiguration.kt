package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class RedisConfiguration {
  @Bean
  fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<*, *>? {
    val template: RedisTemplate<*, *> = RedisTemplate<Any, Any>()
    template.connectionFactory = connectionFactory
    template.keySerializer = StringRedisSerializer()
    return template
  }
}
