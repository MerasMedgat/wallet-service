package com.example.walletservice.controller;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping()
    public DepositResponse createDeposit(@Valid @RequestBody DepositRequest depositRequest){
        return depositService.createDeposit(depositRequest);
    }

    @GetMapping("/{depositId}")
    public DepositResponse getDeposit(@PathVariable Long depositId){
        return depositService.getDeposit(depositId);
    }

    @GetMapping("/wallet/{walletId}")
    public List<DepositResponse> getAllDeposit(@PathVariable Long walletId){
        return depositService.getAllDeposits(walletId);
    }

    @PostMapping("/{depositId}/close")
    public DepositResponse closeDeposit(@PathVariable Long depositId){
        return depositService.closeDeposit(depositId);
    }

}
