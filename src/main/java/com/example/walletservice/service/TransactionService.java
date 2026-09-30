package com.example.walletservice.service;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.mapper.TransactionMapper;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactionsByWalletId(Long walletId, Pageable pageable){
        SecurityUtils.checkOwner(walletRepository.getOrThrow(walletId).getUser());
        return transactionRepository.findByWalletId(walletId, pageable)
                .map(transactionMapper::toResponse);
    }
}
