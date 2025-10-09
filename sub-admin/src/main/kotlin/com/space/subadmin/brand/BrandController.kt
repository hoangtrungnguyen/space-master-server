package com.space.subadmin.brand

import com.space.subadmin.products.Brand
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class BrandWebController(private val brandRepository: BrandRepository) {

    @GetMapping("/brands/add")
    fun showAddBrandForm(
        model: Model,
        @RequestParam(required = false) returnUrl: String?
    ): String {
        model.addAttribute("brandForm", BrandForm())
        model.addAttribute("returnUrl", returnUrl)
        model.addAttribute("allBrands", brandRepository.findAll()) // Add this line to fetch all brands
        return "add-brand"
    }

    @PostMapping("/brands/add")
    fun addBrand(
        @ModelAttribute brandForm: BrandForm,
        @RequestParam(required = false) returnUrl: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        val newBrand = Brand(name = brandForm.name)
        brandRepository.save(newBrand)
        redirectAttributes.addFlashAttribute("successMessage", "Brand '${newBrand.name}' created successfully!")
        return "redirect:${returnUrl ?: "/products/list"}"
    }
}