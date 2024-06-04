package com.gfg.ewallet69.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gfg.ewallet69.transaction.service.resource.TransactionRequest;
import org.springframework.stereotype.Service;


public interface TransactionService {

    public boolean performTransaction(Long userId, TransactionRequest transactionRequest) ;
}
