package com.mr_raushan.corpuscare

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CartManager {
    val cartItems = mutableStateListOf<Pair<Medicine, Int>>()
    
    private fun syncCartToFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        // Save entire cart as a list of maps in the user document
        val cartData = cartItems.map { (medicine, qty) ->
            mapOf(
                "medicine" to medicine,
                "quantity" to qty
            )
        }
        
        db.collection("users").document(uid).update("cart", cartData)
            .addOnFailureListener {
                // If update fails (e.g. field doesn't exist), try to set it
                db.collection("users").document(uid).set(mapOf("cart" to cartData), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    fun loadCartFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val rawCart = doc.get("cart") as? List<Map<String, Any>>
                    if (rawCart != null) {
                        cartItems.clear()
                        rawCart.forEach { m ->
                            val medMap = m["medicine"] as? Map<String, Any>
                            val qty = (m["quantity"] as? Long)?.toInt() ?: 1
                            if (medMap != null) {
                                val med = Medicine(
                                    id = medMap["id"] as? String ?: "",
                                    name = medMap["name"] as? String ?: "",
                                    shopName = medMap["shopName"] as? String ?: "",
                                    shopAddress = medMap["shopAddress"] as? String ?: "",
                                    price = medMap["price"] as? String ?: "",
                                    imageUrl = medMap["imageUrl"] as? String ?: "",
                                    category = medMap["category"] as? String ?: ""
                                )
                                cartItems.add(med to qty)
                            }
                        }
                    }
                }
            }
    }

    fun addMedicine(medicine: Medicine) {
        val existing = cartItems.find { it.first.id == medicine.id }
        if (existing == null) {
            cartItems.add(medicine to 1)
        } else {
            cartItems.removeIf { it.first.id == medicine.id }
        }
        syncCartToFirestore()
    }

    fun removeMedicine(medicineId: String) {
        cartItems.removeIf { it.first.id == medicineId }
        syncCartToFirestore()
    }

    fun incrementQuantity(medicineId: String) {
        val index = cartItems.indexOfFirst { it.first.id == medicineId }
        if (index != -1) {
            val (medicine, qty) = cartItems[index]
            cartItems[index] = medicine to qty + 1
            syncCartToFirestore()
        }
    }

    fun decrementQuantity(medicineId: String) {
        val index = cartItems.indexOfFirst { it.first.id == medicineId }
        if (index != -1) {
            val (medicine, qty) = cartItems[index]
            if (qty > 1) {
                cartItems[index] = medicine to qty - 1
            } else {
                cartItems.removeAt(index)
            }
            syncCartToFirestore()
        }
    }

    fun clearCart() {
        cartItems.clear()
        syncCartToFirestore()
    }

    fun getTotal(): Int {
        return cartItems.sumOf { (it.first.price.replace("₹", "").replace(",", "").trim().toIntOrNull() ?: 0) * it.second }
    }
}
