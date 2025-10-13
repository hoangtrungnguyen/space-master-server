package com.space.subadmin.exception

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView

/**
 * A global exception handler to catch application-specific exceptions
 * and provide user-friendly feedback.
 */
@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handles cases where a user tries to subtract more inventory than is available.
     * It redirects the user back to the adjustment form for the specific product
     * and adds a flash attribute containing the error message.
     *
     * @param ex The caught OverQuantityException.
     * @param redirectAttributes Spring's mechanism for adding attributes to a redirect.
     * @return A RedirectView that sends the user back to the correct form.
     */
    @ExceptionHandler(OverQuantityException::class)
    fun handleOverQuantity(
        ex: OverQuantityException,
        redirectAttributes: RedirectAttributes
    ): RedirectView {
        // Add the error message as a "flash attribute", which is stored in the session
        // just long enough to be displayed on the next page.
        redirectAttributes.addFlashAttribute("errorMessage", ex.message)

        // Redirect back to the adjustment form for the specific product variant.
        val redirectUrl = "/inventory/adjust/${ex.productVariantId}"
        return RedirectView(redirectUrl, true)
    }
}
