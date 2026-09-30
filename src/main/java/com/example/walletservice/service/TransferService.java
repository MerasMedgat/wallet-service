package com.example.walletservice.service;

import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.*;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void transfer(TransferRequest request){
        if (request.getFromWalletId().equals(request.getToWalletId())){
            throw new TransferToSameWalletException("The recipient's and sender's wallets are the same");
        }
        Wallet fromWallet = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(()->
                        new FromWalletIdNotFoundException("Sender's wallet not found"));
        Wallet toWallet = walletRepository.findById(request.getToWalletId())
                .orElseThrow(()->
                        new ToWalletIdNotFoundException("Recipient's wallet not found"));
        if (fromWallet.getStatus() != WalletStatusEnum.ACTIVE){
            throw new WalletNotActiveException("Sender's wallet not activated");
        }
        if (toWallet.getStatus() != WalletStatusEnum.ACTIVE){
            throw new WalletNotActiveException("Recipient's wallet not activated");
        }
        if(fromWallet.getBalance().compareTo(request.getAmount()) <0){
            throw new InsufficientBalanceException("Insufficient balance");
        }
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        Transaction transactionFromWallet = Transaction.builder()
                .wallet(fromWallet)
                .transactionType(TransactionTypeEnum.WITHDRAW)
                .amount(request.getAmount())
                .balanceAfterTransaction(fromWallet.getBalance())
                .build();
        Transaction transactionToWallet = Transaction.builder()
                .wallet(toWallet)
                .transactionType(TransactionTypeEnum.TOP_UP)
                .amount(request.getAmount())
                .balanceAfterTransaction(toWallet.getBalance())
                .build();
        transactionRepository.save(transactionFromWallet);
        transactionRepository.save(transactionToWallet);
    }


}