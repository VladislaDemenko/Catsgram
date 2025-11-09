package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.SortOrder;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@Service
public class PostService {
    private final Map<Long, Post> posts = new HashMap<>();
    private final UserService userService;
    private final Comparator<Post> postDateComparator = Comparator.comparing(Post::getPostDate);

    // Внедряем зависимость UserService через конструктор
    public PostService(UserService userService) {
        this.userService = userService;
    }

    public Collection<Post> findAll(SortOrder sort, int from, int size) {
        return posts.values()
                .stream()
                .sorted(sort.equals(SortOrder.ASCENDING) ?
                        postDateComparator : postDateComparator.reversed())
                .skip(from)
                .limit(size)
                .toList();
    }

    public Post create(Post post) {
        // Проверка описания
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new ConditionsNotMetException("Описание не может быть пустым!");
        }

        // Проверка существования автора
        if (post.getAuthorId() == null) {
            throw new ConditionsNotMetException("Идентификатор автора должен быть указан!");
        }

        // Ищем автора поста
        Optional<User> author = userService.findUserById(post.getAuthorId());
        if (author.isEmpty()) {
            throw new ConditionsNotMetException("Автор с id = " + post.getAuthorId() + " не найден");
        }

        // Создаем пост
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);
        return post;
    }

    public Post update(Post newPost) {
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан!");
        }
        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());

            // Проверка описания
            if (newPost.getDescription() == null || newPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым!");
            }

            // Проверка существования автора (если authorId меняется)
            if (newPost.getAuthorId() != null && !newPost.getAuthorId().equals(oldPost.getAuthorId())) {
                Optional<User> author = userService.findUserById(newPost.getAuthorId());
                if (author.isEmpty()) {
                    throw new ConditionsNotMetException("Автор с id = " + newPost.getAuthorId() + " не найден");
                }
                oldPost.setAuthorId(newPost.getAuthorId());
            }

            oldPost.setDescription(newPost.getDescription());
            return oldPost;
        }
        throw new NotFoundException("Пост с ID = " + newPost.getId() + " не найден");
    }

    public Optional<Post> findById(long psotId) {
        return Optional.ofNullable(posts.get(psotId));
    }

    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}