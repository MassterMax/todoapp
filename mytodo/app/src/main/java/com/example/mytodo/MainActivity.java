package com.example.mytodo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Task> tasks;
    //private ArrayList<String> names;

    //private ArrayAdapter<String> itemsAdapter;
    private ArrayAdapter<Task> itemsAdapter;
    private ListView lvItems;
    String filename = "saved_data";
    File directory;

    int savedIndex = -1;

    EditText newTodo;
    Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        directory = getFileStreamPath(filename);

        readItems();
        itemsAdapter = new ArrayAdapter<Task>(this, android.R.layout.simple_list_item_1, tasks) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Task task = tasks.get(position);
                ((TextView)view).setText(task.name);

                color(view, importance(task));

                return view;
            }
        };

        lvItems = findViewById(R.id.lvItems);
        lvItems.setAdapter(itemsAdapter);

        setupListViewListener();

        addButton = findViewById(R.id.btnAddItem);
        addButton.setEnabled(false);
        newTodo = findViewById(R.id.etNewItem);

        newTodo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = newTodo.getText().toString().trim();
                addButton.setEnabled(!input.isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("result", "someone finished");
        if (data != null) {

            tasks.get(savedIndex).name = data.getExtras().getString("name");
            tasks.get(savedIndex).description = data.getExtras().getString("description");
            tasks.get(savedIndex).hardness = data.getExtras().getInt("hardness");
            tasks.get(savedIndex).progress = data.getExtras().getInt("progress");
            tasks.get(savedIndex).deadline = data.getExtras().getString("date");

            //names.set(savedIndex, tasks.get(savedIndex).name);

            itemsAdapter.notifyDataSetChanged();

            writeItems();
        }

    }

    private void readItems() {

        try {
            FileInputStream fis = new FileInputStream(directory);
            ObjectInputStream ois = new ObjectInputStream(fis);

            tasks = (ArrayList<Task>) ois.readObject();

            fis.close();
            ois.close();


            Log.d("read", "read - success!");

        } catch (IOException | ClassNotFoundException e) {
            Log.d("exception!", e.getMessage());
            tasks = new ArrayList<>();
        }

        //names = new ArrayList<>();
        //for (Task t : tasks)
        //    names.add(t.name);

        Log.d("here", "11");
    }

    private void writeItems() {
        try {
            Log.d("write", "here");
            FileOutputStream fos = new FileOutputStream(directory);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(tasks);
            fos.close();
            oos.close();

            Log.d("write", "success");

        } catch (IOException e) {
            Log.d("not saved", e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupListViewListener() {

        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //names.remove(position);
                tasks.remove(position);
                itemsAdapter.notifyDataSetChanged();

                writeItems();

                return true;
            }
        });

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openSettings(position);
            }
        });

    }

    private void openSettings(int pos) {
        savedIndex = pos;
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);

        intent.putExtra("name", tasks.get(savedIndex).name);
        intent.putExtra("description", tasks.get(savedIndex).description);
        intent.putExtra("hardness", tasks.get(savedIndex).hardness);
        intent.putExtra("progress", tasks.get(savedIndex).progress);
        intent.putExtra("date", tasks.get(savedIndex).deadline);

        startActivityForResult(intent, 1);
    }

    public void onAddItem(View v) {
        String name = newTodo.getText().toString();
        newTodo.setText("");

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Task t = new Task(name, "", 0, 0, formatter.format(new Date()));

        //itemsAdapter.add(name);
        itemsAdapter.add(t);
        //tasks.add(t);

        openSettings(tasks.size() - 1);
    }

    private static double importance(Task t) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date firstDate = new Date();
        Date secondDate = null;
        try {
            secondDate = formatter.parse(t.deadline);
        } catch (ParseException e) {
            Log.d("exception", e.getMessage());
        }

        long diffInMillis = secondDate.getTime() - firstDate.getTime();
        double diff = 0.4;

        if (diffInMillis > 0) {
            diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS) + 0.6;
            // Log.d("diff", "" + diff);
        }
        else if (diffInMillis < -5000000)
            diff = 0.001;

        return (t.hardness * 1.06 + 1) / 100.0 * (100.0 - t.progress) / 100.0 / diff;
    }

    public static double importance(int hardness, int progress, String deadline)
    {
        return importance(new Task("", "", hardness, progress, deadline));
    }

    public static void color(View v, double importance) {
        int color;

        if (importance > 0.4)
            color = 0xFFFFA2A2;
        else if (importance > 0.15)
            color = 0xFFFFD7A2;
        else if (importance > 0.04)
            color = 0xFFFFEFA2;
        else if (importance > 0.015)
            color = 0xFFEFFFA2;
        else
            color = 0xFFA2FFA2;

        v.setBackgroundColor(color);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        try {
            dialog.setMessage(getTitle().toString() + " version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName +
                    "\r\n\nDeveloper - Gudzikevich Maxim Sergeevich BSE198");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        dialog.setTitle("About");
        dialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setIcon(R.mipmap.ic_launcher_round);
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}