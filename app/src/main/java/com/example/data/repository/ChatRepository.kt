package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.model.ActionExecutionStatus
import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class ChatRepository(private val chatDao: ChatDao) {

    val messagesFlow: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { entity ->
            ChatMessage(
                id = entity.id,
                role = try { MessageRole.valueOf(entity.role) } catch (e: Exception) { MessageRole.USER },
                text = entity.text,
                timestamp = entity.timestamp,
                actions = parseActionsJson(entity.actionsJson),
                isError = entity.isError,
                isOffline = entity.isOffline
            )
        }
    }

    suspend fun insertMessage(message: ChatMessage): Long {
        val entity = ChatMessageEntity(
            id = message.id,
            role = message.role.name,
            text = message.text,
            timestamp = message.timestamp,
            actionsJson = serializeActionsJson(message.actions),
            isError = message.isError,
            isOffline = message.isOffline
        )
        return chatDao.insertMessage(entity)
    }

    suspend fun getRecentMessages(limit: Int = 10): List<ChatMessage> {
        return chatDao.getRecentMessages(limit).map { entity ->
            ChatMessage(
                id = entity.id,
                role = try { MessageRole.valueOf(entity.role) } catch (e: Exception) { MessageRole.USER },
                text = entity.text,
                timestamp = entity.timestamp,
                actions = parseActionsJson(entity.actionsJson),
                isError = entity.isError,
                isOffline = entity.isOffline
            )
        }.reversed()
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessageById(id)
    }

    suspend fun clearHistory() {
        chatDao.clearAll()
    }

    private fun serializeActionsJson(actions: List<AssistantAction>): String? {
        if (actions.isEmpty()) return null
        val jsonArray = JSONArray()
        for (action in actions) {
            val obj = JSONObject().apply {
                put("type", action.type.name)
                action.target?.let { put("target", it) }
                action.value?.let { put("value", it) }
                action.description?.let { put("description", it) }
                put("status", action.status.name)
                action.executionMessage?.let { put("executionMessage", it) }
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    private fun parseActionsJson(jsonStr: String?): List<AssistantAction> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val list = mutableListOf<AssistantAction>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val typeName = obj.optString("type", ActionType.SPEAK.name)
                val type = try { ActionType.valueOf(typeName) } catch (e: Exception) { ActionType.SPEAK }
                val target = if (obj.has("target")) obj.getString("target") else null
                val value = if (obj.has("value")) obj.getString("value") else null
                val description = if (obj.has("description")) obj.getString("description") else null
                val statusName = obj.optString("status", ActionExecutionStatus.PENDING.name)
                val status = try { ActionExecutionStatus.valueOf(statusName) } catch (e: Exception) { ActionExecutionStatus.PENDING }
                val executionMessage = if (obj.has("executionMessage")) obj.getString("executionMessage") else null

                list.add(
                    AssistantAction(
                        type = type,
                        target = target,
                        value = value,
                        description = description,
                        status = status,
                        executionMessage = executionMessage
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors on malformed legacy JSON
        }
        return list
    }
}
