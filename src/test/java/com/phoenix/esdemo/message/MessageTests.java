package com.phoenix.esdemo.message;

import com.phoenix.esdemo.entity.Message;
import com.phoenix.esdemo.repository.MessageRepository;
import com.phoenix.esdemo.repository.ReactiveMessageRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTests {

  @Autowired private MessageRepository msgRep;
  @Autowired private ReactiveMessageRepository ractMsgRep;

  @BeforeEach
  void beforeTests() {
    System.out.print("新的测试即将开始");
  }

  @DisplayName("保存新的消息")
  @Order(1)
  @Test
  void saveDoc() {
    Message msg =
        new Message(
            "4",
            "who博士",
            "和dalex对战了很多年",
            "时间领主",
            2,
            Instant.now(),
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3919334208,37253891&fm=26&gp=0.jpg");
    Message savedMsg = msgRep.save(msg);
    System.out.println(savedMsg.getId());
    assertAll(
        "msg",
        () -> assertEquals(savedMsg.getTitle(), msg.getTitle()),
        () -> assertEquals(savedMsg.getSender(), msg.getSender()));
  }

  @DisplayName("通过Flux获取所有消息")
  @Order(2)
  @Test
  void getReactiveMsg() {
    Flux<Message> msg = ractMsgRep.findAll();
    System.out.println("共有消息 ： " + msg.count().block().longValue());
  }
}
