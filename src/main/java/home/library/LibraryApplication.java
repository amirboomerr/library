package home.library;

import home.library.enums.Genre;
import home.library.model.Book;
import home.library.service.BookService;
import javafx.application.Application;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class LibraryApplication extends Application {
	private static ConfigurableApplicationContext springContext;
	private BookService bookService;

	@Override
	public void start(Stage primaryStage) {
		BookService bookService = springContext.getBean(BookService.class);
		primaryStage.setTitle("Библиотека - Главное меню");

		Button btnCreate = new Button("Создать книгу");
		Button btnView = new Button("Просмотр книг");
		Button btnUpdate = new Button("Обновить книгу");
		Button btnDelete = new Button("Удалить книгу");

		btnCreate.setMaxWidth(Double.MAX_VALUE);
		btnView.setMaxWidth(Double.MAX_VALUE);
		btnUpdate.setMaxWidth(Double.MAX_VALUE);
		btnDelete.setMaxWidth(Double.MAX_VALUE);

		VBox menu = new VBox(10, btnCreate, btnView, btnUpdate, btnDelete);
		menu.setPadding(new Insets(20));
		menu.setPrefWidth(200);

		Scene scene = new Scene(menu, 300, 250);
		primaryStage.setScene(scene);
		primaryStage.show();

		btnCreate.setOnAction(e -> showCreateBookWindow());
		btnView.setOnAction(e -> showViewBooksWindow());
		btnUpdate.setOnAction(e -> showUpdateBookWindow());
		btnDelete.setOnAction(e -> showDeleteBookWindow());
	}

	// Окно создания книги
	private void showCreateBookWindow() {
		BookService bookService = springContext.getBean(BookService.class);
		Stage stage = new Stage();
		stage.setTitle("Создать книгу");
		stage.initModality(Modality.APPLICATION_MODAL);

		TextField tfTitle = new TextField();
		TextField tfAuthor = new TextField();
		ComboBox<Genre> cbGenre = new ComboBox<>();
		cbGenre.getItems().setAll(Genre.values());
		cbGenre.setPromptText("Выберите жанр");
		TextField tfYear = new TextField();

		tfTitle.setPromptText("Название");
		tfAuthor.setPromptText("Автор");
		tfYear.setPromptText("Год выпуска");

		Button btnSave = new Button("Сохранить");
		Button btnCancel = new Button("Отмена");

		Label lblMessage = new Label();

		btnSave.setOnAction(e -> {
			try {
				String title = tfTitle.getText().trim();
				String author = tfAuthor.getText().trim();
				Genre genre = cbGenre.getValue();
				int year = Integer.parseInt(tfYear.getText().trim());

				if (title.isEmpty() || author.isEmpty() || genre == null) {
					lblMessage.setText("Заполните все поля!");
					return;
				}

				Book book = new Book(title, author, genre, year);
				bookService.createBook(book);
				lblMessage.setText("Книга создана с ID: " + book.getId());
			} catch (NumberFormatException ex) {
				lblMessage.setText("Год выпуска должен быть числом!");
			}
		});

		btnCancel.setOnAction(e -> stage.close());

		VBox vbox = new VBox(10,
				new Label("Введите данные книги:"),
				tfTitle, tfAuthor, cbGenre, tfYear,
				new HBox(10, btnSave, btnCancel),
				lblMessage);
		vbox.setPadding(new Insets(15));

		stage.setScene(new Scene(vbox, 350, 320));
		stage.showAndWait();
	}

	// Окно просмотра книг с фильтром по названию
	private void showViewBooksWindow() {
		BookService bookService = springContext.getBean(BookService.class);
		Stage stage = new Stage();
		stage.setTitle("Просмотр книг");
		stage.initModality(Modality.APPLICATION_MODAL);

		// Поля для фильтрации
		TextField tfTitleFilter = new TextField();
		tfTitleFilter.setPromptText("Фильтр по названию");

		TextField tfAuthorFilter = new TextField();
		tfAuthorFilter.setPromptText("Фильтр по автору");

		ComboBox<Genre> cbGenreFilter = new ComboBox<>();
		cbGenreFilter.getItems().setAll(Genre.values());
		cbGenreFilter.setPromptText("Фильтр по жанру");
		cbGenreFilter.setEditable(false);

		TextField tfYearFilter = new TextField();
		tfYearFilter.setPromptText("Фильтр по году");

		TableView<Book> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Book, Long> colId = new TableColumn<>("ID");
		colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId()).asObject());

		TableColumn<Book, String> colTitle = new TableColumn<>("Название");
		colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));

		TableColumn<Book, String> colAuthor = new TableColumn<>("Автор");
		colAuthor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAuthor()));

		TableColumn<Book, String> colGenre = new TableColumn<>("Жанр");
		colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGenre().toString()));

		TableColumn<Book, Long> colYear = new TableColumn<>("Год");
		colYear.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getYear()).asObject());

		table.getColumns().addAll(colId, colTitle, colAuthor, colGenre, colYear);

		Button btnLoadAll = new Button("Загрузить все книги");
		Button btnFilter = new Button("Поиск");

		btnLoadAll.setOnAction(e -> {
			List<Book> allBooks = bookService.getAllBooks();
			table.setItems(FXCollections.observableArrayList(allBooks));
		});

		btnFilter.setOnAction(e -> {
			String titleFilter = tfTitleFilter.getText().trim().toLowerCase();
			String authorFilter = tfAuthorFilter.getText().trim().toLowerCase();
			Genre genreFilter = cbGenreFilter.getValue();
			String yearText = tfYearFilter.getText().trim();

			List<Book> filtered = bookService.getAllBooks().stream()
					.filter(book -> titleFilter.isEmpty() || book.getTitle().toLowerCase().contains(titleFilter))
					.filter(book -> authorFilter.isEmpty() || book.getAuthor().toLowerCase().contains(authorFilter))
					.filter(book -> genreFilter == null || book.getGenre() == genreFilter)
					.filter(book -> {
						if (yearText.isEmpty()) return true;
						try {
							int yearFilter = Integer.parseInt(yearText);
							return book.getYear() > yearFilter;
						} catch (NumberFormatException exception) {
							// Если введён некорректный год, игнорируем фильтр по году
							return true;
						}
					})
					.toList();

			table.setItems(FXCollections.observableArrayList(filtered));
		});

		HBox filtersBox = new HBox(10, tfTitleFilter, tfAuthorFilter, cbGenreFilter, tfYearFilter, btnFilter, btnLoadAll);
		filtersBox.setPadding(new Insets(5));

		VBox vbox = new VBox(10,
				new Label("Фильтры:"),
				filtersBox,
				table);
		vbox.setPadding(new Insets(15));
		vbox.setPrefSize(800, 450);

		stage.setScene(new Scene(vbox));
		stage.showAndWait();
	}


	// Окно обновления книги по ID
	private void showUpdateBookWindow() {
		BookService bookService = springContext.getBean(BookService.class);
		Stage stage = new Stage();
		stage.setTitle("Обновить книгу");
		stage.initModality(Modality.APPLICATION_MODAL);

		TextField tfId = new TextField();
		tfId.setPromptText("ID книги");

		TextField tfTitle = new TextField();
		TextField tfAuthor = new TextField();
		ComboBox<Genre> cbGenre = new ComboBox<>();
		cbGenre.getItems().setAll(Genre.values());
		cbGenre.setPromptText("Выберите жанр");
		TextField tfYear = new TextField();

		Button btnLoad = new Button("Загрузить");
		Button btnSave = new Button("Сохранить");
		Button btnCancel = new Button("Отмена");

		Label lblMessage = new Label();

		btnLoad.setOnAction(e -> {
			try {
				Long id = Long.parseLong(tfId.getText().trim());
				Book book = bookService.getBook(id);
				if (book == null) {
					lblMessage.setText("Книга с таким ID не найдена.");
					tfTitle.clear();
					tfAuthor.clear();
					cbGenre.setValue(null);
					tfYear.clear();
				} else {
					tfTitle.setText(book.getTitle());
					tfAuthor.setText(book.getAuthor());
					cbGenre.setValue(book.getGenre());
					tfYear.setText(String.valueOf(book.getYear()));
					lblMessage.setText("Книга загружена. Внесите изменения и нажмите Сохранить.");
				}
			} catch (NumberFormatException ex) {
				lblMessage.setText("Введите корректный ID.");
			}
		});

		btnSave.setOnAction(e -> {
			try {
				Long id = Long.parseLong(tfId.getText().trim());
				String title = tfTitle.getText().trim();
				String author = tfAuthor.getText().trim();
				Genre genre = cbGenre.getValue();
				int year = Integer.parseInt(tfYear.getText().trim());

				if (title.isEmpty() || author.isEmpty() || genre == null) {
					lblMessage.setText("Заполните все поля!");
					return;
				}

				Book book = bookService.getBook(id);
				if (book == null) {
					lblMessage.setText("Книга с таким ID не найдена.");
					return;
				}

				book.setTitle(title);
				book.setAuthor(author);
				book.setGenre(genre);
				book.setYear(year);
				bookService.createBook(book);
				lblMessage.setText("Книга обновлена.");
			} catch (NumberFormatException ex) {
				lblMessage.setText("Год и ID должны быть числами.");
			}
		});

		btnCancel.setOnAction(e -> stage.close());

		VBox vbox = new VBox(10,
				new Label("Введите ID книги для загрузки:"),
				tfId, btnLoad,
				new Label("Редактируйте данные книги:"),
				tfTitle, tfAuthor, cbGenre, tfYear,
				new HBox(10, btnSave, btnCancel),
				lblMessage);
		vbox.setPadding(new Insets(15));
		vbox.setPrefWidth(350);

		stage.setScene(new Scene(vbox));
		stage.showAndWait();
	}

	// Окно удаления книги с подтверждением
	private void showDeleteBookWindow() {
		BookService bookService = springContext.getBean(BookService.class);
		Stage stage = new Stage();
		stage.setTitle("Удалить книгу");
		stage.initModality(Modality.APPLICATION_MODAL);

		TextField tfId = new TextField();
		tfId.setPromptText("ID книги");

		Button btnDelete = new Button("Удалить");
		Button btnCancel = new Button("Отмена");

		Label lblMessage = new Label();

		btnDelete.setOnAction(e -> {
			try {
				Long id = Long.parseLong(tfId.getText().trim());
				Book book = bookService.getBook(id);
				if (book == null) {
					lblMessage.setText("Книга с таким ID не найдена.");
					return;
				}

				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle("Подтверждение удаления");
				alert.setHeaderText(null);
				alert.setContentText("Вы уверены, что хотите удалить книгу с ID " + id + "?\n\"" + book.getTitle() + "\"?");
				Optional<ButtonType> result = alert.showAndWait();
				if (result.isPresent() && result.get() == ButtonType.OK) {
					bookService.deleteBook(id);
					lblMessage.setText("Книга удалена.");
				} else {
					lblMessage.setText("Удаление отменено.");
				}
			} catch (NumberFormatException ex) {
				lblMessage.setText("Введите корректный ID.");
			}
		});

		btnCancel.setOnAction(e -> stage.close());

		VBox vbox = new VBox(10,
				new Label("Введите ID книги для удаления:"),
				tfId,
				new HBox(10, btnDelete, btnCancel),
				lblMessage);
		vbox.setPadding(new Insets(15));
		vbox.setPrefWidth(350);

		stage.setScene(new Scene(vbox));
		stage.showAndWait();
	}

	public static void main(String[] args) {
		springContext = SpringApplication.run(LibraryApplication.class, args);
		launch(args);
	}

    public void setBookService(BookService bookService) {
		this.bookService = bookService;
    }
}
