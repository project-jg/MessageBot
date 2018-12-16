package io.jigoo.message.demo

import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import org.springframework.boot.autoconfigure.SpringBootApplication
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.Message
import org.springframework.boot.SpringApplication
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

@SpringBootApplication
class Application