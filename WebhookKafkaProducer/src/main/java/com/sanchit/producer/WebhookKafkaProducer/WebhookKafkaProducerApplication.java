package com.sanchit.producer.WebhookKafkaProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
public class WebhookKafkaProducerApplication{

	public static void main(String[] args) {

		SpringApplication.run(WebhookKafkaProducerApplication.class, args);


	}

}
