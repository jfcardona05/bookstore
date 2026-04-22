package com.example.bookstore.impl;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.service.BookService;
import org.springframework.stereotype.Service;
import com.example.bookstore.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final UserRepository userRepository;

    public BookServiceImpl(BookRepository repository,
                           UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public Book create(Book book) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        book.setUser(user);

        return repository.save(book);
    }

    @Override
    public List<Book> findAll() {
        return repository.findAll();
    }

    @Override
    public Book findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Book update(Long id, Book book) {
        Book existing = findById(id);

        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setDescription(book.getDescription());
        existing.setPrice(book.getPrice());
        existing.setStock(book.getStock());

        return repository.save(existing);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
