package com.example.bookstore.impl;

import com.example.bookstore.dto.request.AuthorRequest;
import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.entity.Author;
import com.example.bookstore.exception.custom.DuplicateResourceException;
import com.example.bookstore.exception.custom.ResourceNotFoundException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.service.AuthorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public AuthorResponse create(AuthorRequest request) {
        if (authorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ya existe un autor con ese nombre");
        }

        Author author = Author.builder()
                .name(request.getName())
                .biography(request.getBiography())
                .build();

        return toResponse(authorRepository.save(author));
    }

    @Override
    public List<AuthorResponse> findAll() {
        return authorRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AuthorResponse findById(Long id) {
        return toResponse(getAuthor(id));
    }

    @Override
    public AuthorResponse update(Long id, AuthorRequest request) {
        Author author = getAuthor(id);

        if (!author.getName().equalsIgnoreCase(request.getName())
                && authorRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("Ya existe un autor con ese nombre");
        }

        author.setName(request.getName());
        author.setBiography(request.getBiography());
        return toResponse(authorRepository.save(author));
    }

    @Override
    public void delete(Long id) {
        authorRepository.delete(getAuthor(id));
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado"));
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName(), author.getBiography());
    }
}
