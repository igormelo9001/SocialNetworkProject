package com.igor.instragado.appproject.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.igor.instragado.appproject.R;
import com.igor.instragado.appproject.adapter.AdapterMiniaturas;
import com.igor.instragado.appproject.helper.ConfiguracaoFirebase;
import com.igor.instragado.appproject.helper.RecyclerItemClickListener;
import com.igor.instragado.appproject.helper.UsuarioFirebase;
import com.igor.instragado.appproject.model.Postagem;
import com.igor.instragado.appproject.model.Usuario;
import com.zomato.photofilters.FilterPack;
import com.zomato.photofilters.imageprocessors.Filter;
import com.zomato.photofilters.utils.ThumbnailItem;
import com.zomato.photofilters.utils.ThumbnailsManager;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FiltroActivity extends AppCompatActivity {

    static
    {
        System.loadLibrary("NativeImageProcessor");
    }

    private ImageView imageFotoEscolhida;
    private Bitmap imagem;
    private Bitmap imagemFiltro;
    private List<ThumbnailItem> listaFiltros;
    private TextInputEditText textDescricaoFiltro;
    private RecyclerView recyclerFiltros;
    private AdapterMiniaturas adapterMiniaturas;
    private String idUsuarioLogado;
    private DatabaseReference usuariosRef;
    private DatabaseReference usuarioLogadoRef;
    private Usuario usuarioLogado;
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private DataSnapshot seguidoresSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        listaFiltros = new ArrayList<>();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();
        usuariosRef = ConfiguracaoFirebase.getFirebase().child("usuarios");


        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        recyclerFiltros = findViewById(R.id.recylerFiltros);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);

        recuperarDadosPostagem();

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Filtros");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            Bundle bundle = getIntent().getExtras();
            if( bundle != null){
                byte[] dadosImagem = bundle.getByteArray("fotoEscolhida");
                imagem = BitmapFactory.decodeByteArray(dadosImagem, 0, dadosImagem.length);
                imageFotoEscolhida.setImageBitmap(imagem);
                imagemFiltro = imagem.copy(imagem.getConfig(), true);

                adapterMiniaturas = new AdapterMiniaturas(listaFiltros, getApplicationContext());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this, LinearLayoutManager.HORIZONTAL, false);
                recyclerFiltros.setLayoutManager(layoutManager);
                recyclerFiltros.setAdapter(adapterMiniaturas);

                recyclerFiltros.addOnItemTouchListener(
                        new RecyclerItemClickListener(
                                getApplicationContext(),
                                recyclerFiltros,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {

                                        ThumbnailItem item = listaFiltros.get(position);
                                        imagemFiltro = imagem.copy(imagem.getConfig(), true);
                                        Filter filtro = item.filter;
                                        imageFotoEscolhida.setImageBitmap(filtro.processFilter(imagemFiltro));
                                    }

                                    @Override
                                    public void onLongItemClick(View view, int position) {

                                    }

                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    }
                                }
                        ));

                recuperarFiltros();

//


            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void abrirDialogCarregamento(String titulo){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle( titulo );
        alert.setCancelable( false );
        alert.setView(R.layout.carregamento);

        dialog = alert.create();
        dialog.show();
    }

    private void recuperarDadosPostagem(){

        abrirDialogCarregamento("Carregando dados, aguarde");
        usuarioLogadoRef = usuariosRef.child(idUsuarioLogado);
        usuarioLogadoRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        usuarioLogado = dataSnapshot.getValue(Usuario.class);
                            seguidoresSnapshot = dataSnapshot;
                        DatabaseReference seguidoresRef = firebaseRef
                                .child("seguidores")
                                .child(idUsuarioLogado);
                        seguidoresRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                seguidoresSnapshot = dataSnapshot;
                                dialog.cancel();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void recuperarFiltros(){

        ThumbnailsManager.clearThumbs();
        listaFiltros.clear();

        ThumbnailItem item = new ThumbnailItem();
        item.image = imagem;
        item.filterName = "Normal";
        ThumbnailsManager.addThumb(item);

        List<Filter> filters = FilterPack.getFilterPack(getApplicationContext());
        for(Filter filtro: filters){
            ThumbnailItem itemFiltro = new ThumbnailItem();
            itemFiltro.image = imagem;
            itemFiltro.filter = filtro;

            itemFiltro.filterName = filtro.getName();

            ThumbnailsManager.addThumb(itemFiltro);
        }

        listaFiltros.addAll(ThumbnailsManager.processThumbs(getApplicationContext()));
        adapterMiniaturas.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filtro,  menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.ic_salvar_postagem:
                publicarPostagem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publicarPostagem() {

            abrirDialogCarregamento("Salvando postagem");
            final Postagem postagem = new Postagem();
            postagem.setIdUsuario(idUsuarioLogado);
            postagem.setDescricao( textDescricaoFiltro.getText().toString());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imagemFiltro.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] dadosImagem = baos.toByteArray();

            StorageReference storageRef = ConfiguracaoFirebase.getFirebaseStorage();
            StorageReference imagemRef = storageRef.child("imagens")
                    .child("postagens")
                    .child(postagem.getId() + ".jpeg");

            UploadTask uploadTask = imagemRef.putBytes(dadosImagem);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FiltroActivity.this, "Erro ao salvar a imagem, tente novamente", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while(!uriTask.isSuccessful());
                    Uri url = uriTask.getResult();
                    postagem.setCaminhoFoto(url.toString());

                    int qtdPostagem = usuarioLogado.getPostagens() + 1;
                    usuarioLogado.setPostagens(qtdPostagem);
                    usuarioLogado.atualizarQtdPostagem();

                    if( postagem.salvar(seguidoresSnapshot)){

                        Toast.makeText(FiltroActivity.this, "Sucesso ao salvar postagem", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        finish();
                    }
                }
            });


    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return false;

    }
}
