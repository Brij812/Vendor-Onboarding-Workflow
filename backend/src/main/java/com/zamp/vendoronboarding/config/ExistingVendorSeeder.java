package com.zamp.vendoronboarding.config;

import com.zamp.vendoronboarding.service.ExistingVendorSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ExistingVendorSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ExistingVendorSeeder.class);

    private final ExistingVendorSeedService existingVendorSeedService;

    public ExistingVendorSeeder(ExistingVendorSeedService existingVendorSeedService) {
        this.existingVendorSeedService = existingVendorSeedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        long count = existingVendorSeedService.seedAll();
        if (count > 0) {
            log.info("Seeded {} existing vendors", count);
        } else {
            log.info("Existing vendors already seeded");
        }
    }
}
