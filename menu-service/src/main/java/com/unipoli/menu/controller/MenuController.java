package com.unipoli.menu.controller;

import com.unipoli.menu.dto.CategoryResponse;
import com.unipoli.menu.dto.CreateDishRequest;
import com.unipoli.menu.dto.DishResponse;
import com.unipoli.menu.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/dishes")
    public ResponseEntity<List<DishResponse>> findAll() {
        return ResponseEntity.ok(menuService.findAll());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> findAllCategories() {
        return ResponseEntity.ok(
                menuService.findAllCategories()
        );
    }

    @PostMapping("/dishes")
    public ResponseEntity<DishResponse> create(
            @Valid @RequestBody CreateDishRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(menuService.create(request));
    }
}
