package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.controller.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmorateApplicationTests {

	FilmController filmController = new FilmController();
	UserController userController = new UserController();

	@Test
	public void exceptionWhenFilmNameIsEmpty() {
		Film film = new Film(null, "", "Описание фильма", LocalDate.of(2001, 01, 01), 120);
		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	public void exceptionWhenFilmDescriptionMoreThan200() {
		String descripion = "";
		for (int i = 1; i <= 201; i++) {
			descripion += "A";
		}
		Film film = new Film(null, "Фильм 1", descripion, LocalDate.of(2001, 01, 01), 120);
		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	public void exceptionWhenFilmReleaseDateLessThan28121895() {
		Film film = new Film(null, "Фильм 1", "Описание фильма", LocalDate.of(1895, 12, 27), 120);
		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	public void exceptionWhenFilmDurationIsNotPositive() {
		Film film = new Film(null, "Фильм 1", "Описание фильма", LocalDate.of(2001, 01, 01), 0);
		assertThrows(ValidationException.class, () -> filmController.create(film));
	}

	@Test
	public void exceptionWhenFilmIsNotFoundWhileUpdating() {
		Film film = new Film(null, "Фильм 1", "Описание фильма", LocalDate.of(1995, 12, 27), 120);
		filmController.create(film);
		film.setId(null);
		assertThrows(NotFoundException.class, () -> filmController.update(film));
	}

	@Test
	public void exceptionWhenUserEmailIsEmpty() {
		User user = new User(null, "", "user1", "Vasya", LocalDate.of(2000, 01, 01));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	public void exceptionWhenUserEmailDoesNotContainAt() {
		User user = new User(null, "email", "user1", "Vasya", LocalDate.of(2000, 01, 01));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	public void exceptionWhenUserLoginIsEmpty() {
		User user = new User(null, "email@email.ru", "", "Vasya", LocalDate.of(2000, 01, 01));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	public void exceptionWhenUserLoginContainsSpaces() {
		User user = new User(null, "email@email.ru", "user  1", "Vasya", LocalDate.of(2000, 01, 01));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	public void exceptionWhenBirthdayIsFutureDate() {
		User user = new User(null, "email@email.ru", "user1", "Vasya", LocalDate.of(3000, 01, 01));
		assertThrows(ValidationException.class, () -> userController.create(user));
	}

	@Test
	public void exceptionWhenUserIsNotFoundWhileUpdating() {
		User user = new User(null, "email@email.ru", "user1", "Vasya", LocalDate.of(2000, 01, 01));
		userController.create(user);
		user.setId(145);
		assertThrows(NotFoundException.class, () -> userController.update(user));
	}
}
