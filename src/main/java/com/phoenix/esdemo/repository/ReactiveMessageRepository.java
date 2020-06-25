package com.phoenix.esdemo.repository;

import com.phoenix.esdemo.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

public interface ReactiveMessageRepository extends ReactiveSortingRepository<Message, String> {
    Flux<Message> findBySender(String sender, Pageable pageable);
}
