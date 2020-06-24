package com.phoenix.esdemo.repository;

import com.phoenix.esdemo.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

public interface MessageRepository extends ElasticsearchRepository <Message, String>{
    Page<Message> findBySender(String sender, Pageable pageable);

    Page<Message> findByTitle(String title, Pageable pageable);

    @Async
    Future<Message> findTopByMsg(String msg);

    Page<Message> findByTitleAndMsg(String title, String msg, Pageable pageable);
}
