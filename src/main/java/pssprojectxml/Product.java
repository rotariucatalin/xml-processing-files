package pssprojectxml;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_EMPTY) //This annotation is used to display in the xml only the fields with the value
public class Product implements Serializable, Comparable<Product> {

    private String description;
    private String gtin;
    private Price price;
    private String supplier;

    public Product() {
    }

    public Product(String description, String gtin, Price price, String supplier) {
        this.description = description;
        this.gtin = gtin;
        this.price = price;
        this.supplier = supplier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGtin() {
        return gtin;
    }

    public void setGtin(String gtin) {
        this.gtin = gtin;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    @Override
    public int compareTo(Product product) {
        return this.getPrice().getPrice() < product.getPrice().getPrice() ? 1 : (this.getPrice().getPrice() == product.getPrice().getPrice() ? 0 : -1);
    }
}
