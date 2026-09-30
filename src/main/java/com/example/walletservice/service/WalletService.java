package com.example.walletservice.service;

import com.example.walletservice.dto.request.AmountRequest;
import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.ConflictException;
import com.example.walletservice.mapper.WalletMapper;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletResponse createWallet(WalletRequest request){
        Long userId = SecurityUtils.currentUserId();
        if (walletRepository.existsByUserIdAndCurrency(userId, request.currency())) {
            throw new ConflictException("Wallet in " + request.currency() + " already exists");
        }
        Wallet wallet = walletMapper.toEntity(request);
        wallet.setUser(userRepository.getReferenceById(userId));
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatusEnum.ACTIVE);
        return walletMapper.toResponse(walletRepository.save(wallet));
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(Long walletId){
        Wallet wallet = walletRepository.getOrThrow(walletId);
        SecurityUtils.checkOwner(wallet.getUser());
        return walletMapper.toResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getMyWallets(){
        return walletRepository.findByUserId(SecurityUtils.currentUserId())
                .stream()
                .map(walletMapper::toResponse)
                .toList();
    }


    @Transactional
    public WalletResponse blockWallet(Long walletId){
        Wallet wallet = walletRepository.lockOrThrow(walletId);
        if (wallet.getStatus() == WalletStatusEnum.BLOCKED) {
            throw new ConflictException("Wallet already blocked");
        }
        wallet.setStatus(WalletStatusEnum.BLOCKED);
        return walletMapper.toResponse(wallet);
    }


    @Transactional
    public WalletResponse activateWallet(Long walletId){
        Wallet wallet = walletRepository.lockOrThrow(walletId);
        if (wallet.getStatus() == WalletStatusEnum.ACTIVE) {
            throw new ConflictException("Wallet already active");
        }
        wallet.setStatus(WalletStatusEnum.ACTIVE);
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse topUp(Long walletId, AmountRequest request){
        Wallet wallet = walletRepository.lockOrThrow(walletId);
        SecurityUtils.checkOwner(wallet.getUser());
        wallet.credit(request.amount());
        transactionRepository.save(Transaction.of(wallet, TransactionTypeEnum.TOP_UP, request.amount()));
        return walletMapper.toResponse(wallet);
    }

    @Transactional
    public WalletResponse withdraw(Long walletId, AmountRequest request){
        Wallet wallet = walletRepository.lockOrThrow(walletId);
        SecurityUtils.checkOwner(wallet.getUser());
        wallet.debit(request.amount());
        transactionRepository.save(Transaction.of(wallet, TransactionTypeEnum.WITHDRAW, request.amount()));
        return walletMapper.toResponse(wallet);
    }
}
