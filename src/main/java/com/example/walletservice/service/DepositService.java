package com.example.walletservice.service;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.DepositStatusEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.mapper.DepositMapper;
import com.example.walletservice.repository.DepositRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class DepositService {

    static final BigDecimal EARLY_CLOSE_RATE = new BigDecimal("2.00");

    private final DepositRepository depositRepository;
    private final WalletRepository walletRepository;
    private final DepositMapper depositMapper;

    @Transactional
    public DepositResponse createDeposit(DepositRequest request){
        BigDecimal rate = interestRateFor(request.termMonths());
        Wallet wallet = walletRepository.lockOrThrow(request.walletId());
        SecurityUtils.checkOwner(wallet.getUser());
        wallet.debit(request.amount());

        Deposit deposit = depositMapper.toEntity(request);
        deposit.setWallet(wallet);
        deposit.setInterestRate(rate);
        deposit.setStartDate(LocalDate.now());
        deposit.setEndDate(deposit.getStartDate().plusMonths(request.termMonths()));
        deposit.setStatus(DepositStatusEnum.ACTIVE);
        return depositMapper.toResponse(depositRepository.save(deposit));
    }

    @Transactional(readOnly = true)
    public Page<DepositResponse> getDeposits(Long walletId, Pageable pageable){
        SecurityUtils.checkOwner(walletRepository.getOrThrow(walletId).getUser());
        return depositRepository.findByWalletId(walletId, pageable)
                .map(depositMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public DepositResponse getDeposit(Long depositId){
        Deposit deposit = depositRepository.getOrThrow(depositId);
        SecurityUtils.checkOwner(deposit.getWallet().getUser());
        return depositMapper.toResponse(deposit);
    }

    /**
     * Before the end date the deposit is closed early at {@link #EARLY_CLOSE_RATE};
     * on or after it, it completes at the agreed rate.
     */
    @Transactional
    public DepositResponse closeDeposit(Long depositId){
        // lock the deposit first: two concurrent "close" calls must not both pay out
        Deposit deposit = depositRepository.lockOrThrow(depositId);
        Wallet wallet = walletRepository.lockOrThrow(deposit.getWallet().getId());
        SecurityUtils.checkOwner(wallet.getUser());
        if (deposit.getStatus() != DepositStatusEnum.ACTIVE) {
            throw new BadRequestException("Deposit already " + deposit.getStatus().name().toLowerCase());
        }

        LocalDate today = LocalDate.now();
        boolean early = today.isBefore(deposit.getEndDate());
        if (early) {
            deposit.setInterestRate(EARLY_CLOSE_RATE);
        }
        long days = ChronoUnit.DAYS.between(deposit.getStartDate(), today);
        BigDecimal interest = calculateInterest(deposit.getAmount(), deposit.getInterestRate(), days);

        wallet.credit(deposit.getAmount().add(interest));
        deposit.setStatus(early ? DepositStatusEnum.CLOSED : DepositStatusEnum.COMPLETED);
        return depositMapper.toResponse(deposit);
    }


    static BigDecimal calculateInterest(BigDecimal amount, BigDecimal annualRatePercent, long days) {
        return amount
                .multiply(annualRatePercent)
                .multiply(BigDecimal.valueOf(days))
                .divide(BigDecimal.valueOf(36500), 2, RoundingMode.HALF_UP);
    }

    static BigDecimal interestRateFor(int termMonths){
        if (termMonths <= 0 || termMonths > 24) {
            throw new BadRequestException("Deposit term must be between 1 and 24 months");
        }
        if (termMonths <= 5) {
            return new BigDecimal("8.00");
        }
        if (termMonths <= 11) {
            return new BigDecimal("10.00");
        }
        if (termMonths <= 17) {
            return new BigDecimal("15.00");
        }
        return new BigDecimal("20.00");
    }
}
