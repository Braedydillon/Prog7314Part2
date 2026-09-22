package com.example.prog7314p2.Firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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