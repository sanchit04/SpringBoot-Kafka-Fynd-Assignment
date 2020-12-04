package com.sanchit.producer.WebhookKafkaProducer;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.logging.Logger;


@Component
public class KafkaJsonMessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    Logger log=Logger.getLogger(KafkaJsonMessageProducer.class.getName());

    public void sendMessage(List<String> payloadList) {
        log.info("Total Messages to be sent:"+payloadList.size());
        for(int i=0;i<payloadList.size();i++){

            JSONObject webhookJson=new JSONObject(payloadList.get(i));
            String event_type=webhookJson.getString("type");
            ListenableFuture<SendResult<String, String>> future=kafkaTemplate.send(event_type, payloadList.get(i));
            future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
                @Override
                public void onSuccess(SendResult<String, String> stringStringSendResult) {
                    System.out.println("Record Sent Info:Topic:"+stringStringSendResult.getRecordMetadata().topic()+",Offset:"+stringStringSendResult.getRecordMetadata().offset());
                }

                @Override
                public void onFailure(Throwable ex) {
                    System.out.println(ex.getStackTrace());
                }
            });
        }

    }

}
