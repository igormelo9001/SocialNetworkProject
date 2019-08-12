package com.igor.socialnetwork.project.activity;

import androidx.annotation.NonNull;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.igor.socialnetwork.project.R;
import com.igor.socialnetwork.project.adapter.AdapterMiniaturas;
import com.igor.socialnetwork.project.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.project.helper.RecyclerItemClickListener;
import com.igor.socialnetwork.project.helper.UsuarioFirebase;
import com.igor.socialnetwork.project.model.Postagem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtro);

        listaFiltros = new ArrayList<>();
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();


        imageFotoEscolhida = findViewById(R.id.imageFotoEscolhida);
        recyclerFiltros = findViewById(R.id.recylerFiltros);
        textDescricaoFiltro = findViewById(R.id.textDescricaoFiltro);

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

                if( postagem.salvar()){
                    Toast.makeText(FiltroActivity.this, "Sucesso ao salvar postagem", Toast.LENGTH_SHORT).show();
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