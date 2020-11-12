drop table if exists auth_data;
create table auth_data
(
    id                 int       not null auto_increment,
    oauth_access_token text,
    time_out           int            default null,
    last_updated       timestamp null default current_timestamp,
    PRIMARY KEY (id)
)
    engine = InnoDB
    default charset = utf8;

drop table if exists transactions;
create table transactions
(
    id                                int       not null auto_increment,

    transaction_id                    varchar(50)    default null,
    debit_account_balance             varchar(150)   default null,
    debit_account_current_balance     varchar(150)   default null,
    debit_party_charges               varchar(150)   default null,
    debit_party_public_name           varchar(150)   default null,
    originator_conversation_id        varchar(50)    default null,
    initiator_account_current_balance varchar(150)   default null,
    credit_party_public_name          varchar(150)   default null,
    command_id                        varchar(60)    default null,
    conversation_id                   varchar(70)    default null,
    duplicate_count                   int(11)        default null,
    party_a                           varchar(20)    default null,
    party_b                           varchar(20)    default null,
    result_code                       varchar(50)    default null,
    result_desc                       varchar(150)   default null,
    result_type                       varchar(50)    default null,
    result_url                        varchar(250)   default null,
    response_code                     varchar(50)    default null,
    response_description              varchar(150)   default null,
    receiver_id                       varchar(12)    default null,
    receiver_party_charges            varchar(150)   default null,
    amount                            varchar(10)    default null,
    remarks                           varchar(200)   default null,
    trans_completed_time              varchar(150)   default null,

    created_at                        timestamp null default current_timestamp,
    updated_at                        timestamp null default current_timestamp,

    primary key (id)
) engine = InnoDB
  default charset = utf8;