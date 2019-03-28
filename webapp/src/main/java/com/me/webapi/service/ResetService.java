package com.me.webapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.me.webapi.pojo.Reset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class ResetService {

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    private SnsClient snsClient;

    @PostConstruct
    public void init() {
        snsClient = SnsClient.builder().build();
    }

    public void publish(Reset reset){
        reset.setToken(UUID.randomUUID().toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(reset);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        snsClient.publish(PublishRequest.builder().topicArn(topicArn).message(message).build());
    }
}
