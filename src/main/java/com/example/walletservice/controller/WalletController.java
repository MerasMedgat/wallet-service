package com.example.walletservice.controller;

import com.example.walletservice.dto.request.AmountRequest;
import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse createWallet(@Valid @RequestBody WalletRequest request){
        return walletService.createWallet(request);
    }

    @GetMapping
    public List<WalletResponse> getMyWallets(){
        return walletService.getMyWallets();
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable Long walletId){
        return walletService.getWallet(walletId);
    }

    @PostMapping("/{walletId}/top-up")
    public WalletResponse topUp(@PathVariable Long walletId, @Valid @RequestBody AmountRequest request){
        return walletService.topUp(walletId, request);
    }

    @PostMapping("/{walletId}/withdraw")
    public WalletResponse withdraw(@PathVariable Long walletId, @Valid @RequestBody AmountRequest request){
        return walletService.withdraw(walletId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{walletId}/block")
    public WalletResponse blockWallet(@PathVariable Long walletId){
        return walletService.blockWallet(walletId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{walletId}/activate")
    public WalletResponse activateWallet(@PathVariable Long walletId){
        return walletService.activateWallet(walletId);
    }
}
