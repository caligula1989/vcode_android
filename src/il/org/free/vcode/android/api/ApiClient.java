package il.org.free.vcode.android.api;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import il.org.free.vcode.android.R;
import il.org.free.vcode.android.api.listeners.ProductCheckCallback;
import il.org.free.vcode.android.api.listeners.ProductReportCallback;
import il.org.free.vcode.android.data.Product;
import il.org.free.vcode.android.utils.NetworkUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: caligula
 * Date: 23/11/13
 * Time: 15:17
 */
public class ApiClient {

    public static final String WEBSERVICE = "http://vegansite.net/vcode/";
    public static final String CHECK_BARCODE = "?controller=Products&action=check&id=";
    public static final String REPORT_PRODUCT = "?controller=Products&action=report";

    private static final int GENARAL_CONNECTION_TIMEOUT = 9000;
    private static final int GENERAL_SOCKET_TIMEOUT = 25000;

    // Operation IDs
    public static final String OPERATION_ID = "operationId";
    public static final int OPERATION_PRODUCT_CHECK = 1;
    public static final int OPERATION_PRODUCT_REPORT = 2;

    // Api call arguments
    public static final String ARG_BARCODE = "barcode";
    public static final String ARG_PRODUCT = "product";

    // Listeners
    public static ProductCheckCallback mProductCheckCallback;
    public static ProductReportCallback mProductReportCallback;

    // Public API to make asynchronous API calls
    public static void checkProductAsync(Context context, String barcode, ProductCheckCallback listener){
        mProductCheckCallback = listener;
        Bundle args = new Bundle();
        args.putInt(OPERATION_ID, OPERATION_PRODUCT_CHECK);
        args.putString(ARG_BARCODE, barcode);
        if(!NumberUtils.isNumber(barcode)){
            mProductCheckCallback.onApiError(ErrorCodes.INVALID_BARCODE, context.getString(R.string.invalid_barcode));
            return;
        }
        if(!NetworkUtil.connectionPresent(context)){
            mProductCheckCallback.onApiError(ErrorCodes.NO_NETWORK, context.getString(R.string.no_internet_connection));
            return;
        }
        new ApiCall().execute(args);
    }
    public static void reportProductAsync(Context context, Product product, ProductReportCallback callback){
        mProductReportCallback = callback;
        Bundle args = new Bundle();
        args.putInt(OPERATION_ID, OPERATION_PRODUCT_REPORT);
        args.putSerializable(ARG_PRODUCT, product);
        if(NetworkUtil.connectionPresent(context)){
            new ApiCall().execute(args);
            return;
        }
        mProductReportCallback.onReportFail();
    }
    private static void productCheckResult(GenericApiResult result){
        if(result == null){
            mProductCheckCallback.onApiError(ErrorCodes.API_NOT_ACCESSIBLE, "No API access");
            return;
        }
        if(result.getData() == null ||
                !result.getData().getClass().equals(ProductCheckResultData.class) ||
                !TextUtils.isEmpty(result.getError())){
            Log.d("JOSH", "Error is: "+result.getError());
            Log.d("JOSH", "Data class is: "+result.getData().getClass());
            mProductCheckCallback.onApiError(result.getRes(), result.getError());
            return;
        }

        ProductCheckResultData resData = (ProductCheckResultData) result.getData();
        Product product = resData.getProduct();
        if(product == null || result.getRes() == 5){
            mProductCheckCallback.onProductNotFound();
            return;
        }

        mProductCheckCallback.onProductFound(product, resData.getAlternatives());
    }
    private static void productReportResult(GenericApiResult result){
        if(result == null){
            mProductReportCallback.onReportFail();
            return;
        }

        if(!TextUtils.isEmpty(result.getError())){
            mProductReportCallback.onReportFail();
            return;
        }
        mProductReportCallback.onReportSuccess();

    }

    // Synchronous api calls
    public static GenericApiResult checkProduct(String barcode){
        StringBuilder path = new StringBuilder(WEBSERVICE);
        path.append(CHECK_BARCODE);
        path.append(barcode);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        JsonElement element = null;
        try {
            element = doPostRequest(path.toString(), nameValuePairs);
            Log.d("JOSH","Element: "+element);
            Type cType = new TypeToken<GenericApiResult<ProductCheckResultData>>() {}.getType();
            return new Gson().fromJson(element, cType);
        } catch (JsonParseException e){
            Type cType = new TypeToken<GenericApiResult>() {}.getType();
            return new Gson().fromJson(element, cType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static GenericApiResult reportProduct(Product product){
        StringBuilder path = new StringBuilder(WEBSERVICE);
        path.append(REPORT_PRODUCT);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(Product.COMPANY, product.getCompany()));
        nameValuePairs.add(new BasicNameValuePair(Product.BARCODE, product.getBarcode()));
        nameValuePairs.add(new BasicNameValuePair(Product.NAME, product.getName()));
        nameValuePairs.add(new BasicNameValuePair(Product.VEGAN, String.valueOf(product.getVegan())));
        nameValuePairs.add(new BasicNameValuePair(Product.ID, String.valueOf(product.getId())));
        try {
            JsonElement element = doPostRequest(path.toString(), nameValuePairs);
            GenericApiResult result = new Gson().fromJson(element, GenericApiResult.class);
            return result;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // API Access
    private static JsonElement doPostRequest(String url, List<NameValuePair> nameValuePairs) throws IOException {
        JsonElement element;
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = GENARAL_CONNECTION_TIMEOUT;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = GENERAL_SOCKET_TIMEOUT;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        try {
            HttpPost httppost = new HttpPost(url);
            String json;
            httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httppost);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), HTTP.UTF_8));
            json = reader.readLine();
            element = (json != null ? new JsonParser().parse(json) : null);
        } catch (MalformedJsonException e) {
            return null;
        }
        return element;
    }

    // Api access async task
    public static class ApiCall extends AsyncTask<Bundle, Void, GenericApiResult>{

        private int operationId;

        @Override
        public void onPreExecute(){
            super.onPreExecute();
            // stub
        }
        @Override
        protected GenericApiResult doInBackground(Bundle... params) {
            Bundle args = params[0];
            if(args == null) return null;
            operationId = params[0].getInt(OPERATION_ID);
            switch (operationId){
                case OPERATION_PRODUCT_CHECK:
                    String barcode = params[0].getString(ARG_BARCODE);
                    return checkProduct(barcode);
                case OPERATION_PRODUCT_REPORT:
                    Product product = (Product) params[0].getSerializable(ARG_PRODUCT);
                    return reportProduct(product);
            }
            return null;
        }
        @Override
        public void onPostExecute(GenericApiResult result){
            super.onPostExecute(result);
            switch (operationId){
                case OPERATION_PRODUCT_CHECK:
                    productCheckResult(result);
                    break;
                case OPERATION_PRODUCT_REPORT:
                    productReportResult(result);
                    break;
            }
        }
    }
}
