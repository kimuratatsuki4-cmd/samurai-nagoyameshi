package com.example.nagoyameshi.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.FavoriteRepository;

@Service
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 指定したIDを持つお気に入りを取得する。
     * 
     * @param id お気に入りID
     * @return 該当するお気に入り (存在しない場合はOptional.empty())
     */
    public Optional<Favorite> findFavoriteById(Integer id) {
        return favoriteRepository.findById(Objects.requireNonNull(id));
    }

    /**
     * 指定した店舗とユーザーが紐づいたお気に入りを取得する。
     * 
     * @param restaurant 検索対象の店舗エンティティ
     * @param user       検索対象のユーザーエンティティ
     * @return 該当するお気に入り (存在しない場合はOptional.empty())
     */
    public Optional<Favorite> findFavoriteByRestaurantAndUser(Restaurant restaurant, User user) {
        return favoriteRepository.findByRestaurantAndUser(Objects.requireNonNull(restaurant),
                Objects.requireNonNull(user));
    }

    /**
     * 指定したユーザーのすべてのお気に入りを、作成日時が新しい順に並べ替え、ページングされた状態で取得する。
     * 
     * @param user     検索対象のユーザーエンティティ
     * @param pageable ページング情報
     * @return ページングされたお気に入りリスト
     */
    public Page<Favorite> findFavoritesByUserOrderByCreatedAtDesc(User user, Pageable pageable) {
        return favoriteRepository.findByUserOrderByCreatedAtDesc(Objects.requireNonNull(user),
                Objects.requireNonNull(pageable));
    }

    /**
     * お気に入りのレコード数を取得する。
     * 
     * @return お気に入りの総数
     */
    public long countFavorites() {
        return favoriteRepository.count();
    }

    /**
     * お気に入りをデータベースに登録する。
     * 
     * @param favorite 登録するお気に入りエンティティ
     * @return 登録されたお気に入りエンティティ
     */
    @Transactional
    public void createFavorite(Restaurant restaurant, User user) {
        Favorite favorite = new Favorite();
        favorite.setRestaurant(restaurant);
        favorite.setUser(user);
        favoriteRepository.save(Objects.requireNonNull(favorite));
    }

    /**
     * 指定したお気に入りをデータベースから削除する。
     * 
     * @param favorite 削除するお気に入りエンティティ
     */
    @Transactional
    public void deleteFavorite(Favorite favorite) {
        favoriteRepository.delete(Objects.requireNonNull(favorite));
    }

    /**
     * 指定したIDのお気に入りをデータベースから削除する。
     * 
     * @param id 削除するお気に入りID
     */
    @Transactional
    public void deleteFavoriteById(Integer id) {
        favoriteRepository.deleteById(Objects.requireNonNull(id));
    }

    /**
     * 指定したユーザーが指定した店舗をすでにお気に入りに追加済みかどうかをチェックする。
     * 
     * @param restaurant 店舗エンティティ
     * @param user       ユーザーエンティティ
     * @return すでにお気に入りに追加済みであればtrue、そうでなければfalse
     */
    public boolean isFavorite(Restaurant restaurant, User user) {
        return favoriteRepository
                .findByRestaurantAndUser(Objects.requireNonNull(restaurant), Objects.requireNonNull(user)).isPresent();
    }
}