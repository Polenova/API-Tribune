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
import ru.polenova.dto.PostRequestDto
import ru.polenova.dto.UserRequestDto
import ru.polenova.dto.UserResponseDto
import ru.polenova.model.AuthUserModel
import ru.polenova.service.ServicePost
import ru.polenova.service.UserService

class RoutingV1(
    private val staticPath: String,
    private val postService: ServicePost,
    private val userService: UserService
) {
    @KtorExperimentalAPI
    fun setup(configuration: Routing) {
        with(configuration) {
            route("/api/v1/") {
                static("/static") { files(staticPath) }

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
                    }

                    route("/posts") {
                        get("/{idPosts}") {
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
                        get("/{idPost}/get-posts-before") {
                            val me = call.authentication.principal<AuthUserModel>()
                            val idPost = call.parameters["idPost"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "idPost",
                                "Long"
                            )
                            val response = postService.getPostsBefore(idPost, me!!.idUser, userService)
                            call.respond(response)
                        }
                        get ("/{idPost}/reaction-by-users") {
                            val idPost = call.parameters["idPost"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "idPost",
                                "Long"
                            )
                            val response = userService.listUsersReaction(idPost, postService)
                            call.respond(response)
                        }
                        post {
                            val me = call.authentication.principal<AuthUserModel>()
                            val input = call.receive<PostRequestDto>()
                            postService.save(input, me!!.idUser, userService)
                            call.respond(HttpStatusCode.OK)
                        }
                        post("/{idPost}/up") {
                            val me = call.authentication.principal<AuthUserModel>()
                            val idPost = call.parameters["idPost"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "idPost",
                                "Long"
                            )
                            val response = postService.upById(idPost, me!!.idUser, userService)
                            call.respond(response)
                        }

                        post("/{idPost}/down") {
                            val me = call.authentication.principal<AuthUserModel>()
                            val idPost = call.parameters["idPost"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "idPost",
                                "Long"
                            )
                            val response = postService.downById(idPost, me!!.idUser, userService)
                            call.respond(response)
                        }

                        get("/me") {
                            val me = call.authentication.principal<AuthUserModel>()
                            val response = postService.getUserPosts(me!!.idUser, userService)
                            call.respond(response)
                        }

                        get("/username/{username}") {
                            val username = call.parameters["username"]
                            val user = userService.getByUserName(username!!)
                            val response = postService.getUserPosts(user!!.idUser, userService)
                            call.respond(response)
                        }

                        delete("/{idPost}") {
                            val me = call.authentication.principal<AuthUserModel>()
                            val id = call.parameters["idPost"]?.toLongOrNull() ?: throw ParameterConversionException(
                                "idPost",
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
}