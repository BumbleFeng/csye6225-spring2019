#!/bin/bash

sudo systemctl stop webapi

sudo mv -f var/webapi.jar /var/webapi/

sudo chown webapi:webapi /var/webapi/webapi.jar

sudo chmod 500 /var/webapi/webapi.jar

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
    -a fetch-config \
    -m ec2 \
    -c file:/opt/cloudwatch-config.json \
    -s

sudo systemctl enable webapi

sudo systemctl start webapi