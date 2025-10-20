package com.space.subadmin.config

import com.github.f4b6a3.tsid.TsidCreator
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class TsidGenerator : IdentifierGenerator {

    companion object {
        // A static field to hold the TSID creator instance
        lateinit var tsidCreator: com.github.f4b6a3.tsid.Tsid
    }

    override fun generate(session: SharedSessionContractImplementor?, `object`: Any?): Serializable {
        // Generate a new TSID as a Long
        return TsidCreator.getTsid().toLong()
    }
}



@Configuration
class AppConfig {

    @Bean
    fun tsidCreator(): com.github.f4b6a3.tsid.Tsid {
        // You can customize the node ID here. It's crucial for distributed systems
        // to ensure each node has a unique ID to prevent collisions.
        // For example, get it from environment variables or a properties file.
        val nodeId = System.getProperty("node.id", "0").toInt()
        return TsidCreator.getTsid()
    }
}