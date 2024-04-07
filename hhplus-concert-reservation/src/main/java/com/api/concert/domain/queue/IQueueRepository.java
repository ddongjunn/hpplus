package com.api.concert.domain.queue;

import com.api.concert.infrastructure.queue.QueueEntity;

public interface IQueueRepository {
    long getOngoingCountStatusCount();

    Queue save(QueueEntity queueEntity);

    boolean existsByUserIdAndOngoingStatus(Long userId);
}
