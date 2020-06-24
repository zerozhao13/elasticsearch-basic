package com.phoenix.esdemo.repository;

import com.phoenix.esdemo.entity.Message;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;

public interface ReactiveMessageRepository extends ReactiveSortingRepository<Message, String> {
}
