package com.eighth.service.impl;

import com.eighth.Parcel;
import com.eighth.service.ExpressService;

public class ExpressServiceImpl implements ExpressService {

    @Override
    public void addParcel(int shelfIndex, Parcel parcel) {

    }

    @Override
    public Parcel takeParcel(String code) {
        return null;
    }

    @Override
    public Parcel undo() {
        return null;
    }

    @Override
    public Parcel searchByCode(String code) {
        return null;
    }

    @Override
    public Parcel[] getShelfExpress(int shelfIndex) {
        return new Parcel[0];
    }

    @Override
    public int getTotalExpressCount() {
        return 0;
    }

    @Override
    public Parcel[] getTimeoutExpress() {
        return new Parcel[0];
    }

    @Override
    public Parcel[] sortByCode() {
        return new Parcel[0];
    }

    @Override
    public void saveToFile(String filePath) {

    }

    @Override
    public void loadFromFile(String filePath) {

    }
}
