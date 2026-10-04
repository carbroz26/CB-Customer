package com.carbroz.cbpartner.data.dynamic.remote

import com.carbroz.cbpartner.data.network.RemoteDataSource
import com.carbroz.cbpartner.data.network.RemoteResponse
import io.ktor.http.HttpMethod

/**
 * Dynamic-specific remote flow built on the common application RemoteDataSource.
 *
 * Dynamic supplies only backend request details. Common transport metadata is
 * owned by the shared RemoteDataSource boundary.
 */
class DynamicRemoteDataSource(
    private val remoteDataSource: RemoteDataSource,
) {
    suspend fun fetch(
        endpoint: String,
        method: String,
        body: String?,
    ): RemoteResponse =
        remoteDataSource.execute(
            method = HttpMethod.parse(method),
            url = endpoint,
            body = body,
        )
}
