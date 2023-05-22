# jms_message_processing



## seting up the MQ in a docker container:
   ##### 1. get the image
    docker pull icr.io/ibm-messaging/mq:latest
   ##### 2. create a local storage for messages
    docker volume create qm1data
   ##### 3. start the container
    docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 --volume qm1data:/mnt/mqm --publish 1414:1414 --publish 9443:9443 --detach --env MQ_APP_PASSWORD=passw0rd --name QM1 icr.io/ibm-messaging/mq:latest
   ##### 4. modify the default queues to fit our needs
    docker cp src/main/resources/configure_backout_queue.mqsc QM1:/tmp/configure_backout_queue.mqsc
    docker exec -it QM1 bash
    runmqsc QM1 < tmp/configure_backout_queue.mqsc
   ##### 5. Access the MQ Web Console from here - https://localhost:9443/ibmmq/console
