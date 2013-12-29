package il.org.free.vcode.android.api.listeners;

import il.org.free.vcode.android.data.Product;

import java.util.List;

/**
 * Created by caligula on 23/12/13.
 */
public interface ProductCheckCallback {
    public void onApiError(int errorCode, String msg);
    public void onProductFound(Product product, List<Product> alternatives);
    public void onProductNotFound();
}
