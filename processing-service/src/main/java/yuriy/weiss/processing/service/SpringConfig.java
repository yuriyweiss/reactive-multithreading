package yuriy.weiss.processing.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SpringConfig {

    @Value( "${storage.mysql.connection.url}" )
    private String connectionUrl;
    @Value( "${storage.mysql.connection.username}" )
    private String username;
    @Value( "${storage.mysql.connection.password}" )
    private String password;

    @Bean( "mysqlDataSource" )
    public DataSource mysqlDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName( "org.mariadb.jdbc.Driver" );
        dataSourceBuilder.url( connectionUrl );
        dataSourceBuilder.username( username );
        dataSourceBuilder.password( password );
        return dataSourceBuilder.build();
    }

    @Bean( "mysqlJdbcTemplate" )
    public JdbcTemplate mysqlJdbcTemplate( DataSource mysqlDataSource ) {
        return new JdbcTemplate( mysqlDataSource );
    }
}
