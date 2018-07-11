package com.example.things_in_city.admin;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

public class full_image extends AppCompatActivity {

    private List<Uri> list;
    private int position;
    private ImageView image, close, prev, next;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        Intent intent = getIntent();
        list = intent.getParcelableArrayListExtra("imageList");
        position = intent.getIntExtra("position", 0);

        image = findViewById(R.id.singlePlaceImage);
        image.setImageURI(list.get(position));

        close = findViewById(R.id.closeBtn);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position>0){
                    image.setImageURI(list.get(--position));
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position<list.size()-1){
                    image.setImageURI(list.get(++position));
                }
            }
        });
    }


}
