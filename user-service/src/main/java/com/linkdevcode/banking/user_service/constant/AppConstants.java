package com.linkdevcode.banking.user_service.constant;

public final class AppConstants {
	private AppConstants(){
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated"); 
    }

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_BATCH_SERVICE = "ROLE_BATCH_SERVICE";
    
    public static final String CURRENCY_VND = "VND";
    public static final String CURRENCY_USD = "USD";

    public static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    public static final String ACCOUNT_STATUS_SUSPENDED = "SUSPENDED";

}
