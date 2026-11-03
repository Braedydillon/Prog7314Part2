package com.unify.api.rewards

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
@EnableConfigurationProperties(RewardsProperties::class)
class RewardsConfig {

    @Bean
    fun rewardsClock(): Clock = Clock.system(ZoneId.of("Africa/Johannesburg"))
}
