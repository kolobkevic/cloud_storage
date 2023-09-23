package ru.kolobkevic.cloud_storage.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kolobkevic.cloud_storage.services.SearchService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping
    public String search(@AuthenticationPrincipal User user,
                                      @RequestParam("query") String query,
                                      Model model) {
        var results = searchService.search(user.getUsername(), query);
        model.addAttribute("searchResults", results);
        return "search";
    }
}
