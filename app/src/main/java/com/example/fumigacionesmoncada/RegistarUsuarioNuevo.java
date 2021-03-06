package com.example.fumigacionesmoncada;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.content.ContentResolver;
import android.webkit.MimeTypeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.Continuation;
import com.google.firebase.storage.StorageTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class RegistarUsuarioNuevo extends AppCompatActivity{
    EditText nombre,contraseña,correo,confcontra, telefono , apellidos;
    TextView col;
    Button registrar;
    ProgressDialog progreso;
    RequestQueue request;
    JsonObjectRequest jsonObjectRequest;
    ProgressBar pro;
    private File imgFile;
    RadioButton femenino;
    RadioButton masculino;
    private int orientation;
    Bitmap bitmap;
    private File pictureFile;
    private String pictureFilePath;
    private static final int TOMARFOTO = 1;
    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private static final int COD_FOTO = 20;
    String sexo;
    //Firebase
    private Button mBtnSelectedPhoto;
    private ImageView imgFoto;
    private Uri miPath;

    //here
    private FirebaseAuth mAuth;
    DatabaseReference reference;
    private StorageTask uploadTask;
    FirebaseUser fuser;
    StorageReference storageReference;
    String myUid;
    String imagen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario_nuevo);
        confcontra =  findViewById(R.id.comprobacion_contraseña);
        nombre =  findViewById(R.id.registro_nombres);
        correo = findViewById(R.id.registro_correo);
        telefono = findViewById(R.id.registro_telefono);
        apellidos = findViewById(R.id.registro_apellidos);
        contraseña = findViewById(R.id.registro_contraseña);
        registrar =  findViewById(R.id.registrar);
        masculino = findViewById(R.id.maculino);
        femenino = findViewById(R.id.femenino);
        request = Volley.newRequestQueue(this);

        //here
        mAuth = FirebaseAuth.getInstance();
        imgFoto = findViewById(R.id.foto);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
       // storageReference = FirebaseStorage.getInstance().getReference("Images");

        imgFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogOpciones();

            }
        });


        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    validacion();
                //validar();


            }
        });



    }
    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {getString(R.string.tomarFoto), getString(R.string.elegirGaleria), getString(R.string.cancelar)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.elegiropcion);
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals(getString(R.string.tomarFoto))) {
                    abrirCamara();
                } else {
                    if (opciones[i].equals(getString(R.string.elegirGaleria))) {
                        abrirGaleria();
                    } else {
                        dialogInterface.dismiss();
                    }
                }
            }
        });
        builder.show();
    }

    private void abrirGaleria() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);

        } else {

            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent.createChooser(intent, getString(R.string.seleccione)), COD_SELECCIONA);
            try {

                //Se crea un achivo de la foto en blanco
                pictureFile = getPictureFile();

            } catch (Exception e) {

            }

        }
    }
    private void abrirCamara() {
        //Capturar la imagen desde la camara

        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);

        } else {


            Intent inten = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


            try {

                //Se crea un achivo de la foto en blanco
                pictureFile = getPictureFile();

            } catch (Exception e) {

            }


            //se agrega la imagen capturada al archivo en blanco
            Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), "com.permisosunahtec.android.fileprovider", pictureFile);
            inten.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            startActivityForResult(inten, TOMARFOTO);


        }
    }
    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "Servicio" + timeStamp;
        File storeImagen = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(pictureFile, ".jpg", storeImagen);
        pictureFilePath = imagen.getAbsolutePath();


        return imagen;
    }
    public static int getOrientation(Context context, Uri photoUri) {


        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{

                MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        try {
            if (cursor.moveToFirst())
            { return cursor.getInt(0); }
            else
            { return -1; } } finally { cursor.close(); }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case COD_SELECCIONA:
                if(data != null) {
                    Uri path = data.getData();
                    imgFoto.setImageURI(path);
                    try {

                        InputStream picture = this.getContentResolver().openInputStream(path);
                        bitmap = BitmapFactory.decodeStream(picture);
                        orientation = getOrientation(this, path);

                        switch (orientation) {

                            case 90:
                                bitmap = rotateImage(this, bitmap, 90);

                                break;

                            case 180:
                                bitmap = rotateImage(this, bitmap, 180);
                                break;

                            case 270:
                                bitmap = rotateImage(this, bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_NORMAL:

                            default:
                                break;
                        }


                        imgFoto.setImageBitmap(bitmap);


                    } catch (Exception e) {
                        Toast.makeText(this, R.string.IntenteloNuevamente, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case TOMARFOTO:
                imgFile = new File(pictureFilePath);
                if (resultCode == -1) {

                    bitmap = BitmapFactory.decodeFile(String.valueOf(imgFile));


                    try {


                        ExifInterface exif = new ExifInterface(imgFile.getAbsolutePath());

                        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                bitmap = rotateImage(this, bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                bitmap = rotateImage(this, bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                bitmap = rotateImage(this, bitmap, 90);
                                break;

                        }


                        imgFoto.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.IntenteloNuevamente, Toast.LENGTH_LONG).show();

                    }


                } else {
                    //en caso de que no haya foto capturada el archivo en blanco se elimina
                    imgFile.delete();
                }

                break;
        }
    }

    public static Bitmap rotateImage(Context context, Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        try {

            matrix.postRotate(angle);


            source = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (Exception e) {

            Toast.makeText(context, R.string.IntenteloNuevamente, Toast.LENGTH_LONG).show();
        }
        return source;
    } @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIS_PERMISOS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {//el dos representa los 2 permisos
                Toast.makeText(this, R.string.Permisosaceptados, Toast.LENGTH_SHORT);
                imgFoto.setEnabled(true);
            }
        } else {
            solicitarPermisosManual();
        }
    }

    private Bitmap redimensionarImagen(Bitmap bitmap, float anchoNuevo, float altoNuevo) {

        int ancho = bitmap.getWidth();
        int alto = bitmap.getHeight();

        if (ancho > anchoNuevo || alto > altoNuevo) {
            float escalaAncho = anchoNuevo / ancho;
            float escalaAlto = altoNuevo / alto;

            Matrix matrix = new Matrix();
            matrix.postScale(escalaAncho, escalaAlto);

            return Bitmap.createBitmap(bitmap, 0, 0, ancho, alto, matrix, false);

        } else {
            return bitmap;
        }


    }


    //permisos
    ////////////////

    private boolean solicitaPermisosVersionesSuperiores() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {//validamos si estamos en android menor a 6 para no buscar los permisos
            return true;
        }

        //validamos si los permisos ya fueron aceptados
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) || (shouldShowRequestPermissionRationale(CAMERA)))) {
            cargarDialogoRecomendacion();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }


    private void solicitarPermisosManual() {
        final CharSequence[] opciones = {getString(R.string.si), getString(R.string.no)};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(this);//estamos en fragment
        alertOpciones.setTitle(R.string.permisosManual);
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals(getString(R.string.si))) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package",getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext() ,  R.string.permisosnoaceptados, Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }


    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
        dialogo.setTitle(R.string.PermisosDesactivados);
        dialogo.setMessage(R.string.aceptarpermisos);

        dialogo.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
                }
            }
        });
        dialogo.show();
    }
    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);
        return imagenString;
    }
    private void cargarWebService() {
        //progreso=new ProgressDialog(this);
        //progreso.setMessage("Cargando...");
        //progreso.show();
        /**progreso.setContentView(R.layout.custom_progressdialog_register);
        progreso.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        progreso.setCancelable(true);*/
        if (masculino.isChecked()) {
            sexo = "M";
        }
        if (femenino.isChecked()) {
            sexo = "F";
        }


        String ip=getString(R.string.ip);

        String url=ip+"/api/auth/signup";
        try {
            if (bitmap == null){
                 imagen = null;
            }else{
                 imagen = convertirImgString(bitmap);
            }

            JSONObject parametros = new JSONObject();
            parametros.put("name", nombre.getText().toString());
            parametros.put("recidencia", apellidos.getText().toString());
            parametros.put("telefono", telefono.getText().toString());
            parametros.put("email", correo.getText().toString());
            parametros.put("password", contraseña.getText().toString());
            parametros.put("password_confirmation", confcontra.getText().toString());
            parametros.put("sexo", sexo);
            parametros.put("foto", imagen);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, parametros, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //finish();
                Toast.makeText(RegistarUsuarioNuevo.this, R.string.regitro, Toast.LENGTH_SHORT).show();

finish();

        }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //progreso.hide();
                if (error.toString().equals("com.android.volley.ServerError")) {
                    Toast.makeText(getApplicationContext(), R.string.presentamosProblemas, Toast.LENGTH_LONG).show();
                    finish();
                } if (error.toString().equals("com.android.volley.TimeoutError")) {
                    Toast.makeText(getApplicationContext(),  R.string.reviseConexion, Toast.LENGTH_LONG).show();
                    finish();
                }
                    if (error.toString().equals("com.android.volley.ClientError")) {
                    Toast.makeText(getApplicationContext(), R.string.correoregistrado, Toast.LENGTH_LONG).show();
                        finish();
                } else {
                    Toast.makeText(getApplicationContext(),  R.string.reviseConexion, Toast.LENGTH_LONG).show();
                        finish();

                }
            }

        }){
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<>();
                parametros.put("Content-Type", "application/json");
                parametros.put("X-Requested-With", "XMLHttpRequest");

                return parametros;
            }
        };

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);

        ClaseVolley.getIntanciaVolley(getApplicationContext()).addToRequestQueue(jsonObjectRequest);


        //request.add(stringRequest);
       // RequestQueue requestQueue = Volley.newRequestQueue(this);
    } catch(Exception exe){
        Toast.makeText(getApplicationContext(), exe.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
    private void validacion() {
        final String email = correo.getText().toString().trim();
        final String password = contraseña.getText().toString().trim();
        final String name = nombre.getText().toString().trim();
        final String phone = telefono.getText().toString().trim();

if ((nombre.getText().toString().length() <10 )){
    nombre.setError("ingrese el nombre Completo");
}else{
        if(nombre.getText().toString().equals("")){
            nombre.setError(getString(R.string.ingresenombre));
        }else { if (apellidos.getText().toString().equals("")) {
            apellidos.setError(getString(R.string.ingrserecidencia));

            }else{
            if (telefono.getText().toString().equals("")) {
                telefono.setError(getString(R.string.ingreseTelefono));


            }else{
                if (correo.getText().toString().equals("")) {
                    correo.setError(getString(R.string.ingrsecorreo));
                }else{
                if  (contraseña.getText().toString().equals("")) {
                    contraseña.setError(getString(R.string.ingresecontra));

            }else {
                    if (confcontra.getText().toString().equals("")) {
                        confcontra.setError(getString(R.string.ingreseconfirmacontra));
                    } else {

                        if (contraseña.getText().toString().equals(confcontra.getText().toString())) {

                            if (contraseña.getText().toString().length() < 8 || confcontra.getText().toString().length() < 8) {
                                Toast.makeText(getApplicationContext(), R.string.ingrecontrano, Toast.LENGTH_LONG).show();
                            } else {
                                if (telefono.getText().toString().length() < 8) {
                                    telefono.setError(getString(R.string.noestelefono));
                                    Toast.makeText(getApplicationContext(), getString(R.string.noestelefono), Toast.LENGTH_LONG).show();
                                } else {

                                    if (!validarEmail(correo.getText().toString())) {
                                        correo.setError(getString(R.string.correonovalido));
                                        Toast.makeText(getApplicationContext(), getString(R.string.correonovalido), Toast.LENGTH_LONG).show();
                                    } else {

                                        registerUser(name, email, password, phone);
                                        //cargarWebService();
                                    }
                                }


                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.confitrmarcontra, Toast.LENGTH_LONG).show();
                            contraseña.setError(getString(R.string.confitrmarcontra));
                        }
                    }
                }  }}}}}


    }
    private void registerUser(final String name, String  email, String password, final String phone) {
        progreso=new ProgressDialog(this);
        progreso.setMessage(getString(R.string.cargando));
        progreso.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {


                            FirebaseUser user = mAuth.getCurrentUser();
                            String email = user.getEmail();
                            String uid = user.getUid();

                            HashMap<Object, String> hashMap = new HashMap<>();
                            hashMap.put("email", email);
                            hashMap.put("uid", uid);
                            hashMap.put("name", name);
                            hashMap.put("onlineStatus", "online");
                            hashMap.put("typingTo", "noOne");
                            hashMap.put("phone", phone); //luego
                            hashMap.put("image", "");

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            //crear path
                            DatabaseReference reference = database.getReference("Users");

                            reference.child(uid).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        cargarWebService();

                                    }
                                }
                            });
                            updateUI(user);
                        } else {
                            progreso.dismiss();
                            Toast.makeText(RegistarUsuarioNuevo.this, getString(R.string.fallofirebase),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progreso.hide();
               Toast.makeText(RegistarUsuarioNuevo.this, R.string.correoyaregistrado,Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        //hideProgressDialog();
        if (user != null) {
         myUid = user.getUid();

        } else {

        }
    }

    private boolean validarEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( progreso!=null && progreso.isShowing() ){
            progreso.cancel();
        }
    }
    /**
     * Metodos para la authentificacion de firebase
     *
     * **/




    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            mSelectedUri = data.getData();

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mSelectedUri);
                imgFoto.setImageDrawable(new BitmapDrawable(bitmap));
                mBtnSelectedPhoto.setAlpha(0);
            } catch (IOException e) {
            }
        }

    }*/

    /**private void createUser() {
        String name = nombre.getText().toString();
        String email = correo.getText().toString();
        String contra = contraseña.getText().toString();

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || contra == null || contra.isEmpty()) {
          //  Toast.makeText(this, "Al menos un campo vacio", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, contra)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i("Teste", task.getResult().getUser().getUid());

                            saveUserInFirebase();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("Teste", e.getMessage());
                    }
                });
    }

    private void saveUserInFirebase() {
        String filename = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);
        ref.putFile(miPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.i("Teste", uri.toString());

                                String uid = FirebaseAuth.getInstance().getUid();
                                String username = nombre.getText().toString();
                                String profileUrl = uri.toString();

                                User user = new User(uid, username, profileUrl);

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("Teste", e.getMessage());
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Teste", e.getMessage(), e);
                    }
                });

    }*/

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


}

