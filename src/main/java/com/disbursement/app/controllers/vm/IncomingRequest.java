package com.disbursement.app.controllers.vm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class IncomingRequest {

    @NotNull
    private Header header;

    @NotNull
    private RequestPayload requestPayload;

    @Data
    public static class Header{
        @JsonProperty("messageID")
        private String messageID;

        private String featureCode;
        private String featureName;
        private String serviceCode;
        private String serviceName;
        private String serviceSubCategory;
        private String minorServiceVersion;
        private String channelCode;
        private String channelName;
        private String routeCode;
        private String timeStamp;
        private String serviceMode;
        private String subscribeEvents;

        @JsonProperty("callBackURL")
        private String callBackURL;
    }

    @Data
    public static class RequestPayload{

        @NotNull
        private TransactionInfo transactionInfo;
    }

    @Data
    public static class TransactionInfo{

        @NotNull
        private String companyCode;

        @NotNull
        private String transactionType;
        private String creditAccountNumber;

        @NotNull
        private String credintMobileNumber;

        @NotNull
        @Size( min = 10)
        private BigDecimal transactionAmount;
        private String transactionReference;
        private String currencyCode;
        private String amountCurrency;
        private String dateTime;
        private String dateString;
    }
}
