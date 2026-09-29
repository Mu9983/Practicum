

/**
 * 单个货位
 */
public class ShelfSlot {

    private boolean occupied;   //货位是否被占用
    private Parcel parcel;      //绑定包裹引用；null代表空位

    public ShelfSlot(boolean occupied, Parcel parcel) {
        this.occupied = occupied;
        this.parcel = parcel;
    }

    public ShelfSlot() {
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }
}
