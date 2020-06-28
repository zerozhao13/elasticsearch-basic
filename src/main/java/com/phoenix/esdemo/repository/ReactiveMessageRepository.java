package com.phoenix.esdemo.repository;

import com.phoenix.esdemo.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

/**
 * 通过Reactive进行Message操作
 * 返回类型为 Flux<T> 或 Mono<T>
 * ReactiveSortingRepository 继承了 ReactiveCrudRepository，所以我们直接继承ReactiveSortingRepository
 * 在写法上除了返回类型不同，其它与MessageRepository类似
 */
public interface ReactiveMessageRepository extends ReactiveSortingRepository<Message, String> {
    Flux<Message> findBySender(String sender, Pageable pageable);
}
