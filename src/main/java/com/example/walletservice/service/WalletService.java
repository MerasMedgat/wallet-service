package com.example.walletservice.service;

import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.request.WalletTopUpRequest;
import com.example.walletservice.dto.request.WalletWithdrawRequest;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.entity.*;
import com.example.walletservice.exception.*;
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
    private final WalletMapper walletMapper;
    private final TransactionRepository transactionRepository;


    public WalletResponse createWallet(WalletRequest walletRequest){
        Long userId = SecurityUtils.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()->
                        new UserNotFoundException("User not found"));

        if (walletRepository.existsByUserIdAndCurrency(userId,walletRequest.getCurrency())){
            throw new WalletAlreadyExistsException("Wallet already exists");
        }

        Wallet wallet = walletMapper.toEntity(walletRequest);
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatusEnum.ACTIVE);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    public WalletResponse getWallet(Long id){
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        SecurityUtils.checkOwner(wallet.getUser());
        return walletMapper.toResponse(wallet);
    }

    public List<WalletResponse> getMyWallets(){
        List<WalletResponse> wallets = walletRepository.findByUserId(SecurityUtils.currentUserId())
                .stream()
                .map(walletMapper::toResponse)
                .toList();
        return wallets;
    }

    public WalletResponse blockWallet(Long walletId){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        if(wallet.getStatus() == WalletStatusEnum.BLOCKED){
            throw new WalletAlreadyBlockedException("Wallet already blocked");
        }
        wallet.setStatus(WalletStatusEnum.BLOCKED);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    public WalletResponse activateWallet(Long walletId){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        if(wallet.getStatus() == WalletStatusEnum.ACTIVE){
            throw new WalletAlreadyActiveException("Wallet already active");
        }
        wallet.setStatus(WalletStatusEnum.ACTIVE);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponse topUp(Long walletId, WalletTopUpRequest request){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        SecurityUtils.checkOwner(wallet.getUser());
        if(wallet.getStatus() != WalletStatusEnum.ACTIVE){
            throw new WalletNotActiveException("Wallet not active");
        }
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionType(TransactionTypeEnum.TOP_UP)
                .amount(request.getAmount())
                .balanceAfterTransaction(wallet.getBalance())
                .build();
        transactionRepository.save(transaction);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }

    @Transactional
    public WalletResponse withdraw(Long walletId, WalletWithdrawRequest request){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()->
                        new WalletNotFoundException("Wallet not found"));
        SecurityUtils.checkOwner(wallet.getUser());
        if(wallet.getStatus() != WalletStatusEnum.ACTIVE){
            throw new WalletNotActiveException("Wallet not active");
        }
        if(wallet.getBalance().compareTo(request.getAmount()) <0){
            throw new InsufficientBalanceException("Insufficient balance");
        }

        wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .transactionType(TransactionTypeEnum.WITHDRAW)
                .amount(request.getAmount())
                .balanceAfterTransaction(wallet.getBalance())
                .build();
        transactionRepository.save(transaction);
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toResponse(savedWallet);
    }




}
