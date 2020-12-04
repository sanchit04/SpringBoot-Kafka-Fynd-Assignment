package com.sanchit.consumer.WebhookKafkaConsumer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchErrorHandler;
import org.springframework.kafka.listener.BatchLoggingErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() throws Exception {

        File propsFile=new File("src/main/resources/application.properties");
        Properties propsUser=new Properties();
        FileReader reader1=new FileReader(propsFile);
        propsUser.load(reader1);

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, propsUser.getProperty("bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, propsUser.getProperty("group.id"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, propsUser.getProperty("key.deserializer"));
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, propsUser.getProperty("value.deserializer"));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, propsUser.getProperty("client.id"));
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,propsUser.getProperty("max.poll.records"));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, propsUser.getProperty("enable.auto.commit"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, propsUser.getProperty("auto.offset.reset"));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,propsUser.getProperty("security.protocol"));
        props.put(SaslConfigs.SASL_JAAS_CONFIG,propsUser.getProperty("sasl.jaas.config"));
        props.put(SaslConfigs.SASL_MECHANISM,propsUser.getProperty("sasl.mechanism"));
        props.put("client.dns.lookup",propsUser.getProperty("client.dns.lookup"));
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() throws Exception {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(1);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setBatchErrorHandler(new BatchLoggingErrorHandler());
        return factory;
    }

}

