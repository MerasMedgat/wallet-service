package com.example.walletservice.service;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.exception.WalletNotFoundException;
import com.example.walletservice.mapper.TransactionMapper;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final WalletRepository walletRepository;

    public List<TransactionResponse> getTransactionsByWalletId(Long walletId){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        SecurityUtils.checkOwner(wallet.getUser());
        return transactionRepository.findByWalletId(walletId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
