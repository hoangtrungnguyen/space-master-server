package com.space.subadmin.products

import com.space.subadmin.brand.BrandRepository
import com.space.subadmin.category.CategoryRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * REST controller for managing products.
 * Exposes endpoints to create and retrieve products.
 *
 * @param productRepository The repository for accessing product data.
 */
@RestController
@RequestMapping("/api/products")
class ProductController(private val productRepository: ProductRepository) {

    /**
     * Creates a new product.
     * Responds to POST requests at /api/products
     *
     * @param product The product data from the request body.
     * @return The saved product with its generated ID.
     */
    @PostMapping
    fun createProduct(@RequestBody product: Product): ResponseEntity<Product> {
        val savedProduct = productRepository.save(product)
        return ResponseEntity(savedProduct, HttpStatus.CREATED)
    }

    /**
     * Retrieves all products.
     * Responds to GET requests at /api/products
     *
     * @return A list of all products.
     */
    @GetMapping
    fun getAllProducts(): List<Product> {
        return productRepository.findAll().also {
            print("Found products: ${it}")
        }
    }

    /**
     * Deletes a product by its ID.
     * Responds to DELETE requests at /api/products/{id}
     *
     * @param id The ID of the product to delete.
     * @return A response entity indicating the outcome. 204 No Content on success, 404 Not Found if it doesn't exist.
     */
    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        return if (productRepository.existsById(id)) {
            productRepository.deleteById(id)
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
}

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
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository
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
        model.addAttribute("allBrands", brandRepository.findAll())
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
    fun addProduct(@ModelAttribute("productForm") productForm: ProductFormDTO): String {
        productService.createProductWithVariant(productForm)
        return "redirect:/products/list" // Redirect to the product list page after saving
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
        model.addAttribute("products", productRepository.findAllWithDetails())
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
                    brandId = product.brand?.id?.toString(),
                    categoryId = product.category?.id?.toString(),
                    sku = variant?.sku ?: "",
                    price = variant?.price ?: java.math.BigDecimal.ZERO,
                    costPrice = variant?.costPrice,
                    weight = variant?.weight
                )

                model.addAttribute("productForm", productForm)
                model.addAttribute("allBrands", brandRepository.findAll())
                model.addAttribute("allCategories", categoryRepository.findAll())
                "edit-product" // Renders 'src/main/resources/templates/edit-product.html'
            }
            .orElse("redirect:/products/list") // Redirect if product not found
    }

    /**
     * Processes the submission of the edit product form.
     * Responds to POST requests at /products/edit/{id}
     *
     * @param id The ID of the product being updated.
     * @param productForm The DTO populated with form data.
     * @return A redirect instruction to the product list page.
     */
    @PostMapping("/edit/{id}")
    fun updateProduct(@PathVariable id: UUID, @ModelAttribute("productForm") productForm: ProductEditDTO): String {
        productService.updateProductWithVariant(productForm)
        return "redirect:/products/list"
    }
}
