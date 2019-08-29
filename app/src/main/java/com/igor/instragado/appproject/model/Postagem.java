package com.igor.instragado.appproject.model;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.igor.instragado.appproject.helper.ConfiguracaoFirebase;
import com.igor.instragado.appproject.helper.UsuarioFirebase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Postagem implements Serializable {

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

    public boolean salvar(DataSnapshot seguidoresSnapshot){

        Map objeto = new HashMap();
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebase();
        //referencia para postagem
        String combinacaoId = "/" + getIdUsuario() + "/" + getId();
        objeto.put("/postagens" + combinacaoId, this);

        for(DataSnapshot seguidores: seguidoresSnapshot.getChildren()){

            String idSeguidor = seguidores.getKey();

            HashMap<String, Object> dadosSeguidor = new HashMap<>();
            dadosSeguidor.put("fotoPostagem", getCaminhoFoto());
            dadosSeguidor.put("descricao", getDescricao());
            dadosSeguidor.put("id", getId());
            dadosSeguidor.put("nomeUsuario", usuarioLogado.getNome());
            dadosSeguidor.put("fotoUsuario", usuarioLogado.getCaminhoFoto());

            String idsATtualizacao = "/" + idSeguidor + "/" + getId();
            objeto.put("/feed" + idsATtualizacao, dadosSeguidor);

        }


        firebaseRef.updateChildren(objeto);

//        DatabaseReference postagensRef = firebaseRef.child("postagens")
//                .child(getIdUsuario())
//                .child(getId() );
        //postagensRef.setValue(this);
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
