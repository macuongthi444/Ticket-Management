package com.example.ticket_management.config;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final FirebaseDatabase firebaseDatabase;
    private final FirebaseStorage storage;

    // Constructor private để ngăn khởi tạo từ bên ngoài
    private DatabaseManager() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    // Singleton pattern: Lấy instance duy nhất
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // Lấy DatabaseReference cho một node cụ thể
    public DatabaseReference getReference(String node) {
        return firebaseDatabase.getReference(node);
    }

    public StorageReference getStorageReference(String path) {
        return storage.getReference(path);
    }

    public StorageReference getStorageReferenceFromUrl(String url) {
        return storage.getReferenceFromUrl(url);
    }
}
