package home.library.service;

import home.library.enums.Genre;
import home.library.model.Book;
import home.library.repos.BookRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepo repo;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createBook_ShouldCallSave() {
        Book book = new Book( "Title", "Author", Genre.ROMAN, 2020);

        bookService.createBook(book);

        verify(repo, times(1)).save(book);
    }

    @Test
    void getBook_ShouldReturnBook_WhenFound() {
        Book book = new Book("Title", "Author", Genre.ROMAN, 2020);
        when(repo.findById(1L)).thenReturn(Optional.of(book));

        Book result = bookService.getBook(1L);

        assertNotNull(result);
        assertEquals("Title", result.getTitle());
    }

    @Test
    void getBook_ShouldReturnNull_WhenNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        Book result = bookService.getBook(1L);

        assertNull(result);
    }

    @Test
    void getAllBooks_ShouldReturnList() {
        List<Book> books = Arrays.asList(
                new Book( "Title1", "Author1", Genre.ROMAN, 2000),
                new Book( "Title2", "Author2", Genre.FANTASY, 2010)
        );
        when(repo.findAll()).thenReturn(books);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
    }

    @Test
    void updateBook_ShouldCallSave() {
        Book book = new Book( "Updated Title", "Author", Genre.ROMAN, 2021);

        bookService.updateBook(book);

        verify(repo, times(1)).save(book);
    }

    @Test
    void deleteBook_ShouldCallDeleteById() {
        Long id = 1L;

        bookService.deleteBook(id);

        verify(repo, times(1)).deleteById(id);
    }

    @Test
    void getBooksByTitle_ShouldReturnList() {
        String title = "some";
        List<Book> books = Collections.singletonList(new Book( "Some Title", "Author", Genre.ROMAN, 2020));
        when(repo.findByTitleContainingIgnoreCase(title)).thenReturn(books);

        List<Book> result = bookService.getBooksByTitle(title);

        assertEquals(1, result.size());
    }

    @Test
    void getBooksByAuthor_ShouldReturnList() {
        String author = "Author";
        List<Book> books = Collections.singletonList(new Book( "Title", author, Genre.ROMAN, 2020));
        when(repo.findByAuthor(author)).thenReturn(books);

        List<Book> result = bookService.getBooksByAuthor(author);

        assertEquals(1, result.size());
    }

    @Test
    void getBooksByYear_ShouldReturnList() {
        int year = 2020;
        List<Book> books = Collections.singletonList(new Book( "Title", "Author", Genre.ROMAN, year));
        when(repo.findByYear(year)).thenReturn(books);

        List<Book> result = bookService.getBooksByYear(year);

        assertEquals(1, result.size());
    }

    @Test
    void getBooksByGenre_ShouldReturnList() {
        Genre genre = Genre.ROMAN;
        List<Book> books = Collections.singletonList(new Book( "Title", "Author", genre, 2020));
        when(repo.findByGenre(genre)).thenReturn(books);

        List<Book> result = bookService.getBooksByGenre(genre);

        assertEquals(1, result.size());
    }
}
