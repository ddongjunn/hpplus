package com.api.concert.infrastructure.queue;

import com.api.concert.domain.queue.Queue;
import com.api.concert.domain.queue.constant.WaitingStatus;
import com.api.concert.infrastructure.queue.projection.WaitingRank;
import jakarta.persistence.LockModeType;
import org.hibernate.LockMode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QueueJpaRepository extends JpaRepository<QueueEntity, Long> {

    List<QueueEntity> findByStatusAndExpiredAtIsBefore(WaitingStatus status, LocalDateTime queueExpiredTime);

    @Query("SELECT q FROM QueueEntity q WHERE q.status = :status")
    List<QueueEntity> findWaitStatusOrderByCreatedAt(@Param("status") WaitingStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE QueueEntity q SET q.status = :status, q.isExpired = :isExpired, q.expiredAt = :expiredAt WHERE q.queueId = :id")
    void updateStatusAndExpiredAtById(@Param("status") WaitingStatus status, @Param("isExpired") boolean isExpired, @Param("expiredAt") LocalDateTime expiredAt, @Param("id") Long id);

    @Query(value = "SELECT ranking " +
                    "FROM ( " +
                        "SELECT rank() over (ORDER BY created_at) AS ranking, queue_id " +
                        "FROM queue " +
                        "WHERE status = :status " +
                    ") AS ranked_waiting " +
                "WHERE queue_id = :id", nativeQuery = true)
    WaitingRank findWaitingRankById(@Param("id") Long id, @Param("status") String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueEntity q WHERE q.status = :status")
    List<QueueEntity> findByStatusWithPessimisticLock(@Param("status") WaitingStatus status);

    Optional<QueueEntity> findByUserIdAndStatusIn(Long userId, List<WaitingStatus> asList);

    long countByStatus(WaitingStatus ongoing);
}
