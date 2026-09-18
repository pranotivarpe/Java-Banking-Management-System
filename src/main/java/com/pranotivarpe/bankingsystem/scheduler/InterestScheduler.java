package com.pranotivarpe.bankingsystem.scheduler;

import com.pranotivarpe.bankingsystem.service.AccountService;
import com.pranotivarpe.bankingsystem.service.InterestApplicationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InterestScheduler {

    private static final Logger log = LoggerFactory.getLogger(InterestScheduler.class);

    private final AccountService accountService;

    public InterestScheduler(AccountService accountService) {
        this.accountService = accountService;
    }

    // Midnight on the 1st of every month. The admin console menu triggers the same operation on demand,
    // since a console app has no easy way to demo a real cron firing during a manual walkthrough.
    @Scheduled(cron = "0 0 0 1 * *")
    public void applyMonthlyInterest() {
        InterestApplicationResult result = accountService.applyMonthlyInterestToSavingsAccounts();
        log.info("Scheduled interest run: credited {} account(s), total {}",
                result.accountsCredited(), result.totalInterestPaid());
    }
}
