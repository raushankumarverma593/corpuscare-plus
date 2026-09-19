package com.mr_raushan.corpuscare

data class Hospital(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val state: String = "",
    val city: String = "",
    val pincode: String = "",
    val contact: String = "",
    val departments: List<String> = emptyList(),
    val checkups: List<String> = emptyList(),
    val type: String = "Private",
    val imageUrl: String = ""
)

data class Clinic(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val state: String = "",
    val city: String = "",
    val pincode: String = "",
    val contact: String = "",
    val checkups: List<String> = emptyList(),
    val type: String = "Private",
    val imageUrl: String = ""
)

data class Doctor(
    val id: String = "",
    val name: String = "",
    val hospitalName: String = "",
    val facilityId: String = "", // Added facilityId
    val experience: String = "",
    val department: String = "",
    val pincode: String = "",
    val rating: String = "4.8",
    val imageUrl: String = "",
    val city: String = "",
    val state: String = ""
)

data class Medicine(
    val id: String = "",
    val name: String = "",
    val shopName: String = "",
    val shopAddress: String = "",
    val price: String = "",
    val imageUrl: String = "",
    val category: String = ""
)

data class Admin(
    val id: String = "",
    val name: String = "",
    val passcode: String = ""
)

data class Ambulance(
    val id: String = "",
    val driverName: String = "",
    val vehicleNumber: String = "",
    val contact: String = "",
    val pincode: String = "",
    val type: String = "Normal", // Normal, Oxygen, Ventilator
    val status: String = "Available" // Available, Busy
)

data class Order(
    val id: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Int = 0,
    val deliveryAddress: String = "",
    val date: String = "",
    val transactionId: String = "",
    val status: String = "Confirmed"
)

data class OrderItem(
    val medicineName: String = "",
    val price: String = "",
    val quantity: Int = 0
)

data class MedicalReport(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val date: String = "",
    val reportTitle: String = "",
    val reportUrl: String = "", // URL to the uploaded file/image
    val remarks: String = ""
)
