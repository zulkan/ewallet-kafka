package com.zulkan.ewallet.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zulkan.ewallet.dto.message.BalanceTopupMessage;
import com.zulkan.ewallet.dto.message.TransferMessage;
import com.zulkan.ewallet.service.TransactionInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionConsumer {

    private final TransactionInterface transactionService;

    private final ObjectMapper objectMapper;

    public TransactionConsumer(TransactionInterface transactionService, ObjectMapper objectMapper) {
        this.transactionService = transactionService;
        this.objectMapper = objectMapper;
    }


    @KafkaListener(topics = "topup", groupId = "group_id")
    private void topupConsumer(String message) throws JsonProcessingException {
        log.info("Processing Topup message: " + message);
        var topupMessage = objectMapper.readValue(message, BalanceTopupMessage.class);
        transactionService.balanceTopup(topupMessage.getUserId(), topupMessage.getAmount());
        log.info("Finished Processing Topup message: " + message);
    }

    @KafkaListener(topics = "transfer")
    private void transferConsumer(String message) throws JsonProcessingException {
        log.info("Processing Transfer message: " + message);
        var transferMessage = objectMapper.readValue(message, TransferMessage.class);
        transactionService.transfer(transferMessage);
        log.info("Finished Processing Transfer message: " + message);
    }
}
