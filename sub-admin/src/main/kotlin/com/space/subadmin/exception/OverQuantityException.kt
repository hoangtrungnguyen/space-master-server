package com.space.subadmin.exception

/**
 * Custom exception thrown when a user tries to subtract more inventory
 * than is available.
 * @param message A generic message for logging.
 * @param productVariantId The ID of the product variant that caused the error.
 * @param quantityOnHand The quantity available in inventory.
 * @param quantityToSubtract The quantity the user attempted to subtract.
 */
class OverQuantityException(
    message: String,
    val productVariantId: Long,
    val quantityOnHand: Int,
    val quantityToSubtract: Int
) : RuntimeException(message)