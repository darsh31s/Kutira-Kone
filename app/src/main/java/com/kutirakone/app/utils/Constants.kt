package com.kutirakone.app.utils

object Constants {
    const val LISTINGS_COLLECTION = "listings"
    const val USERS_COLLECTION    = "users"
    const val REQUESTS_COLLECTION = "requests"
    const val MESSAGES_COLLECTION = "messages"
    const val REVIEWS_COLLECTION  = "reviews"
    const val DESIGN_IDEAS_COLLECTION = "designIdeas"
    const val DEFAULT_RADIUS_KM   = 5.0
    const val MAX_RADIUS_KM       = 20.0
    const val LISTING_EXPIRY_DAYS = 30L
    const val MAX_IMAGE_SIZE_KB   = 500
    const val MAX_LISTING_IMAGES  = 5
    const val MAX_REVIEW_LENGTH   = 300
    const val GEOHASH_PRECISION   = 9
    const val LOCATION_FUZZ_METRES= 500.0
    const val AI_IDEA_COUNT       = 3
    const val GEMINI_MODEL        = "gemini-2.5-flash"
    const val OTP_TIMEOUT_SECONDS = 60L
}
