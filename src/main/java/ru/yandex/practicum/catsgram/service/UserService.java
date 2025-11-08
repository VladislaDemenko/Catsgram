package ru.yandex.practicum.catsgram.service;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToUserId = new HashMap<>();
    private long nextId = 1;

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Email пользователя должен быть указан!");
        }

        if (emailToUserId.containsKey(user.getEmail())) {
            throw new DuplicatedDataException("Этот Email уже используется");
        }
        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());
        users.put(user.getId(), user);
        emailToUserId.put(user.getEmail(), user.getId());

        return user;
    }

    public User update(@RequestBody User newUser) {
        // Проверяем все необходимые условия
        if (newUser.getId() == 0) {
            throw new ConditionsNotMetException("ID должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            // Проверяем уникаольность нового Email
            if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
                if (emailToUserId.containsKey(newUser.getEmail())) {
                    throw new DuplicatedDataException("Этот Email уже используется");
                }

                // Обновляем Email
                emailToUserId.remove(oldUser.getEmail());
                emailToUserId.put(newUser.getEmail(), newUser.getId());
            }

            // Обновляем поля если они не null
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getUsername() != null) {
                oldUser.setUsername(newUser.getUsername());
            }
            if (newUser.getPassword() != null) {
                oldUser.setPassword(newUser.getPassword());
            }

            return oldUser;
        }

        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }


    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
