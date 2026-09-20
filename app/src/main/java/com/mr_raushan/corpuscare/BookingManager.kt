package com.mr_raushan.corpuscare

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val date: String = "",
    val time: String = "",
    val patientName: String = "",
    val patientAge: String = "",
    val patientGender: String = "",
    val patientMobile: String = "",
    val patientBloodGroup: String = "",
    val patientEmail: String = "",
    val transactionId: String = "",
    val status: String = "Upcoming"
)

object BookingManager {
    val appointments = mutableStateListOf<Appointment>()
    
    val localDoctors = listOf(
        Doctor(
            id = "local_doc_1",
            name = "Dr. Thakur Bijendra Kr. Prasad (BAMS)",
            hospitalName = "City Care Life Hospital",
            experience = "8 Yrs Experience",
            department = "Cardiology",
            pincode = "841203",
            rating = "4.9",
            city = "Siwan",
            imageUrl = "" 
        ),
        Doctor(
            id = "local_doc_2",
            name = "Dr. Pankaj Kumar",
            hospitalName = "Sanjeevani Hospital",
            experience = "12 Yrs Experience",
            department = "Cardiology",
            pincode = "841203",
            rating = "4.8",
            city = "Indore"
        ),
        Doctor(
            id = "local_doc_3",
            name = "Dr. Ananya Sharma",
            hospitalName = "Apex Medical Centre",
            experience = "5 Yrs Experience",
            department = "Orthopedics",
            pincode = "801506",
            rating = "4.7",
            city = "Siwan"
        ),
        Doctor(
            id = "local_doc_4",
            name = "Dr. Sameer Khan",
            hospitalName = "City Care Life Hospital",
            experience = "15 Yrs Experience",
            department = "ENT",
            pincode = "801506",
            rating = "4.6",
            city = "Siwan"
        )
    )

    fun addAppointment(
        doctorId: String,
        doctorName: String,
        facilityId: String,
        facilityName: String,
        date: String,
        time: String,
        patientName: String,
        patientAge: String,
        patientGender: String,
        patientMobile: String,
        patientBloodGroup: String,
        patientEmail: String,
        transId: String
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val newAppointment = Appointment(
            id = UUID.randomUUID().toString(),
            userId = uid,
            doctorId = doctorId,
            doctorName = doctorName,
            facilityId = facilityId,
            facilityName = facilityName,
            date = date,
            time = time,
            patientName = patientName,
            patientAge = patientAge,
            patientGender = patientGender,
            patientMobile = patientMobile,
            patientBloodGroup = patientBloodGroup,
            patientEmail = patientEmail,
            transactionId = transId
        )
        
        appointments.add(newAppointment)
        
        // Save to User's private collection
        if (uid.isNotEmpty()) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .document(uid)
                .collection("appointments")
                .document(newAppointment.id)
                .set(newAppointment)
                
            // Save to Global appointments collection for Facilities to access
            db.collection("appointments")
                .document(newAppointment.id)
                .set(newAppointment)
        }
    }

    fun loadAppointmentsFromFirestore() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("appointments")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Appointment::class.java) }
                appointments.clear()
                appointments.addAll(list)
            }
    }

    fun clearData() {
        appointments.clear()
    }
}
