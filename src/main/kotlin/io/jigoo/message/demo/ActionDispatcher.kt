package io.jigoo.message.demo

import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.*

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
    fun dispatch(event: MessageEvent<TextMessageContent>) = teamInitActionHandler.handle(event)
}

interface ActionHandler {
    fun handle(event: MessageEvent<TextMessageContent>): String
}

@Service
class TeamInitActionHandler(private val teamInitRepository: TeamInitRepository) : ActionHandler {

    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val teamName = TeamNames.valueOf(event.message.text.toUpperCase())
        val userId = event.source.userId

        //TODO 팀과 유저아이디를 스토리지에 저장한다
        teamInitRepository.apply {
            save(Team(id = findByTeamName(teamName)?.id, teamName = teamName, userId = userId))
        }

        //TODO 답장을 보내준다
        return "${teamName} 으로 등록되었습니다! ^^"
    }
}

@Service
class NumberActionHandler(private val teamInitRepository: TeamInitRepository) : ActionHandler {


    /*
    TODO 숫자를 받는다
    팀을 선택 안한사람이면 실패
    (숫자에 대한 유효성 체크도 ?)

    게임회차, 팀, 선택한 숫자를 저장
     */
    override fun handle(event: MessageEvent<TextMessageContent>): String {
        val team = teamInitRepository.findByUserId(event.source.userId)
        return team.teamName.toString()
        // team?.let {  }
    }
}

@Repository
interface TeamInitRepository : CrudRepository<Team, String> {
    fun findByUserId(userId: String): Team
    fun findByTeamName(teamName: TeamNames): Team?
}

@Entity
data class Team(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int? = null,
        @Column(unique = true) val teamName: TeamNames,
        val userId: String
)

@Entity
data class GameConfig(
        @Id val id: Int = 0,
        val currentTurn: Int,
        val answer: Int
)

@Entity
data class Game(
        @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Int?,
        val teamName: TeamNames,
        val turn: Int,
        val selected: Int
)

