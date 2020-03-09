package com.bos.register.config;

import com.bos.register.entity.NasabahDim;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(basePackages = "com.bos.register.repository.bca",
        entityManagerFactoryRef = "bcaEntityManagerFactory",
        transactionManagerRef = "bcaTransactionManager")
public class bcaDataSourceConfig {

    @Bean
    @ConfigurationProperties("bca.datasource")
    public DataSourceProperties bcaDataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("bca.datasource")
    public DataSource bcaDataSource(){
        return bcaDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Bean(name = "bcaEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bcaEntityManagerFactory(
            EntityManagerFactoryBuilder builder){
        return builder
                .dataSource(bcaDataSource())
                .packages(NasabahDim.class)
                .build();
    }

    @Bean
    public PlatformTransactionManager bcaTransactionManager(
            final @Qualifier("bcaEntityManagerFactory") LocalContainerEntityManagerFactoryBean bcaEntityManagerFactory){
        return new JpaTransactionManager(bcaEntityManagerFactory.getObject());
    }
}
