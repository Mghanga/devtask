package com.disbursement.app.controllers;

import com.disbursement.app.controllers.vm.IncomingRequest;
import com.disbursement.app.services.DisburseServiceInterface;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/disburse")
public class DisbursementController {

    @Autowired private DisburseServiceInterface disburseServiceInterface;

    @PostMapping
    public ResponseEntity<?> disburseToMpesa(@RequestBody @Valid IncomingRequest request) throws Exception{
        return disburseServiceInterface.disburseFunds( request );
    }

    @PostMapping("/callback")
    public ResponseEntity<?> mpesaCallbackResultsHandler(@RequestBody JsonNode node) throws Exception{
        log.info("---------------------Incoming B2C Result---------------------");
        log.info( node.toString() );
        log.info("---------------------Incoming B2C Result---------------------");
        Object response = disburseServiceInterface.processResultCallback( node );

        return ResponseEntity.ok( response );
    }

    @PostMapping("/time-out")
    public ResponseEntity<?> mpesaTimeoutHandler(@RequestBody JsonNode node) throws Exception{
        log.info("---------------------Incoming B2C Timeout result---------------------");
        log.info( node.toString() );
        log.info("---------------------Incoming B2C Timeout result---------------------");
        Object response = disburseServiceInterface.processTimeOut( node );
        return ResponseEntity.ok( response );
    }
}
