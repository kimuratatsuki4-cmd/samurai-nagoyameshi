package com.example.nagoyameshi.repository;

import java.time.LocalTime;
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

	/**
	 * 評価点関連のフィルタ
	 * 指定されたスコア以上の平均評価を持つ店舗を、平均評価が高い順に並べ替えて取得する。
	 * 要件: Reviewエンティティを結合し、GROUP BYとHAVINGで平均値を判定。
	 */
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"GROUP BY r.id " +
			"HAVING AVG(rev.score) >= :minRating " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findByAverageScoreGreaterThanEqualOrderByAverageScoreDesc(
			@Param("minRating") Double minRating,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"GROUP BY r.id " +
			"HAVING AVG(rev.score) >= :minRating " +
			"ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByAverageScoreGreaterThanEqualOrderByCreatedAtDesc(@Param("minRating") Double minRating,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"GROUP BY r.id " +
			"HAVING AVG(rev.score) >= :minRating " +
			"ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findByAverageScoreGreaterThanEqualOrderByLowestPriceAsc(
			@Param("minRating") Double minRating,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"LEFT JOIN r.reservations res " +
			"GROUP BY r.id " +
			"HAVING AVG(rev.score) >= :minRating " +
			"ORDER BY COUNT(res) DESC")
	public Page<Restaurant> findByAverageScoreGreaterThanEqualOrderByReservationCountDesc(
			@Param("minRating") Double minRating,
			Pageable pageable);

	/**
	 * 営業中の店舗関連のフィルタ
	 * 現在営業中の店舗をページングされた状態で取得する。
	 * 条件: 現在時刻が営業時間内 かつ 本日が定休日でない。
	 * 定休日判定: NOT EXISTSサブクエリを使用し、該当店舗の定休日リストに本日のdayIndexが含まれないことを確認。
	 */
	@Query("SELECT r FROM Restaurant r " +
			"WHERE :currentTime BETWEEN r.openingTime AND r.closingTime " +
			"AND NOT EXISTS (" +
			"    SELECT rhr FROM r.regularHolidaysRestaurants rhr " +
			"    WHERE rhr.regularHoliday.dayIndex = :dayIndex" +
			") " +
			"ORDER BY r.id")
	public Page<Restaurant> findOpenRestaurants(
			@Param("currentTime") LocalTime currentTime,
			@Param("dayIndex") Integer dayIndex,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"WHERE :currentTime BETWEEN r.openingTime AND r.closingTime " +
			"AND NOT EXISTS (" +
			"    SELECT rhr FROM r.regularHolidaysRestaurants rhr " +
			"    WHERE rhr.regularHoliday.dayIndex = :dayIndex" +
			") " +
			"ORDER BY r.lowestPrice ASC")
	public Page<Restaurant> findOpenRestaurantsOrderByLowestPriceAsc(
			@Param("currentTime") LocalTime currentTime,
			@Param("dayIndex") Integer dayIndex,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reviews rev " +
			"WHERE :currentTime BETWEEN r.openingTime AND r.closingTime " +
			"AND NOT EXISTS (" +
			"    SELECT rhr FROM r.regularHolidaysRestaurants rhr " +
			"    WHERE rhr.regularHoliday.dayIndex = :dayIndex" +
			") " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rev.score) DESC")
	public Page<Restaurant> findOpenRestaurantsOrderByAverageScoreDesc(
			@Param("currentTime") LocalTime currentTime,
			@Param("dayIndex") Integer dayIndex,
			Pageable pageable);

	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reservations res " +
			"WHERE :currentTime BETWEEN r.openingTime AND r.closingTime " +
			"AND NOT EXISTS (" +
			"    SELECT rhr FROM r.regularHolidaysRestaurants rhr " +
			"    WHERE rhr.regularHoliday.dayIndex = :dayIndex" +
			") " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(res) DESC")
	public Page<Restaurant> findOpenRestaurantsOrderByReservationCountDesc(
			@Param("currentTime") LocalTime currentTime,
			@Param("dayIndex") Integer dayIndex,
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

	// 1. 全件検索 × 距離順
	@Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance FROM restaurants WHERE latitude IS NOT NULL AND longitude IS NOT NULL ORDER BY distance ASC", countQuery = "SELECT count(*) FROM restaurants WHERE latitude IS NOT NULL AND longitude IS NOT NULL", nativeQuery = true)
	public Page<Restaurant> findAllByOrderByDistanceAsc(@Param("lat") Double lat, @Param("lng") Double lng,
			Pageable pageable);

	// 2. キーワード検索 × 距離順
	@Query(value = "SELECT DISTINCT r.*, (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) AS distance "
			+
			"FROM restaurants r " +
			"LEFT JOIN categories_restaurants cr ON r.id = cr.restaurant_id " +
			"LEFT JOIN categories c ON cr.category_id = c.id " +
			"WHERE (r.name LIKE :keyword OR r.address LIKE :keyword OR c.name LIKE :keyword) " +
			"AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
			"ORDER BY distance ASC", countQuery = "SELECT count(DISTINCT r.id) FROM restaurants r LEFT JOIN categories_restaurants cr ON r.id = cr.restaurant_id LEFT JOIN categories c ON cr.category_id = c.id WHERE (r.name LIKE :keyword OR r.address LIKE :keyword OR c.name LIKE :keyword) AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL", nativeQuery = true)
	public Page<Restaurant> findByKeywordOrderByDistanceAsc(@Param("keyword") String keyword, @Param("lat") Double lat,
			@Param("lng") Double lng, Pageable pageable);

	// 3. カテゴリ検索 × 距離順
	@Query(value = "SELECT r.*, (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) AS distance "
			+
			"FROM restaurants r " +
			"INNER JOIN categories_restaurants cr ON r.id = cr.restaurant_id " +
			"WHERE cr.category_id = :categoryId " +
			"AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
			"ORDER BY distance ASC", countQuery = "SELECT count(*) FROM restaurants r INNER JOIN categories_restaurants cr ON r.id = cr.restaurant_id WHERE cr.category_id = :categoryId AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL", nativeQuery = true)
	public Page<Restaurant> findByCategoryIdOrderByDistanceAsc(@Param("categoryId") Integer categoryId,
			@Param("lat") Double lat, @Param("lng") Double lng, Pageable pageable);

	// 4. 価格検索 × 距離順
	@Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance "
			+
			"FROM restaurants " +
			"WHERE lowest_price <= :price " +
			"AND latitude IS NOT NULL AND longitude IS NOT NULL " +
			"ORDER BY distance ASC", countQuery = "SELECT count(*) FROM restaurants WHERE lowest_price <= :price AND latitude IS NOT NULL AND longitude IS NOT NULL", nativeQuery = true)
	public Page<Restaurant> findByPriceLessThanEqualOrderByDistanceAsc(@Param("price") Integer price,
			@Param("lat") Double lat, @Param("lng") Double lng, Pageable pageable);

	// 5. 営業中検索 × 距離順
	@Query(value = "SELECT *, (6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(latitude)))) AS distance "
			+
			"FROM restaurants r " +
			"WHERE :currentTime BETWEEN r.opening_time AND r.closing_time " +
			"AND NOT EXISTS (SELECT 1 FROM regular_holidays_restaurants rhr JOIN regular_holidays rh ON rhr.regular_holiday_id = rh.id WHERE rhr.restaurant_id = r.id AND rh.day_index = :dayIndex) "
			+
			"AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
			"ORDER BY distance ASC", countQuery = "SELECT count(*) FROM restaurants r WHERE :currentTime BETWEEN r.opening_time AND r.closing_time AND NOT EXISTS (SELECT 1 FROM regular_holidays_restaurants rhr JOIN regular_holidays rh ON rhr.regular_holiday_id = rh.id WHERE rhr.restaurant_id = r.id AND rh.day_index = :dayIndex) AND r.latitude IS NOT NULL AND r.longitude IS NOT NULL", nativeQuery = true)
	public Page<Restaurant> findOpenRestaurantsOrderByDistanceAsc(@Param("currentTime") LocalTime currentTime,
			@Param("dayIndex") Integer dayIndex, @Param("lat") Double lat, @Param("lng") Double lng, Pageable pageable);

	// 6. 評価検索 × 距離順
	@Query(value = "SELECT r.*, (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))) AS distance "
			+
			"FROM restaurants r " +
			"LEFT JOIN reviews rev ON r.id = rev.restaurant_id " +
			"WHERE r.latitude IS NOT NULL AND r.longitude IS NOT NULL " +
			"GROUP BY r.id " +
			"HAVING AVG(rev.score) >= :minRating " +
			"ORDER BY distance ASC", countQuery = "SELECT count(*) FROM (SELECT r.id FROM restaurants r LEFT JOIN reviews rev ON r.id = rev.restaurant_id WHERE r.latitude IS NOT NULL AND r.longitude IS NOT NULL GROUP BY r.id HAVING AVG(rev.score) >= :minRating) as temp", nativeQuery = true)
	public Page<Restaurant> findByAverageScoreGreaterThanEqualOrderByDistanceAsc(@Param("minRating") Double minRating,
			@Param("lat") Double lat, @Param("lng") Double lng, Pageable pageable);

}
