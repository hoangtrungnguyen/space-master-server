package com.space.features.space

import com.space.features.space.queries.CRUDSpaceRepository
import com.space.features.space.queries.CRUDSpaceRepositoryImpl
import io.ktor.server.plugins.di.DependencyRegistry


fun DependencyRegistry.provideSpaceDependencies() {

    provide<CRUDSpaceRepository> {
        CRUDSpaceRepositoryImpl()
    }
}
