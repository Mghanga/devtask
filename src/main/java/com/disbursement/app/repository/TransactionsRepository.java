package com.disbursement.app.repository;

import com.disbursement.app.entities.Transactions;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionsRepository extends CrudRepository<Transactions, Long> {

    Optional<Transactions> findByOriginatorConversationId(String OriginatorConversationID);
    Optional<Transactions> findByConversationIdAndOriginatorConversationId(String conversationId, String OriginatorConversationId);
}
