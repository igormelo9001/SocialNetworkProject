package com.igor.instragado.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.igor.instragado.app.R;
import com.igor.instragado.app.helper.ConfiguracaoFirebase;
import com.igor.instragado.app.helper.UsuarioFirebase;
import com.igor.instragado.app.model.Usuario;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdapterPesquisa extends RecyclerView.Adapter<AdapterPesquisa.MyViewHolder> {


    private List<Usuario> listUsuarios;
    private Context context;
    private StorageReference storage;
    private String identificadoUsuario;
    private StorageReference storageRef;


    public AdapterPesquisa(List<Usuario> l, Context c) {
        this.listUsuarios = l;
        this.context = c;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_pesquisa_usuario, parent, false);
        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        Usuario usuario = listUsuarios.get(position);

        holder.nome.setText(usuario.getNome());

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        StorageReference imagemRef = storageRef
                .child("imagens")
                .child("perfil")
                .child(usuario.getId() + ".jpeg");

        try {
            final File localFile = File.createTempFile("images", "jpg");
            imagemRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    holder.foto.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}

        if( usuario.getCaminhoFoto() != null){
            Uri uri = Uri.parse( usuario.getCaminhoFoto());
            Glide.with(context)
                    .load(uri)
                    .into(holder.foto);
        }else {
            holder.foto.setImageResource(R.drawable.avatar);
        }

    }

    @Override
    public int getItemCount() {
        return listUsuarios.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        CircleImageView foto;
        TextView nome;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            foto = itemView.findViewById(R.id.imageFotoComentario);
            nome = itemView.findViewById(R.id.textNomePesquisa);
            storage = ConfiguracaoFirebase.getFirebaseStorage();
            identificadoUsuario = UsuarioFirebase.getIdentificadorUsuario();
        }
    }
}
