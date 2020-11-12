package com.disbursement.app.services;

import com.disbursement.app.controllers.vm.IncomingRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;

public interface DisburseServiceInterface {

    /**
     * Handle incoming payment calls
     *
     * @param request
     * @return
     */
    public ResponseEntity<?> disburseFunds(IncomingRequest request) throws Exception;

    /**
     * Handle the result of previously submitted payment request
     *
     * @param jsonNode
     * @return JsonNode
     * @throws Exception
     */
    public Object processResultCallback( JsonNode jsonNode) throws Exception;

    /**
     * Time-out handler
     *
     * @param node
     * @return
     * @throws Exception
     */
    public Object processTimeOut(JsonNode node) throws Exception;
}
