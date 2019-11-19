package serviciosAdministrador;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.fumigacionesmoncada.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.fumigacionesmoncada.ClaseVolley;
import com.example.fumigacionesmoncada.RecyclerTouchListener;
import com.example.fumigacionesmoncada.ui.Principal.ClaseAdapterImagen;
import com.example.fumigacionesmoncada.ui.Principal.ServiciosVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ServiciosAdministradorFragment extends Fragment implements Response.Listener<JSONObject>,Response.ErrorListener  {
    RecyclerView recyclerUsuarios;
    ArrayList<ServiciosVO> listaUsuarios;
    ProgressDialog dialog;
    JsonObjectRequest jsonObjectRequest;

    private OnFragmentInteractionListener mListener;
    ClaseAdapterImagen claseAdapterImagen;

    public ServiciosAdministradorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view = inflater.inflate(R.layout.fragment_servicios_administrador, container, false);
        listaUsuarios=new ArrayList<>();
        recyclerUsuarios =  view.findViewById(R.id.recycler_servicios);
        recyclerUsuarios.setLayoutManager(new GridLayoutManager(this.getContext(),2));
        recyclerUsuarios.setHasFixedSize(true);
        cargarWebService();

        recyclerUsuarios.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerUsuarios, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ServiciosVO serviciosVO = listaUsuarios.get(position);
                Intent intent = new Intent(getContext(), DetalleServicioAdministradorActivity.class);
                intent.putExtra("id",serviciosVO.getId());
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {
                ServiciosVO serviciosVO =  listaUsuarios.get(position);
                eliminarServicio(serviciosVO,position);
            }
        }));
        return view;
    }

    private void eliminarServicio(final ServiciosVO serviciosVO, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirmación");
        builder.setMessage("¿Está seguro que desea eliminar la imágen?");
        builder.setIcon(R.drawable.logofm);
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminarServicioWebService(serviciosVO.getId(), position);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                }
            });
        }
        builder.setNegativeButton("No", null);
        builder.show();

    }


    private void eliminarServicioWebService(String id, final int position) {
        String ip=getString(R.string.ip);
        String url = ip+"/api/imagen/"+id+"/borrar";

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    Toast.makeText(getContext(), ""+object.getString("message"), Toast.LENGTH_LONG).show();
                    listaUsuarios= new ArrayList<>();
                    claseAdapterImagen = new ClaseAdapterImagen(listaUsuarios,getContext());
                    recyclerUsuarios.setAdapter(claseAdapterImagen);
                    cargarWebService();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error al borrar", Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> parametros= new HashMap<>();
                parametros.put("Content-Type","application/json");
                return  parametros;
            }
        };
        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(stringRequest);

    }


    private void cargarWebService() {
        dialog=new ProgressDialog(getContext());
        dialog.setMessage("Consultando Imagenes");
        dialog.show();
        String ip=getString(R.string.ip);

        String url=ip+"/api/recuperar";
        jsonObjectRequest=new JsonObjectRequest(Request.Method.GET,url,null,this,this);
        // request.add(jsonObjectRequest);
        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);


    }



    @Override
    public void onResponse(JSONObject response) {

        ServiciosVO servicio=null;

        JSONArray json=response.optJSONArray("servicio");

        try {

            for (int i=0;i<json.length();i++){
                servicio=new ServiciosVO();
                JSONObject jsonObject=null;
                jsonObject=json.getJSONObject(i);

                servicio.setId(String.valueOf(jsonObject.getInt("id")));
                servicio.setDescripcion(jsonObject.optString("nombre"));
                servicio.setRutaImagen(jsonObject.optString("foto"));
                listaUsuarios.add(servicio);
            }
            dialog.hide();
            claseAdapterImagen=new ClaseAdapterImagen(listaUsuarios, getContext());
            recyclerUsuarios.setAdapter(claseAdapterImagen);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "No se ha podido establecer conexión con el servidor" +
                    " "+response, Toast.LENGTH_LONG).show();
            dialog.hide();
        }

    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Toast.makeText(getContext(), "No hay acceso a internet", Toast.LENGTH_LONG).show();
        System.out.println();
        dialog.hide();
        // Log.d("ERROR: ", error.toString());

    }
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
        }catch (Exception e){

            if (context instanceof OnFragmentInteractionListener) {
                mListener = (OnFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnFragmentInteractionListener");
            }

        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
