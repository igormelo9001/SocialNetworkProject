package com.igor.socialnetwork.project.model;

import com.google.firebase.database.DatabaseReference;
import com.igor.socialnetwork.project.helper.ConfiguracaoFirebase;


public class Postagem {

    private String id;
    private String idUsuario;
    private String descricao;
    private String CaminhoFoto;

    public Postagem() {

        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagemRef = firebaseRef.child("postagens");
        String idPostagem = postagemRef.push().getKey();
        setId(idPostagem);

    }

    public boolean salvar(){
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        DatabaseReference postagensRef = firebaseRef.child("postagens")
                .child(getIdUsuario())
                .child(getId() );
        postagensRef.setValue(this);
        return true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCaminhoFoto() {
        return CaminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        CaminhoFoto = caminhoFoto;
    }
}
