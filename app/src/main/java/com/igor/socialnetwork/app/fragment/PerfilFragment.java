package com.igor.socialnetwork.app.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.igor.socialnetwork.app.R;
import com.igor.socialnetwork.app.activity.EditarPerfilActivity;
import com.igor.socialnetwork.app.adapter.AdapterGrid;
import com.igor.socialnetwork.app.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.app.helper.UsuarioFirebase;
import com.igor.socialnetwork.app.model.Postagem;
import com.igor.socialnetwork.app.model.Usuario;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PerfilFragment extends Fragment {


    private ProgressBar progressBar;
    private CircleImageView imagePerfil;
    public GridView gridViewPerfil;
    private TextView textPublicacoes, textSeguidores, textSeguindo;
    private Button buttonAcaoPerfil;
    private StorageReference storageRef;
    private String identificadoUsuario;
    private Usuario usuarioLogado;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private ValueEventListener valueEventListenerPerfil;
    private DatabaseReference firebaseRef;
    private DatabaseReference postagensUsuariosRef;
    private AdapterGrid adapterGrid;

    public PerfilFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_perfil, container, false);

        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");


        postagensUsuariosRef = ConfiguracaoFirebase.getFirebase()
                .child("postagens")
                .child(usuarioLogado.getId());

        initViews(view);

        buttonAcaoPerfil.setText("Editar Perfil");
        buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), EditarPerfilActivity.class));
            }
        });

        inicializarImageLoader();

        carregarFotosPostagem();

        return view;
    }

    private void initViews(View view) {
        imagePerfil = view.findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = view.findViewById(R.id.buttonAcaoPerfil);
        buttonAcaoPerfil.setText("Carregando");
        textPublicacoes = view.findViewById(R.id.textPublicacoes);
        textSeguidores = view.findViewById(R.id.textSeguidores);
        textSeguindo = view.findViewById(R.id.textSeguindo);
        gridViewPerfil = view.findViewById(R.id.gridviewPerfil);
    }

    public void carregarFotosPostagem(){

        postagensUsuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

//                int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
//                int tamanhoImagem = tamanhoGrid / 3;
//                gridViewPerfil.setColumnWidth(tamanhoImagem);

                List<String> urlFotos = new ArrayList<>();
                for( DataSnapshot ds: dataSnapshot.getChildren()){
                    Postagem postagem = ds.getValue(Postagem.class);
                    //Log.i("postagem", "url " + postagem.getCaminhoFoto());
                    urlFotos.add(postagem.getCaminhoFoto());
                }


                adapterGrid = new AdapterGrid(getContext(), R.layout.grid_postagem, urlFotos);
                gridViewPerfil.setAdapter(  adapterGrid );

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getActivity())
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();

        ImageLoader.getInstance().init(config);

    }

    private void recuperarDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(usuarioLogado.getId());
        valueEventListenerPerfil = usuarioLogadoRef.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Usuario usuario = dataSnapshot.getValue(Usuario.class);
                        String postagens = String.valueOf(usuario.getPostagens());
                        String seguindo = String.valueOf(usuario.getSeguindo());
                        String seguidores = String.valueOf(usuario.getSeguidores());

                        textPublicacoes.setText(postagens);
                        textSeguindo.setText(seguindo);
                        textSeguidores.setText(seguidores);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioLogadoRef.removeEventListener( valueEventListenerPerfil);
    }

    private void recuperarFotoUsuario(){
        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        identificadoUsuario = UsuarioFirebase.getIdentificadorUsuario();

        storageRef = ConfiguracaoFirebase.getFirebaseStorage();

        StorageReference imagemRef = storageRef
                .child("imagens")
                .child("perfil")
                .child(identificadoUsuario + ".jpeg");

        try {
            final File localFile = File.createTempFile("images", "jpg");
            imagemRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    imagePerfil.setImageBitmap(bitmap);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (IOException e ) {}

        String caminhoFoto = usuarioLogado.getCaminhoFoto();
        if (caminhoFoto != null){
            Uri uri = Uri.parse( caminhoFoto );
            Glide.with(getActivity())
                    .load(uri)
                    .into(imagePerfil);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        recuperarDadosUsuarioLogado();

        recuperarFotoUsuario();
    }
}
