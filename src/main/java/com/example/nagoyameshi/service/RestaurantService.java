package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
@Transactional
public class RestaurantService {
	private final RestaurantRepository restaurantRepository;
	private final CategoryRestaurantService categoryRestaurantService;
	private final RegularHolidayRestaurantService regularHolidayRestaurantService;

	public RestaurantService(RestaurantRepository restaurantRepository,
			CategoryRestaurantService categoryRestaurantService,
			RegularHolidayRestaurantService regularHolidayRestaurantService) {
		this.restaurantRepository = restaurantRepository;
		this.categoryRestaurantService = categoryRestaurantService;
		this.regularHolidayRestaurantService = regularHolidayRestaurantService;
	}

	// =========================================================================
	// 1. 基本的なCRUD/単一データ取得メソッド
	// =========================================================================

	// 指定したidを持つ店舗を取得する
	public Optional<Restaurant> findRestaurantById(Integer id) {
		return restaurantRepository.findById(Objects.requireNonNull(id));
	}

	// 店舗のレコード数を取得する
	public long countRestaurants() {
		return restaurantRepository.count();
	}

	// idが最も大きい店舗を取得する
	public Restaurant findFirstRestaurantByOrderByIdDesc() {
		return restaurantRepository.findFirstByOrderByIdDesc();
	}

