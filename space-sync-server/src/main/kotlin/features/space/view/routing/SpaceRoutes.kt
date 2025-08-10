package com.space.features.space.view.routing

import com.space.features.space.queries.CRUDSpaceRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.server.routing.route

// connect to view, have swagger annotations
fun Route.spaceRoutes() {

    route("/space") {

        post("/create") {
            // Create LOGIC
            /**
             * - insert space record to postgres
             * - register space kafka_key
             * - return dto ( id, kafka_key, partition, ...)
             */

            val model = application.dependencies.resolve<CRUDSpaceRepository>().create()
            call.respond(HttpStatusCode.OK, model)
        }

        get("/all"){
            val models = application.dependencies.resolve<CRUDSpaceRepository>().findAll()
            call.respond(HttpStatusCode.OK, models)
        }
    }




}