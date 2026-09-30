package com.example.walletservice.service;

import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
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
        if (request.fromWalletId().equals(request.toWalletId())) {
            throw new BadRequestException("The recipient's and sender's wallets are the same");
        }
        Wallet fromWallet = walletRepository.getOrThrow(request.fromWalletId());
        Wallet toWallet = walletRepository.getOrThrow(request.toWalletId());
        SecurityUtils.checkOwner(fromWallet.getUser());

        fromWallet.debit(request.amount());
        toWallet.credit(request.amount());
        transactionRepository.save(Transaction.of(fromWallet, TransactionTypeEnum.WITHDRAW, request.amount()));
        transactionRepository.save(Transaction.of(toWallet, TransactionTypeEnum.TOP_UP, request.amount()));
    }
}
