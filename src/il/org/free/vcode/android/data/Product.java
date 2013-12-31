package il.org.free.vcode.android.data;

import java.io.Serializable;

/**
 * Author: caligula
 * Date: 23/11/13
 * Time: 15:38
 */
public class Product implements Cloneable, Serializable {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String COMPANY = "company";
    public static final String VEGAN = "vegan";
    public static final String ISAV_APPROVED = "isav_approved";
    public static final String BARCODE = "barcode";

    private int id;
    private String name;
    private String company;
    private int vegan;
    private int isavApproved;
    private String barcode;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getIsavApproved() {
        return isavApproved;
    }
    public void setIsavApproved(int isavApproved) {
        this.isavApproved = isavApproved;
    }
    public int getVegan() {
        return vegan;
    }
    public void setVegan(int vegan) {
        this.vegan = vegan;
    }
    public String getBarcode() {
        return barcode;
    }
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    public String getCompany() {
        return company;
    }
    public void setCompany(String company) {
        this.company = company;
    }

    @Override
    public Product clone(){
        Product obj = new Product();
        obj.setId(getId());
        obj.setBarcode(getBarcode());
        obj.setName(getName());
        obj.setCompany(getCompany());
        obj.setVegan(getVegan());
        obj.setIsavApproved(getIsavApproved());
        return obj;
    }
}
