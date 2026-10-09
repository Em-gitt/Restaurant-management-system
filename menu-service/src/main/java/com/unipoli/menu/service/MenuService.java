package com.unipoli.menu.service;

import com.unipoli.menu.dto.CategoryResponse;
import com.unipoli.menu.dto.CreateDishRequest;
import com.unipoli.menu.dto.DishResponse;
import com.unipoli.menu.model.Category;
import com.unipoli.menu.model.Dish;
import com.unipoli.menu.repository.CategoryRepository;
import com.unipoli.menu.repository.DishRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;

    public MenuService(
            DishRepository dishRepository,
            CategoryRepository categoryRepository
    ) {
        this.dishRepository = dishRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<DishResponse> findAll() {
        return dishRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CategoryResponse> findAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName()
                ))
                .toList();
    }

    public DishResponse create(CreateDishRequest request) {

        Category category = categoryRepository
                .findById(request.categoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category not found: " + request.categoryId()
                        )
                );

        Dish dish = new Dish();

        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setPrice(request.price());
        dish.setCategory(category);

        dish.setAvailable(
                request.available() != null
                        ? request.available()
                        : true
        );

        return toResponse(dishRepository.save(dish));
    }

    private DishResponse toResponse(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getCategory().getId(),
                dish.getCategory().getName(),
                dish.getPrice(),
                dish.getAvailable()
        );
    }
}
