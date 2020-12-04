package com.webhook.event;

import com.stripe.Stripe;
import com.stripe.model.Charge;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class WebhookLoadEvents {

    public void eventGenerator(){


        try {

            File propsFile=new File("src/main/resources/application.properties");
            Properties propsUser=new Properties();
            FileReader reader1=new FileReader(propsFile);
            propsUser.load(reader1);
            int limit=Integer.parseInt(propsUser.getProperty("num.events.generate"));
            System.out.println("Total events that will be generated:"+limit*2);
            List<String>chargeIdList=new ArrayList<>();

            for (int i = 1; i <= limit; i++) {

                Stripe.apiKey = propsUser.getProperty("stripe.secret.test.key");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("amount", 100000);
                params.put("currency", "inr");
                params.put("customer", propsUser.getProperty("customer.id"));
                params.put("description", "Test Transaction");

                Charge charge = Charge.create(params);

                chargeIdList.add(charge.getId());


                System.out.println("Events loaded for charge.succeeded:"+i);


            }
            for (int i = 0; i < chargeIdList.size(); i++) {

                Stripe.apiKey = propsUser.getProperty("stripe.secret.test.key");
                Charge charge1=Charge.retrieve(chargeIdList.get(i));
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("amount", 673588+i*100);
                Map<String, Object> params1 = new HashMap<>();
                params1.put("metadata", metadata);

                Charge updatedCharge = charge1.update(params1);


                System.out.println("Events loaded for charge.updated:"+(i+1));


            }

            System.out.println("Load-Generation is completed!!");
        }catch (Exception e){
            e.printStackTrace();
        }



    }
}
