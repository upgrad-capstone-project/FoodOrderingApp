package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CategoryDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.CategoryEntity;
import com.upgrad.FoodOrderingApp.service.entity.RestaurantEntity;
import com.upgrad.FoodOrderingApp.service.exception.CategoryNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.InvalidRatingException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private CategoryDao categoryDao;


    public List<RestaurantEntity> restaurantsByRating() {
        return restaurantDao.restaurantsByRating();
    }


    public List<RestaurantEntity> restaurantsByName(final String restaurantName) throws RestaurantNotFoundException {
        if(restaurantName.isEmpty()){
            throw new RestaurantNotFoundException("RNF-003", "Restaurant name field should not be empty");
        }

        List<RestaurantEntity> restaurantEntityList = restaurantDao.restaurantsByRating();
        List<RestaurantEntity> matchingRestaurantEntityList = new ArrayList<RestaurantEntity>();
        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            if (restaurantEntity.getRestaurantName().toLowerCase().contains(restaurantName.toLowerCase())) {
                matchingRestaurantEntityList.add(restaurantEntity);
            }
        }

        return matchingRestaurantEntityList;
    }

    public List<RestaurantEntity> restaurantByCategory(final String categoryId) throws CategoryNotFoundException {

        if (categoryId.equals("")) {
            throw new CategoryNotFoundException("CNF-001", "Category id field should not be empty");
        }

        CategoryEntity categoryEntity = categoryDao.getCategoryByUuid(categoryId);

        if(categoryEntity == null) {
            throw new CategoryNotFoundException("CNF-002", "No category by this id");
        }

        List<RestaurantEntity> restaurantEntityList = categoryEntity.getRestaurants();
        restaurantEntityList.sort(Comparator.comparing(RestaurantEntity::getRestaurantName));
        return restaurantEntityList;
    }


    public RestaurantEntity restaurantByUUID(String uuid) throws RestaurantNotFoundException {
        if (uuid.equals("")) {
            throw new RestaurantNotFoundException("RNF-002", "Restaurant id field should not be empty");
        }

        RestaurantEntity restaurantEntity = restaurantDao.getRestaurantByUUID(uuid);

        if (restaurantEntity == null) {
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
        return restaurantEntity;
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public RestaurantEntity updateRestaurantRating(RestaurantEntity restaurantEntity, Double newRating) throws InvalidRatingException {

        if (newRating < 1.0 || newRating > 5.0) {
            throw new InvalidRatingException("IRE-001", "Restaurant should be in the range of 1 to 5");
        }

        Double newAverageRating = Math.round(
                (newRating / (restaurantEntity.getNumberCustomersRated() + 1)
                        + restaurantEntity.getCustomerRating()) * 100.0) / 100.0;
        restaurantEntity.setNumberCustomersRated(restaurantEntity.getNumberCustomersRated() + 1);
        restaurantEntity.setCustomerRating(newAverageRating);

        return restaurantDao.updateRestaurantEntity(restaurantEntity);
    }
}
