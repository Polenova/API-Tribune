package ru.polenova

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.util.*
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.with
import org.kodein.di.ktor.KodeinFeature
import org.kodein.di.ktor.kodein
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import ru.polenova.exception.InvalidPasswordException
import ru.polenova.exception.NullUsernameOrPasswordException
import ru.polenova.exception.PasswordChangeException
import ru.polenova.exception.UserAccessException
import ru.polenova.repository.PostRepository
import ru.polenova.repository.PostRepositoryInMemoryWithMutexImpl
import ru.polenova.repository.UserRepository
import ru.polenova.repository.UserRepositoryInMemoryWithAtomicImpl
import ru.polenova.route.RoutingV1
import ru.polenova.service.*
import javax.naming.ConfigurationException

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@KtorExperimentalAPI
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
            serializeNulls()
        }
    }
    install(StatusPages) {
        exception<NotImplementedError> { e ->
            call.respond(HttpStatusCode.NotImplemented)
            throw e
        }
        exception<ParameterConversionException> { e ->
            call.respond(HttpStatusCode.BadRequest)
            throw e
        }
        exception<Throwable> { e ->
            call.respond(HttpStatusCode.InternalServerError)
            throw e
        }
        exception<NotFoundException> { e ->
            call.respond(HttpStatusCode.NotFound)
            throw e
        }
        exception<UserAccessException> { error ->
            call.respond(HttpStatusCode.Forbidden)
            throw error
        }
        exception<PasswordChangeException> { error ->
            call.respond(HttpStatusCode.Forbidden)
            throw error
        }
        exception<InvalidPasswordException> { error ->
            call.respond(HttpStatusCode.Unauthorized)
            throw error
        }
        exception<NullUsernameOrPasswordException> { error ->
            call.respond(HttpStatusCode.BadRequest)
            throw error
        }
        exception<ConfigurationException> { error ->
            call.respond(HttpStatusCode.NotFound)
            throw error
        }
    }
    install(KodeinFeature) {
        constant(tag = "upload-dir") with (environment.config.propertyOrNull("polenova.upload.dir")?.getString()
            ?: throw ConfigurationException("Upload dir is not specified"))
        bind<PasswordEncoder>() with eagerSingleton { BCryptPasswordEncoder() }
        bind<JWTTokenService>() with eagerSingleton { JWTTokenService() }
        bind<PostRepository>() with eagerSingleton { PostRepositoryInMemoryWithMutexImpl() }
        bind<ServicePost>() with eagerSingleton { ServicePost(instance()) }
        bind<FileService>() with eagerSingleton { FileService(instance(tag = "upload-dir")) }
        bind<UserRepository>() with eagerSingleton { UserRepositoryInMemoryWithAtomicImpl() }
        bind<UserService>() with eagerSingleton { UserService(instance(), instance(), instance()) }
        bind<RoutingV1>() with eagerSingleton {
            RoutingV1(
                instance(tag = "upload-dir"),
                instance(),
                instance()
            )
        }
    }

    install(Authentication) {
        jwt("jwt") {
            val jwtService by kodein().instance<JWTTokenService>()
            verifier(jwtService.verifier)
            val userService by kodein().instance<UserService>()
            validate {
                val id = it.payload.getClaim("id").asLong()
                val password = it.payload.getClaim("password").asString()
                userService.getModelByIdPassword(id, password)
            }
        }
        basic("basic") {
            val encoder by kodein().instance<PasswordEncoder>()
            val userService by kodein().instance<UserService>()
            validate { credentials ->
                val user = userService.getByUserName(credentials.name)
                if (encoder.matches(credentials.password, user?.password)) {
                    user
                } else {
                    null
                }
            }
        }
    }
    install(Routing) {
        val routingV1 by kodein().instance<RoutingV1>()
        routingV1.setup(this)
    }
}
