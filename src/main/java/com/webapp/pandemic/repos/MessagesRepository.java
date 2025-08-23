package com.webapp.pandemic.repos;

import com.webapp.pandemic.dao.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessagesRepository extends JpaRepository<Message, Long> {
    List<Message> findTop45ByIdLessThanEqualOrderByIdDesc(Long id);

    Optional<Message> findTopByOrderByIdDesc();

    List<Message> findTop45ByIdGreaterThanEqualOrderById(Long id);
}
