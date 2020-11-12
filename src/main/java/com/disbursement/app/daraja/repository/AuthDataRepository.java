package com.disbursement.app.daraja.repository;


import com.disbursement.app.daraja.entities.AuthData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthDataRepository extends CrudRepository<AuthData, Long> {

}
