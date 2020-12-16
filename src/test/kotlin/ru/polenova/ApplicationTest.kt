package ru.polenova

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.util.*
import kotlinx.io.streams.asInput
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    private val jsonContentType = ContentType.Application.Json.withCharset(Charsets.UTF_8)
    private val multipartBoundary = "***blob***"
    private val multipartContentType =
        ContentType.MultiPart.FormData.withParameter("boundary", multipartBoundary).toString()
    private val uploadPath = "./uploads"

    @KtorExperimentalAPI
    private val configure: Application.() -> Unit = {
        (environment.config as MapApplicationConfig).apply {
            put("polenova.upload.dir", uploadPath)
            }
        module(testing = true)
    }
    @KtorExperimentalAPI
    @Test
    fun testAuth() {
        withTestApplication(configure) {
            with(auth()) {
                kotlin.test.assertEquals(io.ktor.http.HttpStatusCode.OK, response.status())
            }
        }
    }

    @KtorExperimentalAPI
    @Test
    fun testUpload() {
        withTestApplication(configure) {
            val token = with(auth()) {
                com.jayway.jsonpath.JsonPath.read<String>(response.content!!, "$.token")
            }
            with(handleRequest(HttpMethod.Post, "/api/v1/media") {
                addHeader(HttpHeaders.ContentType, multipartContentType)
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    multipartBoundary,
                    listOf(
                        PartData.FileItem({
                            Files.newInputStream(Paths.get("./src/test/resources/test.png"))
                                .asInput()
                        }, {}, headersOf(
                            HttpHeaders.ContentDisposition to listOf(
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.Name,
                                    "file"
                                ).toString(),
                                ContentDisposition.File.withParameter(
                                    ContentDisposition.Parameters.FileName,
                                    "photo.png"
                                ).toString()
                            ),
                            HttpHeaders.ContentType to listOf(ContentType.Image.PNG.toString())
                        )
                        )
                    )
                )
            }) {
                response
                assertEquals(HttpStatusCode.OK, response.status())
                assertTrue(response.content!!.contains("\"id\""))
            }
        }
    }
    private fun TestApplicationEngine.auth(): TestApplicationCall =
        handleRequest(HttpMethod.Post, "/api/v1/authentication") {
            addHeader(HttpHeaders.ContentType, jsonContentType.toString())
            setBody(
                """
                        {
                            "username": "vasya",
                            "password": "password"
                        }
                    """.trimIndent()
            )
        }
}