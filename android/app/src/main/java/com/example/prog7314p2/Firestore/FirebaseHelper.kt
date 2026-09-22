package com.example.prog7314p2.Firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.prog7314p2.Models.OrderHistoryModel
import com.google.firebase.firestore.ListenerRegistration

class FirebaseHelper {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get the currently logged-in Firebase user's ID
    private fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // Cart
    fun addToCart(
        productId: Int,
        quantity: Int = 1,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        val cartRef = db.collection("carts")
            .document(userId)
            .collection("items")
            .document(productId.toString())

        cartRef.get()
            .addOnSuccessListener { document ->

                val currentQuantity =
                    if (document.exists()) {
                        document.getLong("quantity")?.toInt() ?: 0
                    } else {
                        0
                    }

                val cartItem = CartItem(
                    productId = productId,
                    quantity = currentQuantity + quantity
                )

                cartRef.set(cartItem)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun getCartItems(
        onSuccess: (List<CartItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("carts")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->

                val cartItems =
                    documents.toObjects(CartItem::class.java)

                onSuccess(cartItems)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun updateCartQuantity(
        productId: Int,
        quantity: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        if (quantity <= 0) {
            removeFromCart(
                productId,
                onSuccess,
                onFailure
            )
            return
        }

        db.collection("carts")
            .document(userId)
            .collection("items")
            .document(productId.toString())
            .update("quantity", quantity)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun removeFromCart(
        productId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("carts")
            .document(userId)
            .collection("items")
            .document(productId.toString())
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun clearCart(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("carts")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { snapshot ->

                val batch = db.batch()

                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }

                batch.commit()
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener {
                        onFailure(it)
                    }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    // Orders
    fun saveOrder(
        order: OrderHistoryModel,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("users")
            .document(userId)
            .collection("orders")
            .document(order.orderId)
            .set(order)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun listenToOrderHistory(
        onUpdate: (List<OrderHistoryModel>) -> Unit,
        onFailure: (Exception) -> Unit
    ): ListenerRegistration? {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return null
        }

        return db.collection("users")
            .document(userId)
            .collection("orders")
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    onFailure(exception)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onFailure(Exception("Failed to retrieve orders"))
                    return@addSnapshotListener
                }

                val orders = snapshot.documents.mapNotNull { document ->
                    document.toObject(OrderHistoryModel::class.java)
                }

                onUpdate(orders)
            }
    }

    // Wishlist
    fun addToWishlist(
        productId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        val wishlistItem = WishlistItem(
            productId = productId
        )

        db.collection("wishlists")
            .document(userId)
            .collection("items")
            .document(productId.toString())
            .set(wishlistItem)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun getWishlistItems(
        onSuccess: (List<WishlistItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("wishlists")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { documents ->

                val wishlistItems =
                    documents.toObjects(WishlistItem::class.java)

                onSuccess(wishlistItems)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }


    fun removeFromWishlist(
        productId: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {

        val userId = getUserId()

        if (userId == null) {
            onFailure(Exception("User is not logged in"))
            return
        }

        db.collection("wishlists")
            .document(userId)
            .collection("items")
            .document(productId.toString())
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}