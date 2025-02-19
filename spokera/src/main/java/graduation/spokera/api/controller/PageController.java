package graduation.spokera.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {

    @RequestMapping("/chat-room")
    public String chatRoomPage(Model model){
        return "chat-room";
    }

    @RequestMapping("/chat-rooms")
    public String chatRoomsPage(Model model){
        return "chat-rooms";
    }

    @RequestMapping("/facility-recommend")
    public String facilityRecommendPage(Model model){
        return "facility-recommend";
    }

    @RequestMapping("/match-wait-lists")
    public String matchWaitListsPage(Model model){
        return "match-wait-list";
    }

    @RequestMapping("/matching")
    public String matchingPage(Model model){
        return "matching";
    }
}
