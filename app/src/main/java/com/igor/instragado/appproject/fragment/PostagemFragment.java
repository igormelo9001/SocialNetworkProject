package com.igor.instragado.appproject.fragment;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.igor.instragado.appproject.R;
import com.igor.instragado.appproject.activity.FiltroActivity;
import com.igor.instragado.appproject.helper.Permissao;

import java.io.ByteArrayOutputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostagemFragment extends Fragment {

    private Button buttonGaleria, buttonCamera;
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    public PostagemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_postagem, container, false);

        Permissao.validarPermissoes(permissoesNecessarias, getActivity(), 1);

        buttonCamera = view.findViewById(R.id.buttonAbrirCamera);
        buttonGaleria = view.findViewById(R.id.buttonAbrirGaleria);

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if( i.resolveActivity(getActivity().getPackageManager())!= null){
                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });

        buttonGaleria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getActivity().getPackageManager()) != null){
                   startActivityForResult(i, SELECAO_GALERIA);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == getActivity().RESULT_OK){

            Bitmap imagem = null;

           try {

               switch (requestCode){

                   case SELECAO_CAMERA:
                       imagem = (Bitmap) data.getExtras().get("data");
                       break;
                   case SELECAO_GALERIA:
                       Uri localImagemSelecionada = data.getData();
                       imagem = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),localImagemSelecionada);
                       break;
               }

               if(imagem != null){

                   ByteArrayOutputStream baos = new ByteArrayOutputStream();
                   imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                   byte[] dadosImagem = baos.toByteArray();

                   Intent i = new Intent(getActivity(), FiltroActivity.class);
                   i.putExtra("fotoEscolhida", dadosImagem);
                   startActivity(i);

               }

           }catch (Exception e){
               e.printStackTrace();
           }

        }
    }
}
