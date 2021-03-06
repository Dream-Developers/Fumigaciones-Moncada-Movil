package com.example.fumigacionesmoncada.ui.citas;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.fumigacionesmoncada.ClaseVolley;

import com.example.fumigacionesmoncada.R;

import com.example.fumigacionesmoncada.factura.Facturas;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CitasFragment extends Fragment implements SearchView.OnQueryTextListener {
private FloatingActionButton addcita;
    ListView lista_citas;
    Citas_Adapter citasAdapter;
    ArrayList<Citas> cita;

    String id_usuario;
    String tokenUsuario;
    ConstraintLayout constraintLayout;
    TextView sin_conexion; TextView  Nombre , Detalle ,Fecha,Total ;
    TextView EditNombre,EditFecha, EditDireccion, EditHora, EditPrecio;
    AlertDialog alertDialogFactura;
    TextView sinClientes;
    static SwipeRefreshLayout refreshLayout;
    private int dia, mes, anio;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_citas, container, false);
        addcita = view.findViewById(R.id.add_citas);
        lista_citas= view.findViewById(R.id.lista_citas);
         sinClientes = view.findViewById(R.id.sinclientes);
        lista_citas.setDivider(null);
      lista_citas.setDividerHeight(0);
      constraintLayout = view.findViewById(R.id.error);
      sin_conexion = view.findViewById(R.id.sin_conexion);
        refreshLayout = view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
                cargarPreferencias();
                cargarCitas();
            }
        });





        addcita.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),Crear_Citas.class);

                startActivity(intent);
            }
        });

cargarPreferencias();

        cargarCitas();

        setHasOptionsMenu(true);


        /*lista_citas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Citas citas = (Citas) parent.getItemAtPosition(position);
                Intent intent = new Intent(getContext(), Detalle_Cita.class);
                intent.putExtra("id_citas", citas.getId());
                startActivity(intent);
            }
        });*/
        lista_citas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Citas citas = (Citas) parent.getItemAtPosition(position);
                seleccionarFactura(citas,position);

            }

        });
        return view;

    }

    private void eliminarCitas(final Citas cit, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.confirmacion);
        builder.setMessage(R.string.eliminarlista);
       builder.setIcon(R.drawable.fm);
        builder.setPositiveButton(R.string.si, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eliminarCitaWebService(String.valueOf(cit.getId()),position);

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();

                    cargarCitas();

                }
            });
        }
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
    private void cargarPreferencias() {
        SharedPreferences preferences = getContext().getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        tokenUsuario = preferences.getString("token", "");
        id_usuario = preferences.getString("id", "");

    }
    private void seleccionarFactura(final Citas citas, int position) {
        AlertDialog.Builder builde = new AlertDialog.Builder(getContext());
        View dialogoLayout = getLayoutInflater().inflate(R.layout.item_cita_alertdialogo, null);


        EditNombre = dialogoLayout.findViewById(R.id.detalle_nombre);
        EditFecha = dialogoLayout.findViewById(R.id.detalle_fecha);
        EditDireccion = dialogoLayout.findViewById(R.id.detalle_direccion);
        EditHora = dialogoLayout.findViewById(R.id.detalle_hora);
        EditPrecio = dialogoLayout.findViewById(R.id.detalle_precio);
        EditNombre.setText(citas.getNombre());
        EditDireccion.setText(citas.getDireccion());
        EditFecha.setText(citas.getFechaFumigacion());
        EditHora.setText(citas.getHora());
        EditPrecio.setText(citas.getPrecio());


        builde.setCancelable(false);

        builde.setNegativeButton( "Cerrar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                cancelar();
            }
        });


        builde.setView(dialogoLayout);
        alertDialogFactura = builde.create();
        alertDialogFactura.show();

        alertDialogFactura.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cargarCitas();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        cargarCitas();
    }
    public void cancelar() {
    }
    private void eliminarCitaWebService(String id, final int position) {
        String ip=getString(R.string.ip);
        String url = ip+"/api/citas/"+id+"/borrar";

        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject object = new JSONObject(response);
                    Toast.makeText(getContext(), ""+object.getString("message"), Toast.LENGTH_LONG).show();
                    cita = new ArrayList<>();
                    citasAdapter = new Citas_Adapter(getContext(),cita);
                    lista_citas.setAdapter(citasAdapter);
                    cargarCitas();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), R.string.errorborrra, Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> parametros= new HashMap<>();
                parametros.put("Content-Type","application/json");
                parametros.put("Authorization", "Bearer" + " " + tokenUsuario);

                return  parametros;
            }
        };
        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(stringRequest);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menuprueba, menu);
        MenuItem item = menu.findItem(R.id.buscar);

        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private void cargarCitas() {
        String ip=getString(R.string.ip);
        String url = ip+"/api/citas";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                    cita = new ArrayList<>();
                    Citas citas = null;
                    try {
                        JSONArray array = response.optJSONArray("citas");
                        JSONObject object;
                        for (int i = 0; i < array.length(); i++) {
                            citas = new Citas();
                            object = array.getJSONObject(i);
                            citas.setNombre(object.getString("Nombre"));
                            citas.setDireccion(object.getString("Direccion"));
                            citas.setPrecio(object.getString("Precio"));
                            citas.setFechaFumigacion(object.getString("FechaFumigacion"));
                            citas.setHora(object.getString("Hora"));
                            citas.setId(object.getInt("id"));


                            cita.add(citas);
                            citasAdapter = new Citas_Adapter(getContext(), cita);
                            lista_citas.setAdapter(citasAdapter);


                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                if(cita.size() <= 0) {
                    Toast.makeText(getContext(), "No hay Citas  registradas aun", Toast.LENGTH_LONG).show();
                }

                }


        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                constraintLayout.setBackgroundResource(R.drawable.ic_cloud_off_black_24dp);
                sin_conexion.setVisibility(View.VISIBLE);
                error.getStackTrace();
                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> parametros= new HashMap<>();
                parametros.put("Content-Type","application/json");
                parametros.put("Authorization", "Bearer" + " " + tokenUsuario);

                return  parametros;
            }
        };


        //ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);

        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);

    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if (!(citasAdapter == null)) {
            ArrayList<Citas> listacitas = null;
            try {
                listacitas = filtrarDatosDeptos(cita, s.trim());
                citasAdapter.filtrar(listacitas);
                citasAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                Toast.makeText(getContext(), "" + listacitas, Toast.LENGTH_SHORT).show();

            }
            return true;


        }
        return false;
    }
    private ArrayList<Citas> filtrarDatosDeptos(ArrayList<Citas> listaTarea, String dato) {
        ArrayList<Citas> listaFiltradaPermiso = new ArrayList<>();
        try{
            dato = dato.toLowerCase();
            for(Citas permisos: listaTarea){
                String nombre = permisos.getNombre().toLowerCase().trim();
                String precio = permisos.getPrecio().toLowerCase().trim();

                if(nombre.toLowerCase().contains(dato)){
                    listaFiltradaPermiso.add(permisos);
                }else
                if (precio.toLowerCase().contains(dato)){
                    listaFiltradaPermiso.add(permisos);
                }


            }
            citasAdapter.filtrar(listaFiltradaPermiso);
        }catch (Exception e){
            Toast.makeText(getContext(), ""+e, Toast.LENGTH_SHORT).show();
            e.getStackTrace();
        }

        return listaFiltradaPermiso;

    }

}



