package com.sanchit.producer.WebhookKafkaProducer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory() throws Exception {
        FileReader reader=new FileReader("src/main/resources/application.properties");
        Properties props=new Properties();
        props.load(reader);
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, props.get("bootstrap.servers"));
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, props.get("key.serializer"));
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, props.get("value.serializer"));
        configProps.put(ProducerConfig.ACKS_CONFIG,props.getProperty("acks"));
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG,props.getProperty("client.id"));
        configProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,props.getProperty("security.protocol"));
        configProps.put(SaslConfigs.SASL_JAAS_CONFIG,props.getProperty("sasl.jaas.config"));
        configProps.put(SaslConfigs.SASL_MECHANISM,props.getProperty("sasl.mechanism"));
        configProps.put("client.dns.lookup",props.getProperty("client.dns.lookup"));
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() throws Exception {
        return new KafkaTemplate<>(producerFactory());
    }

}
