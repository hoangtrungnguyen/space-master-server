package com.space.subadmin.products

import com.space.subadmin.brand.BrandRepository
import com.space.subadmin.category.CategoryRepository
import com.space.subadmin.inventory.InventoryRepository
import com.space.subadmin.inventory.InventoryTransactionRepository
import com.space.subadmin.users.User
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val brandRepository: BrandRepository,
    private val categoryRepository: CategoryRepository,
    private val inventoryTransactionRepository: InventoryTransactionRepository,
    private val inventoryRepository: InventoryRepository // Injected repository
) {

    /**
     * Finds a product and a specific variant, then assembles a DTO for the variant detail view.
     *
     * @param productId The ID of the parent product.
     * @param variantId The ID of the specific variant to display.
     * @return A DTO containing the product and variant details, or null if not found.
     */
    @Transactional(readOnly = true)
    fun findProductAndVariantDetail(productId: Long, variantId: Long): ProductAndVariantDetailDTO? {
        // 1. Fetch the product with all its variants efficiently
        val product = productRepository.findProductDetailById(productId).orElse(null) ?: return null

        // 2. Find the specific variant we care about from the already-loaded collection
        val variant = product.variants.find { it.id == variantId } ?: return null

        // 3. Fetch transactions for only this specific variant
        val transactions = inventoryTransactionRepository.findByProductVariantIdIn(listOf(variant.id))

        // 4. Assemble the DTO
        val productInfo = ProductAndVariantDetailDTO.ProductInfo(
            id = product.id,
            name = product.name,
            description = product.description,
            brandName = product.brand?.name,
            categoryName = product.category?.name,
            createdAt = product.createdAt,
            createdByUsername = product.createdBy.username
        )

        val variantDetail = ProductVariantDetailDTO(
            id = variant.id,
            sku = variant.sku,
            price = variant.price,
            costPrice = variant.costPrice,
            weight = variant.weight,
            attributes = variant.attributes,
            inventoryTransactions = transactions.map { tx ->
                InventoryTransactionInfoDTO(
                    type = tx.type,
                    quantity = tx.quantity,
                    notes = tx.notes,
                    createdAt = tx.createdAt
                )
            }
        )

        return ProductAndVariantDetailDTO(product = productInfo, variant = variantDetail)
    }

    /**
     * Gathers a list of all product variants and their current inventory status.
     *
     * @return A list of DTOs suitable for the main product list view.
     */
    @Transactional(readOnly = true)
    fun getAllProductVariantsWithInventory(): List<ProductVariantInventoryDTO> {
        val products = productRepository.findAllWithDetails()
        val inventories = inventoryRepository.findAll().associateBy { it.productVariant.id }

        return products.flatMap { product ->
            product.variants.map { variant ->
                val inventory = inventories[variant.id]
                ProductVariantInventoryDTO(
                    productId = product.id,
                    productName = product.name,
                    brandName = product.brand?.name,
                    categoryName = product.category?.name,
                    variantId = variant.id,
                    variantSku = variant.sku,
                    quantityOnHand = inventory?.quantityOnHand,
                    isActive = product.isActive
                )
            }
        }
    }

    /**
     * Finds a product by its ID and assembles a comprehensive DTO for the detail view.
     *
     * @param id The ID of the product to find.
     * @return A ProductDetailDTO containing all necessary information, or null if not found.
     */
    @Transactional(readOnly = true)
    fun findProductDetailById(id: Long): ProductDetailDTO? {
        val product = productRepository.findProductDetailById(id).orElse(null) ?: return null

        val variantIds = product.variants.mapNotNull { it.id }
        val allTransactions = inventoryTransactionRepository.findByProductVariantIdIn(variantIds)
            .groupBy { it.inventory.productVariant.id }

        return ProductDetailDTO(
            id = product.id,
            name = product.name,
            description = product.description,
            brandName = product.brand?.name,
            categoryName = product.category?.name,
            isActive = product.isActive,
            createdAt = product.createdAt,
            createdByUsername = product.createdBy.username,
            variants = product.variants.map { variant ->
                val variantTransactions = allTransactions[variant.id] ?: emptyList()
                ProductVariantDetailDTO(
                    id = variant.id,
                    sku = variant.sku,
                    price = variant.price,
                    costPrice = variant.costPrice,
                    weight = variant.weight,
                    attributes = variant.attributes,
                    inventoryTransactions = variantTransactions.map { tx ->
                        InventoryTransactionInfoDTO(
                            type = tx.type,
                            quantity = tx.quantity,
                            notes = tx.notes,
                            createdAt = tx.createdAt
                        )
                    }
                )
            }
        )
    }

    /**
     * Creates a new Product entity from a DTO, without any variants.
     *
     * @param dto The form data for the product.
     * @param createdBy The user creating the product.
     * @return The newly created Product.
     */
    @Transactional
    fun createProduct(dto: ProductFormDTO, createdBy: User): Product {
        val brand = dto.brandId?.let { brandRepository.findById(UUID.fromString(it)).orElse(null) }
        val category = dto.categoryId?.let { categoryRepository.findById(UUID.fromString(it)).orElse(null) }

        val product = Product(
            name = dto.name,
            description = dto.description,
            brand = brand,
            category = category,
        )
        product.createdBy = createdBy

        return productRepository.save(product)
    }

    /**
     * Adds a new ProductVariant to an existing Product.
     *
     * @param dto The form data for the new variant.
     * @return The updated Product containing the new variant.
     * @throws EntityNotFoundException if the parent product doesn't exist.
     */
    @Transactional
    fun addVariantToProduct(dto: ProductVariantFormDTO): Product {
        val productId = dto.productId ?: throw IllegalArgumentException("Product ID cannot be null.")

        val product = productRepository.findById(productId)
            .orElseThrow { EntityNotFoundException("Product not found with id: ${dto.productId}") }

        val variant = ProductVariant(
            sku = dto.sku,
            price = dto.price,
            costPrice = dto.costPrice,
            weight = dto.weight,
            attributes = "{}", // Assuming a default for now
        )

        // Establish the bi-directional relationship
        variant.product = product
        product.variants.add(variant)

        // Saving the product will cascade the save to the new variant
        return productRepository.save(product)
    }

    /**
     * Retrieves all products.
     *
     * @return A list of all Product entities.
     */
    @Transactional(readOnly = true)
    fun findAllProducts(): List<Product> {
        return productRepository.findAll()
    }
}