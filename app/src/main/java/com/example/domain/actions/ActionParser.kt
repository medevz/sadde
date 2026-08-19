package com.example.domain.actions

import com.example.domain.model.ActionExecutionStatus
import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction
import com.example.domain.model.AssistantResponse
import org.json.JSONObject

class ActionParser {

    fun parseResponse(rawJsonOrText: String): AssistantResponse {
        val trimmed = rawJsonOrText.trim()
        val jsonStart = trimmed.indexOf('{')
        val jsonEnd = trimmed.lastIndexOf('}')

        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            try {
                val jsonString = trimmed.substring(jsonStart, jsonEnd + 1)
                val jsonObject = JSONObject(jsonString)
                val reply = jsonObject.optString("reply", "Action complete.")
                val actionsList = mutableListOf<AssistantAction>()

                val actionsArray = jsonObject.optJSONArray("actions")
                if (actionsArray != null) {
                    for (i in 0 until actionsArray.length()) {
                        val actionObj = actionsArray.getJSONObject(i)
                        val typeStr = actionObj.optString("type", "").uppercase()
                        val type = parseActionType(typeStr)

                        if (type != null) {
                            val target = actionObj.optString("target", actionObj.optString("package", null))
                            val value = actionObj.optString("value", actionObj.optString("text", null))
                            val description = actionObj.optString("description", null)

                            actionsList.add(
                                AssistantAction(
                                    type = type,
                                    target = if (target.isNullOrBlank() || target == "null") null else target,
                                    value = if (value.isNullOrBlank() || value == "null") null else value,
                                    description = description,
                                    status = ActionExecutionStatus.PENDING
                                )
                            )
                        }
                    }
                }

                val followUpsList = mutableListOf<String>()
                val followUpsArray = jsonObject.optJSONArray("suggestedFollowUps")
                if (followUpsArray != null) {
                    for (i in 0 until followUpsArray.length()) {
                        followUpsList.add(followUpsArray.getString(i))
                    }
                }

                return AssistantResponse(
                    reply = reply,
                    actions = actionsList,
                    suggestedFollowUps = followUpsList
                )
            } catch (e: Exception) {
                // Fallback to plain text parsing
            }
        }

        return AssistantResponse(
            reply = trimmed,
            actions = emptyList()
        )
    }

    private fun parseActionType(typeStr: String): ActionType? {
        return try {
            ActionType.valueOf(typeStr)
        } catch (e: Exception) {
            when (typeStr) {
                "OPEN_APPLICATION" -> ActionType.OPEN_APP
                "LAUNCH_APP" -> ActionType.OPEN_APP
                "SETTINGS" -> ActionType.OPEN_SETTINGS
                "INC_VOLUME", "INCREASE_VOLUME" -> ActionType.VOLUME_UP
                "DEC_VOLUME", "DECREASE_VOLUME" -> ActionType.VOLUME_DOWN
                "MUTE" -> ActionType.SET_VOLUME
                "PAUSE" -> ActionType.PAUSE_MEDIA
                "PLAY" -> ActionType.PLAY_MEDIA
                "NEXT" -> ActionType.NEXT_MEDIA
                "PREVIOUS" -> ActionType.PREVIOUS_MEDIA
                "BACK" -> ActionType.ACCESSIBILITY_BACK
                "HOME" -> ActionType.ACCESSIBILITY_HOME
                "RECENTS" -> ActionType.ACCESSIBILITY_RECENTS
                "CLICK" -> ActionType.ACCESSIBILITY_CLICK
                "SCROLL" -> ActionType.ACCESSIBILITY_SCROLL
                "TYPE_TEXT" -> ActionType.ACCESSIBILITY_TYPE_TEXT
                else -> null
            }
        }
    }
}
