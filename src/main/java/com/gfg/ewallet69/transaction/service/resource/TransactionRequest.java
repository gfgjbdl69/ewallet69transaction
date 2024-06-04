package com.gfg.ewallet69.transaction.service.resource;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TransactionRequest {

    private Long receiverId;
    private Double amount;
    private String description;
    private String transactionType;

}
