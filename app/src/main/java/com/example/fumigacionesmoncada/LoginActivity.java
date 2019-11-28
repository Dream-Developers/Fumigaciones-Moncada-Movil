package com.example.fumigacionesmoncada;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fumigacionesmoncada.Providers.ContractParaListaUsers;
import com.example.fumigacionesmoncada.ui.Mensajes.MensajesFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
public class LoginActivity extends AppCompatActivity {
    private EditText txtCorreo, txtContrasena;
    private TextView recuperarContra;
    private Button btn_registro, btn_login;
    private RadioButton RBsesion;
    // private static String URL_LOGIN = "http://192.168.0.101/api/auth/login";
    ProgressDialog dialogo_progreso;
    RequestQueue solicitar_cola;
    ProgressBar cargando;
    JsonObjectRequest solicitar_objeto_json;
    String success;
    String rol_id;
    String usuario_id;



    //Shared Preferences
    //private SharedPreferences sharedPreferences;
    //private SharedPreferences.Editor editor;
    private boolean isActivateRadioButton;

    private static final String STRING_PREFERENCES = "example.preferencias";
    private static  final String PREFERENCE_ESTADO_BUTTON = "estado.button";



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (obtenerEstadoButton()){
            cargarPreferencias();
            intem();
            finish();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_id);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        FirebaseInstanceId.getInstance().getInstanceId();
        txtCorreo = findViewById(R.id.idCorreoLogin);
        txtContrasena = findViewById(R.id.idContraseñaLogin);
        recuperarContra = findViewById(R.id.recuperarPass);
        RBsesion = findViewById(R.id.noSalir);
        btn_login = findViewById(R.id.idLoginLogin);
        btn_registro = findViewById(R.id.idRegistroLogin);

        isActivateRadioButton = RBsesion.isChecked();

        RBsesion.setVisibility(View.GONE);


        RBsesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActivateRadioButton){
                    RBsesion.setChecked(false);
                }
                isActivateRadioButton = RBsesion.isChecked();
            }
        });


        cargarPreferencias();



        Cursor cursor = getContentResolver().query(ContractParaListaUsers.CONTENT_URI, null,
                ContractParaListaUsers.Columnas.ROL+"=? or "+
                        ContractParaListaUsers.Columnas.ROL+"=?",
                new String[]{"1","2"},null,null);

        cursor.moveToNext();
        Log.i("USUARIO",""+cursor.getCount());

        try{
            if (cursor.getCount()==1) {

                Intent intent = new Intent(LoginActivity.this, MensajesFragment.class);
                startActivity(intent);
                finish();

            } else {

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logeoLaravel();
                logeoFirebase();

            }


        });


        //ojo
             }


        }catch (Exception exc){
            Log.i("Login_Activity",""+exc);
        }

    }


    public void logeoLaravel(){
        if ((txtCorreo.getText().toString().trim().length() > 0) && (txtContrasena.getText().toString().trim().length() > 0)) {
            if(!isEmailValid(txtCorreo.getText())){
                txtCorreo.setError("No es un correo valido");
            }else {
                String email = txtCorreo.getText().toString().trim();
                String contraseniaPass = txtContrasena.getText().toString().trim();
                login(email, contraseniaPass);
            }
        }
        else {
            if (txtCorreo.getText().toString().length() == 0 ||
                    txtCorreo.getText().toString().trim().equalsIgnoreCase("")) {
                txtCorreo.setError("Ingresa el correo");

            }
            if (txtContrasena.getText().toString().trim().length() == 0 ||
                    txtContrasena.getText().toString().trim().equalsIgnoreCase("")) {
                txtContrasena.setError("Ingresa la contraseña");
            }
        }

    }



    private void logeoFirebase() {
        String email = txtCorreo.getText().toString();
        String password = txtContrasena.getText().toString();

        Log.i("Teste", email);
        Log.i("Teste", password);

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return;
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.i("Teste", task.getResult().getUser().getUid());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Teste", e.getMessage());

                    }
                });
    }


    public void guardarEstadoButton(){

        SharedPreferences preferencias = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        preferencias.edit().putBoolean(PREFERENCE_ESTADO_BUTTON, RBsesion.isChecked()).apply();
    }

    public boolean obtenerEstadoButton(){

        SharedPreferences preferencias = getSharedPreferences(STRING_PREFERENCES, MODE_PRIVATE);
        return preferencias.getBoolean(PREFERENCE_ESTADO_BUTTON, false);
    }

    private void cargarPreferencias() {
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);


        String email = preferences.getString("email", "");
        String contra = preferences.getString("contra", "");
        rol_id = preferences.getString("rol", "");

