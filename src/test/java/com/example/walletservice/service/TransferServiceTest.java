package com.example.walletservice.service;

import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.entity.Transaction;
import com.example.walletservice.entity.User;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.repository.IdempotencyKeyRepository;
import com.example.walletservice.repository.TransactionRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.support.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final Long ALICE = 1L;
    private static final Long BOB = 2L;

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;
    @InjectMocks
    private TransferService transferService;

    private Wallet aliceWallet;
    private Wallet bobWallet;

    @BeforeEach
    void setUp() {
        TestSecurity.loginAs(ALICE);
        aliceWallet = wallet(10L, ALICE, "1000.00", CurrencyEnum.KZT);
        bobWallet = wallet(20L, BOB, "0.00", CurrencyEnum.KZT);
    }

    @AfterEach
    void tearDown() {
        TestSecurity.logout();
    }

    @Test
    void movesMoneyAndRecordsBothSides() {
        stubLocks();

        transferService.transfer(new TransferRequest(10L, 20L, new BigDecimal("300.00")), null);

        assertThat(aliceWallet.getBalance()).isEqualByComparingTo("700.00");
        assertThat(bobWallet.getBalance()).isEqualByComparingTo("300.00");
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    @Test
    void rejectsTransferToSameWallet() {
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(10L, 10L, BigDecimal.ONE), null))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(walletRepository);
    }

    @Test
    void rejectsWhenBalanceIsNotEnough() {
        stubLocks();

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(10L, 20L, new BigDecimal("1000.01")), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Insufficient balance");
        assertThat(bobWallet.getBalance()).isEqualByComparingTo("0.00");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void rejectsDifferentCurrencies() {
        bobWallet.setCurrency(CurrencyEnum.USD);
        stubLocks();

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(10L, 20L, BigDecimal.TEN), null))
                .isInstanceOf(BadRequestException.class);
        assertThat(aliceWallet.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void rejectsBlockedRecipient() {
        bobWallet.setStatus(WalletStatusEnum.BLOCKED);
        stubLocks();

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(10L, 20L, BigDecimal.TEN), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotSpendFromSomeoneElsesWallet() {
        TestSecurity.loginAs(BOB);
        stubLocks();

        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(10L, 20L, BigDecimal.TEN), null))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(aliceWallet.getBalance()).isEqualByComparingTo("1000.00");
    }

    @Test
    void locksWalletsInAscendingIdOrderToAvoidDeadlocks() {
        TestSecurity.loginAs(BOB);
        bobWallet.setBalance(new BigDecimal("50.00"));
        stubLocks();

        transferService.transfer(new TransferRequest(20L, 10L, BigDecimal.TEN), null);

        InOrder inOrder = inOrder(walletRepository);
        inOrder.verify(walletRepository).lockOrThrow(10L);
        inOrder.verify(walletRepository).lockOrThrow(20L);
    }

    @Test
    void repeatedIdempotencyKeyDoesNothing() {
        when(idempotencyKeyRepository.existsByUserIdAndKey(ALICE, "key-1")).thenReturn(true);

        transferService.transfer(new TransferRequest(10L, 20L, BigDecimal.TEN), "key-1");

        verifyNoInteractions(walletRepository, transactionRepository);
    }

    private void stubLocks() {
        when(walletRepository.lockOrThrow(10L)).thenReturn(aliceWallet);
        when(walletRepository.lockOrThrow(20L)).thenReturn(bobWallet);
    }

    static Wallet wallet(Long id, Long ownerId, String balance, CurrencyEnum currency) {
        return Wallet.builder()
                .id(id)
                .user(User.builder().id(ownerId).build())
                .balance(new BigDecimal(balance))
                .currency(currency)
                .status(WalletStatusEnum.ACTIVE)
                .build();
    }
}
