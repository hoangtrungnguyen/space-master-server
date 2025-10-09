package com.space.subadmin.products

import com.space.subadmin.brand.BrandRepository
import com.space.subadmin.category.CategoryRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository
) {

    /**
     * Creates a new Product and its initial ProductVariant from a DTO.
     * This operation is transactional, ensuring that both the product and its variant
     * are saved together, or neither is.
     *
     * @param dto The form data.
     * @return The newly created Product.
     */
    @Transactional
    fun createProductWithVariant(dto: ProductFormDTO): Product {
        val brand = dto.brandId?.let { brandRepository.findById(UUID.fromString(it)).orElse(null) }
        val category = dto.categoryId?.let { categoryRepository.findById(UUID.fromString(it)).orElse(null) }

        val product = Product(
            name = dto.name,
            description = dto.description,
            brand = brand,
            category = category,
        )

        val variant = ProductVariant(
            product = product,
            sku = dto.sku,
            price = dto.price,
            costPrice = dto.costPrice,
            weight = dto.weight,
            attributes = "{}",

            )

        product.variants.add(variant)

        return productRepository.save(product)
    }

    /**
     * Updates an existing Product and its Variant from a DTO.
     * This operation is transactional.
     *
     * @param dto The form data containing updated values.
     * @return The updated Product.
     * @throws EntityNotFoundException if the product or variant doesn't exist.
     */
    @Transactional
    fun updateProductWithVariant(dto: ProductEditDTO): Product {
        val productId = dto.productId ?: throw IllegalArgumentException("Product ID cannot be null for an update.")
        val variantId = dto.variantId ?: throw IllegalArgumentException("Variant ID cannot be null for an update.")

        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product not found with id: $productId") }

        val variant = product.variants.find { it.id == variantId }
            ?: throw EntityNotFoundException("Variant not found with id: $variantId for product: $productId")

        // Find Brand and Category entities
        val brand = dto.brandId?.let { brandRepository.findById(UUID.fromString(it)).orElse(null) }
        val category = dto.categoryId?.let { categoryRepository.findById(UUID.fromString(it)).orElse(null) }

        product.updateDetails(dto.name, dto.description, brand, category)
        variant.updateDetails(dto.sku, dto.price, dto.costPrice, dto.weight)

        /*
        . The PersistenceContext automatically compares the current state of the managed product entity
        with its original state.
        If it finds any differences ("dirty" fields),
         it automatically generates and executes the appropriate UPDATE SQL statement.
         */
        return product //
    }
}