package com.disbursement.app.services;

import com.disbursement.app.controllers.vm.BaseResponse;
import com.disbursement.app.controllers.vm.IncomingRequest;
import com.disbursement.app.daraja.MpesaHttpService;
import com.disbursement.app.daraja.http.HttpService;
import com.disbursement.app.daraja.vm.B2cApiErrorResponse;
import com.disbursement.app.daraja.vm.B2cApiRequest;
import com.disbursement.app.entities.DarajaCredentials;
import com.disbursement.app.entities.Transactions;
import com.disbursement.app.repository.DarajaCredentialsRepository;
import com.disbursement.app.repository.TransactionsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class DisburseService implements DisburseServiceInterface {

    @Autowired private MpesaHttpService mpesaHttpService;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private HttpService httpService;
    @Autowired private TransactionsRepository transactionsRepository;

    @Value("${daraja.b2c.initiator}")
    private String initiator;

    @Value("${daraja.b2c.security_credentials}")
    private String b2cSecurityCredentials;

    @Value("${daraja.b2c.timeoutUrl}")
    private String timeoutUrl;

    @Value("${daraja.b2c.callBackUrl}")
    private String callBackUrl;

    /**
     * Process B2C Payment request
     *
     * @return Object
     */
    @Override
    public ResponseEntity<?> disburseFunds(IncomingRequest request ) throws Exception{
        Map<String, Object> map = new HashMap<>();
        boolean transactionFailed = false;
        BaseResponse baseResponse = new BaseResponse();

        IncomingRequest.TransactionInfo transactionInfo = request.getRequestPayload().getTransactionInfo();

        BigDecimal amount = transactionInfo.getTransactionAmount();
        String recipientMobileNo = transactionInfo.getCredintMobileNumber();

//      Prepare transaction entity
        B2cApiRequest apiRequest = new B2cApiRequest();
        apiRequest.setInitiatorName( initiator );
        apiRequest.setAmount( amount.toString() );
        apiRequest.setCommandId( B2cApiRequest.COMMAND_SALARY_PAYAMENT );
        apiRequest.setPartyA( initiator );
        apiRequest.setPartyB( recipientMobileNo );
        apiRequest.setRemarks( transactionInfo.getTransactionType() );
        apiRequest.setQueueTimeOutUrl( timeoutUrl );
        apiRequest.setResultUrl( callBackUrl );
        apiRequest.setSecurityCredential(  b2cSecurityCredentials );

        ResponseEntity<String> responseEntity = mpesaHttpService.sendOutboundRequest(apiRequest);

        String responseBody = responseEntity.getBody();
        JsonNode responseNode = objectMapper.readTree( responseBody );

        Transactions transaction = new Transactions();
        transaction.setAmount( amount );

        // Use salary payment option so that money is received by both registetred and unregistered M-Pesa customers
        transaction.setCommandId( B2cApiRequest.COMMAND_SALARY_PAYAMENT );
        transaction.setPartyA( "" );
        transaction.setPartyB( recipientMobileNo );
        transaction.setRemarks( "" );
        transaction.setResultUrl( "" );


        //When the transaction is successful
        if( responseEntity.getStatusCode().is2xxSuccessful() ){
            //Handle successful transaction
            log.info("B2C Transaction request was successful");
            String responseCode =  responseNode.get("ResponseCode").asText();
            String conversationId =  responseNode.get("ConversationID").asText();
            String originatorConversationId =  responseNode.get("OriginatorConversationID").asText();
            String description =  responseNode.get("ResponseDescription").asText();

            transaction.setResponseCode(responseCode);
            transaction.setConversationId(conversationId);
            transaction.setOriginatorConversationId(originatorConversationId);
            transaction.setResponseDescription(description);
        }

        //When the transaction failed
        else{
            transactionFailed = true;
            log.info("B2C Transaction request failed");
            transaction.setResponseCode(responseNode.get("errorCode").asText());
            transaction.setConversationId(responseNode.get("requestId").asText());
            transaction.setOriginatorConversationId(responseNode.get("requestId").asText());
            transaction.setResponseDescription(responseNode.get("errorMessage").asText());
        }

        //Save parent record
        transactionsRepository.save( transaction );

        BaseResponse.Header header = new BaseResponse.Header();
        header.setMessageID( request.getHeader().getMessageID() );
        header.setConversationID( transaction.getConversationId() );
        header.setStatusMessage( transaction.getResponseDescription() );
        header.setStatusCode( transaction.getResponseCode() );
        baseResponse.setHeader( header );

        return ResponseEntity.ok().body( baseResponse );
    }

    /**
     * Handle the result of previously submitted payment request
     *
     * @param apiResponseNode
     * @return JsonNode
     * @throws Exception
     */
    @Override
    public Map<String, Object> processResultCallback( JsonNode apiResponseNode ) throws Exception{
        Transactions transaction = null;
        String bStatus; // Callback status wrapper

        if ( apiResponseNode.has("errorCode") ) {
            log.error("B2C callback with error...");
            B2cApiErrorResponse errorResponse = objectMapper.readValue( apiResponseNode.toString(), B2cApiErrorResponse.class);
            //Response object has error
            String requestId =  errorResponse.requestId;
            String errorCode =  errorResponse.errorCode;
            bStatus = errorCode;
            Optional<Transactions> oTransaction = transactionsRepository.findByOriginatorConversationId(
                    requestId
            );

            //Ensure call back exists
            transaction = oTransaction.orElseGet( Transactions::new );

            transaction.setResultType(errorCode);
            transaction.setResultCode(errorCode);
            transaction.setResultDesc(errorCode);
            transaction.setOriginatorConversationId(requestId);
            transaction.setConversationId(requestId);
            transaction.setUpdatedAt(new Date());

        } else {
            JsonNode result = apiResponseNode.get("Result");
            bStatus = result.get("ResultCode").asText();
            String conversationId = result.get("ConversationID").asText();
            String originatorConversationId =  result.get("OriginatorConversationID").asText();
            Optional<Transactions> oTransaction = transactionsRepository.findByConversationIdAndOriginatorConversationId(
                    conversationId,
                    originatorConversationId
            );

            if ( oTransaction.isPresent() ) {
                transaction = oTransaction.get();

                log.info("Saving call back request.");
                transaction = processSuccessfulCallback(transaction, result);
            } else {

                // TODO: Why are saving an empty record to the DB?
                transaction = new Transactions();
            }

//          Save response
            transactionsRepository.save( transaction );
        }

        // Forward request to remote servers
        String resultType = transaction.getResultType();

        log.info("Sending request to remote application..");
        String resStatus = bStatus.equalsIgnoreCase("0") ? "00" : bStatus;
        String mpesaRefNo = StringUtils.isEmpty( transaction.getTransactionId() ) ? "": transaction.getTransactionId();

        // TODO: Suppose the request ended with an error?
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", resStatus);
        payload.put("message", transaction.getResultDesc());
        payload.put("transactionId", transaction.getConversationId());
        payload.put("referenceNo", transaction.getOriginatorConversationId());
        payload.put("mpesaRefNo", mpesaRefNo );

        BaseResponse baseResponse = new BaseResponse();
        BaseResponse.Header header = new BaseResponse.Header();
        header.setConversationID( transaction.getConversationId() );
        header.setStatusMessage( transaction.getResponseDescription() );
        header.setStatusCode( transaction.getResponseCode() );
        baseResponse.setHeader( header );
        ResponseEntity<String> responseEntity = httpService.postRequest(
                transaction.getResultUrl(),
                baseResponse
        );

        // TODO: Report the status of this transaction
        log.info("Response from remote server : " + responseEntity.getBody() );

        // Acknowledge receipt of request
        return new HashMap<String, Object>(){{
            put("ResultCode", 0);
            put("ResultDesc", "The service request is processed successfully.");
        }};

    }


    /**
     * Time-out handler
     *
     * @param req
     * @return
     * @throws Exception
     */
    @Override
    public Object processTimeOut(JsonNode req) throws Exception{
        //process b2C timeout request
        log.info("Queue Timeout Response: " + req.toString());
        return processResultCallback(req);
    }

    private Transactions processSuccessfulCallback(Transactions transaction, JsonNode jsonNode) {
        String resultDesc = jsonNode.get("ResultDesc").asText();
        String resultCode = jsonNode.get("ResultCode").asText();
        String resultType = jsonNode.get("ResultType").asText();
        String transactionId = jsonNode.get("TransactionID").asText();

        transaction.setResultDesc(resultDesc);
        transaction.setResultCode(resultCode);
        transaction.setResultType(resultType);
        transaction.setTransactionId(transactionId);

        if ( jsonNode.has("ResultParameters")) {

            JsonNode arrayNodes = jsonNode.get("ResultParameters").get("ResultParameter");
            int noOfItems = arrayNodes.size();

            for (int i = 0; i < noOfItems; i++) {

                JsonNode node = arrayNodes.get( i );
                String key = node.get("Key").asText();
                String value = node.get("Value").asText();

                switch ( key ) {
                    case "DebitAccountBalance":
                        transaction.setDebitAccountBalance( value );
                        break;
                    case "InitiatorAccountCurrentBalance":
                        transaction.setInitiatorAccountCurrentBalance( value );
                        break;
                    case "DebitAccountCurrentBalance":
                        transaction.setDebitAccountCurrentBalance( value );
                        break;
                    case "TransCompletedTime":
                        transaction.setTransCompletedTime( value );
                        break;
                    case "DebitPartyCharges":
                        transaction.setDebitPartyCharges( value );
                        break;
                    case "CreditPartyPublicName":
                        transaction.setCreditPartyPublicName( value );
                        break;
                    case "DebitPartyPublicName":
                        transaction.setDebitPartyPublicName( value );
                        break;
                    case "Currency":
                        break;
                }
            }
        }

        return transaction;
    }
}
