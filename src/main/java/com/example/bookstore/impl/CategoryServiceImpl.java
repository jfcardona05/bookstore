package com.example.bookstore.impl;

import com.example.bookstore.dto.request.CategoryRequest;
import com.example.bookstore.dto.response.CategoryResponse;
import com.example.bookstore.entity.Category;
import com.example.bookstore.exception.custom.DuplicateResourceException;
import com.example.bookstore.exception.custom.ResourceNotFoundException;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ya existe una categor\u00eda con ese nombre");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public CategoryResponse findById(Long id) {
        return toResponse(getCategory(id));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategory(id);

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ya existe una categor\u00eda con ese nombre");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.delete(getCategory(id));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categor\u00eda no encontrada"));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
