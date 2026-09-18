package com.pranotivarpe.bankingsystem.service;

import com.pranotivarpe.bankingsystem.model.AccountType;
import com.pranotivarpe.bankingsystem.model.BankName;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateAccountRequest(
        @NotNull(message = "Bank must be selected") BankName bank,
        @NotBlank(message = "First name is required") String firstName,
        @NotBlank(message = "Last name is required") String lastName,
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past") LocalDate dob,
        @NotBlank(message = "Address is required") String address,
        @NotNull(message = "Contact number is required")
        @Digits(integer = 10, fraction = 0, message = "Contact number must be a valid 10-digit number") Long contactNum,
        @NotNull(message = "Account type must be selected") AccountType accountType,
        @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits") String pin
) {
}
