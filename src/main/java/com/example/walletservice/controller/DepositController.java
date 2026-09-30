package com.example.walletservice.controller;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse createDeposit(@Valid @RequestBody DepositRequest request){
        return depositService.createDeposit(request);
    }

    @GetMapping("/{depositId}")
    public DepositResponse getDeposit(@PathVariable Long depositId){
        return depositService.getDeposit(depositId);
    }

    @GetMapping("/wallet/{walletId}")
    public Page<DepositResponse> getDeposits(
            @PathVariable Long walletId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return depositService.getDeposits(walletId, pageable);
    }

    @PostMapping("/{depositId}/close")
    public DepositResponse closeDeposit(@PathVariable Long depositId){
        return depositService.closeDeposit(depositId);
    }
}
