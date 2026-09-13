package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.domain.GestureAction
import com.mustfa.mediaexplorer.domain.GestureDecision
import com.mustfa.mediaexplorer.domain.GestureInput
import org.junit.Assert.assertEquals
import org.junit.Test

class GestureDecisionTest {
    private val decision = GestureDecision()

    @Test fun horizontalGestureSeeks() = assertEquals(GestureAction.SEEK, decision.decide(GestureInput(120f, 10f, 120f, .5f)))
    @Test fun leftVerticalGestureChangesBrightness() = assertEquals(GestureAction.BRIGHTNESS, decision.decide(GestureInput(4f, 100f, 100f, .05f)))
    @Test fun rightVerticalGestureChangesVolume() = assertEquals(GestureAction.VOLUME, decision.decide(GestureInput(4f, 100f, 100f, .95f)))
    @Test fun deadZoneDoesNothing() = assertEquals(GestureAction.NONE, decision.decide(GestureInput(2f, 3f, 4f, .5f)))
}
