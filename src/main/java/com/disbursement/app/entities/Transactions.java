package com.disbursement.app.entities;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "transactions")
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private BigDecimal amount;
    private String commandId;

    @Column( name = "party_a")
    private String partyA;

    @Column( name = "party_b")
    private String partyB;
    private String remarks;
    private String resultUrl;

    private String responseCode;
    private String conversationId;
    private String originatorConversationId;
    private String responseDescription;

    private String resultDesc;
    private String resultType;
    private String resultCode;
    private String transactionId;

    private String debitAccountBalance;
    private String debitAccountCurrentBalance;
    private String debitPartyCharges;
    private String debitPartyPublicName;
    private String initiatorAccountCurrentBalance;
    private String creditPartyPublicName;
    private String transCompletedTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date( System.currentTimeMillis() );

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt = new Date( System.currentTimeMillis() );
}
