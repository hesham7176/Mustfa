package com.mustfa.mediaexplorer.tabs

import java.io.File
import java.util.UUID

data class FileTab(val id: String = UUID.randomUUID().toString(), val title: String, val location: File)

class TabState(initial: FileTab) {
    private val mutableTabs = mutableListOf(initial)
    var selectedId: String = initial.id
        private set
    val tabs: List<FileTab> get() = mutableTabs.toList()

    fun select(id: String) { if (mutableTabs.any { it.id == id }) selectedId = id }
    fun add(tab: FileTab) { mutableTabs += tab; selectedId = tab.id }
    fun close(id: String): Boolean {
        if (mutableTabs.size == 1 || mutableTabs.none { it.id == id }) return false
        mutableTabs.removeAll { it.id == id }
        if (selectedId == id) selectedId = mutableTabs.last().id
        return true
    }
}
