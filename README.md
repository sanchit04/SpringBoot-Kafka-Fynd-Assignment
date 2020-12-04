# SpringBoot-Kafka-Fynd-Assignment

## INTRODUCTION
This end-to-end pipeline is been developed as part of Fynd-Kafka-Assignment.
It is basically a pipeline which capture the events happening on the Stripes payment platform with the help of webhooks and ingests it into Confluent-Cloud where it can be proccessed and consumed in near-real time fashion.

- Following are the tools used for simulating the event-streaming pipeline:
  - Stripes Payment platform
  - Webhook-Endpoint
  - Confluent Cloud
  - Spring-Boot Kafka Producer and Consumer
  - Tools for data-validation and data-load

## High-Level Architecture
![end-to-end-pipeline](https://user-images.githubusercontent.com/37934048/101222345-93190800-36af-11eb-8220-21172063054b.PNG)

## End-to-End pipeline details

- Events are generated on Stripes-Payment-Platform
- Webhook endpoint is used to capture mainly two events -> charge.succeeded,charge.updated
- Springboot Kafka-Producer gets the http responses from the webhook endpoint and then produces it to Confluent Cloud Kafka Brokers.
- Springboot Kafka-Consumer consumes from event-specific multiple topics and then writes the data into a rotating file.
- Rotating output files are rotated at a user-defined time-interval or specific size reached, any of this two conditions met causes the rotating of files.
- NOTE: Currently Schema-Registry implementation is not done due to the time-crunch. Schema-Registry will be used for backward compatability of JSON messages so the schema/message versioning is in place. 

### Additional Details

- LoadGenerator application is used to generate events on Stripe-Payment platform using Stripe-Api.
- FileMerger application is used to merge the events of same-type in file so that it can be validated with the initial input count.

## Important details about Applications:

- Stripe account is required for getting the Stripe-Key generated for triggering the events. Account can be generated here: (https://dashboard.stripe.com)
  - Stripe Key is required in the application.properties of the application.
  - Customer-Id is required in the application.properties at the time of load-generation.
- Webhook Endpoint can be found at the following link: (https://webhook.site/)
  - Webhook uuid is required in the application.properties at the time of Spring-Boot Kafka-Message production.
- All the Applications Projects have their configurations configurable in "/src/main/resources/application.properties"
  - Each Application project application.properties is unique and explains each configuration in details.
  
  Some ScreenShots:
  
  ![StripeKey](https://user-images.githubusercontent.com/37934048/101224394-74694000-36b4-11eb-813a-63fa00ce17fc.PNG)
  
  ![customerKey](https://user-images.githubusercontent.com/37934048/101224416-88ad3d00-36b4-11eb-984b-ff4913c63e08.PNG)
  
  
  ## Order of sequence to be followed for execution of pipeline:
  
  Following is the order of sequence to be followed for succesfull execution of the pipeline:
  - LoadGenerator
    - Update the application.properties in /src/main/resources
    - Run the application
  - WebhookKafkaProducer
    - Update the application.properties in /src/main/resources
    - Run the application
  - WebhookKafkaConsumer
    - Update the application.properties in /src/main/resources
    - Run the application
  - FileMerger
    - Update the application.properties in /src/main/resources
    - Run the application
  
  ## ScreenShots of validation of Pipeline:
  
  - LOAD-GENERATOR:

![load-charge updated](https://user-images.githubusercontent.com/37934048/101225797-6d443100-36b8-11eb-8d97-36aba7d3dd37.PNG)
![load-charge succeeded](https://user-images.githubusercontent.com/37934048/101225793-6c130400-36b8-11eb-8e83-26847c250ddf.PNG)

  - WEBHOOK-KAFKA-PRODUCER:

![recordSent-Producer-Console](https://user-images.githubusercontent.com/37934048/101225855-9d8bcf80-36b8-11eb-837e-ba83faed3403.PNG)
 
 - CONFLUENT-CONTROL-CENTER:
 
![control-center-topics](https://user-images.githubusercontent.com/37934048/101225929-c7dd8d00-36b8-11eb-9546-762a438145cd.PNG) 
![control-center-dataFlow](https://user-images.githubusercontent.com/37934048/101225925-c6ac6000-36b8-11eb-8e70-8b1401ccbe9c.PNG)

- WEBHOOK-KAFKA-CONSUMER:

![spring-Consumer](https://user-images.githubusercontent.com/37934048/101225980-f2c7e100-36b8-11eb-82e3-8c5547f622f5.PNG)

- FILE-MERGER:

![File-Merger](https://user-images.githubusercontent.com/37934048/101225972-f0fe1d80-36b8-11eb-8169-e050865d7459.PNG)
  





