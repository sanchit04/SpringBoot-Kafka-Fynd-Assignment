package com.webhook.events;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class App 
{
    public static void main( String[] args ) {
        try {

            Map<String, List<String>>eventSorter=new HashMap<>();
            File propsFile=new File("src/main/resources/application.properties");
            final Properties propsUser=new Properties();
            FileReader reader1=new FileReader(propsFile);
            propsUser.load(reader1);

            File dir = new File(propsUser.getProperty("files.dir"));
            File[] foundFiles = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.startsWith(propsUser.getProperty("file.pattern.search"));
                }
            });
            int counter=0;

            for (File file : foundFiles) {

                BufferedReader bufReader = new BufferedReader(new FileReader(file));

                String line = bufReader.readLine();
                while (line != null) {

                    //System.out.println(line);
                    JSONObject jsonObject = new JSONObject(line);
                    String event_type=jsonObject.getString("type");
                    eventSorter.computeIfAbsent(event_type, k -> new ArrayList<>()).add(line);

                    line = bufReader.readLine();
                    counter++;
                }

                bufReader.close();
                System.out.println("Total Records:"+counter);



            }
            System.out.println("----------------------------------------------------");
            System.out.println("SUMMARY:");
            for(Map.Entry<String,List<String>> entry:eventSorter.entrySet()){

                System.out.println("Event_Type:"+entry.getKey());
                System.out.println("Event_Type_Count:"+entry.getValue().size());
                Path path= Paths.get(propsUser.getProperty("files.dir")+"\\"+entry.getKey()+"-events.txt");

                for (int i = 0; i < entry.getValue().size(); i++){

                    BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                    //System.out.println(entry.getValue().get(i));
                    writer.write(entry.getValue().get(i));
                    writer.newLine();
                    writer.flush();
                    writer.close();

                }




            }



        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
