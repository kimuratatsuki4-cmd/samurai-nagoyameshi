package com.example.nagoyameshi.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
    
    /**
     * 指定した店舗とユーザーが紐づいたお気に入りを取得するメソッド
     * * @param restaurant 検索対象の店舗エンティティ
     * @param user 検索対象のユーザーエンティティ
     * @return 該当するお気に入り (存在しない場合はOptional.empty())
     */
    public Optional<Favorite> findByRestaurantAndUser(Restaurant restaurant, User user);

    /**
     * 指定したユーザーのすべてのお気に入りを、作成日時が新しい順に並べ替え、ページングされた状態で取得するメソッド
     * * @param user 検索対象のユーザーエンティティ
     * @param pageable ページング情報 (ページ番号、1ページあたりの件数など)
     * @return ページングされたお気に入りリスト
     */
    public Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}