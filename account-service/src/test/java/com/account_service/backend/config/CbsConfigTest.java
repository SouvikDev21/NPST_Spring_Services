package com.account_service.backend.config;

import com.common.cbs.CbsAdapter;
import com.common.cbs.CbsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CbsConfigTest {

    @Autowired
    private CbsProperties cbsProperties;

    @Autowired
    private CbsAdapter cbsAdapter;

    @Autowired
    private Environment environment;

    @Test
    void testCbsPropertiesBinding() {
        assertThat(cbsProperties).isNotNull();
        assertThat(cbsAdapter).isNotNull();
        assertThat(cbsProperties.getChannelId()).isEqualTo("INTERNET_BANKING");
        assertThat(cbsProperties.getUserId()).isEqualTo("APIUSER");
        assertThat(cbsProperties.getBranchCode()).isEqualTo("001");
    }

    @Test
    void testUriResolutionWithSubpath() {
        String baseUrl = com.common.cbs.CbsAdapterImpl.normalizeBaseUrl("http://103.209.145.243:9101/mock/cbs/");
        assertThat(baseUrl).isEqualTo("http://103.209.145.243:9101/cbs/v3");

        String path = com.common.cbs.CbsAdapterImpl.resolvePath(com.common.cbs.CbsConstants.PATH_V3_CUSTOMER_ACCOUNTS_INQUIRY);
        assertThat(path).isEqualTo("/customers/accounts/inquiry");

        org.springframework.web.util.DefaultUriBuilderFactory factory =
                new org.springframework.web.util.DefaultUriBuilderFactory(baseUrl);
        java.net.URI uri = factory.uriString(path).build();
        assertThat(uri.toString()).isEqualTo("http://103.209.145.243:9101/cbs/v3/customers/accounts/inquiry");
    }

    @Test
    void testLiveCbsCustomerInquiry() {
        java.util.Optional<com.common.cbs.dto.CbsCustomerInquiryResponse> response =
                cbsAdapter.getCustomerAccounts("CIF100001");
        assertThat(response).isPresent();
        com.common.cbs.dto.CbsCustomerInquiryResponse data = response.get();
        assertThat(data.getCustomer()).isNotNull();
        assertThat(data.getCustomer().getCustomerId()).isEqualTo("CIF100001");
        assertThat(data.getCustomer().getCustomerName()).isEqualTo("John Doe");
        assertThat(data.getAccountList()).isNotEmpty();
        assertThat(data.getAccountList().get(0).getAccountNumber()).isEqualTo("101000000001");
    }

    @Test
    void testLiveCbsAccountDetails() {
        java.util.Optional<com.common.cbs.dto.CbsAccountDetailsResponse> response =
                cbsAdapter.getAccountDetails("101000000001");
        assertThat(response).isPresent();
        com.common.cbs.dto.CbsAccountDetailsResponse data = response.get();
        assertThat(data.getAccount()).isNotNull();
        assertThat(data.getAccount().getAccountNumber()).isEqualTo("101000000001");
        assertThat(data.getAccount().getProductName()).isEqualTo("Premium Savings Account");
    }

    @Test
    void testLiveCbsAccountStatements() {
        java.util.Optional<com.common.cbs.dto.CbsStatementResponse> response =
                cbsAdapter.getAccountStatements("101000000001", "2026-08-01", "2026-09-04", 0, 10);
        assertThat(response).isPresent();
        com.common.cbs.dto.CbsStatementResponse data = response.get();
        assertThat(data.getAccountId()).isEqualTo("101000000001");
        assertThat(data.getTransactionList()).isNotEmpty();
        assertThat(data.getTransactionList().get(0).getTransactionId()).startsWith("TXN");
    }

    @Test
    void testLiveCbsCardInquiry() {
        java.util.Optional<com.common.cbs.dto.CbsCardInquiryResponse> response =
                cbsAdapter.getLinkedCards("101000000001");
        assertThat(response).isPresent();
        com.common.cbs.dto.CbsCardInquiryResponse data = response.get();
        assertThat(data.getCardList()).isNotEmpty();
        assertThat(data.getCardList().get(0).getCardNumber()).isEqualTo("XXXXXX1234");
    }
}
