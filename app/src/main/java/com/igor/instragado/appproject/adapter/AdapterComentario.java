package com.igor.instragado.appproject.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.igor.instragado.appproject.R;
import com.igor.instragado.appproject.model.Comentario;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterComentario extends RecyclerView.Adapter<AdapterComentario.MyViewHolder> {


    private List<Comentario> listaComentarios;
    private Context context;

    public AdapterComentario(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_comentario, parent, false);
        return new AdapterComentario.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);

        holder.nomeUsuario.setText(comentario.getNomeUsuario());
        holder.comentario.setText(comentario.getComentario());

        Glide.with(context)
                .load(comentario.getCaminhoFoto())
                .into(holder.imagemPerfil);
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView imagemPerfil;
        TextView nomeUsuario, comentario;

        public MyViewHolder(View view)
        {
            super(view);
            imagemPerfil = view.findViewById(R.id.imageFotoComentario);
            nomeUsuario = view.findViewById(R.id.textNomeComentario);
            comentario = view.findViewById(R.id.textComentario);

        }

    }
}