	// 店舗情報を作成する
	public void createRestaurant(RestaurantRegisterForm restaurantRegisterForm) {
		Restaurant restaurant = new Restaurant();
		List<Integer> categoryIds = restaurantRegisterForm.getCategoryIds();
		List<Integer> regularholidayIds = restaurantRegisterForm.getRegularHolidayIds();
		MultipartFile imageFile = restaurantRegisterForm.getImageFile();

		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);
		}

		restaurant.setName(restaurantRegisterForm.getName());
		restaurant.setDescription(restaurantRegisterForm.getDescription());
		restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
		restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
		restaurant.setAddress(restaurantRegisterForm.getAddress());
		restaurant.setOpeningTime(restaurantRegisterForm.getOpeningTime());
		restaurant.setClosingTime(restaurantRegisterForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantRegisterForm.getSeatingCapacity());
		restaurant.setLatitude(restaurantRegisterForm.getLatitude());
		restaurant.setLongitude(restaurantRegisterForm.getLongitude());

		restaurantRepository.save(Objects.requireNonNull(restaurant));

		if (categoryIds != null) {
			categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);
		}

		if (regularholidayIds != null) {
			regularHolidayRestaurantService.createRegularHolidaysRestaurants(regularholidayIds, restaurant);
		}
	}

	// 店舗情報を更新する
	public void updateRestaurant(RestaurantEditForm restaurantEditForm, Restaurant restaurant) {
		MultipartFile imageFile = restaurantEditForm.getImageFile();
		List<Integer> categoryIds = restaurantEditForm.getCategoryIds();
		List<Integer> regularholidayIds = restaurantEditForm.getRegularHolidayIds();

		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			restaurant.setImage(hashedImageName);
		}

		restaurant.setName(restaurantEditForm.getName());
		restaurant.setDescription(restaurantEditForm.getDescription());
		restaurant.setLowestPrice(restaurantEditForm.getLowestPrice());
		restaurant.setHighestPrice(restaurantEditForm.getHighestPrice());
		restaurant.setPostalCode(restaurantEditForm.getPostalCode());
		restaurant.setAddress(restaurantEditForm.getAddress());
		restaurant.setOpeningTime(restaurantEditForm.getOpeningTime());
		restaurant.setClosingTime(restaurantEditForm.getClosingTime());
		restaurant.setSeatingCapacity(restaurantEditForm.getSeatingCapacity());
		restaurant.setLatitude(restaurantEditForm.getLatitude());
		restaurant.setLongitude(restaurantEditForm.getLongitude());

		restaurantRepository.save(Objects.requireNonNull(restaurant));

		if (categoryIds != null) {
			categoryRestaurantService.syncCategoriesRestaurants(categoryIds, restaurant);
		}

		if (regularholidayIds != null) {
			regularHolidayRestaurantService.syncRegularHolidaysRestaurants(regularholidayIds, restaurant);
		}
	}

	// 店舗情報を削除する
	public void deleteRestaurant(Restaurant restaurant) {
		restaurantRepository.delete(Objects.requireNonNull(restaurant));
	}

	// =========================================================================
	// 2. 基本検索・定休日取得メソッド
	// =========================================================================

	// すべての店舗をページングされた状態で取得する
	public Page<Restaurant> findAllRestaurants(Pageable pageable) {
		return restaurantRepository.findAll(Objects.requireNonNull(pageable));
	}

	// 指定されたキーワードを店舗名に含む店舗を、ページングされた状態で取得する
	public Page<Restaurant> findRestaurantsByNameLike(String keyword, Pageable pageable) {
		return restaurantRepository.findByNameLike("%" + keyword + "%", Objects.requireNonNull(pageable));
	}

	// 指定された店舗の定休日のday_indexフィールドの値をリストで取得する
	public List<Integer> findDayIndexesByRestaurantId(Integer restaurantId) {
		return restaurantRepository.findDayIndexesByRestaurantId(Objects.requireNonNull(restaurantId));
	}

	// =========================================================================
	// 3. 複雑な検索・並べ替えメソッド (OrderBy別)
	// =========================================================================

	// --- 3.1. 作成日時 (CreatedAtDesc) による並べ替え ---

	public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable) {
		return restaurantRepository.findAllRestaurantsByOrderByCreatedAtDesc(Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(nameKeyword,
				addressKeyword, categoryNameKeyword, Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByCreatedAtDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(Objects.requireNonNull(categoryId),
				Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(Objects.requireNonNull(price),
				Objects.requireNonNull(pageable));
	}

	// --- 3.2. 最低価格 (LowestPriceAsc) による並べ替え ---

	// すべての店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
	public Page<Restaurant> findAllRestaurantsByOrderByLowestPriceAsc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByLowestPriceAsc(Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
			String nameKeyword, String addressKeyword, String categoryNameKeyword, Pageable pageable) {
		return restaurantRepository.findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(
				nameKeyword, addressKeyword, categoryNameKeyword, Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByLowestPriceAsc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(Objects.requireNonNull(categoryId),
				Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(Objects.requireNonNull(price),
				Objects.requireNonNull(pageable));
	}

	// --- 3.3. 平均評価スコア (AverageScoreDesc) による並べ替え ---

	public Page<Restaurant> findAllRestaurantsByOrderByAverageScoreDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByAverageScoreDesc(Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
			String nameKeyword,
			String addressKeyword,
			String categoryNameKeyword,
			Pageable pageable) {

		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(
				nameKeyword,
				addressKeyword,
				categoryNameKeyword,
				Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByAverageScoreDesc(Integer categoryId, Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(Objects.requireNonNull(categoryId),
				Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByAverageScoreDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(Objects.requireNonNull(price),
				Objects.requireNonNull(pageable));
	}

	// --- 3.4. 予約数 (ReservationCountDesc) による並べ替え ---

	public Page<Restaurant> findAllRestaurantsByOrderByReservationCountDesc(Pageable pageable) {
		return restaurantRepository.findAllByOrderByReservationCountDesc(Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
			String nameKeyword,
			String addressKeyword,
			String categoryNameKeyword,
			Pageable pageable) {
		return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(
				nameKeyword,
				addressKeyword, categoryNameKeyword, Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByCategoryIdOrderByReservationCountDesc(Integer categoryId,
			Pageable pageable) {
		return restaurantRepository.findByCategoryIdOrderByReservationCountDesc(Objects.requireNonNull(categoryId),
				Objects.requireNonNull(pageable));
	}

	public Page<Restaurant> findRestaurantsByLowestPriceLessThanEqualOrderByReservationCountDesc(Integer price,
			Pageable pageable) {
		return restaurantRepository.findByLowestPriceLessThanEqualOrderByReservationCountDesc(
				Objects.requireNonNull(price), Objects.requireNonNull(pageable));
	}

	// --- 3.5. 現在営業中の店舗検索---
	/**
	 * 現在時刻に基づいて営業中の店舗を検索する。
	 * 現在の曜日と時刻を取得し、Repositoryへ渡す。
	 */
	public Page<Restaurant> findOpenRestaurants(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();

		// Javaの曜日(1:月~7:日)をDBのday_index(1:月~6:土, 0:日)に変換
		int dayIndex = now.getDayOfWeek().getValue();
		if (dayIndex == 7) {
			dayIndex = 0;
		}

		return restaurantRepository.findOpenRestaurants(currentTime, dayIndex, Objects.requireNonNull(pageable));
	}

	// 現在営業中 × 価格が安い順
	public Page<Restaurant> findOpenRestaurantsOrderByLowestPriceAsc(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();
		int dayIndex = now.getDayOfWeek().getValue();
		if (dayIndex == 7)
			dayIndex = 0;

		return restaurantRepository.findOpenRestaurantsOrderByLowestPriceAsc(currentTime, dayIndex,
				Objects.requireNonNull(pageable));
	}

	// 現在営業中 × 評価が高い順
	public Page<Restaurant> findOpenRestaurantsOrderByAverageScoreDesc(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();
		int dayIndex = now.getDayOfWeek().getValue();
		if (dayIndex == 7)
			dayIndex = 0;

		return restaurantRepository.findOpenRestaurantsOrderByAverageScoreDesc(currentTime, dayIndex,
				Objects.requireNonNull(pageable));
	}

	// 現在営業中 × 予約数が多い順
	public Page<Restaurant> findOpenRestaurantsOrderByReservationCountDesc(Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		LocalTime currentTime = now.toLocalTime();
		int dayIndex = now.getDayOfWeek().getValue();
		if (dayIndex == 7)
			dayIndex = 0;

		return restaurantRepository.findOpenRestaurantsOrderByReservationCountDesc(currentTime, dayIndex,
				Objects.requireNonNull(pageable));
	}

	// --- 3.6. 指定された評価以上の店舗を検索する
	public Page<Restaurant> findRestaurantsByMinRating(Double minRating, Pageable pageable) {
		return restaurantRepository.findByAverageScoreGreaterThanEqualOrderByAverageScoreDesc(
				Objects.requireNonNull(minRating), Objects.requireNonNull(pageable));
	}

	// 評価で絞込 × 新着順
	public Page<Restaurant> findRestaurantsByMinRatingOrderByCreatedAtDesc(Double minRating, Pageable pageable) {
		return restaurantRepository.findByAverageScoreGreaterThanEqualOrderByCreatedAtDesc(
				Objects.requireNonNull(minRating), Objects.requireNonNull(pageable));
	}

	// 評価で絞込 × 価格が安い順
	public Page<Restaurant> findRestaurantsByMinRatingOrderByLowestPriceAsc(Double minRating, Pageable pageable) {
		return restaurantRepository.findByAverageScoreGreaterThanEqualOrderByLowestPriceAsc(
				Objects.requireNonNull(minRating), Objects.requireNonNull(pageable));
	}

	// 評価で絞込 × 予約数が多い順
	public Page<Restaurant> findRestaurantsByMinRatingOrderByReservationCountDesc(Double minRating, Pageable pageable) {
		return restaurantRepository.findByAverageScoreGreaterThanEqualOrderByReservationCountDesc(
				Objects.requireNonNull(minRating), Objects.requireNonNull(pageable));
	}

	// =========================================================================
	// 4. ユーティリティメソッド (画像処理、バリデーション)
	// =========================================================================

	// UUIDを使って生成したファイル名を返す
	public String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		// ファイル名をUUIDに変更する処理（拡張子は維持）
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}

	// 画像ファイルを指定したファイルにコピーする
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 価格が正しく設定されているかどうかをチェックする (最高価格 >= 最低価格)
	public boolean isValidPrices(Integer lowestPrice, Integer highestPrice) {
		return highestPrice >= lowestPrice;
	}

	// 営業時間が正しく設定されているかどうかをチェックする (閉店時間 > 開店時間)
	public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {
		return closingTime.isAfter(openingTime);
	}
}