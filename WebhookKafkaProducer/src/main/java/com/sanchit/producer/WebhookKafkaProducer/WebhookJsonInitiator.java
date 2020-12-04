package com.sanchit.producer.WebhookKafkaProducer;

import com.github.wnameless.json.flattener.JsonFlattener;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.kafka.common.protocol.types.Field;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class WebhookJsonInitiator implements ApplicationRunner {

    @Autowired
    private KafkaJsonMessageProducer jsonProducer;

    Logger log=Logger.getLogger(WebhookJsonInitiator.class.getName());

    public Path uuidPath;

    public List<String> getWebHookResponse() throws FileNotFoundException,IOException{

        FileReader reader=new FileReader("src/main/resources/application.properties");
        Properties props=new Properties();
        props.load(reader);

        String token=props.getProperty("webhook.uuid");
        uuidPath=Paths.get(props.getProperty("uuid.storage.file.location"));

        log.info("New GET Request to Webhook");

        List<String> finalWebHookPayload=new ArrayList<>();
        List<String> finalResponseList=new ArrayList<>();
         OkHttpClient client=new OkHttpClient();
         Request versionRequestKey=new Request.Builder()
                .url("https://webhook.site/token/"+token+"/requests")
                .build();


        List<String> idList=new ArrayList<>();
        String uuid="";
        try{

            String versionResult=client.newCall(versionRequestKey).execute().body().string();
            JSONObject json=new JSONObject(versionResult);
            log.info("Initial GET Request is_last_page:"+json.get("is_last_page"));
            String isLastPage=json.get("is_last_page").toString();
            List<JSONArray> jsonArrayList=new ArrayList<>();
            int counter=2;

            while (isLastPage.equals("false")){
                log.info("Traversing to page for new UUID:"+counter);
                Request versionRequestTemp=new Request.Builder()
                        .url("https://webhook.site/token/"+token+"/requests?page="+counter)
                        .build();
                OkHttpClient clientTemp=new OkHttpClient();
                String versionResultTemp=clientTemp.newCall(versionRequestTemp).execute().body().string();
                JSONObject jsonTemp=new JSONObject(versionResultTemp);

                if(jsonTemp.get("is_last_page").equals(true)){
                    log.info("Last Page Found!");
                    jsonArrayList.add((JSONArray) jsonTemp.get("data"));
                    break;
                }
                jsonArrayList.add((JSONArray) jsonTemp.get("data"));
                counter++;

            }

            jsonArrayList.add((JSONArray) json.get("data"));
            JSONObject tempJson;
            for(int i=0;i<jsonArrayList.size();i++) {

                for (int j = 0; j < jsonArrayList.get(i).length(); j++) {
                    tempJson = (JSONObject) jsonArrayList.get(i).get(j);
                    uuid = tempJson.get("uuid").toString();
                    idList.add(uuid);

                }
            }

            log.info("Total UUID FOUND:"+idList.size());
            List<String> filteredList= idChecker(idList);

            if(filteredList.isEmpty()==false){

                log.info("New webhooks will be published!");

                for(int i=0;i<filteredList.size();i++){

                    client=new OkHttpClient();
                    versionRequestKey=new Request.Builder()
                            .url("https://webhook.site/token/"+token+"/request/"+filteredList.get(i).trim()+"/raw")
                            .build();
                    versionResult=client.newCall(versionRequestKey).execute().body().string();
                    JSONObject webhookJson=new JSONObject(versionResult);
                    versionResult=webhookJson.toString();
                    String event_type=webhookJson.getString("type");

                    if(event_type.trim().equals("charge.succeeded") && props.getProperty("event.charge.succeeded.flatten.json").equals("true")){
                        String flattenedJson = JsonFlattener.flatten(webhookJson.toString());
                        versionResult = flattenedJson;


                    }
                    else if(event_type.trim().equals("charge.updated") && props.getProperty("event.charge.updated.flatten.json").equals("true")){

                        String flattenedJson = JsonFlattener.flatten(webhookJson.toString());
                        versionResult=flattenedJson;

                    }
                    finalWebHookPayload.add(versionResult);

                }
            }
            else if(filteredList.isEmpty()){
                log.info("No new webhooks found!");
                System.out.println("No new webhooks found!");
            }


        } catch (Exception e){

            e.printStackTrace();

        }

        return finalWebHookPayload;
    }

    public List<String> idChecker(List<String> webhookId) throws IOException{
        FileReader reader=new FileReader("src/main/resources/application.properties");
        Properties props=new Properties();
        props.load(reader);

        //Path path = Paths.get(props.getProperty("uuid.storage.file.location"));
        File idFile=new File(uuidPath.toString());
        List<String> currentWebhookId = new ArrayList<>();

        if(idFile.exists()){
            BufferedReader bufReader = new BufferedReader(new FileReader(uuidPath.toString()));
            currentWebhookId = new ArrayList<>();
            String line = bufReader.readLine();
            while (line != null) {
                currentWebhookId.add(line);
                line = bufReader.readLine();
            }

            bufReader.close();
            List<String> filteredList=new ArrayList<>(webhookId);
            filteredList.removeAll(currentWebhookId);
            System.out.println("FilteredList:"+filteredList);

            if(filteredList.isEmpty()==false){
               log.info("Filtered list is not empty");
                for(int i=0;i<filteredList.size();i++){

                    try (BufferedWriter writer = Files.newBufferedWriter(uuidPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                        writer.write(filteredList.get(i));
                        writer.newLine();
                        writer.flush();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                log.info("Written all the new uuid to UUID Storage File.");

            }

            System.out.println(filteredList);
            currentWebhookId=filteredList;
        }

        else if(idFile.exists()==false){

            log.info("UUID Storage File is not present.. Creating New File");
            currentWebhookId=webhookId;

            for(int i=0;i<currentWebhookId.size();i++){

                try (BufferedWriter writer = Files.newBufferedWriter(uuidPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                    writer.write(currentWebhookId.get(i)+System.getProperty("line.separator"));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            log.info("Written all the uuid to new UUID Storage file");

        }

        return currentWebhookId;
    }

    @Override
    public void run(ApplicationArguments args)  {
        try {
            jsonProducer.sendMessage(getWebHookResponse());
        }catch (Exception e){
            log.severe("Since Exception is thrown the uuid file will be deleted!!");
            System.out.println("Since Exception is thrown the uuid file will be deleted!!");
            File idFile=new File(uuidPath.toString());
            idFile.delete();
            e.printStackTrace();
        }

    }
}
