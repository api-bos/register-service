package com.bos.register;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.bos.register.repository.bosdb",
        entityManagerFactoryRef = "bosEntityManagerFactory",
        transactionManagerRef = "bosTransactionManager")
public class BosDbConfig {
    @Primary
    @Bean(name = "bosDataSource")
    @ConfigurationProperties(prefix = "bos.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "bosEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    bosEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("bosDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
//                .packages("com.foobar.foo.domain")
                .packages("com.bos.register.entity.bosent")
                .persistenceUnit("bos")
                .build();
    }

    @Primary
    @Bean(name = "bosTransactionManager")
    public PlatformTransactionManager bosTransactionManager(
            @Qualifier("bosEntityManagerFactory") EntityManagerFactory
                    bosEntityManagerFactory
    ) {
        return new JpaTransactionManager(bosEntityManagerFactory);
    }
}
