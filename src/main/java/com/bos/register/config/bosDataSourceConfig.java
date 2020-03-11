package com.bos.register.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@EnableJpaRepositories(basePackages = "com.bos.register.repository.bos",
        entityManagerFactoryRef = "bosEntityManagerFactory",
        transactionManagerRef = "bosTransactionManager")
public class bosDataSourceConfig {
    @Primary
    @Bean
    @ConfigurationProperties(prefix = "bos.datasource")
    public DataSourceProperties bosDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource bosDataSource(@Qualifier("bosDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean bosEntityManagerFactory(@Qualifier("bosDataSource") DataSource hubDataSource, EntityManagerFactoryBuilder builder) {
        return builder.dataSource(hubDataSource).packages("com.bos.register.entity.bos")
                .persistenceUnit("bos").build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager bosTransactionManager(@Qualifier("bosEntityManagerFactory") EntityManagerFactory factory) {
        return new JpaTransactionManager(factory);
    }

    /*
    @Bean
    @Primary
    @ConfigurationProperties("bos.datasource")
    public DataSourceProperties bosDataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("bos.datasource")
    public DataSource bosDataSource(){
        return bosDataSourceProperties().initializeDataSourceBuilder()
                .type(HikariDataSource.class).build();
    }

    @Primary
    @Bean(name = "bosEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean bosEntityManagerFactory(EntityManagerFactoryBuilder builder){
        return builder
                .dataSource(bosDataSource())
                .packages(OTPDim.class)
                .packages(SellerDim.class)
                .packages(testCLK.class)
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager bosTransactionManager(
            final @Qualifier("bosEntityManagerFactory") LocalContainerEntityManagerFactoryBean bosEntityManagerFactory){
        return new JpaTransactionManager(bosEntityManagerFactory.getObject());
    }
     */
}
