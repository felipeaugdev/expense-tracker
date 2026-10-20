package com.felipeaugdev;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.felipeaugdev.dto.CategoryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    @GetMapping
    public List<Category> getCategories() {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            // Guest -> only system categories
            return categoryRepository.findByUserIsNull();
        }

        // Logged-in user -> system categories + their own
        return categoryRepository.findByUserIsNullOrUserId(currentUser.getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody CategoryRequest request) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new RuntimeException("You must be logged in to create a category");
        }

        String name = request.getName().trim().toUpperCase();

        Category category = new Category(name, currentUser);
        return categoryRepository.save(category);
    }
}
