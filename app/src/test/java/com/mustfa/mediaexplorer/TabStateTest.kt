package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.tabs.FileTab
import com.mustfa.mediaexplorer.tabs.TabState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class TabStateTest {
    @Test fun keepsOneTabAndSwitchesSelection() {
        val state = TabState(FileTab(title = "Home", location = File("/")))
        val second = FileTab(title = "USB", location = File("/usb"))
        state.add(second)
        assertEquals(2, state.tabs.size)
        assertTrue(state.close(second.id))
        assertFalse(state.close(state.tabs.single().id))
    }
}
