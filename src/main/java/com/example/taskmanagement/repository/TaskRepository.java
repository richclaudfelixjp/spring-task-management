package com.example.taskmanagement.repository;

import com.example.taskmanagement.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.taskmanagement.model.User;
import java.util.Optional;

import java.util.List;

/**
 * The TaskRepository interface is a Spring Data JPA repository for Task entities.
 * By extending JpaRepository, we get a lot of standard CRUD (Create, Read, Update, Delete)
 * operations for the Task entity for free.
 *
 * Spring will automatically provide the implementation for this interface at runtime.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // That's it!
    // We don't need to write any methods here for now.
    // JpaRepository already provides methods like:
    // - save(Task task)
    // - findById(Long id)
    // - findAll()
    // - deleteById(Long id)
    // ...and many more.

    // Later, we can define custom query methods here if we need them.
    // For example, if we wanted to find all tasks by their completion status, we could add:
    // List<Task> findByCompleted(boolean completed);
    // Spring Data JPA would automatically implement this method for us based on its name.

    List<Task> findByCompleted(Boolean completed);

    List<Task> findByUser(User user);

    Optional<Task> findByIdAndUser(Long id, User user);

    List<Task> findByCompletedAndUser(Boolean completed, User user);

    void deleteByUser(User user);
}
