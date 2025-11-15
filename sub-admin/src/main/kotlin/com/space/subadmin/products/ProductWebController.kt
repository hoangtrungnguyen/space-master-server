package com.space.subadmin.products

import com.space.subadmin.category.CategoryRepository
import com.space.subadmin.users.UserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Controller to handle web requests for products using Thymeleaf.
 * This is separate from ProductController to keep API and UI concerns separate.
 *
 * @param productRepository The repository for accessing product data.
 */
@Controller
@RequestMapping("/products") // Base path for web-related product pages
class ProductWebController(
    private val productRepository: ProductRepository,
    private val productService: ProductService,
    private val categoryRepository: CategoryRepository,
    private val userService: UserService // Injected to get the current user
) {

    /**
     * Displays the form to add a new product.
     * Responds to GET requests at /products/add
     *
     * @param model The Spring Model to pass data to the view.
     * @return The name of the Thymeleaf template to render.
     */
    @GetMapping("/add")
    fun showAddProductForm(model: Model): String {
        // Use a DTO to represent the form's data structure
        model.addAttribute("productForm", ProductFormDTO())
        model.addAttribute("allCategories", categoryRepository.findAll())
        return "add-product" // This corresponds to 'src/main/resources/templates/add-product.html'
    }

    /**
     * Processes the submission of the add product form.
     * Responds to POST requests at /products/add
     *
     * @param productForm The DTO populated with form data.
     * @return A redirect instruction to the product list page.
     */
    @PostMapping("/add")
    fun addProduct(
        @ModelAttribute("productForm") productForm: ProductFormDTO,
        @AuthenticationPrincipal userDetails: UserDetails
    ): String {
        // Find the full User entity from the security principal's username
        val currentUser = userService.findByUsername(userDetails.username)
            ?: throw IllegalStateException("Authenticated user '${userDetails.username}' not found in database, which should not happen.")

        productService.createProduct(productForm, currentUser)
        return "redirect:/products/list" // Redirect to the product list page after saving
    }

    /**
     * Displays a focused detail page for a single product variant.
     */
    @GetMapping(value = ["/{id}"], params = ["variantId"])
    fun showProductVariantDetail(
        @PathVariable id: Long,
        @RequestParam variantId: Long,
        model: Model
    ): String {
        val data = productService.findProductVariantDetail(id, variantId)
        model.addAttribute("info", data?.first)
        model.addAttribute("detail", data?.second)
        return "products/detail-product-variant" // Renders the new template
    }

    /**
     * Displays a list of all products.
     * Responds to GET requests at /products/list
     *
     * @param model The Spring Model to pass data to the view.
     * @return The name of the Thymeleaf template to render.
     */
    @GetMapping("/list")
    fun showProductList(model: Model): String {
        val productVariants = productService.getAllProductVariantsWithInventory()
        model.addAttribute("products", productVariants)
        return "products-list" // This corresponds to 'src/main/resources/templates/products-list.html'
    }

    /**
     * Displays the form to edit an existing product.
     * Responds to GET requests at /products/edit/{id}
     *
     * @param id The ID of the product to edit.
     * @param model The Spring Model to pass data to the view.
     * @return The name of the Thymeleaf template to render, or a redirect if the product is not found.
     */
    @GetMapping("/edit/{id}")
    fun showEditProductForm(@PathVariable id: Long, model: Model): String {
        return productRepository.findById(id)
            .map { product ->
                // For simplicity, we edit the first variant. A real app might need a way to select which variant to edit.
                val variant = product.variants.firstOrNull()

                // Create and populate the DTO from the entity
                val productForm = ProductEditDTO(
                    productId = product.id,
                    variantId = variant?.id,
                    name = product.name,
                    description = product.description,
                    categoryId = product.category?.id?.toString(),
                    sku = variant?.sku ?: "",
                    price = variant?.price ?: java.math.BigDecimal.ZERO,
                    costPrice = variant?.costPrice,
                    weight = variant?.weight
                )

                model.addAttribute("productForm", productForm)
                model.addAttribute("allCategories", categoryRepository.findAll())
                "edit-product" // Renders 'src/main/resources/templates/edit-product.html'
            }
            .orElse("redirect:/products/list") // Redirect if product not found
    }

//    /**
//     * Processes the submission of the edit product form.
//     * Responds to POST requests at /products/edit/{id}
//     *
//     * @param id The ID of the product being updated.
//     * @param productForm The DTO populated with form data.
//     * @return A redirect instruction to the product list page.
//     */
//    @PostMapping("/edit/{id}")
//    fun updateProduct(@PathVariable id: Long, @ModelAttribute("productForm") productForm: ProductEditDTO): String {
//        productService.updateProductWithVariant(productForm)
//        return "redirect:/products/list"
//    }
}