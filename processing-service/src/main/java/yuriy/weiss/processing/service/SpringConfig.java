package yuriy.weiss.processing.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SpringConfig {

    @Value( "${storage.mysql.connection.url}" )
    private String connectionUrl;
    @Value( "${storage.mysql.connection.username}" )
    private String username;
    @Value( "${storage.mysql.connection.password}" )
    private String password;

    @Value( "${spring.kafka.bootstrap.servers}" )
    private String bootstrapAddress;
    @Value( "${spring.kafka.consumer.group.id}" )
    private String groupId;

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

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress );
        props.put( ConsumerConfig.GROUP_ID_CONFIG, groupId );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
        props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class );
        props.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true );
        props.put( ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 2000 );
        props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest" );
        return new DefaultKafkaConsumerFactory<>( props );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory( consumerFactory() );
        return factory;
    }
}
