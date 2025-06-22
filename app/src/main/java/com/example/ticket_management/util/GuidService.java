package com.example.ticket_management.util;
import java.util.UUID;

public class GuidService {
    public static String generateGuid(){
        return UUID.randomUUID().toString();
    }

}
