package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.*;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class, FilmExtractor.class})
class FilmorateApplicationTests {
	private final UserDbStorage userStorage;
	private final FilmDbStorage filmStorage;

	public static User newUser() {
		User user = new User();
		user.setEmail("vasya@vasya.ru");
		user.setLogin("vasya111");
		user.setName("vasya");
		user.setBirthday(LocalDate.of(2001, 01, 01));
		return user;
	}

	public static Film newFilm() {
		Film film = new Film();
		film.setName("film1");
		film.setDescription("film1");
		film.setRatingId(1);
		film.setReleaseDate(LocalDate.of(2002, 01, 01));
		film.setDuration(123);
		return film;
	}

	@Test
	public void testFindUserById() {

		User newUser = newUser();
		userStorage.create(newUser);
		Long id = newUser.getId();

		Optional<User> userOptional = userStorage.findUserById(id);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testUpdateUser() {

		User newUser = newUser();
		userStorage.create(newUser);
		Long id = newUser.getId();

		User user = userStorage.findUserById(id).get();
		user.setEmail("greekVodka@uso.ru");
		user.setLogin("uso1");
		user.setName("uso");
		userStorage.update(user);

		Optional<User> userOptional = userStorage.findUserById(id);

		assertThat(userOptional)
				.isPresent()
				.get()
				.usingRecursiveComparison()
				.ignoringExpectedNullFields()
				.isEqualTo(user);
	}

	@Test
	public void testFindFilmById() {

		Film newFilm = newFilm();
		filmStorage.create(newFilm);
		Long id = newFilm.getId();

		Optional<Film> filmOptional = filmStorage.findFilmById(id);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("id", id)
				);
	}

	@Test
	public void testUpdateFilm() {

		Film newFilm = newFilm();
		filmStorage.create(newFilm);
		Long id = newFilm.getId();

		Film film = filmStorage.findFilmById(1L).get();
		film.setName("film3");
		film.setDescription("film3 description");
		film.setRatingId(2);
		film.setReleaseDate(LocalDate.of(2004, 01, 01));
		film.setDuration(12345);
		filmStorage.update(film);

		Optional<Film> filmOptional = filmStorage.findFilmById(id);

		assertThat(filmOptional)
				.isPresent()
				.get()
				.usingRecursiveComparison()
				.ignoringExpectedNullFields()
				.isEqualTo(film);
	}
}