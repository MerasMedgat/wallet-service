package com.example.walletservice.service;

import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.entity.IdempotencyKey;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.repository.IdempotencyKeyRepository;
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
    private final IdempotencyKeyRepository idempotencyKeyRepository;


    @Transactional
    public void transfer(TransferRequest request, String idempotencyKey){
        if (request.fromWalletId().equals(request.toWalletId())) {
            throw new BadRequestException("The recipient's and sender's wallets are the same");
        }
        if (idempotencyKey != null) {
            Long userId = SecurityUtils.currentUserId();
            if (idempotencyKeyRepository.existsByUserIdAndKey(userId, idempotencyKey)) {
                return;
            }

            idempotencyKeyRepository.saveAndFlush(new IdempotencyKey(userId, idempotencyKey));
        }


        boolean senderFirst = request.fromWalletId() < request.toWalletId();
        Wallet first = walletRepository.lockOrThrow(senderFirst ? request.fromWalletId() : request.toWalletId());
        Wallet second = walletRepository.lockOrThrow(senderFirst ? request.toWalletId() : request.fromWalletId());
        Wallet fromWallet = senderFirst ? first : second;
        Wallet toWallet = senderFirst ? second : first;

        SecurityUtils.checkOwner(fromWallet.getUser());
        if (fromWallet.getCurrency() != toWallet.getCurrency()) {
            throw new BadRequestException("Transfers between different currencies are not supported");
        }

        fromWallet.debit(request.amount());
        toWallet.credit(request.amount());
        transactionRepository.save(Transaction.of(fromWallet, TransactionTypeEnum.WITHDRAW, request.amount()));
        transactionRepository.save(Transaction.of(toWallet, TransactionTypeEnum.TOP_UP, request.amount()));
    }
}
