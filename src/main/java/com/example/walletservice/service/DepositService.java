package com.example.walletservice.service;


import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.DepositStatusEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.InsufficientBalanceException;
import com.example.walletservice.exception.InvalidDepositTermException;
import com.example.walletservice.exception.WalletNotActiveException;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.mapper.DepositMapper;
import com.example.walletservice.repository.DepositRepository;
import com.example.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        deposit.setDepositStatus(DepositStatusEnum.ACTIVE);
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
}
