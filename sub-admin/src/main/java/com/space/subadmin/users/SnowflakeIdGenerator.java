package com.space.subadmin.users;

import com.github.f4b6a3.tsid.TsidFactory;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * Hibernate ID generator that uses the tsid-creator library to generate unique, time-sortable IDs (TSIDs).
 * TSIDs are a type of Snowflake ID that are highly performant and suitable for distributed systems.
 * <p>
 * This generator is Spring-aware, allowing the underlying {@link TsidFactory} to be configured
 * as a Spring bean. This is important for setting a unique {@code nodeId} for each application instance
 * to prevent ID collisions.
 * <p>
 * To use this generator, annotate an entity's ID field:
 * <pre>
 * {@code
 * @Id
 * @GenericGenerator(name = "snowflake_id", strategy = "com.space.subadmin.users.SnowflakeIdGenerator")
 * @GeneratedValue(generator = "snowflake_id")
 * private Long id;
 * }
 * </pre>
 */
public class SnowflakeIdGenerator implements IdentifierGenerator, ApplicationContextAware {

    private static volatile TsidFactory tsidFactory;

    @Configuration
    public static class TsidFactoryConfig {
        @Bean
        public TsidFactory tsidFactory() {
            // The node ID should be unique for each application instance.
            // It can be configured via system properties, environment variables, or a config file.
            int nodeId = Integer.parseInt(System.getProperty("myapp.node-id", "0"));
            return TsidFactory.builder()
                    .withNode(nodeId)
                    .build();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (tsidFactory == null) {
            synchronized (SnowflakeIdGenerator.class) {
                if (tsidFactory == null) {
                    tsidFactory = applicationContext.getBean(TsidFactory.class);
                }
            }
        }
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return tsidFactory.create().toLong();
    }
}
