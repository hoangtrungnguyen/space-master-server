package com.space.subadmin.config

import org.hsqldb.server.Server
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import javax.sql.DataSource

@Configuration
class HsqlServerConfiguration {

    @Bean(name = ["hsqlServer"], initMethod = "start", destroyMethod = "stop")
    fun hsqlServer(): Server {
        val server = Server()
        server.setDatabaseName(0, "sub-admin-db")
        server.setDatabasePath(0, "file:./data/sub-admin-db")
        server.isNoSystemExit = true
        server.isSilent = true
        return server
    }

    @Bean
    @DependsOn("hsqlServer")
    fun dataSource(properties: DataSourceProperties): DataSource {
        return properties.initializeDataSourceBuilder().build()
    }
}
