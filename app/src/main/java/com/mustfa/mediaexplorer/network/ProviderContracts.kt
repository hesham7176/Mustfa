package com.mustfa.mediaexplorer.network

import kotlinx.coroutines.flow.Flow
import java.io.InputStream
import java.io.OutputStream

sealed interface RemoteResult<out T> {
    data class Success<T>(val value: T) : RemoteResult<T>
    data class Failure(val message: String, val cause: Throwable? = null) : RemoteResult<Nothing>
}

data class RemoteItem(val id: String, val name: String, val directory: Boolean, val size: Long? = null)

interface NetworkFileProvider {
    val scheme: String
    suspend fun connect(endpoint: String, username: String?, password: CharArray?): RemoteResult<Unit>
    fun list(path: String): Flow<RemoteItem>
    suspend fun download(path: String, output: OutputStream): RemoteResult<Unit>
    suspend fun upload(input: InputStream, path: String): RemoteResult<Unit>
    suspend fun delete(path: String): RemoteResult<Unit>
    suspend fun disconnect()
}

interface CloudProvider : NetworkFileProvider
