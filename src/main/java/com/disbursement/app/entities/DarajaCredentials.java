package com.disbursement.app.entities;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table( name = "daraja_credentials")
public class DarajaCredentials implements Serializable {

    @Id
    private Long id;
}
