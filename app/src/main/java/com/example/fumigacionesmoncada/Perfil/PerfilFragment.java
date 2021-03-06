package com.example.fumigacionesmoncada.Perfil;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.fumigacionesmoncada.ClaseVolley;
import com.example.fumigacionesmoncada.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;


public class PerfilFragment extends Fragment {
    private String tokenUsuario;
    private TextInputEditText mostrarNombre, mostrarDireccion, mostraraTelefono, mostrarCorreo;
    private TextView mostrarnombre1;
    NetworkImageView imagen;
    private TextView guradarDatos;
    ProgressBar pro;
    JsonObjectRequest jsonObjectRequest;
    RequestQueue request;
    ProgressDialog progreso;
    String id_usuario;
    Bitmap bitmap;
    private static final int TOMARFOTO = 1;
    private File imgFile;
    private int orientation;
    private File pictureFile;
    private String pictureFilePath;
    private Uri uri;
    private final int MIS_PERMISOS = 100;
    private static final int COD_SELECCIONA = 300;
    String foto;
    LinearLayout linearLayout;
    ImageView editar;
    String storagePath = "Users_Profile_Cover_Imgs/";
    String profileOrCoverPhoto;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    DatabaseReference dataRefe;

    StorageReference storageReference;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {



        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        mostrarNombre = view.findViewById(R.id.nombresP);
        mostrarDireccion = view.findViewById(R.id.direccionP);
        mostraraTelefono = view.findViewById(R.id.telefonoP);
        imagen = view.findViewById(R.id.circleview);
        mostrarCorreo = view.findViewById(R.id.correoP);
        guradarDatos = view.findViewById(R.id.pedir);
        request = Volley.newRequestQueue(getContext());
         linearLayout = view.findViewById(R.id.error);
         editar = view.findViewById(R.id.editar);
        getActivity().getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        mostrarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), R.string.validacionCorreo, Toast.LENGTH_SHORT).show();
            }
        });
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        //  storageReference = getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        //int arrays of permitions


        dataRefe = FirebaseDatabase.getInstance().getReference().child("Datas");


        cargarPreferencias();
        cargarClienteWeb();

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogOpciones();

            }
        });

        editar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDialogOpciones();

            }
        });
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String direccion = ""+ ds.child("recidencia").getValue();


                    //Set data




                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        setHasOptionsMenu(true);
        return view;

    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_boton_guardar, menu);

    }

    private String convertirImgString(Bitmap bitmap) {

        ByteArrayOutputStream array = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, array);
        byte[] imagenByte = array.toByteArray();
        String imagenString = Base64.encodeToString(imagenByte, Base64.DEFAULT);

        return imagenString;
    }

    public void actualizar_datos(final String id) {

        try {
            if (mostrarNombre.getText().toString().trim().equals("")) {
                Toast.makeText(getContext(), R.string.campoNombre, Toast.LENGTH_LONG).show();
            }else if(mostraraTelefono.getText().toString().trim().equals("")){
                Toast.makeText(getContext(), R.string.campoTelefono, Toast.LENGTH_LONG).show();
            }else if(mostraraTelefono.getText().toString().trim().length() < 8){
                Toast.makeText(getContext(), R.string.NumeroInvalido, Toast.LENGTH_LONG).show();
            }else if( mostrarDireccion.getText().toString().trim().equals("")){
                Toast.makeText(getContext(), R.string.campoResidencia, Toast.LENGTH_LONG).show();
            } else {
                {


                    progreso = new ProgressDialog(getContext());
                    progreso.setMessage(getString(R.string.cargandoAdquirir));
                    progreso.show();

                    if (bitmap == null) {
                        foto = null;
                    } else {
                        foto = convertirImgString(bitmap);
                    }
                    String ip = getString(R.string.ip);
                    String url = ip + "/api/clientes/" + id + "/update";
                    JSONObject parametros = new JSONObject();
                    parametros.put("name", mostrarNombre.getText().toString());
                    parametros.put("recidencia", mostrarDireccion.getText().toString());
                    parametros.put("telefono", mostraraTelefono.getText().toString());
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
                                Toast.makeText(getContext(), "" + response.getString("message"), Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            cargarClienteWeb();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progreso.dismiss();
                            Toast.makeText(getContext(), "" + error.toString(), Toast.LENGTH_SHORT).show();
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
                    ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);
                }
            }
        } catch (Exception exe) {
            Toast.makeText(getContext(), exe.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }


    private void cargarPreferencias() {
        SharedPreferences preferences = getContext().getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        tokenUsuario = preferences.getString("token", "");
        id_usuario = preferences.getString("id", "");
    }

    private void cargarClienteWeb() {
        String ip = getString(R.string.ip);

        String url = ip + "/api/auth/user";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject object = response;
                            id_usuario = object.getString("id");

                            mostrarNombre.setText(object.getString("name"));
                            mostraraTelefono.setText(object.getString("telefono"));
                            mostrarDireccion.setText(object.getString("recidencia"));
                            mostrarCorreo.setText(object.getString("email"));
                            cargarImagen(object.getString("foto"));

                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "sinoda." + e, Toast.LENGTH_LONG).show();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.toString().equals("com.android.volley.ServerError")) {
                    Toast.makeText(getContext(), R.string.presentamosProblemas, Toast.LENGTH_LONG).show();

                } else if (error.toString().equals("com.android.volley.TimeoutError")) {

                    Toast.makeText(getContext(),R.string.reviseConexion, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(),R.string.reviseConexion, Toast.LENGTH_SHORT).show();
                }
            }


        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer" + " " + tokenUsuario);


                return params;
            }
        };

        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);


    }

    private void cargarImagen(String foto) {
        String ip = getResources().getString(R.string.ip);
        String url = ip + "/foto/" + foto;
        ImageLoader imageLoader = ClaseVolley.getIntanciaVolley(getContext()).getImageLoader();

        imagen.setImageUrl(url, imageLoader);

    }


    private void mostrarDialogOpciones() {
        final CharSequence[] opciones = {getString(R.string.tomarFoto), getString(R.string.elegirGaleria), getString(R.string.cancelar)};
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
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
        //Capturar la imagen del empleado desde la camara

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);

        } else {


            Intent inten = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


            try {

                //Se crea un achivo de la foto en blanco
                pictureFile = getPictureFile();
                ContentValues values = new ContentValues();
                uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );

            } catch (Exception e) {

            }


            //se agrega la imagen capturada al archivo en blanco
            Uri photoUri = FileProvider.getUriForFile(getContext(), "com.permisosunahtec.android.fileprovider", pictureFile);
            inten.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

            startActivityForResult(inten, TOMARFOTO);


        }
    }

    private File getPictureFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String pictureFile = "Servicio" + timeStamp;
        File storeImagen = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), miPath);

                        String rutaImagen = getRealPathFromDocumentUri(getContext(), miPath);
                        ExifInterface exif = new ExifInterface(rutaImagen);

                        uri = data.getData();
                        imagen.setImageURI(uri);
                        foto = "image";
                        uploadProfileCoverPhoto(uri);
                        orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {

                            case ExifInterface.ORIENTATION_ROTATE_270:
                                bitmap = rotateImage(getContext(), bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                bitmap = rotateImage(getContext(), bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                bitmap = rotateImage(getContext(), bitmap, 90);
                                break;


                        }

                        imagen.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Log.i(String.valueOf(R.string.error), "" + e.getMessage());
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
                                bitmap = rotateImage(getContext(), bitmap, 270);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_180:
                                bitmap = rotateImage(getContext(), bitmap, 180);
                                break;

                            case ExifInterface.ORIENTATION_ROTATE_90:
                                bitmap = rotateImage(getContext(), bitmap, 90);
                                break;


                        }


                        imagen.setImageBitmap(bitmap);


                    } catch (Exception e) {
                        Toast.makeText(getContext(), R.string.IntenteloNuevamente, Toast.LENGTH_LONG).show();

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
        if ((getContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && getContext().checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }


        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) || (shouldShowRequestPermissionRationale(CAMERA)))) {
            cargarDialogoRecomendacion();
        } else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MIS_PERMISOS);
        }

        return false;//implementamos el que procesa el evento dependiendo de lo que se defina aqui
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MIS_PERMISOS) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {//el dos representa los 2 permisos
                Toast.makeText(getContext(), R.string.Permisosaceptados, Toast.LENGTH_SHORT);
                imagen.setEnabled(true);
            }
        } else {
            solicitarPermisosManual();
        }
    }


    private void solicitarPermisosManual() {
        final CharSequence[] opciones = {getString(R.string.si), getString(R.string.no)};
        final AlertDialog.Builder alertOpciones = new AlertDialog.Builder(getContext());//estamos en fragment
        alertOpciones.setTitle(R.string.permisosManual);
        alertOpciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (opciones[i].equals(getString(R.string.si))) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.permisosnoaceptados, Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }
        });
        alertOpciones.show();
    }


    private void cargarDialogoRecomendacion() {
        AlertDialog.Builder dialogo = new AlertDialog.Builder(getContext());
        dialogo.setTitle(R.string.PermisosDesactivados);
        dialogo.setMessage(R.string.aceptarpermisos);

        dialogo.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, 100);
            }
        });
        dialogo.show();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.telefono) {
            actualizar_datos(id_usuario);
            return true;
        }
             return super.onOptionsItemSelected(item);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        progreso = new ProgressDialog(getContext());
        progreso.setMessage(getString(R.string.cargandoAdquirir));
        progreso.show();

        String filePathAndName = storagePath+ ""+ foto +"_"+ user.getUid();

        StorageReference storageReference2 = storageReference.child(filePathAndName);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();


                        //chequear si ha sido actualizaada o no y el url es recibido
                        if (uriTask.isSuccessful()){
                            //image unload
                            // add update image in users firebase
                            HashMap<String, Object> results = new HashMap<>();

                            results.put(foto, downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            progreso.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {


                                }
                            });


                        }else {
                            //error
                            progreso.dismiss();
                            Toast.makeText(getActivity(), getString(R.string.errorimagen), Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(),  e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
