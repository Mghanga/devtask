package com.disbursement.app.repository;

import com.disbursement.app.entities.DarajaCredentials;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DarajaCredentialsRepository extends CrudRepository<DarajaCredentials, Long> {
}
