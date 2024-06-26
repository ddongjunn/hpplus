package com.api.concert.application;

import com.api.concert.controller.payment.dto.PaymentRequest;
import com.api.concert.controller.payment.dto.PaymentResponse;
import com.api.concert.controller.point.dto.PointUseRequest;
import com.api.concert.domain.concert.*;
import com.api.concert.domain.point.PointService;
import com.api.concert.global.common.model.ResponseCode;
import com.api.concert.infrastructure.concert.projection.ReservationInfoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentFacade {

    private final ConcertService concertService;
    private final ConcertSeatService concertSeatService;
    private final PointService pointService;
    private final ReservationService reservationService;

    public PaymentResponse payment(PaymentRequest paymentRequest) {
        Long userId = paymentRequest.userId();

        //임시 예약된 좌석들 가져오기
        List<ConcertSeat> temporarilyReservedSeats = concertSeatService.findSeatTemporarilyReservedByUserId(userId);
        Long paymentAmount = temporarilyReservedSeats.stream().mapToLong(ConcertSeat::getPrice).sum();

        //예약된 좌석들의 총 금액 결제
        PointUseRequest pointUseRequest = PointUseRequest.builder().userId(userId).point(paymentAmount).build();
        pointService.use(pointUseRequest);

        //예약된 좌석들 상태 업데이트
        List<ConcertSeat> concertSeats = concertSeatService.updateSeatToReserved(userId, temporarilyReservedSeats);

         /***
         * ConcertOptionId 로 Concert 메타 데이터 가져오기
         * reservation_history 저장
         */
        concertSeats.forEach(
                concertSeat ->
                {
                    ReservationInfoProjection concertInformation = concertService.findConcertInformation(concertSeat.getConcertOptionId());
                    reservationService.save(Reservation.builder()
                                    .userId(concertSeat.getUserId())
                                    .name(concertInformation.getName())
                                    .singer(concertInformation.getSinger())
                                    .seatNo(concertSeat.getSeatNo())
                                    .price(concertSeat.getPrice())
                                    .StartAt(concertInformation.getStartDate())
                                    .build()
                    );
                }
        );

        return PaymentResponse.builder()
                .code(ResponseCode.SUCCESS)
                .build();
    }
}
