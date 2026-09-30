package com.example.walletservice.service;

import com.example.walletservice.dto.request.DepositRequest;
import com.example.walletservice.dto.response.DepositResponse;
import com.example.walletservice.entity.Deposit;
import com.example.walletservice.entity.Wallet;
import com.example.walletservice.enums.CurrencyEnum;
import com.example.walletservice.enums.DepositStatusEnum;
import com.example.walletservice.exception.BadRequestException;
import com.example.walletservice.mapper.DepositMapper;
import com.example.walletservice.repository.DepositRepository;
import com.example.walletservice.repository.WalletRepository;
import com.example.walletservice.support.TestSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.walletservice.service.TransferServiceTest.wallet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepositServiceTest {

    private static final Long OWNER = 1L;

    @Mock
    private DepositRepository depositRepository;
    @Mock
    private WalletRepository walletRepository;

    private DepositService depositService;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        depositService = new DepositService(depositRepository, walletRepository, Mappers.getMapper(DepositMapper.class));
        wallet = wallet(10L, OWNER, "1000.00", CurrencyEnum.KZT);
        TestSecurity.loginAs(OWNER);
    }

    @AfterEach
    void tearDown() {
        TestSecurity.logout();
    }

    @ParameterizedTest
    @CsvSource({"1, 8.00", "5, 8.00", "6, 10.00", "11, 10.00", "12, 15.00", "17, 15.00", "18, 20.00", "24, 20.00"})
    void interestRateDependsOnTerm(int months, String expectedRate) {
        assertThat(DepositService.interestRateFor(months)).isEqualByComparingTo(expectedRate);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 25})
    void termOutsideOneToTwentyFourMonthsIsRejected(int months) {
        assertThatThrownBy(() -> DepositService.interestRateFor(months))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void interestIsProportionalToDaysAndRoundedToCents() {
        // 500 * 2% * 100/365 = 2.7397...
        assertThat(DepositService.calculateInterest(new BigDecimal("500"), new BigDecimal("2"), 100))
                .isEqualByComparingTo("2.74");
        assertThat(DepositService.calculateInterest(new BigDecimal("1000"), new BigDecimal("10"), 365))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void createDepositTakesMoneyFromWallet() {
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);
        when(depositRepository.save(any(Deposit.class))).thenAnswer(inv -> inv.getArgument(0));

        DepositResponse response = depositService.createDeposit(new DepositRequest(10L, new BigDecimal("400.00"), 6));

        assertThat(wallet.getBalance()).isEqualByComparingTo("600.00");
        assertThat(response.interestRate()).isEqualByComparingTo("10.00");
        assertThat(response.depositStatus()).isEqualTo(DepositStatusEnum.ACTIVE);
        assertThat(response.endDate()).isEqualTo(LocalDate.now().plusMonths(6));
    }

    @Test
    void closingEarlyPaysReducedRate() {
        Deposit deposit = deposit(LocalDate.now().minusDays(100), LocalDate.now().plusDays(80));
        stubClose(deposit);

        DepositResponse response = depositService.closeDeposit(1L);

        // 500 + 500 * 2% * 100/365
        assertThat(wallet.getBalance()).isEqualByComparingTo("1502.74");
        assertThat(response.depositStatus()).isEqualTo(DepositStatusEnum.CLOSED);
        assertThat(response.interestRate()).isEqualByComparingTo(DepositService.EARLY_CLOSE_RATE);
    }

    @Test
    void closingAfterEndDatePaysFullRate() {
        Deposit deposit = deposit(LocalDate.now().minusDays(183), LocalDate.now());
        stubClose(deposit);

        DepositResponse response = depositService.closeDeposit(1L);

        // 500 + 500 * 10% * 183/365
        assertThat(wallet.getBalance()).isEqualByComparingTo("1525.07");
        assertThat(response.depositStatus()).isEqualTo(DepositStatusEnum.COMPLETED);
    }

    @Test
    void closedDepositCannotBeClosedAgain() {
        Deposit deposit = deposit(LocalDate.now().minusDays(10), LocalDate.now().plusDays(10));
        deposit.setStatus(DepositStatusEnum.CLOSED);
        stubClose(deposit);

        assertThatThrownBy(() -> depositService.closeDeposit(1L))
                .isInstanceOf(BadRequestException.class);
        assertThat(wallet.getBalance()).isEqualByComparingTo("1000.00");
    }

    private void stubClose(Deposit deposit) {
        when(depositRepository.lockOrThrow(1L)).thenReturn(deposit);
        when(walletRepository.lockOrThrow(10L)).thenReturn(wallet);
    }

    private Deposit deposit(LocalDate start, LocalDate end) {
        return Deposit.builder()
                .id(1L)
                .wallet(wallet)
                .amount(new BigDecimal("500.00"))
                .interestRate(new BigDecimal("10.00"))
                .termMonths(6)
                .startDate(start)
                .endDate(end)
                .status(DepositStatusEnum.ACTIVE)
                .build();
    }
}
