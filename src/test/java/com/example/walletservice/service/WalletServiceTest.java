package com.example.walletservice.service;

import com.example.walletservice.dto.request.AmountRequest;
import com.example.walletservice.dto.request.WalletRequest;
import com.example.walletservice.dto.response.WalletResponse;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.TransactionTypeEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.exception.ConflictException;
import com.example.walletservice.mapper.WalletMapper;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.support.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;

import static com.example.walletservice.service.TransferServiceTest.wallet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    private static final Long OWNER = 1L;

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private WalletService walletService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository, userRepository, transactionRepository,
                Mappers.getMapper(WalletMapper.class));
        wallet = wallet(10L, OWNER, "100.00", CurrencyEnum.KZT);
        TestSecurity.loginAs(OWNER);
    }

    @AfterEach
    void tearDown() {
        TestSecurity.logout();
    }

    @Test
    void withdrawDecreasesBalanceAndRecordsTransaction() {
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);

        WalletResponse response = walletService.withdraw(10L, new AmountRequest(new BigDecimal("40.00")));

        assertThat(response.balance()).isEqualByComparingTo("60.00");
        ArgumentCaptor<Transaction> saved = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(saved.capture());
        assertThat(saved.getValue().getTransactionType()).isEqualTo(TransactionTypeEnum.WITHDRAW);
        assertThat(saved.getValue().getBalanceAfterTransaction()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdrawMoreThanBalanceIsRejected() {
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);

        assertThatThrownBy(() -> walletService.withdraw(10L, new AmountRequest(new BigDecimal("100.01"))))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient balance");
        assertThat(wallet.getBalance()).isEqualByComparingTo("100.00");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void topUpOfBlockedWalletIsRejected() {
        wallet.setStatus(WalletStatusEnum.BLOCKED);
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);

        assertThatThrownBy(() -> walletService.topUp(10L, new AmountRequest(BigDecimal.TEN)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotReadSomeoneElsesWallet() {
        TestSecurity.loginAs(2L);
        when(walletRepository.getOrThrow(10L)).thenReturn(wallet);

        assertThatThrownBy(() -> walletService.getWallet(10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void secondWalletInSameCurrencyIsRejected() {
        when(walletRepository.existsByUserIdAndCurrency(OWNER, CurrencyEnum.KZT)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(new WalletRequest(CurrencyEnum.KZT)))
                .isInstanceOf(ConflictException.class);
        verify(walletRepository, never()).save(any());
    }

    @Test
    void blockingAlreadyBlockedWalletIsRejected() {
        wallet.setStatus(WalletStatusEnum.BLOCKED);
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);

        assertThatThrownBy(() -> walletService.blockWallet(10L))
                .isInstanceOf(ConflictException.class);
    }
}
