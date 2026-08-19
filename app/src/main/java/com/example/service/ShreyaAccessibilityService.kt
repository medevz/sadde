package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShreyaAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "ShreyaAccessService"
        var instance: ShreyaAccessibilityService? = null
            private set

        fun isConnected(): Boolean {
            return instance != null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "ShreyaAccessibilityService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Passive observation of authorized UI events for context if needed
    }

    override fun onInterrupt() {
        Log.w(TAG, "ShreyaAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
        }
    }

    /**
     * Finds and clicks a clickable node matching text or description.
     * Enforces security: Ignores password or sensitive nodes.
     */
    fun clickNodeByText(searchText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val cleanSearch = searchText.trim().lowercase()

        val targetNode = findNodeRecursive(root) { node ->
            if (node.isPassword) return@findNodeRecursive false
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""
            (text.contains(cleanSearch) || desc.contains(cleanSearch))
        }

        if (targetNode != null) {
            // Find clickable parent or node itself
            var clickableNode: AccessibilityNodeInfo? = targetNode
            while (clickableNode != null && !clickableNode.isClickable) {
                clickableNode = clickableNode.parent
            }

            val performed = clickableNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK) ?: false
            targetNode.recycle()
            return performed
        }
        return false
    }

    /**
     * Enters text into a focused field or search field matching target query.
     */
    fun typeText(targetHint: String?, textToType: String): Boolean {
        val root = rootInActiveWindow ?: return false
        var targetNode: AccessibilityNodeInfo? = null

        if (!targetHint.isNullOrBlank()) {
            val cleanHint = targetHint.trim().lowercase()
            targetNode = findNodeRecursive(root) { node ->
                if (node.isPassword) return@findNodeRecursive false
                val isEditable = node.isEditable || node.className?.contains("EditText", ignoreCase = true) == true
                val text = node.text?.toString()?.lowercase() ?: ""
                val desc = node.contentDescription?.toString()?.lowercase() ?: ""
                isEditable && (text.contains(cleanHint) || desc.contains(cleanHint))
            }
        }

        if (targetNode == null) {
            // Find first editable node or currently focused node
            targetNode = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
                ?: findNodeRecursive(root) { node ->
                    !node.isPassword && (node.isEditable || node.className?.contains("EditText", ignoreCase = true) == true)
                }
        }

        if (targetNode != null) {
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            val result = targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
            targetNode.recycle()
            return result
        }
        return false
    }

    fun scroll(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollableNode = findNodeRecursive(root) { node ->
            node.isScrollable
        }

        if (scrollableNode != null) {
            val action = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
            val result = scrollableNode.performAction(action)
            scrollableNode.recycle()
            return result
        }
        return false
    }

    fun goBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun goHome(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_HOME)
    }

    fun openRecents(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_RECENTS)
    }

    fun openNotifications(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
    }

    fun readVisibleScreenSummary(): String {
        val root = rootInActiveWindow ?: return "Screen content is currently unavailable."
        val sb = StringBuilder()
        collectTextRecursive(root, sb, 0)
        return if (sb.isNotBlank()) sb.toString().trim() else "No visible text detected on active screen."
    }

    private fun collectTextRecursive(node: AccessibilityNodeInfo?, sb: StringBuilder, depth: Int) {
        if (node == null || depth > 10 || sb.length > 1000) return
        if (node.isPassword || !node.isVisibleToUser) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank() && !sb.contains(text)) {
            sb.append(text).append(". ")
        } else if (!desc.isNullOrBlank() && !sb.contains(desc)) {
            sb.append(desc).append(". ")
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                collectTextRecursive(child, sb, depth + 1)
                child.recycle()
            }
        }
    }

    private fun findNodeRecursive(
        node: AccessibilityNodeInfo?,
        predicate: (AccessibilityNodeInfo) -> Boolean
    ): AccessibilityNodeInfo? {
        if (node == null) return null
        if (predicate(node)) {
            return node
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = findNodeRecursive(child, predicate)
            if (result != null) {
                return result
            }
            child?.recycle()
        }
        return null
    }
}
