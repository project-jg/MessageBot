package io.jigoo.message.demo

import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.spring.boot.annotation.EventMapping
import org.springframework.stereotype.Service

@Service
class MessageEventHandler(private val actionDispatcher: ActionDispatcher) {

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
        println("event: $event")

        return try {
            actionDispatcher.dispatch(event)
            TextMessage("안녕하세요")
        } catch (e: Exception) {
            TextMessage("잘못된 요청입니다")
        }
    }

    @EventMapping
    fun handleDefaultMessageEvent(event: Event) {
        println("event: $event")
    }
}