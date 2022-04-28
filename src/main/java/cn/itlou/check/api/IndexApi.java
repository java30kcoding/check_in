package cn.itlou.check.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexApi {

    @GetMapping("/index")
    public String index(Model model){
        System.out.println("index....");
        return "index";
    }

}
