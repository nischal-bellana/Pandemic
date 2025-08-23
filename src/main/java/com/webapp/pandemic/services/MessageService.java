package com.webapp.pandemic.services;

import com.webapp.pandemic.dao.Message;
import com.webapp.pandemic.repos.MessagesRepository;
import org.aspectj.bridge.MessageWriter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {
    private final MessagesRepository repo;

    public MessageService(MessagesRepository repo){
        this.repo = repo;
    }

    public Optional<Message> getMessagesFirst(){
        return repo.findTopByOrderByIdDesc();
    }

    public List<Message> getMessagesStart(Long id){
        return repo.findTop45ByIdLessThanEqualOrderByIdDesc(id);
    }

    public List<Message> getMessagesEnd(Long id){
        return repo.findTop45ByIdGreaterThanEqualOrderById(id);
    }

    public Message saveMessage(Message message){
        return repo.save(message);
    }

}
