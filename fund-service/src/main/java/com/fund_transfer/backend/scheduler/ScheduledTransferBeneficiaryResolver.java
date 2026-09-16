package com.fund_transfer.backend.scheduler;

import com.fund_transfer.backend.entity.Beneficiary;
import com.fund_transfer.backend.entity.ScheduledTransfer;
import com.fund_transfer.backend.enums.BeneficiaryStatus;
import com.fund_transfer.backend.repository.BeneficiaryRepo;
import org.springframework.stereotype.Component;

/**
 * ScheduledTransfer only stores beneficiaryId — TransferRequest needs the
 * actual account number + IFSC. Re-resolved on every execution (not cached
 * at schedule-creation time) because the beneficiary can be blocked/deleted
 * or its IFSC re-resolved in between two scheduled runs.
 */
@Component
public class ScheduledTransferBeneficiaryResolver {

    private final BeneficiaryRepo beneficiaryRepo;

    public ScheduledTransferBeneficiaryResolver(BeneficiaryRepo beneficiaryRepo) {
        this.beneficiaryRepo = beneficiaryRepo;
    }

    public Beneficiary resolve(ScheduledTransfer scheduledTransfer) {

        Beneficiary beneficiary = beneficiaryRepo
                .findByIdAndOwnerCif(
                        scheduledTransfer.getBeneficiaryId(),
                        scheduledTransfer.getCif())
                .orElseThrow(() -> new IllegalStateException(
                        "Beneficiary " + scheduledTransfer.getBeneficiaryId()
                                + " no longer exists or is not owned by "
                                + scheduledTransfer.getCif()));

        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Beneficiary " + beneficiary.getId()
                            + " is " + beneficiary.getStatus()
                            + " — cannot execute scheduled transfer "
                            + scheduledTransfer.getId());
        }

        return beneficiary;
    }
}