package yuriy.weiss.processing.service;

import akka.actor.typed.ActorSystem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ProcessingServiceApplication {

    public static void main( String[] args ) {
        ConfigurableApplicationContext context = SpringApplication.run( ProcessingServiceApplication.class, args );

//        final ActorSystem system = ActorSystem.create("QuickStart");
    }
}
