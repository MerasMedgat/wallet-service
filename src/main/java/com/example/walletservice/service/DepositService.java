package com.example.walletservice.service;


import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.DepositStatusEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.*;
import com.example.walletservice.mapper.DepositMapper;
import com.example.walletservice.repository.DepositRepository;
import com.example.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositMapper depositMapper;
    private final WalletRepository walletRepository;

    @Transactional
    public DepositResponse createDeposit(DepositRequest depositRequest){
        Wallet wallet = walletRepository.findById(depositRequest.getWalletId())
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        if(wallet.getStatus() != WalletStatusEnum.ACTIVE){
            throw new WalletNotActiveException("Wallet not active");
        }
        if((wallet.getBalance().compareTo(depositRequest.getAmount()) <0)){
            throw new InsufficientBalanceException("Insufficient balance");
        }
        wallet.setBalance((wallet.getBalance().subtract(depositRequest.getAmount())));
        walletRepository.save(wallet);
        Deposit deposit = depositMapper.toEntity(depositRequest);
        deposit.setWallet(wallet);
        deposit.setInterestRate(calculateInterestRate(depositRequest.getTermMonths()));
        deposit.setStartDate(LocalDate.now());
        deposit.setEndDate(deposit.getStartDate().plusMonths(depositRequest.getTermMonths()));
        deposit.setStatus(DepositStatusEnum.ACTIVE);
        Deposit savedDeposit =  depositRepository.save(deposit);
        return depositMapper.toResponse(savedDeposit);

    }

    private BigDecimal calculateInterestRate(Integer termMonths){
        if(termMonths <= 0 ) {
            throw new InvalidDepositTermException("Invalid deposit term");
        }
        if (termMonths <= 5){
            return BigDecimal.valueOf(8.00);
        }
        else if(termMonths <= 11){
            return BigDecimal.valueOf(10.00);
        }
        else if(termMonths <= 17){
            return BigDecimal.valueOf(15.00);
        }
        else if(termMonths <= 24){
            return BigDecimal.valueOf(20.00);
        }
        else{throw new InvalidDepositTermException("Invalid deposit term");}

    }

    public List<DepositResponse> getAllDeposits(Long walletId){
        walletRepository.findById(walletId).orElseThrow(()->
                new WalletNotFoundException("Wallet not found"));
        List<DepositResponse> deposits = depositRepository.findAllDepositsByWalletId(walletId)
                .stream()
                .map(depositMapper::toResponse)
                .toList();
        return deposits;
    }


    public DepositResponse getDeposit(Long depositId){
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(()->
                        new DepositNotFoundException("Deposit not found"));
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public DepositResponse closeDeposit(Long depositId){
        Deposit deposit = depositRepository.findById(depositId)
                .orElseThrow(()->
                        new DepositNotFoundException("Deposit not found"));
        if(deposit.getStatus() == DepositStatusEnum.CLOSED){
            throw new DepositAlreadyClosedException("Deposit already closed");
        }
        else if(deposit.getStatus() == DepositStatusEnum.COMPLETED){
            throw new DepositAlreadyCompletedException("Deposit already completed");
        }
        if(LocalDate.now().isBefore(deposit.getEndDate())){
            deposit.setInterestRate(BigDecimal.valueOf(2.00));
        }
        long days = ChronoUnit.DAYS.between(deposit.getStartDate(),LocalDate.now());
        BigDecimal interest = deposit.getAmount()
                .multiply((deposit.getInterestRate()
                .divide(BigDecimal.valueOf(100))
                .multiply((BigDecimal.valueOf(days)
                        .divide(BigDecimal.valueOf(365),RoundingMode.HALF_UP)))));
        BigDecimal amountAfterDeposit = deposit.getAmount().add(interest);
        Wallet wallet = deposit.getWallet();
        wallet.setBalance(wallet.getBalance().add(amountAfterDeposit));
        if(LocalDate.now().isBefore(deposit.getEndDate())){
            deposit.setStatus(DepositStatusEnum.CLOSED);
        }
        else{
            deposit.setStatus(DepositStatusEnum.COMPLETED);
        }
        walletRepository.save(wallet);
        depositRepository.save(deposit);
        return depositMapper.toResponse(deposit);
    }

}
