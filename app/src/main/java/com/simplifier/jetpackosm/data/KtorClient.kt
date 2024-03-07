package com.simplifier.jetpackosm.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.logging.SIMPLE
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

object KtorClient {

    val client: HttpClient =
        HttpClient(Android) {
            defaultRequest {
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
            }

            engine {
                connectTimeout = 20000
                socketTimeout = 20000
            }

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    json = Json {
                        isLenient = true
                        ignoreUnknownKeys = true

                    }
                )
            }
        }
}