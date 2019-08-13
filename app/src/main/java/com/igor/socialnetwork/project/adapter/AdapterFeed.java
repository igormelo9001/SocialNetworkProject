package com.igor.socialnetwork.project.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.igor.socialnetwork.project.R;
import com.igor.socialnetwork.project.model.Feed;
import com.like.LikeButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterFeed extends RecyclerView.Adapter<AdapterFeed.MyViewHolder> {


    private List<Feed> listaFeed;
    private Context context;

    public AdapterFeed(List<Feed> listaFeed, Context context) {
        this.listaFeed = listaFeed;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_feed, parent, false);
        return new AdapterFeed.MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Feed feed = listaFeed.get(position);

        Uri uriFotoUsuario = Uri.parse(feed.getFotoUsuario());
        Uri uriFotoPostagem = Uri.parse(feed.getFotoPostagem());

        holder.descricao.setText(feed.getDescricao());
        Glide.with(context).load(uriFotoUsuario).into(holder.fotoPerfil);
        Glide.with(context).load(uriFotoPostagem).into(holder.fotoPostagem);

        holder.nome.setText(feed.getNomeUsuario());

    }

    @Override
    public int getItemCount() {
        return listaFeed.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView fotoPerfil;
        TextView nome, descricao, qtdCurtidas;
        ImageView fotoPostagem, visualizarComentario;
        LikeButton likeButton;

        public MyViewHolder(View view){
            super(view);

            fotoPerfil = view.findViewById(R.id.imagePerfilPostagem);
            fotoPostagem = view.findViewById(R.id.imagePostagemSelecionada);
            nome = view.findViewById(R.id.textPerfilPostagem);
            qtdCurtidas = view.findViewById(R.id.textQtdCurtidasPostagens);
            descricao = view.findViewById(R.id.textDescricaoPostagem);
            visualizarComentario = view.findViewById(R.id.imageComentarioFeed);
            likeButton = view.findViewById(R.id.likeButtonFeed);
        }

    }
}
