package com.example

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import graphql.GraphQLContext
import graphql.schema.DataFetchingEnvironment
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.header
import io.ktor.server.routing.routing
import kotlinx.coroutines.coroutineScope
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(GraphQL) {
        schema {
            packages = listOf("com.example")
            queries = listOf(
                ConferenceQuery()
            )
            mutations = listOf(
                ConferenceMutation()
            )
            typeHierarchy = mapOf(
                People::class to listOf(Attendee::class, Speaker::class)
            )
        }
        server {
            contextFactory = CustomContextFactory()
        }
    }
    routing {
        graphQLPostRoute()
        graphiQLRoute()
        graphQLSDLRoute()
    }
}

class ConferenceQuery : Query {
    fun conference(): Conference = Conference(name = "KotlinConf 2023", location = "Amsterdam")

    fun people(nameStartsWith: String? = null): List<People> = listOf(
        Speaker(name = "Derek", talks = listOf("Supercharged GraphQL!")),
        Attendee(name = "Alice", ticketType = TicketType.FULL),
        Attendee(name = "Bob", ticketType = TicketType.CONFERENCE)
    ).filter { it.name.startsWith(nameStartsWith ?: "") }

    fun schedule(): List<Talk> = listOf(
        Talk(ID(UUID.randomUUID().toString()), "Supercharged GraphQL")
    )
}

class ConferenceMutation : Mutation {
    fun buyTicket(ticketType: TicketType): Boolean = true
}

class Talk(val id: ID, val title: String) {
    fun date(env: DataFetchingEnvironment): String {
        val locale: Locale = env.graphQlContext.get("locale")
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale)
        return dateFormat.format(Date())
    }

    suspend fun description(): String = coroutineScope{
//        delay(1000)
        "Live Coding!"
    }
}

@GraphQLDescription("Some type description")
data class Conference(
    @GraphQLDescription("**conference name**") val name: String,
    @Deprecated("will be removed in future versions") val location: String? = null
)

interface People {
    val name: String
}

data class Speaker(override val name: String, val talks: List<String>) : People
data class Attendee(override val name: String, val ticketType: TicketType) : People

enum class TicketType {
    WORKSHOP,
    CONFERENCE,
    FULL
}

class CustomContextFactory : DefaultKtorGraphQLContextFactory() {
    override suspend fun generateContext(request: ApplicationRequest): GraphQLContext {
        return super.generateContext(request).plus(mapOf("locale" to Locale.forLanguageTag(request.header("Accept-Language") ?: "en_US")))
    }
}