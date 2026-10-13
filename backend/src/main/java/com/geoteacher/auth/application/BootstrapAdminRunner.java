package com.geoteacher.auth.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final AccountService accountService;
    private final BootstrapAdminProperties properties;

    public BootstrapAdminRunner(AccountService accountService, BootstrapAdminProperties properties) {
        this.accountService = accountService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        accountService.createInitialAccount(properties)
                .ifPresent(account -> log.info(
                        "Created the initial GeoTeacher account with username '{}'.",
                        account.getUsername()
                ));
    }
}
