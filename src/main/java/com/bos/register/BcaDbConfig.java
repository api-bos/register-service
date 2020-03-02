package com.bos.register;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "com.bos.register.repository.bcadb",
        entityManagerFactoryRef = "bcaEntityManagerFactory",
        transactionManagerRef = "bcaTransactionManager")
public class BcaDbConfig {
    @Bean(name = "bcaDataSource")
    @ConfigurationProperties(prefix = "bca.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "bcaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean
    bcaEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("bcaDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("com.bos.register.entity.bcaent")
                .persistenceUnit("bca")
                .build();
    }
    @Bean(name = "bcaTransactionManager")
    public PlatformTransactionManager bcaTransactionManager(
            @Qualifier("bcaEntityManagerFactory") EntityManagerFactory
                    bcaEntityManagerFactory
    ) {
        return new JpaTransactionManager(bcaEntityManagerFactory);
    }
}
