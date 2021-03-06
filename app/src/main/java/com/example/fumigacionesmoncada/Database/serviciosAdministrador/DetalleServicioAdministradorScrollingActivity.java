package com.example.fumigacionesmoncada.Database.serviciosAdministrador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.example.fumigacionesmoncada.ClaseVolley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.fumigacionesmoncada.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DetalleServicioAdministradorScrollingActivity extends AppCompatActivity {
    private EditText titulo, descripcion;
    NetworkImageView imagen;
    private Button guardarCambios;
    String id;
    JsonObjectRequest jsonObjectRequest;
    RequestQueue request;
    ProgressDialog progreso;
    private String tokenUsuario;
    private File imgFile;
    private File pictureFile;
    private String pictureFilePath;
    private static final int TOMARFOTO = 1;
    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 10;
    private int orientation;
    String foto;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_servicio_administrador_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        try {

        }catch (Exception e){
            setSupportActionBar(toolbar);
        }

        setTitle(R.string.publicidad);

        titulo = findViewById(R.id.tituloImagenDA);
        descripcion = findViewById(R.id.descripcionImagenDA);
        imagen = findViewById(R.id.idImagenDA);
        guardarCambios = findViewById(R.id.guardarcambios);
        id = getIntent().getStringExtra("id");
        cargarPreferencias();
        cargarImagenWeb(id);

        guardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizar_datos(id);

            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogOpciones();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalle_admin, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idm = item.getItemId();

        if (idm == R.id.guardarcambios) {
            mostrarDialogOpciones();
            return true;
        }

        if (idm == R.id.fab) {
            actualizar_datos(id);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void actualizar_datos(final String id) {

        try {
            if (titulo.getText().toString().trim().equals("")
                    || descripcion.getText().toString().trim().equals("")) {
                Toast.makeText(this, R.string.campos_admon, Toast.LENGTH_LONG).show();


            } else {
                {

                }
                {
                    progreso = new ProgressDialog(this);
                    progreso.setMessage(getString(R.string.cargar));
                    progreso.show();
                    if (bitmap == null) {
                        foto = null;
                    } else {
                        foto = convertirImgString(bitmap);
                    }

                    String ip = getString(R.string.ip);
                    String url = ip + "/api/servicios/" + id + "/update";


                    JSONObject parametros = new JSONObject();
                    parametros.put("nombre", titulo.getText().toString());
                    parametros.put("descripcion", descripcion.getText().toString());
                    if (bitmap == null) {
                        parametros.put("foto", null);
                    } else {
                        parametros.put("foto", foto);
                    }


                    jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, parametros, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            progreso.dismiss();
                            try {
                                Toast.makeText(getApplicationContext(), "" + response.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cargarImagenWeb(id);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "" + error.toString(), Toast.LENGTH_SHORT).show();
                            Log.d("volley", "onErrorResponse: " + error.networkResponse);
                        }
                    }) {

                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> parametros = new HashMap<>();
                            parametros.put("Content-Type", "application/json");
                            parametros.put("X-Requested-With", "XMLHttpRequest");
                            parametros.put("Authorization", "Bearer" + " " + tokenUsuario);

                            return parametros;
                        }
                    };
                    ClaseVolley.getIntanciaVolley(this).addToRequestQueue(jsonObjectRequest);
                }
            }
        } catch (Exception exe) {
            Toast.makeText(getApplicationContext(), exe.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void cargarPreferencias() {
        SharedPreferences preferences = this.getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        tokenUsuario = preferences.getString("token", "");

    }

    private void cargarImagenWeb(final String id) {
        String ip = getString(R.string.ip);

        String url = ip + "/api/imagen/" + id + "/mostrar";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = response.getJSONObject("servicio");
                            titulo.setText(object.getString("nombre"));
                            descripcion.setText(object.getString("descripcion"));
                            cargarImagen(object.getString("foto"));


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(DetalleServicioAdministradorScrollingActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }

        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer" + " " + tokenUsuario);


                return params;
            }
        };

        ClaseVolley.getIntanciaVolley(this).addToRequestQueue(jsonObjectRequest);


    }


    private void cargarImagen(String foto) {
        String ip = getResources().getString(R.string.ip);
        String url = ip + "/imagen/" + foto;
        ImageLoader imageLoader = ClaseVolley.getIntanciaVolley(this).getImageLoader();

        imagen.setImageUrl(url, imageLoader);

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
        //Capturar la imagen del empleado desde la camara

        if (ContextCompat.checkSelfPermission(DetalleServicioAdministradorScrollingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(DetalleServicioAdministradorScrollingActivity.this,
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


            //se agrega la imagen capturada al archivo en blanco


        }
    }


    private void abrirCamara() {
        //Capturar la imagen desde la camara

        if (ContextCompat.checkSelfPermission(DetalleServicioAdministradorScrollingActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, WRITE_EXTERNAL_STORAGE},
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

    public static String getRealPathFromDocumentUri(Context context, Uri uri) {
        String filePath = "";

        Pattern p = Pattern.compile("(\\d+)$");
        Matcher m = p.matcher(uri.toString());
        if (!m.find()) {
            Log.e("Perfil", "ID for requested image not found: " + uri.toString());
            return filePath;
        }
        String imgId = m.group();

        String[] column = {MediaStore.Images.Media.DATA};
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{imgId}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();

        return filePath;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case COD_SELECCIONA:
                if(data != null) {
                    Uri miPath = data.getData();
                    imagen.setImageURI(miPath);

                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), miPath);
                        String rutaImagen = getRealPathFromDocumentUri(this, miPath);
                        ExifInterface exif = new ExifInterface(rutaImagen);

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

                        imagen.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.i(String.valueOf(R.string.error), "" + e.getMessage());
                    }

                    break;
                }
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


                        imagen.setImageBitmap(bitmap);
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

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
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
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIS_PERMISOS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {//el dos representa los 2 permisos
                Toast.makeText(this,  R.string.Permisosaceptados, Toast.LENGTH_SHORT);
                imagen.setEnabled(true);
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
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),  R.string.permisosnoaceptados, Toast.LENGTH_SHORT).show();
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
}
