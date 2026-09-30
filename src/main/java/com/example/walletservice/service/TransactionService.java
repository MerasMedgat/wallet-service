package com.example.walletservice.service;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.mapper.TransactionMapper;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByWalletId(Long walletId){
        SecurityUtils.checkOwner(walletRepository.getOrThrow(walletId).getUser());
        return transactionRepository.findByWalletId(walletId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}
