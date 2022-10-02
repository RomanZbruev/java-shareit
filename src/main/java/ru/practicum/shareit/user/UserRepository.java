package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserRepository extends JpaRepository<User,Long> {

    User getUserById(Long id);

    void deleteUserById(Long id);

    @Query(value = "SELECT u.id FROM User u")
    List<Long> getAllIds();

}
