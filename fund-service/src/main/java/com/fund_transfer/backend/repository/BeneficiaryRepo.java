package com.fund_transfer.backend.repository;

import java.util.List;
import java.util.Optional;

import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiaryRepo extends JpaRepository<Beneficiary, Long> {

    // Excludes soft-deleted rows — this is the one the service layer's "list" should call.
    List<Beneficiary> findByOwnerCifAndStatusNot(String ownerCif, BeneficiaryStatus excludedStatus);

    // Ownership-scoped single fetch — required before any get/rename/delete/block acts on a
    // beneficiary, so a customer can never touch a beneficiary that isn't theirs (IDOR guard).
    Optional<Beneficiary> findByIdAndOwnerCif(Long id, String ownerCif);

    boolean existsByOwnerCifAndBeneficiaryAccountNumberAndBeneficiaryIfscCode(
            String ownerCif, String beneficiaryAccountNumber, String beneficiaryIfscCode);
}
