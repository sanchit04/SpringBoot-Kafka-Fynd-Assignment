package com.sanchit.consumer.WebhookKafkaConsumer;

import org.apache.commons.io.FileUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.event.ConsumerPausedEvent;
import org.springframework.kafka.event.ConsumerStoppedEvent;
import org.springframework.kafka.event.ContainerStoppedEvent;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.processing.SupportedSourceVersion;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

@PropertySource(name="kafkaProps",value ="application.properties")
@Service
public class KafkaJsonMessageConsumer implements ConsumerSeekAware {

    public File propsFile;
    public Properties propsUser;
    public FileReader reader1;
    public Logger log;
    CountDownLatch latch = new CountDownLatch(1);
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    KafkaListenerEndpointRegistry registry;
    ConsumerPausedEvent event;


    @Autowired
    public void indexer() {
        propsFile = new File("src/main/resources/application.properties");
        propsUser = new Properties();

        try {
            log = Logger.getLogger(KafkaJsonMessageConsumer.class.getName());
            reader1 = new FileReader(propsFile);
            propsUser.load(reader1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    public List<String> messageList;


    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {

    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
       // assignments.forEach((t, o) -> callback.seekToBeginning(t.topic(), t.partition()));
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {


    }

    @EventListener
    public void onAppStarted(ApplicationStartedEvent event) {
        System.out.println("Started");
        registry.getListenerContainer(propsUser.getProperty("client.id")).start();

    }





    @KafkaListener(id = "#{'${client.id}'}", topics = "#{'${topicName}'.split(',')}", groupId = "#{'${group.id}'}", autoStartup = "false")
    public void listen(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {

        messageList = new ArrayList<>();
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total Batch:" + records.size());
        log.info("Total Batch:" + records.size());

        for (int i = 0; i < records.size(); i++) {
            messageList.add(records.get(i).value());
        }
        log.info("All the batch messages are added!");
        pauseForIO(messageList, acknowledgment);


    }

    private void pauseForIO(List<String> messageList, Acknowledgment acknowledgment) {
        try {

            log.info("The consumer will be paused for IO.");
            this.registry.getListenerContainer(propsUser.getProperty("client.id")).pause();
            Thread.sleep(1000);
            log.info("Pause-Requested:" + this.registry.getListenerContainer(propsUser.getProperty("client.id")).isPauseRequested());

            if (propsUser.getProperty("client.id").equals("") || propsUser.getProperty("file.name.with.location").equals("") || propsUser.getProperty("time.to.wait").equals("") || propsUser.getProperty("max.size.in.mb").equals("")) {
                throw new Exception("Mandatory Configurations are missing!");
            }

            if (this.registry.getListenerContainer(propsUser.getProperty("client.id")).isPauseRequested()) {

                log.info("System is paused now!!");
                Path path = Paths.get(propsUser.getProperty("file.name.with.location") + ".txt");
                File idFile = new File(path.toString());

                if (idFile.exists()) {

                    Double fileSizeInMb = (double) idFile.length() / (1024 * 1024);
                    BasicFileAttributes fatr = Files.readAttributes(path, BasicFileAttributes.class);
                    long creationTime = fatr.creationTime().toMillis();
                    long systemTime = System.currentTimeMillis();
                    long differenceTime = systemTime - creationTime;

                    Double maxFileSizeInMb = Double.parseDouble(propsUser.getProperty("max.size.in.mb"));
                    long maxTimeToWait = Long.parseLong(propsUser.getProperty("time.to.wait"));

                    if (differenceTime > maxTimeToWait || fileSizeInMb > maxFileSizeInMb) {

                        log.info("File Condition met getting ready for Rotation!");
                        rotateFile(path);
                        Thread.sleep(1000);
                        latch.await();

                    }
                    for (int i = 0; i < messageList.size(); i++) {

                        try {

                            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                            System.out.println(messageList.get(i));
                            writer.write(messageList.get(i));
                            writer.newLine();
                            writer.flush();
                            writer.close();


                        } catch (Exception e) {

                            log.severe("Exception caught while writing new records to existing file!");
                            log.severe(e.getStackTrace().toString());
                            System.out.println("Exception caught while writing new records to existing file!");
                            e.printStackTrace();
                            this.registry.getListenerContainer(propsUser.getProperty("client.id")).stop();
                            System.exit(0);

                        }

                    }
                    acknowledgment.acknowledge();


                } else if (idFile.exists() == false) {

                    log.info("File not found creating new");
                    for (int i = 0; i < messageList.size(); i++) {

                        try {

                            BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                            System.out.println(messageList.get(i));
                            writer.write(messageList.get(i));
                            writer.newLine();
                            writer.flush();
                            writer.close();

                        } catch (Exception e) {

                            log.severe("Exception caught while writing file to newly created file!");
                            log.severe(e.getStackTrace().toString());
                            e.printStackTrace();
                            this.registry.getListenerContainer(propsUser.getProperty("client.id")).stop();
                            System.exit(0);

                        }

                    }
                    acknowledgment.acknowledge();

                }

                log.info("Resuming the consumer now!!");
                log.info("All the batch messages have been processed!");
                this.registry.getListenerContainer(propsUser.getProperty("client.id")).resume();

            }


        } catch (Exception e) {
            log.severe("Exception caught at method-level");
            log.severe(e.getStackTrace().toString());
            System.out.println("Exception caught at method-level!");
            e.printStackTrace();
            this.registry.getListenerContainer(propsUser.getProperty("client.id")).stop();
            System.exit(0);

        }

    }

    private void rotateFile(Path path) {

        log.info("Starting the rotation activity now!");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");
        LocalDateTime now = LocalDateTime.now();

        Path targetPath = Paths.get(propsUser.getProperty("file.name.with.location") + dtf.format(now) + ".txt");

        try {
            Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
            Files.newBufferedWriter(path, StandardOpenOption.TRUNCATE_EXISTING);
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis());
            Files.setAttribute(path, "basic:creationTime", fileTime, LinkOption.NOFOLLOW_LINKS);
            latch.countDown();

        } catch (Exception e) {

            log.severe("Exception caught while rotating file!");
            log.severe(e.getStackTrace().toString());
            e.printStackTrace();
            System.out.println("Exception caught while rotating file!");
            this.registry.getListenerContainer(propsUser.getProperty("client.id")).stop();
            System.exit(0);


        }

    }


}