package com.example.bazadanychczycos;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.bazadanychczycos.Item;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Locale;

public class DataBase extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        long newRowId = dbHelper.addItem("Example Item", 25, "Example description", "example_image.png");

        List<Item> items = dbHelper.getAllItems();
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG = "DatabaseHelper";

        private static final String DATABASE_NAME = "itemsDatabase";
        private static final int DATABASE_VERSION = 2;

        private static final String TABLE_ITEMS = "items_table";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_NAME = "name";
        private static final String COLUMN_PRICE = "price";
        private static final String COLUMN_DESCRIPTION = "description";
        private static final String COLUMN_PICTURE = "picture";
        private static final String COLUMN_CATEGORY = "category";

        private static final String TABLE_ORDERS = "orders";
        private static final String COLUMN_ORDER_ID = "order_id";
        private static final String COLUMN_ORDER_DATE = "order_date";
        private static final String COLUMN_CUSTOMER_NAME = "customer_name";
        private static final String COLUMN_ORDER_DETAILS = "order_details";
        private static final String COLUMN_TOTAL_PRICE = "total_price";
        private static final String COLUMN_USERNAME = "username";

        private static final String TABLE_USERS = "users";
        private static final String COLUMN_USER_ID = "user_id";
        private static final String COLUMN_PASSWORD = "password";

        private final Context context;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
                String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_NAME + " TEXT,"
                        + COLUMN_PRICE + " REAL,"
                        + COLUMN_DESCRIPTION + " TEXT,"
                        + COLUMN_PICTURE + " TEXT,"
                        + COLUMN_CATEGORY + " TEXT" + ")";
                db.execSQL(CREATE_ITEMS_TABLE);

                addDefaultItems(db);

                // Dodaj tabelę zamówień
                String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + "("
                        + COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_ORDER_DATE + " TEXT,"
                        + COLUMN_CUSTOMER_NAME + " TEXT,"
                        + COLUMN_ORDER_DETAILS + " TEXT,"
                        + COLUMN_TOTAL_PRICE + " REAL,"
                        + COLUMN_USERNAME + " TEXT" + ")";
                db.execSQL(CREATE_ORDERS_TABLE);

                // Dodaj tabelę użytkowników
                String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                        + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_USERNAME + " TEXT,"
                        + COLUMN_PASSWORD + " TEXT" + ")";
                db.execSQL(CREATE_USERS_TABLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }

        public long addItem(String name, float price, String description, String picture) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PICTURE, picture);
            long id = db.insert(TABLE_ITEMS, null, values);
            db.close();
            return id;
        }

        public List<Item> getAllItems() {
            List<Item> itemList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String selectQuery = "SELECT * FROM " + TABLE_ITEMS;
            Cursor cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    @SuppressLint("Range") float price = cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE));
                    @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                    @SuppressLint("Range") String picture = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE));

                    Item item = new Item(id, name, price, description, picture);
                    itemList.add(item);
                } while (cursor.moveToNext());
            } else {
                Log.e(TAG, "Brak elementów w bazie danych.");
            }
            cursor.close();
            db.close();
            return itemList;
        }

        // Generate a list of default items and add to the database
        private void addDefaultItems(SQLiteDatabase db) {
            // Klawiatury
            addItemDirectly(db, context.getString(R.string.gaming_keyboard), 199.99f, context.getString(R.string.gaming_keyboard_desc), "klawiatura1.png", "klawiatury");
            addItemDirectly(db, context.getString(R.string.office_keyboard), 89.99f, context.getString(R.string.office_keyboard_desc), "klawiatura2.png", "klawiatury");
            addItemDirectly(db, context.getString(R.string.multimedia_keyboard), 129.99f, context.getString(R.string.multimedia_keyboard_desc), "klawiatura3.png", "klawiatury");
            addItemDirectly(db, context.getString(R.string.wireless_keyboard), 159.99f, context.getString(R.string.wireless_keyboard_desc), "klawiatura4.png", "klawiatury");

            // Komputery
            addItemDirectly(db, context.getString(R.string.gaming_computer), 4999.99f, context.getString(R.string.gaming_computer_desc), "komputer1.png", "komputery");
            addItemDirectly(db, context.getString(R.string.office_computer), 2299.99f, context.getString(R.string.office_computer_desc), "komputer2.png", "komputery");
            addItemDirectly(db, context.getString(R.string.mini_pc), 1499.99f, context.getString(R.string.mini_pc_desc), "komputer3.png", "komputery");
            addItemDirectly(db, context.getString(R.string.all_in_one_computer), 3599.99f, context.getString(R.string.all_in_one_desc), "komputer4.png", "komputery");

            // Monitory
            addItemDirectly(db, context.getString(R.string.uhd_monitor), 1199.99f, context.getString(R.string.uhd_monitor_desc), "monitor1.png", "monitory");
            addItemDirectly(db, context.getString(R.string.fhd_monitor), 799.99f, context.getString(R.string.fhd_monitor_desc), "monitor2.png", "monitory");
            addItemDirectly(db, context.getString(R.string.gaming_monitor), 1599.99f, context.getString(R.string.gaming_monitor_desc), "monitor3.png", "monitory");
            addItemDirectly(db, context.getString(R.string.ultrawide_monitor), 2299.99f, context.getString(R.string.ultrawide_monitor_desc), "monitor4.png", "monitory");

            // Myszki
            addItemDirectly(db, context.getString(R.string.gaming_mouse), 249.99f, context.getString(R.string.gaming_mouse_desc), "myszka1.png", "myszki");
            addItemDirectly(db, context.getString(R.string.office_mouse), 59.99f, context.getString(R.string.office_mouse_desc), "myszka2.png", "myszki");
            addItemDirectly(db, context.getString(R.string.multimedia_mouse), 129.99f, context.getString(R.string.multimedia_mouse_desc), "myszka3.png", "myszki");
            addItemDirectly(db, context.getString(R.string.professional_mouse), 199.99f, context.getString(R.string.professional_mouse_desc), "myszka4.png", "myszki");
        }

        private void addItemDirectly(SQLiteDatabase db, String name, float price, String description, String picture, String category) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_PRICE, price);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PICTURE, picture);
            values.put(COLUMN_CATEGORY, category);
            db.insert(TABLE_ITEMS, null, values);
        }

        // Generate a default set of items

        public List<Item> getItemsByCategory(String category) {
            List<Item> itemList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            
            String selectQuery = "SELECT * FROM " + TABLE_ITEMS + " WHERE " + COLUMN_CATEGORY + " = ?";
            Cursor cursor = db.rawQuery(selectQuery, new String[]{category});

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    @SuppressLint("Range") float price = cursor.getFloat(cursor.getColumnIndex(COLUMN_PRICE));
                    @SuppressLint("Range") String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
                    @SuppressLint("Range") String picture = cursor.getString(cursor.getColumnIndex(COLUMN_PICTURE));
                    @SuppressLint("Range") String itemCategory = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY));

                    Item item = new Item(id, name, price, description, picture, itemCategory);
                    itemList.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            return itemList;
        }

        // Metoda do dodawania nowego sprzętu do bazy danych
        public long addEquipment(String name, float price, String description, String picture) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_PRICE, price); 
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_PICTURE, picture);

            long newRowId = db.insert(TABLE_ITEMS, null, values);
            db.close();
            return newRowId;
        }

        // Metoda do zapisywania zamówienia
        public long saveOrder(String customerName, String orderDetails, float totalPrice, String username) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            
            values.put(COLUMN_ORDER_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            values.put(COLUMN_CUSTOMER_NAME, customerName);
            values.put(COLUMN_ORDER_DETAILS, orderDetails);
            values.put(COLUMN_TOTAL_PRICE, totalPrice);
            values.put(COLUMN_USERNAME, username);

            long id = db.insert(TABLE_ORDERS, null, values);
            db.close();
            return id;
        }

        public List<String> getAllOrders() {
            List<String> ordersList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            
            Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, 
                COLUMN_ORDER_DATE + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME));
                    @SuppressLint("Range") String details = cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DETAILS));
                    @SuppressLint("Range") float total = cursor.getFloat(cursor.getColumnIndex(COLUMN_TOTAL_PRICE));
                    
                    ordersList.add(String.format("Data: %s\nZamawiający: %s\n%s\nSuma: %.2f zł\n", 
                        date, name, details, total));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            
            return ordersList;
        }

        public List<OrderItem> getAllOrdersWithIds() {
            List<OrderItem> ordersList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            
            Cursor cursor = db.query(TABLE_ORDERS, null, null, null, null, null, 
                COLUMN_ORDER_DATE + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ORDER_ID));
                    @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DATE));
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOMER_NAME));
                    @SuppressLint("Range") String details = cursor.getString(cursor.getColumnIndex(COLUMN_ORDER_DETAILS));
                    @SuppressLint("Range") float total = cursor.getFloat(cursor.getColumnIndex(COLUMN_TOTAL_PRICE));
                    
                    ordersList.add(new OrderItem(id, date, name, details, total));
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            
            return ordersList;
        }

        public void deleteOrder(long orderId) {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_ORDERS, COLUMN_ORDER_ID + " = ?", new String[]{String.valueOf(orderId)});
            db.close();
        }

        // Metoda do dodawania użytkownika
        public long addUser(String username, String password) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_PASSWORD, password);
            long id = db.insert(TABLE_USERS, null, values);
            db.close();
            return id;
        }

        // Metoda do sprawdzania czy użytkownik istnieje
        public boolean userExists(String username) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + "=?",
                    new String[]{username}, null, null, null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return exists;
        }

        // Metoda do weryfikacji użytkownika
        public boolean verifyUser(String username, String password) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                    COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                    new String[]{username, password}, null, null, null);
            boolean exists = (cursor.getCount() > 0);
            cursor.close();
            db.close();
            return exists;
        }

        // Metoda do pobierania zamówień dla konkretnego użytkownika
        public List<OrderItem> getOrdersByUsername(String username) {
            List<OrderItem> ordersList = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            
            Cursor cursor = db.query(TABLE_ORDERS, null, COLUMN_USERNAME + " = ?", new String[]{username}, null, null, COLUMN_ORDER_DATE + " DESC");

            if (cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(COLUMN_ORDER_ID);
                    int dateIndex = cursor.getColumnIndex(COLUMN_ORDER_DATE);
                    int nameIndex = cursor.getColumnIndex(COLUMN_CUSTOMER_NAME);
                    int detailsIndex = cursor.getColumnIndex(COLUMN_ORDER_DETAILS);
                    int totalIndex = cursor.getColumnIndex(COLUMN_TOTAL_PRICE);

                    if (idIndex != -1 && dateIndex != -1 && nameIndex != -1 && detailsIndex != -1 && totalIndex != -1) {
                        long id = cursor.getLong(idIndex);
                        String date = cursor.getString(dateIndex);
                        String name = cursor.getString(nameIndex);
                        String details = cursor.getString(detailsIndex);
                        float total = cursor.getFloat(totalIndex);
                        
                        ordersList.add(new OrderItem(id, date, name, details, total));
                    } else {
                        Log.e("Database", "Jedna lub więcej kolumn nie istnieje w tabeli orders.");
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
            
            return ordersList;
        }
    }
}
