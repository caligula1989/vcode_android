package il.org.free.vcode.android.api;

import il.org.free.vcode.android.data.Product;

import java.util.List;

/**
 * Author: caligula
 * Date: 23/11/13
 * Time: 15:33
 */
public class ProductCheckResultData {

    private Product product;
    private List<Product> alternatives;

    public List<Product> getAlternatives() {
        return alternatives;
    }
    public void setAlternatives(List<Product> alternatives) {
        this.alternatives = alternatives;
    }
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }
}
