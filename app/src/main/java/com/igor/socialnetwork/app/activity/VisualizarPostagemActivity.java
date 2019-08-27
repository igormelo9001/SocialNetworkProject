package com.igor.socialnetwork.app.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;
import com.igor.socialnetwork.app.R;
import com.igor.socialnetwork.app.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.app.model.Postagem;
import com.igor.socialnetwork.app.model.Usuario;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class VisualizarPostagemActivity extends AppCompatActivity {

    private TextView textPerfilPostagem, textQtdCurtidasPostagem, textDescricaoPostagem, textVisualizarComentariosPostagem;
    private ImageView imagePostagemSelecionada;
    private CircleImageView imagePerfilPostagem;
    private StorageReference storageRef;
    private DatabaseReference usuarioAmigoRef;
    private Usuario usuarioSelecionado;
    private DatabaseReference usuariosRef;
    private DatabaseReference firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_postagem);

        initViews();

//        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
//        toolbar.setTitle("Visualizar postagem");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseRef = ConfiguracaoFirebase.getFirebase();
        storageRef = ConfiguracaoFirebase.getFirebaseStorage();
        usuariosRef = firebaseRef.child("usuarios");

        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            Postagem postagem = (Postagem) bundle.getSerializable("postagem");
            Usuario usuario = (Usuario) bundle.getSerializable("usuario");

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
                        imagePerfilPostagem.setImageBitmap(bitmap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            } catch (IOException e ) {}

            Uri uri = Uri.parse((postagem.getCaminhoFoto()));
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uri)
                    .into(imagePerfilPostagem);
            textPerfilPostagem.setText(usuario.getNome());

            Uri uriPostagem = Uri.parse(postagem.getCaminhoFoto());
            Glide.with(VisualizarPostagemActivity.this)
                    .load(uriPostagem)
                    .into(imagePostagemSelecionada);
            textDescricaoPostagem.setText(postagem.getDescricao());
        }
    }

    private void initViews() {

        textPerfilPostagem = findViewById(R.id.textPerfilPostagem);
        textQtdCurtidasPostagem = findViewById(R.id.textQtdCurtidasPostagens);
        textDescricaoPostagem = findViewById(R.id.textDescricaoPostagem);
        imagePerfilPostagem = findViewById(R.id.imagePerfilPostagem);
        imagePostagemSelecionada = findViewById(R.id.imagePostagemSelecionada);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
