package com.mustfa.mediaexplorer.domain

import kotlin.math.abs

enum class GestureAction { NONE, SEEK, BRIGHTNESS, VOLUME, TAP }

data class GestureInput(val deltaX: Float, val deltaY: Float, val distance: Float, val horizontalPosition: Float)

class GestureDecision(private val touchSlop: Float = 24f, private val deadZone: Float = 0.12f) {
    fun decide(input: GestureInput): GestureAction {
        if (input.distance < touchSlop) return GestureAction.NONE
        val horizontal = abs(input.deltaX) > abs(input.deltaY)
        if (horizontal) return GestureAction.SEEK
        if (input.horizontalPosition < deadZone) return GestureAction.BRIGHTNESS
        if (input.horizontalPosition > 1f - deadZone) return GestureAction.VOLUME
        return GestureAction.NONE
    }
}
