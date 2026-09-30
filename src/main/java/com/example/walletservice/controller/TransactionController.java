package com.example.walletservice.controller;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/wallet/{walletId}")
    public List<TransactionResponse> getTransactionsByWalletId(@PathVariable Long walletId){
        return transactionService.getTransactionsByWalletId(walletId);
    }
}
