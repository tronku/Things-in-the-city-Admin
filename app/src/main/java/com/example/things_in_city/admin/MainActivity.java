package com.example.things_in_city.admin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, ImageAdapter.ImageClickListener {

    private CardView photo;
    private static final int GET_IMAGE = 2;
    private String geoUrl, geoLabel;
    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private TextView add_place;
    private EditText name, about, tags, city, weburl, category;
    private List<Uri> imageUriList = new ArrayList<>();
    private Uri imageUri = null;
    private Map<String, ArrayList<String>> cities;
    private Map<String, Object> data = new HashMap<>();
    private String placeKey;

    //Firebase references
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference cityReference;
    private DatabaseReference placeReference;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.name);
        about = findViewById(R.id.about);
        tags = findViewById(R.id.tags);
        city = findViewById(R.id.city);
        add_place = findViewById(R.id.add_place);
        weburl = findViewById(R.id.website);
        category = findViewById(R.id.category);

        //Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        cityReference = firebaseDatabase.getReference();
        placeReference = firebaseDatabase.getReference();
        storageReference = firebaseStorage.getReference();


        if (savedInstanceState != null) {
            imageUriList = savedInstanceState.getParcelableArrayList("uri");
        }

        //location_autocomplete
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                geoLabel = place.getName().toString();
                geoUrl = "http://maps.google.com/maps?q=loc:" + place.getLatLng().latitude + "," + place.getLatLng().longitude + "(" + geoLabel + ")";
                Uri websiteUri = place.getWebsiteUri();
                if (websiteUri != null) {
                    weburl.setText(websiteUri.toString());
                }
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        photo = findViewById(R.id.photo_button);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        recyclerView = findViewById(R.id.recyclerview);
        adapter = new ImageAdapter(imageUriList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        add_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addData();
                imageUriList.clear();
                adapter.setUriList(imageUriList);
                adapter.updateList();
                name.setText("");
                city.setText("");
                weburl.setText("");
                tags.setText("");
                about.setText("");
                category.setText("");
                autocompleteFragment.setText("");
            }
        });
        addData();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String category_selected = parent.getItemAtPosition(position).toString();
        if (position != 0) {
            Toast.makeText(this, "Category: " + category_selected, Toast.LENGTH_LONG).show();
        }
        //storing data in database
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "Please select a category.", Toast.LENGTH_LONG).show();
    }

    public void getImage() {
        Intent photo = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photo.setType("images/jpeg");
        photo.putExtra(Intent.EXTRA_LOCAL_ONLY, "false");
        startActivityForResult(Intent.createChooser(photo, "Complete action using..."), GET_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == GET_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageUriList.add(imageUri);
            adapter.setUriList(imageUriList);
            adapter.updateList();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("uri", (ArrayList<? extends Parcelable>) imageUriList);
    }

    @Override
    public void onImageClick(int position) {
        //Pop-up for bigger view
        Intent bigger = new Intent(this, full_image.class);
        bigger.putExtra("position", position);
        bigger.putParcelableArrayListExtra("imageList", (ArrayList<? extends Parcelable>) imageUriList);
        startActivity(bigger);
    }

    //Adding data to Firebase database
    public void addData() {
        String tag = tags.getText().toString();
        String placeName = name.getText().toString();
        String categ = category.getText().toString();
        String desc = about.getText().toString();
        String cityName = city.getText().toString();
        String website = weburl.getText().toString();
        String key = placeReference.child("places").push().getKey();
        com.example.things_in_city.admin.Places places = new com.example.things_in_city.admin.Places(placeName, desc, categ, tag, cityName, geoUrl, geoLabel, website);
        Map<String, Object> placeValues = places.toMap();
        Map<String, Object> values = new HashMap<>();
        values.put("/places/" + key, placeValues);
        placeReference.updateChildren(values);

        //cities
        collectData(getCitiesData());
        if(cities == null)
            cities = new HashMap<>();
        ArrayList<String> pushIds;
        boolean city_present;
        if (cities.containsKey(cityName)) {
            pushIds = cities.get(cityName);
            city_present = true;
            Log.d("present?","true");
        } else {
            pushIds = new ArrayList<>();
            city_present = false;
            Log.d("present?","false");
        }
        pushIds.add(key);
        cities.put(cityName, pushIds);

        if(cityName.length()==0)
            removeNullData(key);
        else{
            setCityData(cityName, city_present);
            setDownloadUri(key);
        }
    }

    private void removeNullData(String key) {
        placeReference.child("places").child(key).removeValue();
    }

    private Map<String, Object> getCitiesData() {
        DatabaseReference ref = firebaseDatabase.getReference().child("cities");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    data = (Map<String, Object>)dataSnapshot.getValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return data;
    }

    private void collectData(Map<String, Object> city){
        String cityName;
        for(Map.Entry<String, Object> entry : city.entrySet()){
            Map singleCity = (Map)entry.getValue();
            cityName = (String)singleCity.get("title");
            ArrayList<String> placeList = new ArrayList<>();
            Log.d("city", cityName);
            Map<String, String> place = (Map<String, String>)singleCity.get("places");
            for(Map.Entry<String, String> singleEntry : place.entrySet()){
                placeList.add(singleEntry.getValue());
                Log.d("place", singleEntry.getValue());
            }
            cities.put(cityName, placeList);
        }
    }

    public void setDownloadUri(final String key) {

        for (int i = 0; i < imageUriList.size(); i++) {

            //high res picture
            final StorageReference photoUriHigh = storageReference.child(key).child("images").child("highres").child(imageUriList.get(i).getLastPathSegment());
            UploadTask uploadTask = photoUriHigh.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return photoUriHigh.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        DatabaseReference imageRefHigh = firebaseDatabase.getReference().child(key).child("images").child("highres");
                        String imageKey = imageRefHigh.push().getKey();
                        Map<String, Object> imagesHigh = new HashMap<>();
                        imagesHigh.put("/places/" + key + "/images/highres/" + imageKey, downloadUri.toString());
                        placeReference.updateChildren(imagesHigh);
                    } else {
                        Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_LONG).show();
                    }
                }
            });


            //low res picture
            try {
                InputStream is = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                byte[] data = baos.toByteArray();

                final StorageReference photoUriLow = storageReference.child(key).child("images").child("lowres").child(imageUriList.get(i).getLastPathSegment());
                UploadTask uploadTaskLow = photoUriLow.putBytes(data);
                Task<Uri> urlTaskLow = uploadTaskLow.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return photoUriLow.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            DatabaseReference imageRefLow = firebaseDatabase.getReference().child(key).child("images").child("lowres");
                            String imageKey = imageRefLow.push().getKey();
                            Map<String, Object> imagesLow = new HashMap<>();
                            imagesLow.put("/places/" + key + "/images/lowres/" + imageKey, downloadUri.toString());
                            placeReference.updateChildren(imagesLow);
                        } else {
                            Toast.makeText(MainActivity.this, "Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCityData(final String cityName, boolean city_present) {
        String key;
        final ArrayList<String> cityPlaces = cities.get(cityName);

        if(city_present){
            Query cityQuery = cityReference.child("cities").orderByChild("title").equalTo(cityName);
            cityQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot citySnapshot : dataSnapshot.getChildren()) {
                        Map<String, Object> places = new HashMap<>();
                        String placeKey = citySnapshot.getRef().push().getKey();
                        places.put("/cities/" + citySnapshot.getKey() + "/places/" + placeKey, cityPlaces.get(cityPlaces.size() - 1));
                        cityReference.updateChildren(places);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        else {
            key = cityReference.child("cities").push().getKey();
            DatabaseReference cityData = firebaseDatabase.getReference().child(key);

            Map<String, Object> city = new HashMap<>();
            city.put("/cities/" + key + "/title", cityName);
            cityReference.updateChildren(city);
            Map<String, Object> places = new HashMap<>();

            for (int i = 0; i < cityPlaces.size(); i++) {
                placeKey = cityData.child("places").push().getKey();
                places.put("/cities/" + key + "/places/" + placeKey, cityPlaces.get(i));
                cityReference.updateChildren(places);
            }
        }
    }
}