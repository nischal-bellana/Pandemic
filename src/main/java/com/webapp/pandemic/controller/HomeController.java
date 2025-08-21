package com.webapp.pandemic.controller;

import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class HomeController {

    public ArrayList<String> messages = new ArrayList<>();
    public HashMap<String, Integer> users = new HashMap<>();


    @GetMapping("/")
    public String root(){
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, Model model){
        if(username.length() < 5 || username.length()>16) return "invalidusername";

        users.put(username, 0);
        messages.add("--"+username+" joined");
        model.addAttribute("username", username);
        return "postlogin";
    }

    @ResponseBody
    @PostMapping("/user-left")
    public String user_left(@RequestParam String username){
        users.remove(username);
        messages.add("--"+username+" left");
        return "";
    }

    @ResponseBody
    @PostMapping("/message")
    public String message(@RequestParam String message, @RequestParam String username){
        messages.add(username.length()+"&"+username+message);
        return "<input name=\"message\" type=\"text\" placeholder=\"Type a message...\" hx-post=\"/message\" hx-trigger=\"keydown[keyCode==13]\" hx-include=\".username\" hx-swap=\"outerHTML\">";
    }

    @ResponseBody
    @GetMapping("/messages")
    public String messages(@RequestParam String username) {
        if (!users.containsKey(username) || users.get(username) == messages.size()) return "";

        StringBuilder res = new StringBuilder();
        for (int i = users.get(username); i < messages.size(); i++) {
            String message = messages.get(i);
            if (message.charAt(0) == '-') {
                res.append("<div class='message-block'>");
                res.append(message);
                res.append("</div>");
                continue;
            }

            int start = 0;
            while (message.charAt(start) != '&') {
                start++;
            }
            int username_len = Integer.parseInt(message.substring(0, start));
            String name = message.substring(start + 1, start + 1 + username_len);
            String act_message = message.substring(start + 1 + username_len);

            res.append("<div class='messageblock'><span class='username'>");
            res.append(name);
            res.append(": </span><span class='message'>");
            res.append(act_message);
            res.append("</span></div>");
        }
        users.put(username, messages.size());
        return res.toString();
    }

}
