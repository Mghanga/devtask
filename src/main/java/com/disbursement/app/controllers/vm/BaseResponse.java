package com.disbursement.app.controllers.vm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BaseResponse {

    private Header header;
    private ResponsePayload responsePayload;

    @Data
    public static class Header{

        @JsonProperty("messageID")
        private String messageID;

        @JsonProperty("conversationID")
        private String conversationID;

        @JsonProperty("targetSystemID")
        private String targetSystemID;

        @JsonProperty("routeCode")
        private String routeCode;

        @JsonProperty("statusCode")
        private String statusCode;

        @JsonProperty("statusDescription")
        private String statusDescription;

        @JsonProperty("statusMessage")
        private String statusMessage;
    }

    @Data
    public static class ResponsePayload{
        private TransactionInfo transactionInfo;
    }

    @Data
    public static class TransactionInfo{
        @JsonProperty("transactionId")
        private String transactionId;
        @JsonProperty("falconBalance")
        private String falconBalance;
    }

}
