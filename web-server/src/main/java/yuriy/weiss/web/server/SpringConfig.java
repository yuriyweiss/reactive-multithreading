package yuriy.weiss.web.server;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringConfig {

    @Bean( "mysqlDataSource" )
    public DataSource mysqlDataSource(
            @Value( "${storage.mysql.connection.url}" ) String connectionUrl,
            @Value( "${storage.mysql.connection.username}" ) String username,
            @Value( "${storage.mysql.connection.password}" ) String password ) {
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

    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value( "spring.kafka.bootstrap.servers" ) String bootstrapAddress ) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress );
        configProps.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        configProps.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class );
        return new DefaultKafkaProducerFactory<>( configProps );
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate( ProducerFactory<String, String> producerFactory ) {
        return new KafkaTemplate<>( producerFactory );
    }
}
