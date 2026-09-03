package com.example.walletservice.controller;

import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping()
    public void transfer(@Valid @RequestBody TransferRequest request){
        transferService.transfer(request);
    }

}
