package application.controllers;

import application.ui.NeuralDialog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NeuralController {
    @Autowired
    private NeuralDialog neuralDialog;

    @Operation(summary = "Запустить Тестовый Neural UI")
    @Parameter(name = "noise", example = "0.01")
    @GetMapping("ui")
    public String showUI(double noise){
        new Thread(() -> neuralDialog.init(noise)).start();
        return "Neural UI has started in separated window.";
    }
}
