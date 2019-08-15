package com.igor.socialnetwork.project.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.igor.socialnetwork.project.R;
import com.igor.socialnetwork.project.helper.UsuarioFirebase;
import com.igor.socialnetwork.project.model.Comentario;
import com.igor.socialnetwork.project.model.Usuario;

public class ComentariosActivity extends AppCompatActivity {

    private EditText editComentario;
    private String idPostagem;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Comentários");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editComentario = findViewById(R.id.editComentario);

        usuario = UsuarioFirebase.getDadosUsuarioLogado();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            idPostagem = bundle.getString("idPostagem");
        }

    }

    public void salvarComentario(View view){

        String textoComentario = editComentario.getText().toString();
        if(textoComentario != null && !textoComentario.equals("")){

            Comentario comentario = new Comentario();
            comentario.setIdPostagem(idPostagem);
            comentario.setIdUsuario(usuario.getId());
            comentario.setNomeUsuario(usuario.getNome());
            comentario.setCaminhoFoto(usuario.getCaminhoFoto());
            comentario.setComentario(textoComentario);
            if(comentario.salvar()){
                Toast.makeText(this, "Comentário salvo com sucesso!", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(ComentariosActivity.this, "Insira o comentário antes de salvar", Toast.LENGTH_SHORT).show();
        }

        editComentario.setText("");

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
