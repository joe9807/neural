package application.controllers;

import application.ui.NeuralUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NeuralController {
    @Autowired
    private NeuralUI neuralUI;

    @GetMapping("ui")
    public String showUI(){
        new Thread(() -> neuralUI.init()).start();
        return "UI";
    }
}
