package com.phoenix.esdemo.message;

import com.phoenix.esdemo.entity.Message;
import com.phoenix.esdemo.repository.MessageRepository;
import com.phoenix.esdemo.repository.ReactiveMessageRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MessageTests {

  @Autowired private MessageRepository msgRep;
  @Autowired private ReactiveMessageRepository ractMsgRep;

  private long recordCount = 0;
  int tmpType = 1000;

  @AfterAll
  static void deleteAllMsg() {}

  @Order(0)
  @Test
  @DisplayName("清空所有数据")
  void clearIndex() {
    msgRep.deleteAll();
    assertTrue(0 == msgRep.count(), "数据已清空完毕");
  }

  @ParameterizedTest
  @Order(1)
  @CsvSource({
    "陶渊明,盛年不重来，一日难再晨。及时宜自勉，岁月不待人。,陶渊明,1,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg",
    "老子,千里之行，始于足下。,老子,2,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg",
    "庄子,君子之交淡若水，小人之交甘若醴，君子淡以亲，小人甘以绝。,庄子,3,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg",
    "2015年11月15日XX版本强势来袭！,亲爱的小伙伴，全新版本“逐鹿中原”将于2015年11月15日更新，将新增“逐鹿中原”功能，五大神将祝君争霸天下！,运营,4,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg",
    "服务器停机维护公告,亲爱的各位小伙伴，为了给大家一个更好的游戏体验，服务器将于XX停服维护，预计维护时间1个小时，服务器开启时间将根据实际操作情况进行提前或者延顺，给您带来的不便请您谅解，感谢您对我们的理解与支持，祝您游戏愉快！,运营,5,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg",
    "整治任务,全力整治县城和蔡家坡地区城镇环境卫生。对各路段、背街小巷和城乡结合部环境卫生以及乱搭乱建、乱停乱放、乱贴乱画、乱发传单广告等 “五乱”现象彻底整治，取缔所有马路市场、占道经营等行为，坚决消除乱倒垃圾、乱泼污水现象。,干部,6,https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1593064379767&di=c51c7e5e4cb68be7f81362efe43090de&imgtype=0&src=http%3A%2F%2Fp1.meituan.net%2Favatar%2F1d5b7593a0679ddc03240b9b6d7630fa77146.jpg"
  })
  // @CsvFileSource(resources = "/messages.csv")
  @DisplayName("初始化数据")
  void initTest(String title, String msg, String sender, Integer type, String icon) {
    recordCount++;
    String uuid = UUID.randomUUID().toString();
    Instant time = Instant.now();
    Message message = new Message(uuid, title, msg, sender, type, time, icon);
    Mono<Message> monoMsg = ractMsgRep.save(message);
    Message savedMsg = monoMsg.block();
    assertEquals(message.getTitle(), savedMsg.getTitle(), "Title一致");
  }

  @BeforeEach
  void beforeTests(TestInfo testInfo) {
    System.out.print(testInfo.getDisplayName() + " : 测试即将开始");
  }

  @DisplayName("保存新的消息")
  @Order(2)
  @Test
  void saveDoc() {
    Message msg =
        new Message(
            "99",
            "who博士",
            "和dalex对战了很多年",
            "时间领主",
            2,
            Instant.now(),
            "https://ss1.bdstatic.com/70cFuXSh_Q1YnxGkpoWK1HF6hhy/it/u=3919334208,37253891&fm=26&gp=0.jpg");
    Message savedMsg = msgRep.save(msg);
    assertAll(
        "msg",
        () -> assertEquals(savedMsg.getTitle(), msg.getTitle()),
        () -> assertEquals(savedMsg.getSender(), msg.getSender()));
  }

  @DisplayName("通过Flux获取所有消息")
  @Order(3)
  @Test
  void getReactiveMsg() {
    Flux<Message> msg = ractMsgRep.findAll();
    System.out.println("共有消息 ： " + msg.count().block().longValue());
  }

  @DisplayName("获取运营发的消息")
  @Order(4)
  @Test
  void getMesFromSenderOps() {
    String sender = "运营";
    Flux<Message> msgs =
        ractMsgRep.findBySender(sender, PageRequest.of(0, 2, Sort.by("type").descending()));
    System.out.println("消息共有: " + msgs.count().block().longValue());
    msgs.toStream()
        .forEach(
            message -> {
              System.out.println("发送者是: " + message.getSender());
              System.out.println("消息是: " + message.getMsg());
              System.out.println("类型是: " + message.getType());
              assertEquals(sender, message.getSender(), "发送者是: " + message.getSender());
              assertTrue(tmpType >= message.getType());
              tmpType = message.getType();
            });
  }
}
