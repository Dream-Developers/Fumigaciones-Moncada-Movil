package com.example.fumigacionesmoncada.ui.clientes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.fumigacionesmoncada.ClaseVolley;
import com.example.fumigacionesmoncada.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ClientesFragment extends Fragment {

    ListView lista;
    ClientesAdapter clientesAdapter ;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_clientes, container, false);
        lista = view.findViewById(R.id.lista_clientes);

        cargarClientes();



        return view;
    }

    private void cargarClientes() {

        String ip = "http://192.168.137.1/api/clientes";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, ip, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                ArrayList<ClientesVO> cliente = new ArrayList<>();
                ClientesVO clientesVO = null;
                try {
                    JSONArray array = response.getJSONArray("clientes");
                    JSONObject object = null;
                    for(int i=0; i<array.length();i++){
                        clientesVO = new ClientesVO();
                        object = array.getJSONObject(i);
                        clientesVO.setNombre(object.getString("name"));
                        clientesVO.setTelefono(object.getString("telefono"));

                        cliente.add(clientesVO);
                        clientesAdapter = new ClientesAdapter(getContext(), cliente);
                        lista.setAdapter(clientesAdapter);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                error.getStackTrace();
                Toast.makeText(getContext(), "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        ClaseVolley.getIntanciaVolley(getContext()).addToRequestQueue(jsonObjectRequest);

    }
}