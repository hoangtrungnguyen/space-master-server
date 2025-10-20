package com.space.subadmin.products

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetAllProductVariants(
    private val productVariantRepository: ProductVariantRepository
) {

    /**
     * Executes the query to fetch all product variants and maps them to DTOs.
     *
     * @return A list of [ProductAndVariantDTO.ProductVariantInfoDTO] containing combined product and variant information.
     */
    @Transactional(readOnly = true)
    fun execute(): List<ProductAndVariantDTO> {
        return productVariantRepository.findAllWithDetails().map { it.toProductAndVariantDetailDTO() }
    }

    /**
     * Extension function to map a [ProductVariant] entity to a [ProductAndVariantDTO.ProductVariantInfoDTO].
     * This keeps the mapping logic cleanly separated.
     */
    private fun ProductVariant.toProductAndVariantDetailDTO(): ProductAndVariantDTO {
        val parentProduct = this.product
        return ProductAndVariantDTO(
            product = ProductAndVariantDTO.ProductInfo(
                id = parentProduct.id,
                description = parentProduct.description,
                name = parentProduct.name,
                brandName = parentProduct.brand?.name,
                categoryName = parentProduct.category?.name,
                createdAt = parentProduct.createdAt,
                createdByUsername = parentProduct.createdBy.username

            ),
            variant = ProductAndVariantDTO.ProductVariantInfoDTO(
                id = this.id,
                sku = this.sku,
                price = this.price,
                costPrice = this.costPrice,
                weight = this.weight,
                attributes = this.attributes,
                createdAt = this.createdAt,
                updatedAt = this.updatedAt,
            )
        )
    }


}