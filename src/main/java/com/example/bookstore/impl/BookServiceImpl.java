package com.example.bookstore.impl;

import com.example.bookstore.dto.request.BookRequest;
import com.example.bookstore.dto.response.AuthorResponse;
import com.example.bookstore.dto.response.BookResponse;
import com.example.bookstore.dto.response.CategoryResponse;
import com.example.bookstore.entity.Author;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.custom.DuplicateResourceException;
import com.example.bookstore.exception.custom.ResourceNotFoundException;
import com.example.bookstore.repository.AuthorRepository;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.CategoryRepository;
import com.example.bookstore.service.BookService;
import com.example.bookstore.service.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public BookServiceImpl(BookRepository bookRepository,
                           AuthorRepository authorRepository,
                           CategoryRepository categoryRepository,
                           CurrentUserService currentUserService) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    public BookResponse create(BookRequest request) {
        if (bookRepository.existsByIsbnIgnoreCase(request.getIsbn())) {
            throw new DuplicateResourceException("Ya existe un libro con ese ISBN");
        }

        User user = currentUserService.getCurrentUser();
        Author author = getAuthor(request.getAuthorId());
        Category category = getCategory(request.getCategoryId());

        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .author(author)
                .category(category)
                .createdBy(user)
                .build();

        return toResponse(bookRepository.save(book));
    }

    @Override
    public List<BookResponse> findAll(String title) {
        List<Book> books = title == null || title.isBlank()
                ? bookRepository.findAll()
                : bookRepository.findByTitleContainingIgnoreCase(title);
        return books.stream().map(this::toResponse).toList();
    }

    @Override
    public BookResponse findById(Long id) {
        return toResponse(getBook(id));
    }

    @Override
    public BookResponse update(Long id, BookRequest request) {
        Book book = getBook(id);

        if (!book.getIsbn().equalsIgnoreCase(request.getIsbn())
                && bookRepository.existsByIsbnIgnoreCase(request.getIsbn())) {
            throw new DuplicateResourceException("Ya existe un libro con ese ISBN");
        }

        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPrice(request.getPrice());
        book.setStock(request.getStock());
        book.setAuthor(getAuthor(request.getAuthorId()));
        book.setCategory(getCategory(request.getCategoryId()));

        return toResponse(bookRepository.save(book));
    }

    @Override
    public void delete(Long id) {
        bookRepository.delete(getBook(id));
    }

    private Book getBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
    }

    private Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor no encontrado"));
    }

    private Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categor\u00eda no encontrada"));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPrice(),
                book.getStock(),
                new AuthorResponse(book.getAuthor().getId(), book.getAuthor().getName(), book.getAuthor().getBiography()),
                new CategoryResponse(book.getCategory().getId(), book.getCategory().getName(), book.getCategory().getDescription()),
                book.getCreatedBy().getEmail()
        );
    }
}
