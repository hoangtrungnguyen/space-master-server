package com.space.subadmin.customers

import com.space.subadmin.db.Customer
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    @Transactional
    fun createCustomer(request: CustomerRequest): Customer {
        // 1. Check if a customer with this email already exists.
        customerRepository.findByEmail(request.email)?.let {
            throw CustomerAlreadyExistsException("A customer with email '${request.email}' already exists.")
        }

        // 2. If not, create the new customer using the updated fields.
        val customer = Customer(
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            phoneNumber = request.phoneNumber
        )
        return customerRepository.save(customer)
    }

    fun findById(id: Long): Customer? {
        return customerRepository.findById(id).orElse(null)
    }

    @Transactional
    fun createCustomerZero() : Customer {
        // 2. If not, create the new customer using the updated fields.
        val customer = Customer(
            id = 0,
            firstName = "",
            lastName = "",
            email = "hoangtrungnguyen18102000@gmail.com",
            phoneNumber = "0962485001"
        )

        return customerRepository.save(customer)
    }


}
