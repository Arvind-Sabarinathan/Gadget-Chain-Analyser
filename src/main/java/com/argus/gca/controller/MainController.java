package com.argus.gca.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String getIndex(Model model) {
        model.addAttribute("pageTitle", "Home");
        return "index";
    }

    @GetMapping("/upload")
    public String getUploadPage(Model model) {
        model.addAttribute("pageTitle", "Upload Artifact");
        return "upload";
    }

    @GetMapping("/result")
    public String getResultPage(Model model) {
        model.addAttribute("pageTitle", "Analysis Result");
        return "result";
    }

    @GetMapping("/artifacts")
    public String getArtifactsPage(Model model) {
        model.addAttribute("pageTitle", "Artifacts History");
        return "artifacts";
    }

    @GetMapping("/artifact")
    public String getArtifactPage(Model model) {
        model.addAttribute("pageTitle", "Analysis Results");
        return "artifact";
    }

}
