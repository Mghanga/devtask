package com.disbursement.app.daraja.entities;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@Entity
@Table(name = "auth_data")
public class AuthData implements java.io.Serializable {

    private static final Long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "oauth_access_token")
    private String oauthAccessToken;

    @Basic(optional = false)
    @NotNull
    @Column(name = "last_updated", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "time_out")
    private Long safaricomTimeout;


}
