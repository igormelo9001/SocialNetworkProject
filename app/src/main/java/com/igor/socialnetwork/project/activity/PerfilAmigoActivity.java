package com.igor.socialnetwork.project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
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
import com.igor.socialnetwork.project.R;
import com.igor.socialnetwork.project.adapter.AdapterGrid;
import com.igor.socialnetwork.project.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.project.helper.UsuarioFirebase;
import com.igor.socialnetwork.project.model.Postagem;
import com.igor.socialnetwork.project.model.Usuario;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PerfilAmigoActivity extends AppCompatActivity {

    private Usuario usuarioSelecionado;
    private Usuario usuarioLogado;
    private Button buttonAcaoPerfil;
    private CircleImageView imagePerfil;
    private StorageReference storageRef;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioAmigoRef;
    private DatabaseReference firebaseRef;
    private DatabaseReference seguidoresRef;
    private DatabaseReference usuarioLogadoRef;
    private DatabaseReference postagensUsuariosRef;
    private ValueEventListener valueEventListenerPerfilAmigo;
    private String idUsuarioLogado;
    private List<Postagem> postagens;
    private GridView gridViewPerfil;
    private AdapterGrid adapterGrid;


    private String identificadoUsuario;
    private String caminhoFoto;
    private TextView textPublicacoes, textSeguidores, textSeguindo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_amigo);

        firebaseRef = ConfiguracaoFirebase.getFirebase();
        usuariosRef = firebaseRef.child("usuarios");
        seguidoresRef = firebaseRef.child("seguidores");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        identificadoUsuario = UsuarioFirebase.getIdentificadorUsuario();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        initViews();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Perfil");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if( bundle != null){
            usuarioSelecionado = (Usuario) bundle.getSerializable("usuarioSelecionado");

            postagensUsuariosRef = ConfiguracaoFirebase.getFirebase()
                    .child("postagens")
                    .child(usuarioSelecionado.getId());

            getSupportActionBar().setTitle( usuarioSelecionado.getNome() );

            StorageReference imagemRef = storageRef
                    .child("imagens")
                    .child("perfil")
                    .child(usuarioSelecionado.getId() + ".jpeg");

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

            if (caminhoFoto != null){
                Uri uri = Uri.parse( caminhoFoto );
                Glide.with(PerfilAmigoActivity.this)
                        .load(uri)
                        .into(imagePerfil);
            }
        }

        recuperarDadosUsuarioLogado();
        inicializarImageLoader();
        carregarFotosPostagem();

        gridViewPerfil.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Postagem postagem = postagens.get(i);
                Intent intent = new Intent(PerfilAmigoActivity.this, VisualizarPostagemActivity.class);
                intent.putExtra("postagem", postagem);
                intent.putExtra("usuario", usuarioSelecionado);
                startActivity(intent);
            }
        });

    }

    private void initViews() {

        imagePerfil = findViewById(R.id.imagePerfil);
        buttonAcaoPerfil = findViewById(R.id.buttonAcaoPerfil);
        buttonAcaoPerfil.setText("Carregando");
        textPublicacoes = findViewById(R.id.textPublicacoes);
        textSeguidores = findViewById(R.id.textSeguidores);
        textSeguindo = findViewById(R.id.textSeguindo);
        gridViewPerfil = findViewById(R.id.gridviewPerfil);

    }

    public void inicializarImageLoader(){

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .diskCacheSize(50 * 1024 * 1024)
                .diskCacheFileCount(100)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .build();

        ImageLoader.getInstance().init(config);

    }

    public void carregarFotosPostagem(){

            postagens = new ArrayList<>();
            postagensUsuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    int tamanhoGrid = getResources().getDisplayMetrics().widthPixels;
                    int tamanhoImagem = tamanhoGrid / 3;
                    gridViewPerfil.setColumnWidth(tamanhoImagem);

                    List<String> urlFotos = new ArrayList<>();
                    for( DataSnapshot ds: dataSnapshot.getChildren()){
                        Postagem postagem = ds.getValue(Postagem.class);
                        postagens.add(postagem);
                        urlFotos.add(postagem.getCaminhoFoto());
                        //Log.i("postagem", "url " + postagem.getCaminhoFoto());
                    }

                    adapterGrid = new AdapterGrid(getApplicationContext(), R.layout.grid_postagem, urlFotos);
                    gridViewPerfil.setAdapter(  adapterGrid );

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void recuperarDadosUsuarioLogado(){

        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        usuarioLogado = dataSnapshot.getValue(Usuario.class);

                        verificaSegueUsuarioAmigo();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void verificaSegueUsuarioAmigo(){

        DatabaseReference seguidorRef = seguidoresRef
                .child(usuarioSelecionado.getId())
                .child(idUsuarioLogado);

        seguidorRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            habilitarBotaoSeguir(true);

                        }else {
                            habilitarBotaoSeguir(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void habilitarBotaoSeguir( boolean segueUsuario){
        if( segueUsuario ){
            buttonAcaoPerfil.setText("Seguindo");
        }else {
            buttonAcaoPerfil.setText("Seguir");

            buttonAcaoPerfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //salvar seguidor
                    salvarSeguidor(usuarioLogado, usuarioSelecionado);
                }
            });
        }
    }

    private void salvarSeguidor(Usuario uLogado, Usuario uAmigo){

        HashMap<String, Object> dadosUsuarioLogado = new HashMap<>();
        dadosUsuarioLogado.put("nome", uLogado.getNome());
        dadosUsuarioLogado.put("caminhoFoto" , uLogado.getCaminhoFoto());
        DatabaseReference seguidorRef = seguidoresRef
                .child( uAmigo.getId() )
                .child( uLogado.getId());
                seguidorRef.setValue(dadosUsuarioLogado);

                buttonAcaoPerfil.setText("Seguindo");
                buttonAcaoPerfil.setOnClickListener(null);

                //Incrementar seguindo do usuario logado
                int seguindo = uLogado.getSeguindo() + 1;
                HashMap<String, Object> dadosSeguindo = new HashMap<>();
                dadosSeguindo.put("seguindo", seguindo);
                DatabaseReference usuarioSeguindo = usuariosRef
                        .child( uLogado.getId());
                usuarioSeguindo.updateChildren(dadosSeguindo);
//                textSeguindo.setText(seguindo);
                //Incrementar seguidores do amigo
                int seguidores = uAmigo.getSeguidores() + 1;
                HashMap<String, Object> dadosSeguidores = new HashMap<>();
                dadosSeguidores.put("seguidores", seguidores);
                DatabaseReference usuarioSeguidores = usuariosRef
                        .child( uAmigo.getId());
                usuarioSeguidores.updateChildren(dadosSeguidores);


    }

    @Override
    protected void onStop() {
        super.onStop();
        usuarioAmigoRef.removeEventListener(valueEventListenerPerfilAmigo);
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarDadosPerfilAmigo();
        recuperarDadosUsuarioLogado();
    }

    private void recuperarDadosPerfilAmigo(){

        usuarioAmigoRef = usuariosRef.child(usuarioSelecionado.getId());
        valueEventListenerPerfilAmigo = usuarioAmigoRef.addValueEventListener(
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
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
