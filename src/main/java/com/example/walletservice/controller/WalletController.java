package com.example.walletservice.controller;


import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.request.WalletTopUpRequest;
import com.example.walletservice.dto.request.WalletWithdrawRequest;
import com.example.walletservice.dto.response.TransactionResponse;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/{userId}")
    public WalletResponse createWallet(@PathVariable Long userId, @Valid @RequestBody WalletRequest walletRequest){
        return walletService.createWallet(userId,walletRequest);

    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable Long walletId){
        return walletService.getWallet(walletId);
    }

    @GetMapping("/users/{userId}/wallets")
    public List<WalletResponse> getAllWallets(@PathVariable Long userId){
        return walletService.getAllWallets(userId);
    }

    @PostMapping("/{walletId}/block")
    public WalletResponse blockWallet(@PathVariable Long walletId){
        return walletService.blockWallet(walletId);
    }

    @PostMapping("/{walletId}/activate")
    public WalletResponse activateWallet(@PathVariable Long walletId){
        return walletService.activateWallet(walletId);
    }

    @PostMapping("/{walletId}/top-up")
    public WalletResponse topUp(@PathVariable Long walletId, @Valid @RequestBody WalletTopUpRequest request){
        return walletService.topUp(walletId,request);
    }

    @PostMapping("/{walletId}/withdraw")
    public WalletResponse withdraw(@PathVariable Long walletId, @Valid @RequestBody WalletWithdrawRequest request){
        return walletService.withdraw(walletId,request);
    }


}
