package com.example.nagoyameshi.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository; // 🚨 DIのためにインポート

@Service // サービスコンポーネントとして登録
public class ReservationService {
    
    private final ReservationRepository reservationRepository; // 🚨 リポジトリをフィールドとして定義
    
    // 🚨 コンストラクタインジェクション (DI)
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
        /**
     * 指定したIDを持つ予約を取得する。
     */
    public Optional<Reservation> findReservationById(Integer id) {
        return reservationRepository.findById(id);
    }

    /**
     * 指定されたユーザーに紐づく予約を予約日時が新しい順（未来→過去）に並べ替え、
     * ページングされた状態で取得する。
     */
    public Page<Reservation> findReservationsByUserOrderByReservedDatetimeDesc(User user, Pageable pageable) {
        return reservationRepository.findByUserOrderByReservedDatetimeDesc(user, pageable);
    }

    /**
     * 予約のレコード総数を取得する。
     */
    public long countReservations() {
        return reservationRepository.count();
    }

    /**
     * IDが最も大きい予約を取得する。（最新の予約を取得）
     */
    public Reservation findFirstReservationByOrderByIdDesc() {
        return reservationRepository.findTopByOrderByIdDesc();
    }

    /**
     * フォームから送信された予約情報をデータベースに登録する。
     */
    public void createReservation(Restaurant restaurant, User user, ReservationRegisterForm reservationRegisterForm) {
        Reservation reservation = new Reservation();
        
        // 日付と時間を結合してLocalDateTimeを作成
        LocalDateTime reservedDateTime = LocalDateTime.of(
            reservationRegisterForm.getReservationDate(), 
            reservationRegisterForm.getReservationTime()
        );
        
        reservation.setRestaurant(restaurant);
        reservation.setUser(user);
        reservation.setReservedDatetime(reservedDateTime);
        reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
        
        reservationRepository.save(reservation);
    }

    /**
     * 指定した予約をデータベースから削除する。
     */
    public void deleteReservation(Reservation reservation) {
        reservationRepository.delete(reservation);
    }


    /**
     * 予約日時が現在よりも2時間以上後であればtrueを返す。
     */
    public boolean isAtLeastTwoHoursInFuture(LocalDateTime reservationDateTime) {
        // 予約日時が、現在時刻の2時間後以降であるかを確認 (2時間後と等しい場合も含む)
        return java.time.Duration.between(LocalDateTime.now(), reservationDateTime).toHours() >= 2;
    }
}