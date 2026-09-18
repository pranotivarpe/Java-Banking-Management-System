package com.pranotivarpe.bankingsystem.service;

import java.math.BigDecimal;

public record InterestApplicationResult(int accountsCredited, BigDecimal totalInterestPaid) {
}
