package ru.polenova.route

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.*
import ru.polenova.dto.*
import ru.polenova.model.AuthUserModel
import ru.polenova.service.ServicePost
import ru.polenova.service.UserService

class RoutingV1(
    private val staticPath: String,
    private val postService: ServicePost,
    private val fileService: FileService,
    private val userService: UserService
) {
    @KtorExperimentalAPI
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") { files(staticPath) }
            }
            route("/") {
                post("/registration") {
                    val input = call.receive<UserRequestDto>()
                    val response = userService.save(input)
                    call.respond(response)
                }
                post("/authentication") {
                    val input = call.receive<UserRequestDto>()
                    val response = userService.authenticate(input)
                    call.respond(response)
                }
            }
            authenticate("basic", "jwt") {
                route("/me") {
                    get {
                        val me = call.authentication.principal<AuthUserModel>()
                        call.respond(UserResponseDto.fromModel(me!!.idUser, userService, postService))
                    }
                    post("/change-password") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val input = call.receive<PasswordChangeRequestDto>()
                        val response = userService.changePassword(me!!.idUser, input)
                        call.respond(response)
                    }
                }

                route("/posts") {
                    get {
                        val me = call.authentication.principal<AuthUserModel>()
                        val response = postService.getAllPosts(me!!.idUser, userService)
                        call.respond(response)
                    }
                    get("/{idPosts}") {
                        //val me = call.authentication.principal<AuthUserModel>()
                        val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "id",
                            "Long"
                        )
                        val response = postService.getByIdPost(id)
                        call.respond(response)
                    }
                    get("/recent") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val response = postService.getRecent(me!!.idUser, userService)
                        call.respond(response)
                    }
                    get("{id}/get-posts-after") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "id",
                            "Long"
                        )
                        val response = postService.getPostsAfter(id, me!!.idUser, userService)
                        call.respond(response)
                    }
                    get("{id}/get-posts-before") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "id",
                            "Long"
                        )
                        val response = postService.getPostsBefore(id, me!!.idUser, userService)
                        call.respond(response)
                    }
                    post {
                        val me = call.authentication.principal<AuthUserModel>()
                        val input = call.receive<PostRequestDto>()
                        postService.save(input, me!!, userService)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("/{idPost}/up") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val idPost = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "idPost",
                            "Long"
                        )
                        val response = postService.upById(idPost, me!!.idUser, userService)
                        call.respond(response)
                    }

                    post("/{idPost}/down") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val idPost = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "idPost",
                            "Long"
                        )
                        val response = postService.downById(idPost, me!!.idUser, userService)
                        call.respond(response)
                    }

                    post {
                        val me = call.authentication.principal<AuthUserModel>()
                        val input = call.receive<PostRequestDto>()
                        postService.save(input, me!!, userService)
                        call.respond(HttpStatusCode.OK)
                    }
                    post("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "id",
                            "Long"
                        )
                        val input = call.receive<PostRequestDto>()
                        val me = call.authentication.principal<AuthUserModel>()
                        postService.saveById(id, input, me!!, userService)
                        call.respond(HttpStatusCode.OK)
                    }
                    delete("/{id}") {
                        val me = call.authentication.principal<AuthUserModel>()
                        val id = call.parameters["id"]?.toLongOrNull() ?: throw ParameterConversionException(
                            "id",
                            "Long"
                        )
                        if (!postService.removePostByIdPost(id, me!!)) {
                            println("You can't delete post of another user")
                        }
                    }
                }
            }
        }
    }
}
