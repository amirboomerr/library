package home.library;

import home.library.enums.Genre;
import home.library.model.Book;
import home.library.service.BookService;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class LibraryApplicationTests {

	private BookService bookService;

	private LibraryApplication app;

	@Start
	private void start(Stage stage) throws Exception {
		// Создаём мок сервиса
		bookService = mock(BookService.class);

		// Создаём экземпляр приложения
		app = new LibraryApplication();

		// Внедряем мок в приложение (предполагается, что есть такой метод)
		app.setBookService(bookService);

		// Запускаем приложение
		app.start(stage);
	}

	@BeforeEach
	void setup() {
		// Можно добавить дополнительную инициализацию, если нужно
	}

	@Test
	void testCreateBookWindow(FxRobot robot) {
		robot.clickOn("Создать книгу");
		robot.clickOn(".text-field").write("Тестовая книга");
		robot.clickOn(".text-field").write("\tТестовый автор");
		robot.clickOn(".combo-box").clickOn("РОМАН");
		robot.clickOn(".text-field").write("\t2024");
		robot.clickOn("Сохранить");

		// Проверяем, что был вызван метод createBook
		verify(bookService, atLeastOnce()).createBook(any(Book.class));
	}

	@Test
	void testViewBooksWindow(FxRobot robot) {
		when(bookService.getAllBooks()).thenReturn(
				Collections.singletonList(new Book("Книга", "Автор", Genre.ROMAN, 2020))
		);
		robot.clickOn("Просмотр книг");
		robot.clickOn("Загрузить все книги");

		TableView<Book> table = robot.lookup(".table-view").queryTableView();
		assertFalse(table.getItems().isEmpty());
		assertEquals("Книга", table.getItems().get(0).getTitle());
	}

	@Test
	void testUpdateBookWindow(FxRobot robot) {
		Book book = new Book("Старое название", "Автор", Genre.ROMAN, 2020);
		book.setId(1L);
		when(bookService.getBook(1L)).thenReturn(book);

		robot.clickOn("Обновить книгу");
		robot.clickOn("ID книги").write("1");
		robot.clickOn("Загрузить");

		robot.clickOn("Название").write("Новое название");
		robot.clickOn("Сохранить");

		verify(bookService, atLeastOnce()).createBook(any(Book.class));
	}

	@Test
	void testDeleteBookWindow(FxRobot robot) {
		Book book = new Book("Удаляемая книга", "Автор", Genre.ROMAN, 2020);
		book.setId(2L);
		when(bookService.getBook(2L)).thenReturn(book);

		robot.clickOn("Удалить книгу");
		robot.clickOn("ID книги").write("2");
		robot.clickOn("Удалить");

		verify(bookService, atLeastOnce()).deleteBook(2L);
	}

	public void setBookService(BookService bookService) {
		this.bookService = bookService;
	}
}
