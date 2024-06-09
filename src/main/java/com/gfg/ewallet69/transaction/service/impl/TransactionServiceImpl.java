package com.gfg.ewallet69.transaction.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gfg.ewallet69.transaction.service.TransactionService;
import com.gfg.ewallet69.transaction.service.resource.NotificationRequest;
import com.gfg.ewallet69.transaction.service.resource.TransactionRequest;
import com.gfg.ewallet69.transaction.service.resource.WalletTransactionRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Service
public class TransactionServiceImpl  implements TransactionService {

    @Autowired
    RestTemplate template;

    public ObjectMapper mapper=new ObjectMapper();

    @Autowired
    KafkaTemplate<String,String> kafkaTemplate;

    @Override
    public boolean performTransaction(Long userId, TransactionRequest transactionRequest) {
        try {
            WalletTransactionRequest walletTransactionRequest = new WalletTransactionRequest();

            walletTransactionRequest.setSenderId(userId);
            walletTransactionRequest.setReceiverId(transactionRequest.getReceiverId());
            walletTransactionRequest.setAmount(transactionRequest.getAmount());
            walletTransactionRequest.setDescription(transactionRequest.getDescription());
            walletTransactionRequest.setTransactionType(transactionRequest.getTransactionType());


            String url = "http://localhost:8082/wallet/transaction";
            ResponseEntity<Boolean> response = template.postForEntity(url, walletTransactionRequest, Boolean.class);
            String content = Strings.EMPTY;
            if (response.getStatusCode().is2xxSuccessful()) {
                NotificationRequest senderNotificationRequest = new NotificationRequest();
                senderNotificationRequest.setTransactionStatus("SUCCESS");
                senderNotificationRequest.setAmount(transactionRequest.getAmount());
                senderNotificationRequest.setUserId(userId);
                senderNotificationRequest.setUserType("SENDER");
                content = mapper.writeValueAsString(senderNotificationRequest);
                kafkaTemplate.send("notification-topic", content);

                NotificationRequest reciverNotificationRequest = new NotificationRequest();
                reciverNotificationRequest.setTransactionStatus("SUCCESS");
                reciverNotificationRequest.setAmount(transactionRequest.getAmount());
                reciverNotificationRequest.setUserId(transactionRequest.getReceiverId());
                reciverNotificationRequest.setUserType("RECEIVER");
                content = mapper.writeValueAsString(reciverNotificationRequest);
                kafkaTemplate.send("notification-topic", content);
            } else {

                NotificationRequest senderNotificationRequest = new NotificationRequest();
                senderNotificationRequest.setTransactionStatus("FAILURE");
                senderNotificationRequest.setAmount(transactionRequest.getAmount());
                senderNotificationRequest.setUserId(userId);
                senderNotificationRequest.setUserType("SENDER");
                content = mapper.writeValueAsString(senderNotificationRequest);
                kafkaTemplate.send("notification-topic", content);
            }
            return response.getStatusCode().is2xxSuccessful();
        }catch (JsonProcessingException jx){

            return false;
        }
    }
}
