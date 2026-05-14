package com.kutirakone.app.navigation

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object VendorDashboard : Screen("vendor_dashboard")
    object Upload : Screen("upload")
    object RequestMgmt : Screen("request_management?initialTab={initialTab}") {
        fun createRoute(initialTab: Int) = "request_management?initialTab=$initialTab"
    }
    object CustomerDashboard : Screen("customer_dashboard")
    object ListingDetail : Screen("listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "listing_detail/$listingId"
    }
    object Chat : Screen("chat/{conversationId}/{otherPartyName}/{vendorId}/{listingId}") {
        fun createRoute(conversationId: String, otherPartyName: String, vendorId: String, listingId: String) = "chat/$conversationId/$otherPartyName/$vendorId/$listingId"
    }
    object Review : Screen("review/{vendorId}/{listingId}") {
        fun createRoute(vendorId: String, listingId: String) = "review/$vendorId/$listingId"
    }
    object Inspire : Screen("inspire")
    object MapView : Screen("map_view")
    object Profile : Screen("profile")
    object PreviousOrders : Screen("previous_orders")
}
