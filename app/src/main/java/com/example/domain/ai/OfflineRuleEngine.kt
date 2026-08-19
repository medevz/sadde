package com.example.domain.ai

import com.example.domain.model.ActionType
import com.example.domain.model.AssistantAction
import com.example.domain.model.AssistantResponse

class OfflineRuleEngine {

    fun processCommand(input: String, language: String = "hi"): AssistantResponse {
        val lower = input.trim().lowercase()
        val isHindi = language.startsWith("hi")

        // 1. YouTube
        if (lower.contains("youtube") && (lower.contains("kholo") || lower.contains("open") || lower.contains("chalao") || lower.contains("start"))) {
            return AssistantResponse(
                reply = if (isHindi) "YouTube खोल रही हूँ." else "Opening YouTube.",
                actions = listOf(AssistantAction(ActionType.OPEN_APP, target = "com.google.android.youtube")),
                suggestedFollowUps = listOf("Music pause karo", "Volume badhao", "Back jao")
            )
        }

        // 2. Chrome
        if (lower.contains("chrome") && (lower.contains("kholo") || lower.contains("open") || lower.contains("start"))) {
            return AssistantResponse(
                reply = if (isHindi) "Google Chrome खोल रही हूँ." else "Opening Google Chrome.",
                actions = listOf(AssistantAction(ActionType.OPEN_APP, target = "com.android.chrome")),
                suggestedFollowUps = listOf("Search web", "Home jao")
            )
        }

        // 3. Settings
        if (lower.contains("settings") || lower.contains("setting")) {
            val sub = when {
                lower.contains("sound") || lower.contains("volume") || lower.contains("aawaz") -> "sound"
                lower.contains("display") || lower.contains("brightness") -> "display"
                lower.contains("wifi") || lower.contains("wi-fi") -> "wifi"
                lower.contains("bluetooth") -> "bluetooth"
                lower.contains("accessibility") -> "accessibility"
                else -> null
            }
            return AssistantResponse(
                reply = if (isHindi) "Settings खोल रही हूँ." else "Opening device settings.",
                actions = listOf(AssistantAction(ActionType.OPEN_SETTINGS, target = sub)),
                suggestedFollowUps = listOf("Volume badhao", "Brightness check", "Back jao")
            )
        }

        // 4. Volume Up
        if ((lower.contains("volume") || lower.contains("aawaz") || lower.contains("sound")) &&
            (lower.contains("badhao") || lower.contains("tez") || lower.contains("up") || lower.contains("increase") || lower.contains("high"))) {
            return AssistantResponse(
                reply = if (isHindi) "Volume बढ़ा दिया गया है." else "Increasing volume.",
                actions = listOf(AssistantAction(ActionType.VOLUME_UP)),
                suggestedFollowUps = listOf("Volume aur badhao", "Volume kam karo")
            )
        }

        // 5. Volume Down
        if ((lower.contains("volume") || lower.contains("aawaz") || lower.contains("sound")) &&
            (lower.contains("kam") || lower.contains("dheeme") || lower.contains("down") || lower.contains("decrease") || lower.contains("low"))) {
            return AssistantResponse(
                reply = if (isHindi) "Volume कम कर दिया गया है." else "Decreasing volume.",
                actions = listOf(AssistantAction(ActionType.VOLUME_DOWN)),
                suggestedFollowUps = listOf("Volume aur kam karo", "Music pause karo")
            )
        }

        // 6. Media Pause / Stop
        if (lower.contains("pause") || lower.contains("roko") || lower.contains("band karo") ||
            (lower.contains("music") && lower.contains("stop"))) {
            return AssistantResponse(
                reply = if (isHindi) "Music रोक दिया गया है." else "Music paused.",
                actions = listOf(AssistantAction(ActionType.PAUSE_MEDIA)),
                suggestedFollowUps = listOf("Music play karo", "Next song")
            )
        }

        // 7. Media Play / Resume
        if ((lower.contains("music") || lower.contains("gana") || lower.contains("song")) &&
            (lower.contains("play") || lower.contains("chalao") || lower.contains("bajao") || lower.contains("shuru"))) {
            return AssistantResponse(
                reply = if (isHindi) "Music चालू कर दिया गया है." else "Playing media.",
                actions = listOf(AssistantAction(ActionType.PLAY_MEDIA)),
                suggestedFollowUps = listOf("Music pause karo", "Next song", "Volume badhao")
            )
        }

        // 8. Next / Previous song
        if (lower.contains("next") || lower.contains("agla") || lower.contains("change song")) {
            return AssistantResponse(
                reply = if (isHindi) "अगला गाना प्ले किया जा रहा है." else "Playing next track.",
                actions = listOf(AssistantAction(ActionType.NEXT_MEDIA))
            )
        }
        if (lower.contains("previous") || lower.contains("pichhla") || lower.contains("prev")) {
            return AssistantResponse(
                reply = if (isHindi) "पिछला गाना प्ले किया जा रहा है." else "Playing previous track.",
                actions = listOf(AssistantAction(ActionType.PREVIOUS_MEDIA))
            )
        }

        // 9. Brightness
        if (lower.contains("brightness") || lower.contains("roshni")) {
            return AssistantResponse(
                reply = if (isHindi) "Brightness adjust karne ke liye Display settings khol rahi hoon." else "Opening Display settings to adjust brightness.",
                actions = listOf(AssistantAction(ActionType.SET_BRIGHTNESS))
            )
        }

        // 10. Navigation Back / Home / Recents
        if (lower.contains("back jao") || lower.contains("piche jao") || lower.contains("go back") || lower == "back") {
            return AssistantResponse(
                reply = if (isHindi) "Back ja rahi hoon." else "Navigating back.",
                actions = listOf(AssistantAction(ActionType.ACCESSIBILITY_BACK))
            )
        }

        if (lower.contains("home jao") || lower.contains("go home") || lower == "home screen") {
            return AssistantResponse(
                reply = if (isHindi) "Home screen par ja rahi hoon." else "Going to Home screen.",
                actions = listOf(AssistantAction(ActionType.ACCESSIBILITY_HOME))
            )
        }

        if (lower.contains("recent") || lower.contains("task list")) {
            return AssistantResponse(
                reply = if (isHindi) "Recent apps dikha rahi hoon." else "Opening recent apps.",
                actions = listOf(AssistantAction(ActionType.ACCESSIBILITY_RECENTS))
            )
        }

        // 11. Type text on screen (e.g., "Screen par jo search box hai usmein maths likho")
        val typeRegex = Regex("(?:likho|type|search box|box|input)\\s*(?:mein|par|into|in)?\\s*([a-zA-Z0-9\\s]+)\\s*(?:likho|type)?", RegexOption.IGNORE_CASE)
        val typeMatch = typeRegex.find(lower)
        if (lower.contains("likho") || lower.contains("type") || (lower.contains("search box") && lower.contains("maths"))) {
            val textToType = when {
                lower.contains("maths") -> "maths"
                lower.contains("likho") -> lower.substringAfter("usmein", lower.substringAfter("par", lower.substringBefore("likho"))).trim()
                else -> typeMatch?.groupValues?.getOrNull(1)?.trim() ?: "search"
            }
            return AssistantResponse(
                reply = if (isHindi) "Screen par '$textToType' likh rahi hoon." else "Typing '$textToType' into screen field.",
                actions = listOf(
                    AssistantAction(
                        type = ActionType.ACCESSIBILITY_TYPE_TEXT,
                        target = "search",
                        value = textToType
                    )
                )
            )
        }

        // 12. Accessibility click
        if (lower.contains("click") || lower.contains("dabaao") || lower.contains("press")) {
            val target = lower.replace("click", "")
                .replace("dabaao", "")
                .replace("press", "")
                .replace("karo", "")
                .replace("par", "")
                .trim()
            return AssistantResponse(
                reply = if (isHindi) "Screen par '$target' par click kar rahi hoon." else "Clicking '$target' on screen.",
                actions = listOf(
                    AssistantAction(
                        type = ActionType.ACCESSIBILITY_CLICK,
                        target = target
                    )
                )
            )
        }

        // 13. WhatsApp / Maps / Camera
        if (lower.contains("whatsapp")) {
            return AssistantResponse(
                reply = if (isHindi) "WhatsApp खोल रही हूँ." else "Opening WhatsApp.",
                actions = listOf(AssistantAction(ActionType.OPEN_APP, target = "com.whatsapp"))
            )
        }

        if (lower.contains("camera") || lower.contains("photo") || lower.contains("camera kholo")) {
            return AssistantResponse(
                reply = if (isHindi) "Camera खोल रही हूँ." else "Opening Camera.",
                actions = listOf(AssistantAction(ActionType.TAKE_PHOTO))
            )
        }

        if (lower.contains("map") || lower.contains("maps") || lower.contains("rasta")) {
            return AssistantResponse(
                reply = if (isHindi) "Google Maps खोल रही हूँ." else "Opening Google Maps.",
                actions = listOf(AssistantAction(ActionType.OPEN_APP, target = "com.google.android.apps.maps"))
            )
        }

        // 14. Help
        if (lower.contains("help") || lower.contains("madad") || lower.contains("kya kar sakti ho") || lower.contains("capabilities")) {
            return AssistantResponse(
                reply = if (isHindi) {
                    "Main ye sab kar sakti hoon:\n• Apps kholna (YouTube, Chrome, WhatsApp, Maps, Camera)\n• Phone settings & Volume control\n• Music play / pause / next\n• Screen UI assistance (Click, Scroll, Text typing, Back)\n• Offline smart voice & text commands"
                } else {
                    "Here is what I can do:\n• Launch apps (YouTube, Chrome, WhatsApp, Maps, Camera)\n• Adjust volume & settings\n• Media playback controls\n• Screen UI actions (Click, Scroll, Typing, Back)\n• Voice and text assistance"
                },
                suggestedFollowUps = listOf("YouTube kholo", "Volume badhao", "Music pause karo", "Settings kholo")
            )
        }

        // Generic fallback response for offline rule engine
        return AssistantResponse(
            reply = if (isHindi) {
                "Namaste! Maine aapki command '$input' suni. Offline mode mein main Apps kholne, Volume badhane/ghatane, Media control karne, aur UI assistance ke commands perform kar sakti hoon."
            } else {
                "I received your command: '$input'. In offline mode, I can help you open apps, adjust sound & settings, control music, or interact with screen elements."
            },
            suggestedFollowUps = listOf("YouTube kholo", "Chrome kholo", "Volume badhao", "Help")
        )
    }
}
