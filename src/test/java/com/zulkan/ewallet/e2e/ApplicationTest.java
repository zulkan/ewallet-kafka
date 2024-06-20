package com.zulkan.ewallet.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zulkan.ewallet.dto.request.BalanceTopupRequest;
import com.zulkan.ewallet.model.User;
import com.zulkan.ewallet.repository.TransactionRepository;
import com.zulkan.ewallet.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Collections;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTest {


    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @LocalServerPort
    private int port = 8080;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    private final User firstUser = new User();

    @BeforeEach
    void init() {
        transactionRepository.deleteAll();
        userRepository.deleteAll();

        baseUrl = "http://localhost:" + port;

        firstUser.setToken("secretKey");
        firstUser.setUsername("zulkan");
        firstUser.setBalance(10000);

        userRepository.save(firstUser);
        userRepository.flush();
    }

    void setAuth() {
        restTemplate.getRestTemplate().setInterceptors(
                Collections.singletonList((request, body, execution) -> {
                    request.getHeaders()
                            .add("Authorization", "Bearer " + firstUser.getToken());
                    return execution.execute(request, body);
                }));
    }

    @Test
    void balanceTopupTest() throws JsonProcessingException, InterruptedException {
        var users = userRepository.findAll();

        Assertions.assertEquals(1, users.size());

        System.out.println(users.get(0).getUsername()+" "+users.get(0).getToken());

        BalanceTopupRequest requestDTO = new BalanceTopupRequest();
        requestDTO.setAmount(300000);

        setAuth();

        var resp = restTemplate.postForEntity(baseUrl + "/transactions/balance_topup", requestDTO, String.class );

        System.out.println(resp.getBody());

        Assertions.assertEquals(200, resp.getStatusCodeValue());
        Assertions.assertTrue(resp.getBody().contains("balance topup request received"));

        int ct = 0;
        while(ct++<30) {
            if(transactionRepository.count() == 1) {
                break;
            }
            Thread.sleep(1000);
        }

        Assertions.assertEquals(1, transactionRepository.count());

        var transactions = transactionRepository.findAll();

        Assertions.assertEquals(300000, transactions.get(0).getAmount());

        var latestUserData = userRepository.getUserByUsername(firstUser.getUsername());

        Assertions.assertEquals(10000 + 300000, latestUserData.getBalance());


    }
}