//        txtCorreo.setText(email);
  //      txtContrasena.setText(contra);

    }


    private void login(final String txtCorreo, final String txtContrasena){

        String ip = getString(R.string.ip);

        String url = ip + "/api/auth/login";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        guardarEstadoButton();
                        //Toast.makeText(LoginActivity.this, "Si responde"+response.toString(), Toast.LENGTH_SHORT).show();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                             success = jsonObject.getString("access_token");
                             rol_id = jsonObject.getString("rol_id");
                            usuario_id = jsonObject.getString("id");

                            //Toast.makeText(LoginActivity.this, "Si manda los resultados"+rol_id , Toast.LENGTH_LONG).show();

                            JSONArray jsonArray = jsonObject.getJSONArray("login");
                            if (success.equals("1")){
                                for(int i = 0; i < jsonArray.length(); i++){
                                    JSONObject object = jsonArray.getJSONObject(i);

                                    String name = object.getString("name").trim();
                                    String email = object.getString("email").trim();
                                    Toast.makeText(LoginActivity.this, "Se ha logeado. " +
                                            " \nTu nombre : " +name+"" +
                                            "\nTu correo :" +email, Toast.LENGTH_SHORT).show();
                                    cargando.setVisibility(View.GONE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            Toast.makeText(LoginActivity.this, "Error al iniciar sesión"+e.toString(), Toast.LENGTH_SHORT).show();
                        }

                        //
                        //Bienvenido();
                        savePreferences(success,usuario_id);
                        intem();
                        finish();

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                         btn_login.setVisibility(View.VISIBLE);
                        Toast.makeText(LoginActivity.this, "Error al iniciar sesión, verifique que su " +
                                "contraseña esté correcta ", Toast.LENGTH_SHORT).show();

                    }
                })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("email", txtCorreo);
                parametros.put("password", txtContrasena);
                return parametros;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
}


    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void intem(){

        if (rol_id.equals("1")){

            Intent intent = new Intent(getApplicationContext(), NavegacionAdministradorActivity.class);
            startActivity(intent);
        }else {
            Intent i = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(i);
        }


    }

    public void Bienvenido(){
        LayoutInflater inflater= (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View customToast=inflater.inflate(R.layout.toas_personalizado,null);
        TextView txt= (TextView)customToast.findViewById(R.id.txtToast);
        txt.setText("Bienvenido, Gracias por preferirnos. " +
                " Sea feliz, que Dios lo bendiga ");
        Toast toast =new Toast(this);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(customToast);
        toast.show();

        //Agregar arriba Bienvenido();
    }


    private void savePreferences(String token,String usuario_id){
        SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);

        String correo = txtCorreo.getText().toString();
        String contra = txtContrasena.getText().toString();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", correo);
        editor.putString("token", token);
        editor.putString("password", contra);
        editor.putString("rol", rol_id);
        editor.putString("id", usuario_id);

        String token_firebase = preferences.getString("token_firebase","") ;
        guardarTokenFirebaseEnLaravel(usuario_id,token_firebase);
        editor.apply();

    }

    private void guardarTokenFirebaseEnLaravel(String usuario_id, String token_firebase) {

        String ip = getString(R.string.ip);
        String url  = ip+ "/api/token_firebase" ;


        JSONObject parametros = new JSONObject();

        try {
            parametros.put("id",usuario_id);
            parametros.put("firebase_token",token_firebase);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, parametros, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                    Toast.makeText(LoginActivity.this, "Se actualizo el token firebase", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(LoginActivity.this, ""+error.toString(), Toast.LENGTH_SHORT).show();
                Log.d("volley", "onErrorResponse: "+error.networkResponse);
            }
        }){

            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<>();
                parametros.put("Content-Type","application/json");
                parametros.put("X-Requested-With","XMLHttpRequest");

                return parametros;
            }
        };
        ClaseVolley.getIntanciaVolley(this).addToRequestQueue(jsonObjectRequest);
    }

    public void botonregistrar(View view) {
        Intent i = new Intent(getApplicationContext(), RegistarUsuarioNuevo.class);
        startActivity(i);

    }

    public void resetPassword(View view) {
        Intent intent = new Intent(getApplicationContext(), PasswordReset.class);
        startActivity(intent);
    }
}