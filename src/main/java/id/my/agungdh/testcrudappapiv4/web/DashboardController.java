package id.my.agungdh.testcrudappapiv4.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Dashboard: GET / */
@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("appName", "Test CRUD App");
        return "pages/dashboard";
    }
}
