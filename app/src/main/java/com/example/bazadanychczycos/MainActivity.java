package com.example.bazadanychczycos;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.RequiresApi;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.content.res.Configuration;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Spinner spinnerMouse, spinnerKeyboard, spinnerMonitor, spinnerComputer;
    private DataBase.DatabaseHelper dbHelper;
    private Toolbar toolbar;
    private SeekBar quantitySeekBar, quantitySeekBarMouse, quantitySeekBarKeyboard, quantitySeekBarMonitor;
    private Button decreaseButton, increaseButton, decreaseButtonMouse, increaseButtonMouse, 
                   decreaseButtonKeyboard, increaseButtonKeyboard, decreaseButtonMonitor, increaseButtonMonitor;
    private CheckBox checkBoxMouse, checkBoxKeyboard, checkBoxMonitor;
    private TextView quantityTextMouse, quantityTextKeyboard, quantityTextMonitor;
    private RecyclerView recyclerView;
    private ArrayList<Item> itemList;
    private String username;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLocale(getPreferredLanguage());
        setContentView(R.layout.activity_main);

        // Konfiguracja toolbara
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }

        // Inicjalizacja bazy danych
        dbHelper = new DataBase.DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);

        // Inicjalizacja spinnerów
        spinnerMouse = findViewById(R.id.spinner_mouse);
        spinnerKeyboard = findViewById(R.id.spinner_keyboard);
        spinnerMonitor = findViewById(R.id.spinner_monitor);
        spinnerComputer = findViewById(R.id.spinner_computer);

        // Konfiguracja spinnerów i ich listenerów
        initializeSpinners();
        setupSpinnerListeners();

        // Konfiguracja przycisku zamówienia
        Button orderButton = findViewById(R.id.button_order);
        orderButton.setOnClickListener(v -> handleOrder());

        // Inicjalizacja kontrolek ilości
        setupQuantityControls();

        // Inicjalizacja CheckBoxów
        checkBoxMouse = findViewById(R.id.checkbox_mouse);
        checkBoxKeyboard = findViewById(R.id.checkbox_keyboard);
        checkBoxMonitor = findViewById(R.id.checkbox_monitor);

        checkBoxMouse.setOnCheckedChangeListener((buttonView, isChecked) -> updateTotalPrice());
        checkBoxKeyboard.setOnCheckedChangeListener((buttonView, isChecked) -> updateTotalPrice());
        checkBoxMonitor.setOnCheckedChangeListener((buttonView, isChecked) -> updateTotalPrice());

        // Inicjalizacja kontrolek ilości dla każdego produktu
        quantitySeekBarMouse = findViewById(R.id.seekbar_quantity_mouse);
        quantitySeekBarKeyboard = findViewById(R.id.seekbar_quantity_keyboard);
        quantitySeekBarMonitor = findViewById(R.id.seekbar_quantity_monitor);

        decreaseButtonMouse = findViewById(R.id.button_decrease_mouse);
        increaseButtonMouse = findViewById(R.id.button_increase_mouse);
        decreaseButtonKeyboard = findViewById(R.id.button_decrease_keyboard);
        increaseButtonKeyboard = findViewById(R.id.button_increase_keyboard);
        decreaseButtonMonitor = findViewById(R.id.button_decrease_monitor);
        increaseButtonMonitor = findViewById(R.id.button_increase_monitor);

        quantityTextMouse = findViewById(R.id.text_quantity_mouse);
        quantityTextKeyboard = findViewById(R.id.text_quantity_keyboard);
        quantityTextMonitor = findViewById(R.id.text_quantity_monitor);

        setupProductQuantityControls(quantitySeekBarMouse, decreaseButtonMouse,
            increaseButtonMouse, quantityTextMouse, "mouse");
        setupProductQuantityControls(quantitySeekBarKeyboard, decreaseButtonKeyboard,
            increaseButtonKeyboard, quantityTextKeyboard, "keyboard");
        setupProductQuantityControls(quantitySeekBarMonitor, decreaseButtonMonitor,
            increaseButtonMonitor, quantityTextMonitor, "monitor");

        recyclerView = findViewById(R.id.recycler_view_shortcuts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemList = new ArrayList<>();

        // Ustawienie nazwy użytkownika
        TextView userProfileTextView = findViewById(R.id.text_user_profile);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = sharedPreferences.getString("username", getString(R.string.guest));
        userProfileTextView.setText(String.format(getString(R.string.logged_in_as), username));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createShortcuts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void initializeSpinners() {
        setupSpinner(spinnerComputer, "komputery");
        setupSpinner(spinnerMouse, "myszki");
        setupSpinner(spinnerKeyboard, "klawiatury");
        setupSpinner(spinnerMonitor, "monitory");
    }

    private void setupSpinner(Spinner spinner, String category) {
        List<Item> items = dbHelper.getItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println(getString(R.string.no_products_category, category));
        }
        ArrayAdapter<Item> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupSpinnerListeners() {
        spinnerMouse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = (Item) parent.getItemAtPosition(position);
                updateItemDetails(selectedItem, R.id.image_mouse, R.id.text_mouse_name,
                    R.id.text_mouse_description, R.id.text_mouse_price);
                quantitySeekBarMouse.setProgress(0);
                quantityTextMouse.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerKeyboard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = (Item) parent.getItemAtPosition(position);
                updateItemDetails(selectedItem, R.id.image_keyboard, R.id.text_keyboard_name,
                    R.id.text_keyboard_description, R.id.text_keyboard_price);
                quantitySeekBarKeyboard.setProgress(0);
                quantityTextKeyboard.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerMonitor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = (Item) parent.getItemAtPosition(position);
                updateItemDetails(selectedItem, R.id.image_monitor, R.id.text_monitor_name,
                    R.id.text_monitor_description, R.id.text_monitor_price);
                quantitySeekBarMonitor.setProgress(0);
                quantityTextMonitor.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerComputer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Item selectedItem = (Item) parent.getItemAtPosition(position);
                updateItemDetails(selectedItem, R.id.image_computer, R.id.text_computer_name,
                    R.id.text_computer_description, R.id.text_computer_price);
                quantitySeekBar.setProgress(0);
                TextView quantityText = findViewById(R.id.text_quantity);
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateItemDetails(Item item, int imageViewId, int nameViewId, int descriptionViewId, int priceViewId) {
        if (item == null) {
            System.out.println("Item jest null!");
            return;
        }

        try {
            ImageView imageView = findViewById(imageViewId);
            TextView nameView = findViewById(nameViewId);
            TextView descriptionView = findViewById(descriptionViewId);
            TextView priceView = findViewById(priceViewId);

            nameView.setText(item.getName());
            descriptionView.setText(item.getDescription());
            priceView.setText(String.format(getString(R.string.price_format), item.getPrice()));

            String imageName = item.getPicture().replace(".png", "");
            int imageResource = getResources().getIdentifier(imageName, "drawable", getPackageName());

            if (imageResource != 0) {
                imageView.setImageResource(imageResource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item1) {
            zapiszKoszyk();
            return true;
        } else if (id == R.id.item2) {
            wczytajKoszyk();
            return true;
        } else if (id == R.id.item3) {
            wyslijSMS();
            return true;
        } else if (id == R.id.item4) {
            pokazListe();
            return true;
        } else if (id == R.id.item5) {
            udostepnij();
            return true;
        } else if (id == R.id.item6) {
            oProgramie();
            return true;
        } else if (id == R.id.item_logout) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.change_language) {
            String currentLanguage = getPreferredLanguage();
            if (currentLanguage.equals("en")) {
                setLocale("pl");
                savePreferredLanguage("pl");
            } else {
                setLocale("en");
                savePreferredLanguage("en");
            }
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void wyslijSMS() {
        Intent intent = new Intent(this, SendSmsActivity.class);
        startActivity(intent);
    }

    private void pokazListe() {
        Intent intent = new Intent(this, OrderListActivity.class);
        startActivity(intent);
    }

    private void udostepnij() {
        showOrderSelectionDialog();
    }

    private void showOrderSelectionDialog() {
        List<OrderItem> orders = dbHelper.getAllOrdersWithIds();
        if (orders.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_orders), Toast.LENGTH_SHORT).show();
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
        builder.setTitle(getString(R.string.share_order_title))
               .setItems(orderItems, (dialog, which) -> {
                   OrderItem selectedOrder = orders.get(which);
                   shareOrder(selectedOrder);
               })
               .setNegativeButton(getString(R.string.cancel), null)
               .show();
    }

    private void shareOrder(OrderItem order) {
        String subject = getString(R.string.share_order_subject);
        String message = String.format(getString(R.string.share_order_details),
            order.getDate(),
            order.getCustomerName(),
            order.getDetails(),
            order.getTotalPrice()
        );

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.share_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getString(R.string.email_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void oProgramie() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_title))
                .setMessage(getString(R.string.about_content)).show();
    }
    private void handleOrder() {
        EditText customerNameInput = findViewById(R.id.edit_text_notes);
        String customerName = customerNameInput.getText().toString();

        if (customerName.trim().isEmpty()) {
            customerNameInput.setError(getString(R.string.enter_name));
            return;
        }

        if (quantitySeekBar.getProgress() == 0 &&
            (!checkBoxMouse.isChecked() || quantitySeekBarMouse.getProgress() == 0) &&
            (!checkBoxKeyboard.isChecked() || quantitySeekBarKeyboard.getProgress() == 0) &&
            (!checkBoxMonitor.isChecked() || quantitySeekBarMonitor.getProgress() == 0)) {

            Toast.makeText(this, getString(R.string.select_product), Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder orderDetails = new StringBuilder();
        float totalPrice = 0;

        Item selectedComputer = (Item) spinnerComputer.getSelectedItem();
        int computerQuantity = quantitySeekBar.getProgress();
        float computerPrice = selectedComputer.getPrice() * computerQuantity;
        totalPrice += computerPrice;

        orderDetails.append(String.format(getString(R.string.computer_format),
            selectedComputer.getName(), computerQuantity, computerPrice));

        if (checkBoxMouse.isChecked()) {
            Item selectedMouse = (Item) spinnerMouse.getSelectedItem();
            int mouseQuantity = quantitySeekBarMouse.getProgress();
            float mousePrice = selectedMouse.getPrice() * mouseQuantity;
            totalPrice += mousePrice;
            orderDetails.append(String.format(getString(R.string.mouse_format),
                selectedMouse.getName(), mouseQuantity, mousePrice));
        }

        if (checkBoxKeyboard.isChecked()) {
            Item selectedKeyboard = (Item) spinnerKeyboard.getSelectedItem();
            int keyboardQuantity = quantitySeekBarKeyboard.getProgress();
            float keyboardPrice = selectedKeyboard.getPrice() * keyboardQuantity;
            totalPrice += keyboardPrice;
            orderDetails.append(String.format(getString(R.string.keyboard_format),
                selectedKeyboard.getName(), keyboardQuantity, keyboardPrice));
        }

        if (checkBoxMonitor.isChecked()) {
            Item selectedMonitor = (Item) spinnerMonitor.getSelectedItem();
            int monitorQuantity = quantitySeekBarMonitor.getProgress();
            float monitorPrice = selectedMonitor.getPrice() * monitorQuantity;
            totalPrice += monitorPrice;
            orderDetails.append(String.format(getString(R.string.monitor_format),
                selectedMonitor.getName(), monitorQuantity, monitorPrice));
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", getString(R.string.guest));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        float finalTotalPrice = totalPrice;
        builder.setTitle(getString(R.string.order_confirmation))
                .setMessage(String.format(getString(R.string.order_details), 
                    customerName, orderDetails.toString(), finalTotalPrice))
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    long orderId = dbHelper.saveOrder(customerName, orderDetails.toString(), finalTotalPrice, username);
                    if (orderId != -1) {
                        Toast.makeText(this, getString(R.string.order_saved), Toast.LENGTH_SHORT).show();
                        resetForm();
                    } else {
                        Toast.makeText(this, getString(R.string.order_error), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void resetForm() {
        quantitySeekBar.setProgress(0);

        checkBoxMouse.setChecked(false);
        checkBoxKeyboard.setChecked(false);
        checkBoxMonitor.setChecked(false);

        quantitySeekBarMouse.setProgress(0);
        quantitySeekBarKeyboard.setProgress(0);
        quantitySeekBarMonitor.setProgress(0);

        EditText customerNameInput = findViewById(R.id.edit_text_notes);
        customerNameInput.setText("");

        updateTotalPrice();
    }

    private void setupQuantityControls() {
        quantitySeekBar = findViewById(R.id.seekbar_quantity);
        decreaseButton = findViewById(R.id.button_decrease);
        increaseButton = findViewById(R.id.button_increase);
        TextView quantityText = findViewById(R.id.text_quantity);

        quantitySeekBar.setProgress(0);
        quantityText.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));

        decreaseButton.setOnClickListener(v -> {
            int currentProgress = quantitySeekBar.getProgress();
            if (currentProgress > 0) {
                quantitySeekBar.setProgress(currentProgress - 1);
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    currentProgress - 1, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }
        });

        increaseButton.setOnClickListener(v -> {
            int currentProgress = quantitySeekBar.getProgress();
            if (currentProgress < quantitySeekBar.getMax()) {
                quantitySeekBar.setProgress(currentProgress + 1);
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    currentProgress + 1, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }
        });

        quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    progress, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateTotalPrice() {
        float basePrice = 0;
        float totalPrice = 0;
        int mainQuantity = quantitySeekBar.getProgress();

        // Cena komputera (cena podstawowa)
        Item selectedComputer = (Item) spinnerComputer.getSelectedItem();
        if (selectedComputer != null) {
            basePrice = selectedComputer.getPrice() * mainQuantity;
            totalPrice += basePrice;

            // Aktualizacja wyświetlanej ceny podstawowej
            TextView basePriceView = findViewById(R.id.text_base_price);
            basePriceView.setText(String.format(getString(R.string.base_price_format), basePrice));
        }

        if (checkBoxMouse.isChecked()) {
            Item selectedMouse = (Item) spinnerMouse.getSelectedItem();
            if (selectedMouse != null) {
                int mouseQuantity = quantitySeekBarMouse.getProgress();
                float mousePrice = selectedMouse.getPrice() * mouseQuantity;
                totalPrice += mousePrice;
                TextView mouseAddonPrice = findViewById(R.id.text_mouse_addon_price);
                mouseAddonPrice.setText(String.format(getString(R.string.mouse_addon_price_format), mousePrice));
            }
        } else {
            TextView mouseAddonPrice = findViewById(R.id.text_mouse_addon_price);
            mouseAddonPrice.setText(getString(R.string.mouse_addon_price_default));
        }

        if (checkBoxKeyboard.isChecked()) {
            Item selectedKeyboard = (Item) spinnerKeyboard.getSelectedItem();
            if (selectedKeyboard != null) {
                int keyboardQuantity = quantitySeekBarKeyboard.getProgress();
                float keyboardPrice = selectedKeyboard.getPrice() * keyboardQuantity;
                totalPrice += keyboardPrice;
                TextView keyboardAddonPrice = findViewById(R.id.text_keyboard_addon_price);
                keyboardAddonPrice.setText(String.format(getString(R.string.keyboard_addon_price_format), keyboardPrice));
            }
        } else {
            TextView keyboardAddonPrice = findViewById(R.id.text_keyboard_addon_price);
            keyboardAddonPrice.setText(getString(R.string.keyboard_addon_price_default));
        }

        if (checkBoxMonitor.isChecked()) {
            Item selectedMonitor = (Item) spinnerMonitor.getSelectedItem();
            if (selectedMonitor != null) {
                int monitorQuantity = quantitySeekBarMonitor.getProgress();
                float monitorPrice = selectedMonitor.getPrice() * monitorQuantity;
                totalPrice += monitorPrice;
                TextView monitorAddonPrice = findViewById(R.id.text_monitor_addon_price);
                monitorAddonPrice.setText(String.format(getString(R.string.monitor_addon_price_format), monitorPrice));
            }
        } else {
            TextView monitorAddonPrice = findViewById(R.id.text_monitor_addon_price);
            monitorAddonPrice.setText(getString(R.string.monitor_addon_price_default));
        }

        TextView totalPriceView = findViewById(R.id.text_total_price);
        totalPriceView.setText(String.format(getString(R.string.total_price_format), totalPrice));
    }

    private void setupProductQuantityControls(SeekBar seekBar, Button decreaseButton,
        Button increaseButton, TextView quantityText, String productType) {

        seekBar.setProgress(0);
        quantityText.setText(String.format(getString(R.string.quantity_with_unit), 0, getString(R.string.unit_pieces)));

        decreaseButton.setOnClickListener(v -> {
            int currentProgress = seekBar.getProgress();
            if (currentProgress > 0) {
                seekBar.setProgress(currentProgress - 1);
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    currentProgress - 1, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }
        });

        increaseButton.setOnClickListener(v -> {
            int currentProgress = seekBar.getProgress();
            if (currentProgress < seekBar.getMax()) {
                seekBar.setProgress(currentProgress + 1);
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    currentProgress + 1, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                quantityText.setText(String.format(getString(R.string.quantity_with_unit), 
                    progress, getString(R.string.unit_pieces)));
                updateTotalPrice();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void zapiszKoszyk() {
        SharedPreferences sharedPreferences = getSharedPreferences("Koszyk_" + username, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("monitorQuantity", quantitySeekBarMonitor.getProgress());
        editor.putInt("mouseQuantity", quantitySeekBarMouse.getProgress());
        editor.putInt("keyboardQuantity", quantitySeekBarKeyboard.getProgress());
        editor.putInt("computerQuantity", quantitySeekBar.getProgress());

        EditText customerNameInput = findViewById(R.id.edit_text_notes);
        String customerName = customerNameInput.getText().toString();
        editor.putString("customerName", customerName);
        editor.apply();

        Toast.makeText(this, getString(R.string.cart_saved), Toast.LENGTH_SHORT).show();
    }

    private void wczytajKoszyk() {
        SharedPreferences sharedPreferences = getSharedPreferences("Koszyk_" + username, MODE_PRIVATE);

        int monitorQuantity = sharedPreferences.getInt("monitorQuantity", 0);
        int mouseQuantity = sharedPreferences.getInt("mouseQuantity", 0);
        int keyboardQuantity = sharedPreferences.getInt("keyboardQuantity", 0);
        int computerQuantity = sharedPreferences.getInt("computerQuantity", 0);

        quantitySeekBarMonitor.setProgress(monitorQuantity);
        quantitySeekBarMouse.setProgress(mouseQuantity);
        quantitySeekBarKeyboard.setProgress(keyboardQuantity);
        quantitySeekBar.setProgress(computerQuantity);

        String customerName = sharedPreferences.getString("customerName", "");
        EditText customerNameInput = findViewById(R.id.edit_text_notes);
        customerNameInput.setText(customerName);

        updateTotalPrice();

        Toast.makeText(this, getString(R.string.cart_loaded), Toast.LENGTH_SHORT).show();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createShortcuts() {
        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.setAction(Intent.ACTION_VIEW);

        ShortcutInfo loginShortcut = new ShortcutInfo.Builder(this, "login_shortcut")
                .setShortLabel(getString(R.string.login_shortcut_short))
                .setLongLabel(getString(R.string.login_shortcut_long))
                .setIcon(Icon.createWithResource(this, R.drawable.login))
                .setIntent(loginIntent)
                .build();

        Intent registerIntent = new Intent(this, RegisterActivity.class);
        registerIntent.setAction(Intent.ACTION_VIEW);

        ShortcutInfo registerShortcut = new ShortcutInfo.Builder(this, "register_shortcut")
                .setShortLabel(getString(R.string.register_shortcut_short))
                .setLongLabel(getString(R.string.register_shortcut_long))
                .setIcon(Icon.createWithResource(this, R.drawable.register))
                .setIntent(registerIntent)
                .build();

        shortcutManager.setDynamicShortcuts(Arrays.asList(loginShortcut, registerShortcut));
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void savePreferredLanguage(String lang) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("preferred_language", lang);
        editor.apply();
    }

    private String getPreferredLanguage() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("preferred_language", Locale.getDefault().getLanguage());
    }
}
