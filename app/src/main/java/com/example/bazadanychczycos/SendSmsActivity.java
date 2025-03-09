package com.example.bazadanychczycos;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SendSmsActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private EditText phoneNumberInput;
    private DataBase.DatabaseHelper dbHelper;
    private OrderItem selectedOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);

        // Konfiguracja toolbara
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.Wyslij_sms));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        phoneNumberInput = findViewById(R.id.edit_text_phone);
        Button sendButton = findViewById(R.id.button_send_sms);
        dbHelper = new DataBase.DatabaseHelper(this);

        sendButton.setOnClickListener(v -> showOrderSelectionDialog());
    }

    private void showOrderSelectionDialog() {
        List<OrderItem> orders = dbHelper.getAllOrdersWithIds();
        if (orders.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_orders_to_send), Toast.LENGTH_SHORT).show();
            return;
        }

        String[] orderItems = new String[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            OrderItem order = orders.get(i);
            orderItems[i] = String.format(getString(R.string.order_from_date), 
                order.getDate(), 
                order.getCustomerName());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_order_for_sms))
               .setItems(orderItems, (dialog, which) -> {
                   OrderItem selectedOrder = orders.get(which);
                   checkPermissionAndSendSMS(selectedOrder);
               })
               .setNegativeButton(getString(R.string.cancel), null)
               .show();
    }

    private void checkPermissionAndSendSMS(OrderItem order) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            selectedOrder = order;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
        } else {
            sendSMS(order);
        }
    }

    private void sendSMS(OrderItem order) {
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            phoneNumberInput.setError(getString(R.string.enter_phone_number));
            return;
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            phoneNumberInput.setError(getString(R.string.enter_valid_phone));
            return;
        }

        String message = String.format(
            getString(R.string.order_message_format),
            order.getCustomerName(),
            order.getDetails(),
            order.getTotalPrice()
        );

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            Toast.makeText(this, getString(R.string.sms_sent), Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, 
                getString(R.string.sms_error, e.getMessage()), 
                Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (selectedOrder != null) {
                    sendSMS(selectedOrder);
                }
            } else {
                Toast.makeText(this, 
                    getString(R.string.sms_permission_required), 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber.matches("[0-9]{9}");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}