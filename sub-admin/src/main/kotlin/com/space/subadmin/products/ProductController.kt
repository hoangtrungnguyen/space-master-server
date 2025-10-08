package com.space.subadmin.products

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

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
        return productRepository.findAll()
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
class ProductWebController(private val productRepository: ProductRepository) {

    /**
     * Displays the form to add a new product.
     * Responds to GET requests at /products/add
     *
     * @param model The Spring Model to pass data to the view.
     * @return The name of the Thymeleaf template to render.
     */
    @GetMapping("/add")
    fun showAddProductForm(model: Model): String {
        // Add an empty Product object to the model to bind form data
        model.addAttribute("product", Product(name = "", price = 0.0))
        return "add-product" // This corresponds to 'src/main/resources/templates/add-product.html'
    }

    /**
     * Processes the submission of the add product form.
     * Responds to POST requests at /products/add
     *
     * @param product The Product object populated with form data.
     * @return A redirect instruction to the product list page.
     */
    @PostMapping("/add")
    fun addProduct(@ModelAttribute product: Product): String {
        productRepository.save(product)
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
        model.addAttribute("products", productRepository.findAll())
        return "products-list" // This corresponds to 'src/main/resources/templates/products-list.html'
    }
}
