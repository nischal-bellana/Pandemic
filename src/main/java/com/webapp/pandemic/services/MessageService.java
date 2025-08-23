package com.webapp.pandemic.services;

import com.webapp.pandemic.dao.Message;
import com.webapp.pandemic.repos.MessagesRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MessageService {
    private final MessagesRepository repo;
    private final Deque<Message> latest_45_msgs;

    public MessageService(MessagesRepository repo){
        this.repo = repo;
        latest_45_msgs = new ArrayDeque<>();
    }

    public  List<Message> getMessagesFirst(){
        if(latest_45_msgs.isEmpty()) return null;
        return latest_45_msgs.stream().toList();
    }

    public List<Message> getMessagesStart(Long id){
        return repo.findTop45ByIdLessThanEqualOrderByIdDesc(id);
    }

    public List<Message> getMessagesEnd(Long id){
        if(latest_45_msgs.peekLast() == null  || Objects.equals(id, latest_45_msgs.peekLast().getId())) return null;

        if(latest_45_msgs.peekFirst().getId().compareTo(id) < 0){
            List<Message> res = new ArrayList<>();

            for(Message m : latest_45_msgs){
                if(m.getId().compareTo(id) >= 0){
                    res.add(m);
                }
            }
            return res;
        }

        return repo.findTop45ByIdGreaterThanEqualOrderById(id);
    }

    public Message getLastMessage(){
        return latest_45_msgs.peekLast();
    }

    public void saveMessage(Message message){
        latest_45_msgs.offerLast(message);
        while(latest_45_msgs.size() > 45){
            latest_45_msgs.pollFirst();
        }
        repo.save(message);
    }

}
