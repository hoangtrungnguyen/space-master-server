package com.space.customerinsight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
open class CustomerInsightApplication

fun main(args: Array<String>) {
    runApplication<CustomerInsightApplication>(*args)
}

// In your Spring Boot @RestController

@RestController
@RequestMapping("/api/insights")
class CsvAnalysisController {

//    @PostMapping("/analyze")
//    fun analyzeCsv(
//        @RequestHeader("X-User-Id") userId: String, // Trust this header from the Gateway
//        @RequestParam("file") file: MultipartFile
//    ): ResponseEntity<AnalysisResult> {
//
//        // The user is already authenticated. You have the userId.
//        // Now you can proceed with the business logic.
//        println("Processing file for user: $userId")
//
//        val result = analysisService.processCsv(file.inputStream)
//
//        return ResponseEntity.ok(result)
//    }

    @GetMapping("")
    fun index(): ResponseEntity<String> {
        return ResponseEntity.ok().body("Hello World!")
    }
}
