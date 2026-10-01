package com.eighth;

import com.eighth.service.impl.ExpressServiceImpl;

public class AddParcelTest {

    public static void main(String[] args) {
        ExpressServiceImpl expressService = new ExpressServiceImpl();

        expressService.addParcel("122384123", "mu9983", 10, 1);

        Parcel parcel = expressService.getMyHashMap().get("122384123");
        System.out.println(parcel);

    }
}
