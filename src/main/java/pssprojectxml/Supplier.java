package pssprojectxml;

import java.util.List;

public class Supplier {

    private String name;
    List<Product> productList;

    public Supplier() {
    }

    public Supplier(String name, List<Product> productList) {
        this.name = name;
        this.productList = productList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Supplier)) return false;
        Supplier supplier = (Supplier) obj;

        return supplier.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
