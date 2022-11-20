package controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller
{
    @GetMapping("/hallo")
    public String test()
    {
        return "das sollte man im browser unter    localhost:8080/hallo    lesen koennen";
    }

}
