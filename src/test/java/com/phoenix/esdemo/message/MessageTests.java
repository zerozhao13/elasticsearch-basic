package com.phoenix.esdemo.message;

import com.phoenix.esdemo.entity.Message;
import com.phoenix.esdemo.repository.MessageRepositroy;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;

import java.util.Date;

@SpringBootTest
public class MessageTests {

 // @Autowired private ElasticsearchRestTemplate est;

  @Autowired private MessageRepositroy msgRep;

  @BeforeAll
  static void createIndex() {}

  @AfterAll
  static void deleteIndex() {}

  @BeforeEach
  void beforeTests() {
    System.out.print("新的测试即将开始");
  }

  @DisplayName("保存新的消息")
  @Test
  void saveDoc() {
    Message msg =
        new Message(
            "1L",
            "你好",
            "欢迎使用即管家",
            "鲜肉",
            1,
            (new Date()).toString(),
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3919334208,37253891&fm=26&gp=0.jpg");
    msgRep.save(msg);
  }
}
