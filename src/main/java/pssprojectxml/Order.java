package pssprojectxml;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Order implements Comparable<Order> {

    private int orderId;
    private String dateCreated;
    private List<Product> productList;

    public Order() {
    }

    public Order(int orderId, String dateCreated, List<Product> productList) {
        this.orderId = orderId;
        this.dateCreated = dateCreated;
        this.productList = productList;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public long getDateCreated() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
        Date date = null;
        try {
            date = dateFormat.parse(dateCreated);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long millis = date.getTime();

        return millis;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public int compareTo(Order order) {

        return this.getDateCreated() < order.getDateCreated() ? 1 : (this.getDateCreated() == order.getDateCreated() ? 0 : -1);
    }
}
