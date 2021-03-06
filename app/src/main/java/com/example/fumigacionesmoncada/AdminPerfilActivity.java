package com.example.fumigacionesmoncada;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class AdminPerfilActivity extends AppCompatActivity {

    ImageView avatarIv;
    TextView nameTv, emailTv, phoneTv, address;
    String number = "89217523";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_perfil);

        getSupportActionBar().setHomeButtonEnabled(true);

        nameTv = findViewById(R.id.nameTvc);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        avatarIv = findViewById(R.id.avatarIv);
        address = findViewById(R.id.direccionP);

        DatabaseReference usersDbRef = FirebaseDatabase.getInstance().getReference();
        Query query = usersDbRef.child("Users").orderByChild("uid").equalTo("rIRg1DDkEIcsqbtY9WMp2cD2Nrq2");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String addres = ""+ ds.child("address").getValue();


                    //Set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);


                    try {
                        Picasso.get().load(image).into(avatarIv);
                    }catch (Exception e){
                        Picasso.get().load(R.drawable.fondo_chat4).into(avatarIv);
                    }

                    address.setText(addres);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_option_perfil, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_call:
                // Do onlick on menu action here
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                startActivity(intent);
                return true;
            case R.id.action_map:
                Intent intent1 = new Intent(AdminPerfilActivity.this, MapsActivity.class);
                startActivity(intent1);
        }
        return false;
    }
}
