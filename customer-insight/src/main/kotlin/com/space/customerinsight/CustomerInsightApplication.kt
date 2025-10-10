package com.space.customerinsight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@SpringBootApplication
open class CustomerInsightApplication

fun main(args: Array<String>) {
    runApplication<CustomerInsightApplication>(*args)
}

// In your Spring Boot @RestController

@RestController
@RequestMapping("/api/customers/")
class CsvCustomerController(
    private val csvProcessingService: ProcessCsvService
) {

    @PostMapping("/upload")
    fun upload(
        @RequestHeader("X-User-Id") userId: String, // Trust this header from the Gateway
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<String> {
        // Basic validation for content type
        if (file.isEmpty || file.contentType != "text/csv") {
            return ResponseEntity.badRequest().body("Please upload a non-empty CSV file.")
        }

        return try {
//            val customers = csvProcessingService.processMallCustomerCsv(file)
            // Return the list of products or a success message
//            ResponseEntity.ok("Customers' size: ${customers.size}")
            ResponseEntity.ok("Customers' size: 12")
        } catch (e: Exception) {
            // A more specific exception handling is better in production
            ResponseEntity.internalServerError().body("Failed to process CSV file: ${e.message}")
        }
    }


    @GetMapping("/insights/gender")
    fun getGender(@RequestHeader("X-User-Id") userId: String): ResponseEntity<String> {
        println("Processing user: $userId")

        return ResponseEntity.ok("Okay")
    }


    @GetMapping("")
    fun index(): ResponseEntity<String> {
        return ResponseEntity.ok().body("Hello World!")
    }
}
