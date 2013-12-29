package il.org.free.vcode.android.api;

/**
 * Author: caligula
 * Date: 23/11/13
 * Time: 17:36
 */
public class GenericApiResult<K> {
    private int res;
    private String error;
    private K data;

    public int getRes() {
        return res;
    }
    public void setRes(int res) {
        this.res = res;
    }
    public String getError() {
        return error;
    }
    public void setError(String error) {
        this.error = error;
    }
    public K getData() {
        return data;
    }
    public void setData(K data) {
        this.data = data;
    }
}
