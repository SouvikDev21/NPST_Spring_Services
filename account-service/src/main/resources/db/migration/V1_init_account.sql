CREATE TABLE accounts (
                          id                  BINARY(16) NOT NULL,
                          account_number      VARCHAR(30) NOT NULL,
                          cif_id              VARCHAR(20) NOT NULL,
                          account_type        VARCHAR(30) NOT NULL,
                          status              VARCHAR(20) NOT NULL,
                          balance             DECIMAL(19,2) NOT NULL,
                          available_balance   DECIMAL(19,2) NOT NULL,
                          currency            VARCHAR(3) NOT NULL,
                          branch_code         VARCHAR(20),
                          ifsc_code            VARCHAR(15),
                          opened_date         DATE,
                          version              BIGINT NOT NULL,
                          created_at          TIMESTAMP(6) NOT NULL,
                          updated_at          TIMESTAMP(6) NOT NULL,

                          CONSTRAINT pk_accounts PRIMARY KEY (id),
                          CONSTRAINT uk_accounts_account_number UNIQUE (account_number)
);

CREATE INDEX idx_account_cif_id
    ON accounts (cif_id);

CREATE INDEX idx_account_status
    ON accounts (status);