package com.kutirakone.app.utils

object ValidationUtils {
    fun isValidIndianPhone(phone: String): Boolean {
        // e.g., +91 followed by 10 digits
        return phone.matches(Regex("^\\+91[6-9]\\d{9}\$"))
    }

    fun isValidOtp(otp: String): Boolean {
        return otp.length == 6 && otp.all { it.isDigit() }
    }

    fun isValidListingSize(size: String): Boolean {
        val sizeVal = size.toDoubleOrNull()
        return sizeVal != null && sizeVal > 0.0
    }

    fun isValidPrice(price: String): Boolean {
        val priceVal = price.toDoubleOrNull()
        return priceVal != null && priceVal >= 0.0
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
}
