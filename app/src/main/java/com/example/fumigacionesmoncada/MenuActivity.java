package com.example.fumigacionesmoncada;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MenuActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private Object vision_mision;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_principal,
                R.id.nav_servicio,
                R.id.nav_acerca,
                R.id.nav_perfil,
                R.id.nav_registro_citas,
                R.id.nav_contactar)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            getMenuInflater().inflate(R.menu.menu_usuarios, menu);
        }catch (Exception e){

        }
        return true;
    }

    public void red_user(MenuItem item) {
        alertaCerrarSesionUsuario();
    }


    public void alertaCerrarSesionUsuario() {
        new AlertDialog.Builder(MenuActivity.this)
                .setTitle("Confirmación")
                .setMessage("¿Estás seguro que quieres cerrar sesión?")
                .setIcon(R.drawable.logofm)
                .setPositiveButton("SI",
                        new DialogInterface.OnClickListener() {
                            @TargetApi(11)
                            public void onClick(DialogInterface dialog, int id) {
                                logout();
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @TargetApi(11)
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    public void vision_user(Class<VisionMision> view){
        Intent i= new Intent(this, VisionMision.class);

        startActivity(i);
    }

    public void confi_user(Class<Desarrolladores> view){
        Intent intent = new Intent(this, Desarrolladores.class);

        startActivity(intent);
    }

   @Override public boolean onOptionsItemSelected(MenuItem opcion_menu) {
       switch (opcion_menu.getItemId()) {
           case R.id.confi:
               return true;

           case R.id.mision:
               return true;

           default:
       }
       return super.onOptionsItemSelected(opcion_menu);
   }



    private void logout() {
        SharedPrefManager.getInstance(MenuActivity.this).clear();
        Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
