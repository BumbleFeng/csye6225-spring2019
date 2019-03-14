#!/bin/bash

sudo systemctl stop webapi

sudo mv -f var/webapi.jar /var/webapi/

sudo chown webapi:webapi /var/webapi/webapi.jar

sudo chmod 500 /var/webapi/webapi.jar

sudo systemctl enable webapi

sudo systemctl start webapi