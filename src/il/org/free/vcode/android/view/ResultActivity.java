package il.org.free.vcode.android.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import il.org.free.vcode.android.R;
import il.org.free.vcode.android.api.ApiClient;
import il.org.free.vcode.android.api.listeners.ProductCheckCallback;
import il.org.free.vcode.android.api.listeners.ProductReportCallback;
import il.org.free.vcode.android.data.Product;

import java.util.List;

/**
 * Author: caligula
 * Date: 20/10/13
 * Time: 16:32
 */
public class ResultActivity extends Activity
        implements View.OnClickListener, ProductCheckCallback, ProductReportCallback {

    public static final String EXTRA_BARCODE_TEXT = "barcode";

    public static final int IS_VEGAN = 1;
    public static final int IS_NOT_VEGAN = 2;
    private static final String TAG = ResultActivity.class.getSimpleName();

    private Product product;

    private String barcode;
    private TextView productName;

    private TextView resultText;
    private TextView barcodeText;
    private Button reportVeganButton;
    private Button reportNotVeganButton;
    private LinearLayout resultLayout;
    private RelativeLayout progressBarContainer;
    private TextView companyName;
    private TextView errorMessage;
    private Button retryButton;
    private LinearLayout errorLayout;
    private ImageView veganismResultDrawable;

    // Framework callbacks
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_activity);
        registerViews();
        bindViewListeners();
        setDefaultLayout();

        // Start checking the product
        if(!TextUtils.isEmpty(barcode)) {
            ApiClient.checkProductAsync(this, barcode, this);
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.report_vegan:
            case R.id.report_not_vegan:
                reportProduct(view.getId());
                break;
        }
    }

    // Internal preprocessing
    private void registerViews(){
        productName = (TextView) findViewById(R.id.product_name);
        resultText = (TextView) findViewById(R.id.veganism_result);
        barcodeText = (TextView) findViewById(R.id.barcode_text);
        reportVeganButton = (Button) findViewById(R.id.report_vegan);
        reportNotVeganButton = (Button) findViewById(R.id.report_not_vegan);
        resultLayout = (LinearLayout) findViewById(R.id.result_layout_container);
        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        companyName = (TextView) findViewById(R.id.company_name);
        errorLayout = (LinearLayout) findViewById(R.id.error_layout);
        errorMessage = (TextView) findViewById(R.id.error_msg);
        retryButton = (Button) findViewById(R.id.retry_button);
        veganismResultDrawable = (ImageView) findViewById(R.id.veganism_result_drawable);
    }
    private void bindViewListeners(){
        reportVeganButton.setOnClickListener(this);
        reportNotVeganButton.setOnClickListener(this);
    }
    private void setDefaultLayout(){
        resultText.setText(getResources().getText(R.string.checking_product));
        barcodeText.setText(barcode);
        barcode = getIntent().getStringExtra(EXTRA_BARCODE_TEXT);
    }

    // Methods to report product info
    private void reportProduct(int itemClicked){
        Product productClone;

        if(product == null){
            productClone = new Product();
            productClone.setBarcode(barcode);
            productClone.setVegan(itemClicked == R.id.report_vegan ? 1 : 0);
            // Show dialog to ask for product name
            showNameInputDialog(productClone);
            return;
        }

        productClone = product.clone();
        productClone.setVegan(itemClicked == R.id.report_vegan ? IS_VEGAN : IS_NOT_VEGAN);
        ApiClient.reportProductAsync(this, productClone, this);

    }
    private void showNameInputDialog(final Product product){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(getString(R.string.product_details));
        final EditText name = new EditText(this);
        final EditText company = new EditText(this);
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        company.setHint(getString(R.string.company_name));
        name.setHint(getString(R.string.add_product_name));
        layout.addView(name);
        layout.addView(company);
        alertBuilder.setView(layout);
        alertBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                product.setName(name.getText().toString());
                ApiClient.reportProductAsync(ResultActivity.this, product, ResultActivity.this);
                return;
            }
        });
        alertBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertBuilder.show();
    }

    // Internal helpers
    private void showError(String error, final Runnable retry){
        progressBarContainer.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorMessage.setText(error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retry.run();
            }
        });
    }
    private void processNoResult(){
        veganismResultDrawable.setImageResource(R.drawable.custom_veganism_unknown);
        resultText.setText(getResources().getText(R.string.product_is_unknown));
        reportNotVeganButton.setVisibility(View.VISIBLE);
        reportVeganButton.setVisibility(View.VISIBLE);
    }
    private void showAlternatives(List<Product> alternatives){
        if(alternatives != null && alternatives.size() > 0){
            ListView alternativesList = (ListView) findViewById(R.id.alternative_list);
            ArrayAdapter adapter = new AlternativesAdapter(this, R.layout.alternative_list_item);
            adapter.addAll(alternatives);
            TextView title = new TextView(this);
            title.setText(R.string.alternatives);
            alternativesList.addHeaderView(title);
            alternativesList.setAdapter(adapter);
            alternativesList.setVisibility(View.VISIBLE);
        }
    }

    // Product check listener implementation
    @Override
    public void onProductFound(Product product, List<Product> alternatives) {
        Log.d(TAG, "Product found: "+product);
        this.product = product;
        progressBarContainer.setVisibility(View.GONE);
        productName.setText(product.getName());
        companyName.setText(product.getCompany());

        if(product.getVegan() == IS_VEGAN){
            veganismResultDrawable.setImageResource(R.drawable.custom_is_vegan);
            resultText.setText(getResources().getText(R.string.product_is_vegan));
            reportNotVeganButton.setVisibility(View.VISIBLE);
        } else if(product.getVegan() == IS_NOT_VEGAN) {
            veganismResultDrawable.setImageResource(R.drawable.custom_not_vegan);
            resultText.setText(getResources().getText(R.string.product_is_not_vegan));
            reportVeganButton.setVisibility(View.VISIBLE);
            showAlternatives(alternatives);
        } else {
            processNoResult();
        }
        resultLayout.setVisibility(View.VISIBLE);
    }
    @Override
    public void onProductNotFound() {
        processNoResult();
        progressBarContainer.setVisibility(View.GONE);
        productName.setText("");
        companyName.setText("");
        resultLayout.setVisibility(View.VISIBLE);
    }
    @Override
    public void onApiError(int errorCode, String msg) {
        Log.d(TAG, "an error occured. code: "+errorCode+", message: "+msg);
        String errMsg = TextUtils.isEmpty(msg) ? getString(R.string.no_internet_connection) : msg;
        Runnable retry = new Runnable() {
            @Override
            public void run() {
                errorLayout.setVisibility(View.GONE);
                progressBarContainer.setVisibility(View.VISIBLE);
                ApiClient.checkProductAsync(ResultActivity.this, barcode, ResultActivity.this);
            }
        };
        showError(errMsg, retry);
    }

    // Product report callback implementation
    @Override
    public void onReportFail() {
        Toast.makeText(ResultActivity.this,"Reporting failed",Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onReportSuccess() {
        Toast.makeText(ResultActivity.this,"Report sent",Toast.LENGTH_SHORT).show();
        reportNotVeganButton.setVisibility(View.GONE);
        reportVeganButton.setVisibility(View.GONE);
    }

    // Adapter for alternative products
    private class AlternativesAdapter extends ArrayAdapter {

        public AlternativesAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Product product = (Product) getItem(position);
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(R.id.product_title);
            textView.setText(product.getName());
            return view;
        }
    }

}
