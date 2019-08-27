package com.igor.socialnetwork.app.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.igor.socialnetwork.app.R;
import com.igor.socialnetwork.app.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.app.model.Usuario;

public class LoginActivity extends AppCompatActivity{

    private EditText campoEmail, campoSenha;
    private Button entrar;
    private Button cadastrar;
    private Usuario usuario;
    private ProgressBar progressBar;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        verificarUsuarioLogado();

        initViews();
        progressBar.setVisibility(View.GONE);
        entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoEmail.isEmpty()){
                    if(!textoSenha.isEmpty()){
                        usuario = new Usuario();
                        usuario.setEmail(textoEmail);
                        usuario.setSenha(textoSenha);
                        login(usuario);
                    }else {
                        Toast.makeText(LoginActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(LoginActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
                }

            }
        });

    cadastrar.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
        }
    });
}

    private void verificarUsuarioLogado(){
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        if( autenticacao.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        }
    }

    private void login(Usuario usuario) {

        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.signInWithEmailAndPassword(
                usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            Toast.makeText(LoginActivity.this, "Sucesso ao logar", Toast.LENGTH_SHORT).show();
                            finish();

                        }else {

                            progressBar.setVisibility(View.GONE);

                            String erroExcecao = "";

                            try{

                            }catch (Exception e){
                                erroExcecao = "ao entrar com o usuário";
                                e.printStackTrace();
                            }

                            Toast.makeText(LoginActivity.this, "Erro ao logar, verifique se o email ou a senha estão incorretos"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void initViews() {
            campoEmail = findViewById(R.id.campoEmail);
            campoSenha = findViewById(R.id.campoSenha);
            progressBar = findViewById(R.id.progressBar);
            entrar = findViewById(R.id.botaoEntrar);
            cadastrar = findViewById(R.id.cadastrar);

            campoEmail.requestFocus();
        }
    }
