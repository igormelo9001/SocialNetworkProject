package com.igor.socialnetwork.project.activity;

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
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.igor.socialnetwork.project.R;
import com.igor.socialnetwork.project.helper.ConfiguracaoFirebase;
import com.igor.socialnetwork.project.helper.UsuarioFirebase;
import com.igor.socialnetwork.project.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoNome;
    private EditText campoSenha;
    private EditText campoEmail;
    private Button botaoCadastrar;
    private ProgressBar progressBar;
    private Usuario usuario;
    private FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        initViews();
        progressBar.setVisibility(View.GONE);
        botaoCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String textoNome = campoNome.getText().toString();
                String textoEmail = campoEmail.getText().toString();
                String textoSenha = campoSenha.getText().toString();

                if(!textoNome.isEmpty()){
                    if(!textoEmail.isEmpty()){
                        if(!textoSenha.isEmpty()){

                            usuario = new Usuario();
                            usuario.setNome(textoNome);
                            usuario.setEmail(textoEmail);
                            usuario.setSenha(textoSenha);

                            cadastrar(usuario);
                            
                        }else {
                        Toast.makeText(CadastroActivity.this, "Preencha a senha", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                    Toast.makeText(CadastroActivity.this, "Preencha o email", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(CadastroActivity.this, "Preencha o nome", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void cadastrar(final Usuario usuario) {

        progressBar.setVisibility(View.VISIBLE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        autenticacao.createUserWithEmailAndPassword(
            usuario.getEmail(),
            usuario.getSenha()
        ).addOnCompleteListener(
                this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if( task.isSuccessful()){

                            try{
                                progressBar.setVisibility(View.GONE);

                                String idUsuario = task.getResult().getUser().getUid();
                                usuario.setId(idUsuario);
                                usuario.salvar();

                                UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());

                                Toast.makeText(CadastroActivity.this, "Cadastro com sucesso", Toast.LENGTH_SHORT).show();

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();


                            }catch (Exception e){
                                e.printStackTrace();
                            }


                        }else {

                            progressBar.setVisibility(View.GONE);

                            String erroExcecao = "";

                            try {
                                throw task.getException();
                            }catch (FirebaseAuthWeakPasswordException e){
                                erroExcecao = "Digite uma senha mais forte";
                            }catch (FirebaseAuthInvalidUserException e){
                                erroExcecao = "Por favor, digite um email válido";
                            }catch (FirebaseAuthUserCollisionException e){
                                erroExcecao = "Esta conta ja foi cadastrada";
                            }catch (Exception e){
                                erroExcecao = "ao cadastrar usuário" + e.getMessage();
                                e.printStackTrace();
                            }

                            Toast.makeText(CadastroActivity.this, "Erro:"
                                    + erroExcecao, Toast.LENGTH_SHORT).show();

                        }

                    }
                }
        );

    }

    private void initViews() {
        campoNome = findViewById(R.id.campoUsuario);
        campoEmail = findViewById(R.id.campoEmailCadastro);
        campoSenha = findViewById(R.id.campoSenhaCadastro);
        botaoCadastrar = findViewById(R.id.botaoCadastrar);
        progressBar = findViewById(R.id.progressBarCadastro);

        campoEmail.requestFocus();
    }
}
