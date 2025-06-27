package home.library.service;

import home.library.enums.Genre;
import home.library.model.Book;
import home.library.repos.BookRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    @Autowired
    BookRepo repo;

    public BookService(BookRepo repo) {
        this.repo = repo;
    }


    public void createBook (Book book) {
        repo.save(book);
    }

    public Book getBook (Long id) {
       return repo.findById(id).orElse(null);
    }

    public List<Book> getAllBooks() {
        return repo.findAll();
    }

    public void updateBook (Book book) {
        repo.save(book);
    }

    public void deleteBook (Long id) {
        repo.deleteById(id);
    }


    public List<Book> getBooksByTitle(String title) {
        return repo.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> getBooksByAuthor(String author) {
        return repo.findByAuthor(author);
    }

    public List<Book> getBooksByYear(int year) {
        return repo.findByYear(year);
    }

    public List<Book> getBooksByGenre(Genre genre) {
        return repo.findByGenre(genre);
    }

}
