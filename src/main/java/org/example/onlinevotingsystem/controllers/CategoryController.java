package org.example.onlinevotingsystem.controllers;

import org.example.onlinevotingsystem.models.Category;
import org.example.onlinevotingsystem.services.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/admin-category-create")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "category-create"; // Thymeleaf template
    }

    @PostMapping("/admin-category-create")
    public String createCategory(@ModelAttribute Category category, Model model) {
        categoryService.createCategory(category);
        model.addAttribute("message", "Category created successfully!");
        return "category-create";
    }
}
