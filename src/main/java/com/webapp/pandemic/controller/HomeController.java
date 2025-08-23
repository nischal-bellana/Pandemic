package com.webapp.pandemic.controller;

import com.webapp.pandemic.dao.Message;
import com.webapp.pandemic.services.MessageService;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class HomeController {
    private final MessageService messageService;
    private final HashSet<String> usernames_used;

    public HomeController(MessageService messageService) {
        this.messageService = messageService;
        usernames_used = new HashSet<>();
    }

    @GetMapping("/")
    public String root(){
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, Model model){
        //length should be >= 5 and <= 16
        if(username.length() < 5 || username.length() > 16){
            model.addAttribute("error_message", "length should be >= 5 and <= 16");
            return "invalidusername";
        }

        if(usernames_used.contains(username)){
            model.addAttribute("error_message", "username already used and online");
            return "invalidusername";
        }

        usernames_used.add(username);
        model.addAttribute("username", username);
        Message m = new Message();
        m.setUsername("");
        m.setContent("[" + username + " joined" + "]");
        messageService.saveMessage(m);
        return "postlogin";
    }

    @ResponseBody
    @PostMapping("/user-left")
    public String user_left(@RequestParam String username){
        usernames_used.remove(username);
        Message m = new Message();
        m.setUsername("");
        m.setContent("[" + username + " left" + "]");
        messageService.saveMessage(m);
        return "";
    }

    @ResponseBody
    @PostMapping("/message")
    public String message(@RequestParam String message, @RequestParam String username){
        Message m = new Message();
        m.setUsername(username);
        m.setContent(message);
        messageService.saveMessage(m);
        return "<input name=\"message\" type=\"text\" placeholder=\"Type a message...\" hx-post=\"/message\" hx-trigger=\"keydown[keyCode==13]\" hx-include=\".username\" hx-swap=\"outerHTML\">";
    }

    @ResponseBody
    @GetMapping("/messages-first")
    public String messagerFirst(@RequestParam String username){
        Optional<Message> message_retrieved = messageService.getMessagesFirst();

        if(message_retrieved.isEmpty()) return """
                <div class="messageblock"
                             id="message-1"
                             hx-get="/messages-first"
                             hx-trigger="intersect delay:300ms"
                             hx-swap="outerHTML"
                             hx-include=".username">
                             </div>
                """;

        Message m = message_retrieved.get();

        return (m.getId() != 1 ? """ 
                <div class="messageblock"
                             id="message-%d"
                             hx-get="/messages-start"
                             hx-trigger="intersect"
                             hx-swap="outerHTML"
                             hx-include=".username"
                             hx-vals='{"id" : %d}'>
                </div>
                """.formatted(m.getId()-1, m.getId()-1) :
                "") +

                """
                <div class="messageblock"
                             id="message-%d"
                             hx-get="/messages-end"
                             hx-trigger="intersect delay:300ms"
                             hx-swap="outerHTML"
                             hx-include=".username"
                             hx-vals='{"id" : %d}'>
                             <span class='username-text'>%s: </span>%s
                </div>
                """.formatted(m.getId(), m.getId(), m.getUsername(), m.getContent());
    }

    @ResponseBody
    @GetMapping("/messages-start")
    public String messagesStart(@RequestParam String username, @RequestParam Long id) {
        List<Message> messages_retrieved = messageService.getMessagesStart(id);

        StringBuilder res = new StringBuilder();

        int messages_count = messages_retrieved.size();
        boolean nomore = messages_count < 45 || messages_retrieved.get(messages_count-1).getId()==1;
        Message lastm = messages_retrieved.get(messages_count-1);

        res.append("""
        <div class='messageblock' id='message-%d' %s>
            <span class='username-text'>%s: </span>%s
        </div>
        """.formatted(lastm.getId(),
                nomore ? "" : "hx-get='/message-start' hx-trigger='intersect delay:300ms' hx-swap='outerHTML' hx-include='.username' hx-vals='{\"id\" : 1}'",
                lastm.getUsername(),
                lastm.getContent()));


        for (int i = messages_count - 2; i >= 0; i--) {
            Message m = messages_retrieved.get(i);
            res.append("""
        <div class='messageblock' id='message-%d'>
            <span class='username-text'>%s: </span>%s
        </div>
        """.formatted(m.getId(), m.getUsername(), m.getContent()));
        }

        return res.toString();
    }

    @ResponseBody
    @GetMapping("/messages-end")
    public String messagesEnd(@RequestParam String username, @RequestParam Long id){
        List<Message> messages_retrieved = messageService.getMessagesEnd(id);

        if(messages_retrieved.size() == 1) {
            Message m = messages_retrieved.get(0);
            return """
                <div class="messageblock"
                             id="message-%d"
                             hx-get="/messages-end"
                             hx-trigger="intersect delay:300ms"
                             hx-swap="outerHTML"
                             hx-include=".username"
                             hx-vals='{"id" : %d}'>
                             <span class='username-text'>%s: </span>%s
                </div>
                """.formatted(m.getId(), m.getId(), m.getUsername(), m.getContent());
        }

        int  messages_count = messages_retrieved.size();
        StringBuilder res = new StringBuilder();

        for (int i = 0; i < messages_count - 1; i++) {
            Message m = messages_retrieved.get(i);
            res.append("""
        <div class='messageblock' id='message-%d'>
            <span class='username-text'>%s: </span>%s
        </div>
        """.formatted(m.getId(), m.getUsername(), m.getContent()));
        }

        Message lastm = messages_retrieved.get(messages_count - 1);

        res.append("""
                <div class="messageblock"
                             id="message-%d"
                             hx-get="/messages-end"
                             hx-trigger="intersect delay:300ms"
                             hx-swap="outerHTML"
                             hx-include=".username"
                             hx-vals='{"id" : %d}'>
                             <span class='username-text'>%s: </span>%s
                </div>
                """.formatted(lastm.getId(), lastm.getId(), lastm.getUsername(), lastm.getContent()));

        return  res.toString();
    }

}
