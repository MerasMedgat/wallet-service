package com.example.walletservice.integration;

import com.example.walletservice.entity.User;
import com.example.walletservice.enums.RoleEnum;
import com.example.walletservice.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ApiIntegrationTest extends AbstractIntegrationTest {

    private static final AtomicInteger PHONE_SEQ = new AtomicInteger(1_000_000);

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void registerLoginCreateWalletAndTransfer() throws Exception {
        String alice = registerAndLogin();
        String bob = registerAndLogin();
        long aliceWallet = createWallet(alice, "KZT");
        long bobWallet = createWallet(bob, "KZT");

        postJson("/api/wallets/" + aliceWallet + "/top-up", alice, "{\"amount\": 1000}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.0));

        postJson("/api/transfer", alice, transfer(aliceWallet, bobWallet, "300"))
                .andExpect(status().isOk());

        getJson("/api/wallets/" + aliceWallet, alice).andExpect(jsonPath("$.balance").value(700.0));
        getJson("/api/wallets/" + bobWallet, bob).andExpect(jsonPath("$.balance").value(300.0));
        getJson("/api/transactions/wallet/" + aliceWallet, alice)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].transactionType").value("WITHDRAW"))
                .andExpect(jsonPath("$.page.totalElements").value(2));
    }

    @Test
    void requestsWithoutValidTokenAreRejected() throws Exception {
        mockMvc.perform(get("/api/wallets")).andExpect(status().isUnauthorized());
        getJson("/api/wallets", "not-a-jwt").andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        String email = register();
        postJson("/api/users/login", null, "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void usersCannotTouchOtherUsersWallets() throws Exception {
        String alice = registerAndLogin();
        String mallory = registerAndLogin();
        long aliceWallet = createWallet(alice, "KZT");
        long malloryWallet = createWallet(mallory, "KZT");
        postJson("/api/wallets/" + aliceWallet + "/top-up", alice, "{\"amount\": 100}");

        getJson("/api/wallets/" + aliceWallet, mallory).andExpect(status().isForbidden());
        postJson("/api/wallets/" + aliceWallet + "/withdraw", mallory, "{\"amount\": 10}")
                .andExpect(status().isForbidden());
        postJson("/api/transfer", mallory, transfer(aliceWallet, malloryWallet, "10"))
                .andExpect(status().isForbidden());
        getJson("/api/transactions/wallet/" + aliceWallet, mallory).andExpect(status().isForbidden());

        getJson("/api/wallets/" + aliceWallet, alice).andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    void onlyAdminCanBlockWallet() throws Exception {
        String user = registerAndLogin();
        long wallet = createWallet(user, "USD");

        postJson("/api/wallets/" + wallet + "/block", user, "").andExpect(status().isForbidden());

        String adminEmail = register();
        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        admin.setRole(RoleEnum.ADMIN);
        userRepository.save(admin);
        String adminToken = login(adminEmail);

        postJson("/api/wallets/" + wallet + "/block", adminToken, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
        postJson("/api/wallets/" + wallet + "/top-up", user, "{\"amount\": 10}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void sameIdempotencyKeyTransfersOnlyOnce() throws Exception {
        String alice = registerAndLogin();
        String bob = registerAndLogin();
        long aliceWallet = createWallet(alice, "EUR");
        long bobWallet = createWallet(bob, "EUR");
        postJson("/api/wallets/" + aliceWallet + "/top-up", alice, "{\"amount\": 100}");

        String key = UUID.randomUUID().toString();
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/transfer")
                            .header("Authorization", "Bearer " + alice)
                            .header("Idempotency-Key", key)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transfer(aliceWallet, bobWallet, "40")))
                    .andExpect(status().isOk());
        }

        getJson("/api/wallets/" + aliceWallet, alice).andExpect(jsonPath("$.balance").value(60.0));
    }

    @Test
    void invalidBodyReturnsFieldErrors() throws Exception {
        String token = registerAndLogin();
        long wallet = createWallet(token, "KZT");

        postJson("/api/wallets/" + wallet + "/top-up", token, "{\"amount\": -5}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    // --- helpers ---

    private String register() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@test.com";
        String body = """
                {"email":"%s","password":"password1","phone":"+7700%d","firstName":"Test","lastName":"User"}
                """.formatted(email, PHONE_SEQ.incrementAndGet());
        postJson("/api/users/register", null, body).andExpect(status().isCreated());
        return email;
    }

    private String login(String email) throws Exception {
        String response = postJson("/api/users/login", null,
                "{\"email\":\"" + email + "\",\"password\":\"password1\"}")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private String registerAndLogin() throws Exception {
        return login(register());
    }

    private long createWallet(String token, String currency) throws Exception {
        String response = postJson("/api/wallets", token, "{\"currency\":\"" + currency + "\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static String transfer(long from, long to, String amount) {
        return "{\"fromWalletId\":%d,\"toWalletId\":%d,\"amount\":%s}".formatted(from, to, amount);
    }

    private ResultActions postJson(String url, String token, String body) throws Exception {
        var request = post(url).contentType(MediaType.APPLICATION_JSON).content(body);
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return mockMvc.perform(request);
    }

    private ResultActions getJson(String url, String token) throws Exception {
        return mockMvc.perform(get(url).header("Authorization", "Bearer " + token));
    }
}
