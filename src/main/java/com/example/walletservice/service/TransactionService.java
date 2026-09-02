package com.example.walletservice.service;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.mapper.TransactionMapper;
import com.example.walletservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public List<TransactionResponse> getTransactionsByWalletId(Long walletId){
        List<TransactionResponse> transactions = transactionRepository.findByWalletId(walletId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
        return transactions;

    }
}
