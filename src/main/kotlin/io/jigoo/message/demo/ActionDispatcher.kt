package io.jigoo.message.demo

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

enum class Action {
    TEAM_INIT,
    NUMBER
}

enum class TeamNames {
    TECH_1,
    TECH_2,
    APP_DEV,
    SERVER_DEV,
    KIDS_DEV
}

@Service
class ActionDispatcher(private val teamInitActionHandler: TeamInitActionHandler) {

    fun dispatch(event: MessageEvent<TextMessageContent>) {
        teamInitActionHandler.handle(event)
    }
}

interface ActionHandler {
    fun handle(event: MessageEvent<TextMessageContent>)
}

@Service
class TeamInitActionHandler(private val teamInitRepository: TeamInitRepository) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>) {
        val teamName = TeamNames.valueOf(event.message.text.toUpperCase())
        val userId = event.source.userId

        //TODO 팀과 유저아이디를 스토리지에 저장한다
        teamInitRepository.save(Team(userId, teamName))

        //TODO 답장을 보내준다
    }
}

@Repository
interface TeamInitRepository : CrudRepository<Team, String> {
}

data class Team(val userId: String, val teamName: TeamNames)
