package com.example.walletservice.integration;

import com.example.walletservice.dto.request.AmountRequest;
import com.example.walletservice.dto.request.TransferRequest;
import com.example.walletservice.entity.User;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.RoleEnum;
import com.example.walletservice.enums.WalletStatusEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.repository.UserRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.service.TransferService;
import com.example.walletservice.service.WalletService;
import com.example.walletservice.support.TestSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


class ConcurrencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WalletService walletService;
    @Autowired
    private TransferService transferService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletRepository walletRepository;

    @Test
    void parallelWithdrawalsNeverOverdraw() throws Exception {
        User user = createUser();
        Wallet wallet = createWallet(user, "500.00");
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        runConcurrently(10, i -> {
            TestSecurity.loginAs(user.getId());
            try {
                walletService.withdraw(wallet.getId(), new AmountRequest(new BigDecimal("100.00")));
                succeeded.incrementAndGet();
            } catch (BadRequestException e) {
                rejected.incrementAndGet();
            } finally {
                TestSecurity.logout();
            }
        });

        assertThat(succeeded.get()).isEqualTo(5);
        assertThat(rejected.get()).isEqualTo(5);
        assertThat(walletRepository.getOrThrow(wallet.getId()).getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void oppositeTransfersDoNotDeadlockAndKeepTotal() throws Exception {
        User alice = createUser();
        User bob = createUser();
        Wallet aliceWallet = createWallet(alice, "1000.00");
        Wallet bobWallet = createWallet(bob, "1000.00");

        runConcurrently(40, i -> {
            boolean aliceSends = i % 2 == 0;
            TestSecurity.loginAs(aliceSends ? alice.getId() : bob.getId());
            try {
                transferService.transfer(aliceSends
                        ? new TransferRequest(aliceWallet.getId(), bobWallet.getId(), BigDecimal.TEN)
                        : new TransferRequest(bobWallet.getId(), aliceWallet.getId(), BigDecimal.ONE), null);
            } finally {
                TestSecurity.logout();
            }
        });

        // 20 x (-10) and 20 x (+1) for Alice, the opposite for Bob
        assertThat(walletRepository.getOrThrow(aliceWallet.getId()).getBalance()).isEqualByComparingTo("820.00");
        assertThat(walletRepository.getOrThrow(bobWallet.getId()).getBalance()).isEqualByComparingTo("1180.00");
    }

    /** Starts all tasks at the same instant and rethrows the first unexpected failure. */
    private static void runConcurrently(int tasks, IntConsumerWithIndex task) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < tasks; i++) {
            int index = i;
            futures.add(pool.submit(() -> {
                start.await();
                task.accept(index);
                return null;
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();
    }

    @FunctionalInterface
    private interface IntConsumerWithIndex {
        void accept(int index);
    }

    private User createUser() {
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return userRepository.save(User.builder()
                .email(unique + "@concurrency.test")
                .phone("+1" + Math.abs(unique.hashCode()))
                .password("not-used")
                .firstName("Test")
                .lastName("User")
                .role(RoleEnum.USER)
                .build());
    }

    private Wallet createWallet(User user, String balance) {
        return walletRepository.save(Wallet.builder()
                .user(user)
                .balance(new BigDecimal(balance))
                .currency(CurrencyEnum.KZT)
                .status(WalletStatusEnum.ACTIVE)
                .build());
    }
}
