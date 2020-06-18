package com.phoenix.esdemo.repository;

import com.phoenix.esdemo.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface MessageRepositroy extends ElasticsearchRepository <Message, Long>{
    Page<Message> findBySender(String sender, Pageable pageable);

    List<Message> findByTitle(String title);
    List<Message> findByTitleAndMsg(String keyword);
}
