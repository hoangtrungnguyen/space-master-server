package com.space.subadmin.category

import com.space.subadmin.db.Category
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class CategoryController(private val categoryRepository: CategoryRepository) {

    @GetMapping("/categories/add")
    fun showAddCategoryForm(
        model: Model,
        @RequestParam(required = false) returnUrl: String?
    ): String {
        model.addAttribute("categoryForm", CategoryForm())
        model.addAttribute("returnUrl", returnUrl)
        model.addAttribute("allCategories", categoryRepository.findAll())
        return "add-category"
    }

    @PostMapping("/categories/add")
    fun addCategory(
        @ModelAttribute categoryForm: CategoryForm,
        @RequestParam(required = false) returnUrl: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        val newCategory = Category(name = categoryForm.name)
        categoryRepository.save(newCategory)
        redirectAttributes.addFlashAttribute("successMessage", "Category '${newCategory.name}' created successfully!")
        return "redirect:${returnUrl ?: "/products/list"}"
    }
}