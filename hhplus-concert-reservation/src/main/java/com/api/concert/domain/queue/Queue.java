package com.api.concert.domain.queue;

import com.api.concert.domain.queue.constant.WaitingStatus;
import com.api.concert.global.common.exception.CommonException;
import com.api.concert.global.common.model.ResponseCode;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Queue {

    private Long concertWaitingId;
    private Long userId;
    private int waitingNumber;
    private WaitingStatus status;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    private Queue(Long userId) {
        this.userId = userId;
    }

    public void toDone() {
        this.status = WaitingStatus.DONE;
    }

    public void toWait(){
        this.status = WaitingStatus.WAIT;
    }
    public void toOngoing(final long QUEUE_EXPIRED_TIME){
        this.status = WaitingStatus.ONGOING;
        this.expiredAt = LocalDateTime.now().plusMinutes(QUEUE_EXPIRED_TIME);
    }

    public void waitingNumber(int ranking) {
        this.waitingNumber = ranking;
    }

    public void ifStatusOngoingThrowException() {
        if(this.status == WaitingStatus.ONGOING){
            String message = String.format("대기열 만료 시간 [%s]", this.getExpiredAt());
            throw new CommonException(ResponseCode.ALREADY_ONGOING_USER, message);
        }
    }
}
