package com.space.subadmin.products

import com.space.subadmin.brand.BrandRepository
import com.space.subadmin.category.CategoryRepository
import com.space.subadmin.users.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
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


