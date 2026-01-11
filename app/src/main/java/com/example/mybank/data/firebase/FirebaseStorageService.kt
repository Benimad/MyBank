package com.example.mybank.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String {
        val fileName = "profile_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference
            .child("profile_images/$userId/$fileName")
        
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }
    
    suspend fun uploadDocument(userId: String, documentUri: Uri, fileName: String): String {
        val ref = storage.reference
            .child("documents/$userId/$fileName")
        
        ref.putFile(documentUri).await()
        return ref.downloadUrl.await().toString()
    }
    
    suspend fun uploadReceipt(transactionId: String, receiptUri: Uri): String {
        val fileName = "receipt_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference
            .child("receipts/$transactionId/$fileName")
        
        ref.putFile(receiptUri).await()
        return ref.downloadUrl.await().toString()
    }
    
    suspend fun deleteFile(fileUrl: String) {
        val ref = storage.getReferenceFromUrl(fileUrl)
        ref.delete().await()
    }
}
