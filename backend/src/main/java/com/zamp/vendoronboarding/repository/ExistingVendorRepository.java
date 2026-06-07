package com.zamp.vendoronboarding.repository;

import com.zamp.vendoronboarding.entity.ExistingVendor;
import com.zamp.vendoronboarding.entity.enums.VendorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExistingVendorRepository extends JpaRepository<ExistingVendor, UUID> {

    boolean existsByTaxId(String taxId);

    List<ExistingVendor> findAllByOrderByLegalNameAsc();

    Optional<ExistingVendor> findByTaxId(String taxId);

    List<ExistingVendor> findByBankAccountLast4(String bankAccountLast4);

    Optional<ExistingVendor> findByNormalizedLegalName(String normalizedLegalName);

    List<ExistingVendor> findByStatus(VendorStatus status);
}
