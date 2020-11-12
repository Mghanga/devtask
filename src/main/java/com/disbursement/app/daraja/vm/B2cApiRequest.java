package com.disbursement.app.daraja.vm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class B2cApiRequest {

    public static final String COMMAND_SALARY_PAYAMENT = "SalaryPayment";

    @JsonProperty("InitiatorName")
    private String initiatorName;

    @JsonProperty("SecurityCredential")
    private String securityCredential;

    @JsonProperty("CommandID")
    private String commandId;

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("PartyA")
    private String partyA;

    @JsonProperty("PartyB")
    private String partyB;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("QueueTimeOutURL")
    private String queueTimeOutUrl;

    @JsonProperty("ResultURL")
    private String resultUrl;

    @JsonProperty("Occasion")
    private String occasion;

}
