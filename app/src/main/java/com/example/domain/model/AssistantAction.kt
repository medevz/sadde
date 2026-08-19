package com.example.domain.model

enum class ActionType {
    OPEN_APP,
    LAUNCH_ACTIVITY,
    OPEN_SETTINGS,
    VOLUME_UP,
    VOLUME_DOWN,
    SET_VOLUME,
    PLAY_MEDIA,
    PAUSE_MEDIA,
    NEXT_MEDIA,
    PREVIOUS_MEDIA,
    SET_BRIGHTNESS,
    SHOW_NOTIFICATION,
    SPEAK,
    WAIT,
    REQUEST_PERMISSION,
    ACCESSIBILITY_CLICK,
    ACCESSIBILITY_SCROLL,
    ACCESSIBILITY_TYPE_TEXT,
    ACCESSIBILITY_BACK,
    ACCESSIBILITY_HOME,
    ACCESSIBILITY_RECENTS,
    SEARCH_WEB,
    MAKE_CALL,
    SEND_MESSAGE,
    TAKE_PHOTO
}

enum class ActionExecutionStatus {
    PENDING,
    SUCCESS,
    FAILED,
    RESTRICTED,
    SKIPPED
}

data class AssistantAction(
    val type: ActionType,
    val target: String? = null,
    val value: String? = null,
    val description: String? = null,
    var status: ActionExecutionStatus = ActionExecutionStatus.PENDING,
    var executionMessage: String? = null
)
