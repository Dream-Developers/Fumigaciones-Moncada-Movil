package com.example.fumigacionesmoncada;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import java.util.List;


import androidx.recyclerview.widget.RecyclerView;

public class ClaseAdapterImagen extends RecyclerView.Adapter<ClaseAdapterImagen.UsuariosHolder> {


    List<ClaseImagen> listaimegen;
    Context context;

    public ClaseAdapterImagen(List<ClaseImagen> listaimegen, Context context) {
        this.listaimegen = listaimegen;
        this.context = context;
    }


    @Override
    public UsuariosHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View vista= LayoutInflater.from(parent.getContext()).inflate(R.layout.lista_imagenes,parent,false);
        RecyclerView.LayoutParams layoutParams=new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        vista.setLayoutParams(layoutParams);
        return new UsuariosHolder(vista);
    }



    @Override
    public void onBindViewHolder(UsuariosHolder holder, int position) {
        holder.txtDocumento.setText(listaimegen.get(position).getDescripcion());


        if (listaimegen.get(position).getRutaImagen()!=null){
            //
            cargarImagenWebService(listaimegen.get(position).getRutaImagen(),holder);
        }else{
            holder.imagen.setImageResource(R.drawable.img_base);
        }
    }

    private void cargarImagenWebService(String rutaImagen, final UsuariosHolder holder) {


    }

    @Override
    public int getItemCount() {
        return listaimegen.size();
    }

    public class UsuariosHolder extends RecyclerView.ViewHolder {

        TextView txtDocumento;
        ImageView imagen;

        public UsuariosHolder(View itemView) {
            super(itemView);
            txtDocumento =  itemView.findViewById(R.id.idDocumento);
            imagen = itemView.findViewById(R.id.idImagen);
        }

    }
}