# java-filmorate
Template repository for Filmorate project.


<img width="852" height="760" alt="Image" src="https://github.com/user-attachments/assets/5ad0b047-aa9f-47ff-a77c-ba9f229de498" />

Примеры запросов
1) Список 5 наиболее популярных фильмов
SELECT f.id,
       f.name,
       COUNT(fl.film_id)
FROM film AS f
LEFT OUTER JOIN film_likes AS fl ON f.id = fl.film_id
GROUP BY id,
         name
ORDER BY COUNT(fl.film_id) DESC
LIMIT 5

2) Список общих друзей пользователей с id = 1 и id = 2
SELECT u.name
FROM user_friends AS uf1
INNER JOIN user_friends AS uf2 ON uf1.user2_id = uf2.user2_id
INNER JOIN user AS u ON uf1.user2_id = u.id
WHERE uf1.user1_id = 1
  AND uf1.user2_id = 2
