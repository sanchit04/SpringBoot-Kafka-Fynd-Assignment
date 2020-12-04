package com.webhook.event;

import com.stripe.Stripe;
import com.stripe.model.WebhookEndpoint;

import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        File propsFile=new File("src/main/resources/application.properties");
        Properties propsUser=new Properties();
        FileReader reader1=new FileReader(propsFile);
        propsUser.load(reader1);

        Stripe.apiKey = propsUser.getProperty("stripe.secret.test.key");
        WebhookLoadEvents eventsGen=new WebhookLoadEvents();
        List<Object> events1 = new ArrayList<Object>();
        events1.add("charge.succeeded");
        events1.add("charge.failed");
        events1.add("charge.updated");
        events1.add("charge.pending");
        Map<String,Object> param=new HashMap<String, Object>();
        param.put("enabled_events",events1);
        param.put("url",propsUser.getProperty("webhook.test.point"));

        if(propsUser.getProperty("register.webhook.endpoint").equals("true")) {
            WebhookEndpoint webhookEndpoint = WebhookEndpoint.create(param);
            System.out.println("Webhook Successfully registered with Stripe Payment!");

        }
        System.out.println("Starting with load-test of webhook");

        eventsGen.eventGenerator();



    }
}
