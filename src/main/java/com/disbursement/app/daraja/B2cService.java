//package com.disbursement.app.daraja;
//
//import com.disbursement.app.daraja.http.HttpService;
//import com.disbursement.app.daraja.vm.B2cApiErrorResponse;
//import com.disbursement.app.daraja.vm.B2cApiRequest;
//import com.disbursement.app.entities.DarajaCredentials;
//import com.disbursement.app.entities.Transactions;
//import com.disbursement.app.repository.DarajaCredentialsRepository;
//import com.disbursement.app.repository.TransactionsRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//@Service
//@Transactional
//public class B2cService implements B2cServiceInterface {
//
//    private static final Logger LOGGER = LoggerFactory.getLogger(B2cService.class);
//
//    @Value("${daraja.b2c.iniator}")
//    private String initiatorShortCode;
//
//    @Value("${daraja.b2c.requestUrl}")
//    private String requestUrl;
//
//    @Value("${daraja.b2c.callBackUrl}")
//    private String callbackURL;
//
//    @Value("${daraja.b2c.timeoutUrl}")
//    private String queueTimeoutURL;
//
//    @Value("${daraja.b2c.SecurityCredential}")
//    private String b2cSecurityCredentials;
//
//    @Autowired private DarajaCredentialsRepository darajaCredentialsRepository;
//    @Autowired private MpesaHttpService mpesaHttpService;
//    @Autowired private ObjectMapper objectMapper;
//
//
//    @Autowired private HttpService httpService;
//    @Autowired private TransactionsRepository transactionsRepository;
//
//    /**
//     * Process B2C Payment request
//     *
//     * @param clientRequest
//     * @return Object
//     */
//    public Map<String, Object> processPayment(BigDecimal amount, String recipientMobileNo ) throws Exception{
//        Map<String, Object> map = new HashMap<>();
//
////      Prepare transaction entity
//        B2cApiRequest apiRequest = new B2cApiRequest(
//                null,
//                queueTimeoutURL,
//                callbackURL
//        );
//
//        //Update request with security credentials
//        Optional<DarajaCredentials> oCredentials = darajaCredentialsRepository.findByIdentifier( initiatorShortCode );
//
//        //Generate security Credential
//        if ( oCredentials.isPresent() ) {
//            DarajaCredentials credentials = oCredentials.get();
//            apiRequest
//                    .setInitiatorName( credentials.getInitiator() )
//                    .setSecurityCredential( b2cSecurityCredentials )
//            ;
//        }
//        else {
//            map.put("status", "01");
//            map.put("message", "No Security Credential configured");
//            return map;
//        }
//
//        ResponseEntity<String> responseEntity = mpesaHttpService.sendOutboundRequest(
//                requestUrl,
//                apiRequest
//        );
//
//        String responseBody = responseEntity.getBody();
//        System.err.println( responseBody );
//        JsonNode responseNode = objectMapper.readTree( responseBody );
//
//        Transactions transaction = new Transactions();
//        transaction.setAmount( amount );
//        transaction.setCommandId(clientRequest.getTransactionType().name());
//        transaction.setPartyA(clientRequest.getSource());
//        transaction.setPartyB(clientRequest.getRecipientPhoneNo());
//        transaction.setRemarks(clientRequest.getDescription());
//        transaction.setResultUrl(clientRequest.getResultUrl());
//
//
//        //When the transaction is successful
//        if( responseEntity.getStatusCode().is2xxSuccessful() ){
//            //Handle successful transaction
//            LOGGER.info("B2C Transaction request was successful");
//            String responseCode =  responseNode.get("ResponseCode").asText();
//            String conversationId =  responseNode.get("ConversationID").asText();
//            String originatorConversationId =  responseNode.get("OriginatorConversationID").asText();
//            String description =  responseNode.get("ResponseDescription").asText();
//
//            transaction.setResponseCode(responseCode);
//            transaction.setConversationId(conversationId);
//            transaction.setOriginatorConversationId(originatorConversationId);
//            transaction.setResponseDescription(description);
//        }
//
//        //When the transaction failed
//        else{
//            LOGGER.info("B2C Transaction request failed");
//            transaction.setResponseCode(responseNode.get("errorCode").asText());
//            transaction.setConversationId(responseNode.get("requestId").asText());
//            transaction.setOriginatorConversationId(responseNode.get("requestId").asText());
//            transaction.setResponseDescription(responseNode.get("errorMessage").asText());
//
//        }
//
//        //Save parent record
//        transactionsRepository.save( transaction );
//
//        String respCode = transaction.getResponseCode();
//
//        //TODO: What if the transaction failed?
//        map.put("status", respCode.equalsIgnoreCase("0") ? "00" : respCode);
//        map.put("message", transaction.getResponseDescription());
//        map.put("transactionId", transaction.getConversationId() );
//        map.put("referenceNo",  transaction.getOriginatorConversationId() );
//        return map;
//    }
//
//    /**
//     * Handle the result of previously submitted payment request
//     *
//     * @param apiResponseNode
//     * @return JsonNode
//     * @throws Exception
//     */
//    @Override
//    public Map<String, Object> processResultCallback( JsonNode apiResponseNode ) throws Exception{
//        boolean isDuplicateConfirmation = false;
//        LOGGER.info("Call back Response: " + apiResponseNode.toString());
//        Transactions transaction = null;
//        String bStatus; // Callback status wrapper
//
//        if ( apiResponseNode.has("errorCode") ) {
//            LOGGER.error("B2C callback with error...");
//            B2cApiErrorResponse errorResponse = objectMapper.readValue( apiResponseNode.toString(), B2cApiErrorResponse.class);
//            //Response object has error
//            String requestId =  errorResponse.requestId;
//            String errorCode =  errorResponse.errorCode;
//            bStatus = errorCode;
//            Optional<Transactions> oTransaction = transactionsRepository.findByOriginatorConversationId(
//                    requestId
//            );
//
//            //Ensure call back exists
//            transaction = oTransaction.orElseGet( Transactions::new );
//
//            transaction.setResultType(errorCode);
//            transaction.setResultCode(errorCode);
//            transaction.setResultDesc(errorCode);
//            transaction.setOriginatorConversationId(requestId);
//            transaction.setConversationId(requestId);
//            transaction.setUpdatedAt(new Date());
//
//        } else {
//            JsonNode result = apiResponseNode.get("Result");
//            bStatus = result.get("ResultCode").asText();
//            String conversationId = result.get("ConversationID").asText();
//            String originatorConversationId =  result.get("OriginatorConversationID").asText();
//            Optional<Transactions> oTransaction = transactionsRepository.findByConversationIdAndOriginatorConversationId(
//                    conversationId,
//                    originatorConversationId
//            );
//
//            if ( oTransaction.isPresent() ) {
//                transaction = oTransaction.get();
//                String resultType = transaction.getResultType();
//                if ( !StringUtils.isEmpty( resultType ) ) {
//
//                    if ( resultType.equals("0")) {
//                        isDuplicateConfirmation = true;
//                        int count = transaction.getDuplicateCount();
//                        count++;
//                        transaction.setDuplicateCount( count );
//                    }
//                }
//
//
//
//                LOGGER.info("Saving call back request.");
//                transaction = processSuccessfulCallback(transaction, result);
//            } else {
//
//                // TODO: Why are saving an empty record to the DB?
//                transaction = new Transactions();
//            }
//
////          Save response
//            transactionsRepository.save( transaction );
//        }
//
//        if (!isDuplicateConfirmation) {
//            // Forward request to remote servers
//            String resultType = transaction.getResultType();
//
//            LOGGER.info("Sending request to remote application..");
//            String resStatus = bStatus.equalsIgnoreCase("0") ? "00" : bStatus;
//            String mpesaRefNo = StringUtils.isEmpty( transaction.getTransactionId() ) ? "": transaction.getTransactionId();
//
//            // TODO: Suppose the request ended with an error?
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("status", resStatus);
//            payload.put("message", transaction.getResultDesc());
//            payload.put("transactionId", transaction.getConversationId());
//            payload.put("referenceNo", transaction.getOriginatorConversationId());
//            payload.put("mpesaRefNo", mpesaRefNo );
//
//            ResponseEntity<String> responseEntity = httpService.postRequest(
//                    transaction.getResultUrl(),
//                    payload
//            );
//
//            // TODO: Report the status of this transaction
//
//            LOGGER.info("Response from remote server : " + responseEntity.getBody() );
//        }
//
//        // Acknowledge receipt of request
//        return new HashMap<String, Object>(){{
//            put("ResultCode", 0);
//            put("ResultDesc", "The service request is processed successfully.");
//        }};
//
//    }
//
//    /**
//     * Transaction timeout result end point.
//     * Safaricom queue may grow and fail to process request in their queue.
//     *
//     * @param req Safaricom request pay load
//     * @return
//     */
//    @Override
//    public Object processTimeOut(JsonNode req) throws Exception{
//        //process b2C timeout request
//        LOGGER.info("Queue Timeout Response: " + req.toString());
//        return processResultCallback(req);
//    }
//
//    private Transactions processSuccessfulCallback(Transactions transaction, JsonNode jsonNode) {
//        String resultDesc = jsonNode.get("ResultDesc").asText();
//        String resultCode = jsonNode.get("ResultCode").asText();
//        String resultType = jsonNode.get("ResultType").asText();
//        String transactionId = jsonNode.get("TransactionID").asText();
//
//        transaction.setResultDesc(resultDesc);
//        transaction.setResultCode(resultCode);
//        transaction.setResultType(resultType);
//        transaction.setTransactionId(transactionId);
//
//        if ( jsonNode.has("ResultParameters")) {
//
//            JsonNode arrayNodes = jsonNode.get("ResultParameters").get("ResultParameter");
//            int noOfItems = arrayNodes.size();
//
//            for (int i = 0; i < noOfItems; i++) {
//
//                JsonNode node = arrayNodes.get( i );
//                String key = node.get("Key").asText();
//                String value = node.get("Value").asText();
//
//                switch ( key ) {
//                    case "DebitAccountBalance":
//                        transaction.setDebitAccountBalance( value );
//                        break;
//                    case "InitiatorAccountCurrentBalance":
//                        transaction.setInitiatorAccountCurrentBalance( value );
//                        break;
//                    case "DebitAccountCurrentBalance":
//                        transaction.setDebitAccountCurrentBalance( value );
//                        break;
//                    case "TransCompletedTime":
//                        transaction.setTransCompletedTime( value );
//                        break;
//                    case "DebitPartyCharges":
//                        transaction.setDebitPartyCharges( value );
//                        break;
//                    case "CreditPartyPublicName":
//                        transaction.setCreditPartyPublicName( value );
//                        break;
//                    case "DebitPartyPublicName":
//                        transaction.setDebitPartyPublicName( value );
//                        break;
//                    case "Currency":
//                        break;
//                }
//            }
//        }
//
//        return transaction;
//    }
//}
