package com.mustfa.mediaexplorer

import com.mustfa.mediaexplorer.data.FileFilter
import com.mustfa.mediaexplorer.data.ViewMode
import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModeTest {
    @Test fun exposesAllRequestedModes() = assertEquals(9, ViewMode.entries.size)

    @Test fun detailsModesAreNotGridModes() {
        assert(ViewMode.entries.filter { it.details }.all { !it.grid })
    }
    
    @Test fun filterContractIncludesMediaAndDocumentTypes() {
        assert(FileFilter.IMAGES != FileFilter.VIDEOS)
        assert(FileFilter.AUDIO != FileFilter.DOCUMENTS)
        assertEquals(6, FileFilter.entries.size)
    }
}