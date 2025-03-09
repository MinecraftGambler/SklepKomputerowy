package com.example.bazadanychczycos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.List;

public class OrderListActivity extends AppCompatActivity {

    private DataBase.DatabaseHelper dbHelper;
    private ListView orderListView;
    private List<OrderItem> orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.order_list_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DataBase.DatabaseHelper(this);

        orderListView = findViewById(R.id.order_list_view);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", getString(R.string.guest));

        orders = dbHelper.getOrdersByUsername(username);
        displayOrders();
    }

    private void displayOrders() {
        if (!orders.isEmpty()) {
            OrderAdapter adapter = new OrderAdapter(this, orders, dbHelper);
            orderListView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}