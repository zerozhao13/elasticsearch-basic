package com.phoenix.esdemo.message;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MessageTests {

    @BeforeAll
    static void createIndex() {

    }

    @AfterAll
    static void deleteIndex() {

    }

    @BeforeEach
    void beforeTests() {
        System.out.print("新的测试即将开始");
    }

    @DisplayName("保存新的消息")
    @Test
    void saveDoc() {

    }
}
