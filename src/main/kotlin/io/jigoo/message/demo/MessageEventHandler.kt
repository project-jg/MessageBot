@file:Suppress("UnstableApiUsage")

package io.jigoo.message.demo

import com.linecorp.bot.client.LineMessagingClient
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.profile.UserProfileResponse
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Service
@LineMessageHandler
class MessageEventHandler(private val actionDispatcher: ActionDispatcher) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
        return try {
            TextMessage(actionDispatcher.dispatch(event))
        } catch (e: Exception) {
            TextMessage(e.message)
        }
    }

    @EventMapping
    fun handleDefaultMessageEvent(event: Event): TextMessage {
        println("event: $event")
        return TextMessage("텍스트 메시지만 지원합니다 (sad)")
    }
}

@Component
@ConfigurationProperties("line.bot")
class LineBotConfig {
    lateinit var channelToken: String
}

@Configuration
@EnableConfigurationProperties(LineBotConfig::class)
class LineMessagingClientConfig(val lineBotConfig: LineBotConfig) {

    fun lineMessagingClient(): LineMessagingClient {
        return LineMessagingClient
            .builder(lineBotConfig.channelToken)
            .build()
    }

}

@RestController
class ProfileHandler(private val client: LineMessagingClient) {

    @GetMapping("/profile")
    fun test(): UserProfileResponse? {
        return client.getProfile("Uf0e31cac8e8ab40053ece85d6fb9e03e").get()
    }
}
