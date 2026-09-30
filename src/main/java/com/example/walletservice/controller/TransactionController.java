package com.example.walletservice.controller;

import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;


    @GetMapping("/wallet/{walletId}")
    public Page<TransactionResponse> getTransactionsByWalletId(
            @PathVariable Long walletId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return transactionService.getTransactionsByWalletId(walletId, pageable);
    }
}
