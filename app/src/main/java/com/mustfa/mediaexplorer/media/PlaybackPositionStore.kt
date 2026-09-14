package com.mustfa.mediaexplorer.media

import android.content.Context

class PlaybackPositionStore(context: Context) {
    private val preferences = context.getSharedPreferences("playback_positions", Context.MODE_PRIVATE)

    fun save(uri: String, positionMs: Long) { preferences.edit().putLong(uri, positionMs).apply() }
    fun read(uri: String): Long = preferences.getLong(uri, 0L)
    fun clear(uri: String) { preferences.edit().remove(uri).apply() }
}
