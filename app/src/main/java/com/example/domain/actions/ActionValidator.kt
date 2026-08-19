package com.example.domain.actions

import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction

class ActionValidator {

    sealed interface ValidationResult {
        object Valid : ValidationResult
        data class Invalid(val reason: String) : ValidationResult
    }

    fun validate(action: AssistantAction): ValidationResult {
        return when (action.type) {
            ActionType.OPEN_APP -> {
                if (action.target.isNullOrBlank()) {
                    ValidationResult.Invalid("Package name or app name is required to open an app.")
                } else {
                    ValidationResult.Valid
                }
            }
            ActionType.SET_VOLUME -> {
                val vol = action.value?.toIntOrNull()
                if (vol == null || vol !in 0..100) {
                    ValidationResult.Invalid("Volume level must be a percentage between 0 and 100.")
                } else {
                    ValidationResult.Valid
                }
            }
            ActionType.ACCESSIBILITY_TYPE_TEXT -> {
                if (action.value.isNullOrBlank()) {
                    ValidationResult.Invalid("Text to type cannot be empty.")
                } else {
                    ValidationResult.Valid
                }
            }
            ActionType.ACCESSIBILITY_CLICK -> {
                if (action.target.isNullOrBlank()) {
                    ValidationResult.Invalid("Target UI text or description is required for click.")
                } else {
                    ValidationResult.Valid
                }
            }
            ActionType.SEARCH_WEB -> {
                if (action.target.isNullOrBlank()) {
                    ValidationResult.Invalid("Search query cannot be empty.")
                } else {
                    ValidationResult.Valid
                }
            }
            else -> ValidationResult.Valid
        }
    }
}
