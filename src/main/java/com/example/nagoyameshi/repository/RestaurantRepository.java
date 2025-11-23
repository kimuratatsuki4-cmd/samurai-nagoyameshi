package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	public Page<Restaurant> findByNameLike(String name, Pageable pageable);

	public Restaurant findFirstByOrderByIdDesc();

	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable);

	public Page<Restaurant> findAllByOrderByLowestPriceAsc(Pageable pageable);

	@Query("SELECT DISTINCT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(
			@Param("name") String nameKeyword,
			@Param("address") String addressKeyword,
			@Param("categoryName") String categoryNameKeyword,
			Pageable pageable);

	@Query("SELECT DISTINCT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"ORDER BY r.lowestPrice DESC")
	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
			@Param("name") String nameKeyword,
			@Param("address") String addressKeyword,
			@Param("categoryName") String categoryNameKeyword,
			Pageable pageable);

	// 指定されたidのカテゴリが設定された店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"WHERE cr.category.id = :categoryId " +
			"ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Integer categoryId,
			Pageable pageable);

	// 指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"WHERE cr.category.id = :categoryId " +
			"ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findByCategoryIdOrderByLowestPriceAsc(@Param("categoryId") Integer categoryId,
			Pageable pageable);

	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);

	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price, Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findAllByOrderByAverageScoreDesc(Pageable pageable);

	// 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reviews rev " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
			@Param("name") String nameKeyword,
			@Param("address") String addressKeyword,
			@Param("categoryName") String categoryNameKeyword,
			Pageable pageable);

	// 指定されたidのカテゴリが設定された店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reviews rev " +
			"WHERE cr.category.id = :categoryId " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findByCategoryIdOrderByAverageScoreDesc(@Param("categoryId") Integer categoryId,
			Pageable pageable);

	// 指定された最低価格以下の店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"WHERE r.lowestPrice <= :price " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByAverageScoreDesc(@Param("price") Integer price,
			Pageable pageable);

	/**
	 * すべての店舗を予約数が多い順に並べ替え、ページングされた状態で取得する。
	 * 要件: Reservationエンティティを左外部結合し、店舗IDでグループ化、COUNT関数で並べ替え。
	 */
	@Query("SELECT r FROM Restaurant r LEFT JOIN r.reservations res GROUP BY r ORDER BY COUNT(res) DESC")
	public Page<Restaurant> findAllByOrderByReservationCountDesc(Pageable pageable);

	/**
	 * 指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を予約数が多い順に並べ替え、
	 * ページングされた状態で取得する。
	 * 要件: Reservationエンティティを左外部結合し、店舗IDでグループ化、COUNT(DISTINCT res.id)で並べ替え。
	 */
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reservations res " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(DISTINCT res.id) DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
			@Param("name") String nameKeyword,
			@Param("address") String addressKeyword,
			@Param("categoryName") String categoryNameKeyword,
			Pageable pageable);

	/**
	 * 指定されたidのカテゴリが設定された店舗を予約数が多い順に並べ替え、
	 * ページングされた状態で取得する。
	 * 要件: Reservationエンティティを左外部結合し、店舗IDでグループ化、COUNT関数で並べ替え。
	 */
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reservations res " +
			"WHERE cr.category.id = :categoryId " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(res) DESC")
	public Page<Restaurant> findByCategoryIdOrderByReservationCountDesc(@Param("categoryId") Integer categoryId,
			Pageable pageable);

	/**
	 * 指定された最低価格以下の店舗を予約数が多い順に並べ替え、
	 * ページングされた状態で取得する。
	 * 要件: Reservationエンティティを左外部結合し、店舗IDでグループ化、COUNT関数で並べ替え。
	 */
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reservations res " +
			"WHERE r.lowestPrice <= :price " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(res) DESC")
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByReservationCountDesc(@Param("price") Integer price,
			Pageable pageable);

	// ------------------------------------------------
	// 定休日情報の取得メソッド
	// ------------------------------------------------

	/**
	 * 指定された店舗の定休日のday_indexフィールドの値をリストで取得する。
	 * 要件: RegularHoliday、RegularHolidaysRestaurants、Restaurantエンティティを内部結合。
	 */
	@Query("SELECT rh.dayIndex FROM RegularHoliday rh " +
			"INNER JOIN rh.regularHolidaysRestaurants rhr " +
			"INNER JOIN rhr.restaurant r " +
			"WHERE r.id = :restaurantId")
	public List<Integer> findDayIndexesByRestaurantId(@Param("restaurantId") Integer restaurantId);
}
