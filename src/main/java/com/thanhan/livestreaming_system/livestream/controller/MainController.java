package com.thanhan.livestreaming_system.livestream.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @Controller
    public class StreamViewController {

        @GetMapping("/watch")
        public String viewStream(Model model, @RequestParam(name = "key") String streamKey) {
            model.addAttribute("streamKey", streamKey);
            return "index"; //for hls
        }
    }



}
