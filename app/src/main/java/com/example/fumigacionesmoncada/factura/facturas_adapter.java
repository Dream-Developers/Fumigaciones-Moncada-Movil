package com.example.fumigacionesmoncada.factura;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.fumigacionesmoncada.R;
import com.example.fumigacionesmoncada.listadoPeticionesCita.Citas_Peticiones;

import java.util.ArrayList;


public class facturas_adapter extends ArrayAdapter<Facturas> {
    private ListView lista_citas;


    private ArrayList<Facturas> usuarios_cita;
    public facturas_adapter(@NonNull Context context, ArrayList<Facturas>lista_citas) {
        super(context, R.layout.lista_facturas_item,lista_citas);
        this.usuarios_cita = lista_citas;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lista_facturas_item, null, false);


        TextView numeroFactura = convertView.findViewById(R.id.numerofactura);
        TextView fecha = convertView.findViewById(R.id.mostrarfecha);
        TextView nombre = convertView.findViewById(R.id.mostrarNombre);
        TextView total = convertView.findViewById(R.id.mostrartotal);
        TextView detalle = convertView.findViewById(R.id.mosstardetalle);



        Facturas citas = getItem(position);
        numeroFactura.setText(citas.getNumero());
        fecha.setText(citas.getFecha());
        nombre.setText(citas.getNombre());
        total.setText(citas.getTotal());
        detalle.setText(citas.getDetalle());
        return convertView;


    }
    @Override
    public int getCount() {
        return usuarios_cita.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public Facturas getItem(int position) {
        return usuarios_cita.get(position);
    }

    public void filtrar(ArrayList<Facturas> permisosgetYset) {
        this.usuarios_cita = new ArrayList<>();
        this.usuarios_cita.addAll(permisosgetYset);
        notifyDataSetChanged();


    }
}
