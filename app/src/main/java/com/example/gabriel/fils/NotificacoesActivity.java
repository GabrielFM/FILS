package com.example.gabriel.fils;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by Felipe on 11/30/2016.
 */

public class NotificacoesActivity  extends AppCompatActivity {

    private static final String TAG = "NotificacoesActivity";
    private static final String PREFS_NAME = "MyPrefs";
    String key = null;

    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference userDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notificacoes_main);

        //Pega dados de autenticação
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        //Pega referencia do banco de dados
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final int perfil = settings.getInt("perfil", 0);
        if(perfil == 1){
            userDatabaseReference = mFirebaseDatabaseReference.child("Atletas").child(user.getUid());
        } else {
            userDatabaseReference = mFirebaseDatabaseReference.child(ProfissionalMainActivity.perfilString).child(user.getUid());
        }

        //Pega a referencia da lista de Notificacoes
        userDatabaseReference.child("Notificacoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final com.google.firebase.database.DataSnapshot dataSnapshot) {
                //Referencia a listview do xml
                ListView listaDeNotificacoes = (ListView) findViewById(R.id.listaNotificacoes);

                // Construct the data source
                ArrayList<Notificacao> arrayOfAlunos = new ArrayList<Notificacao>();
                // Create the adapter to convert the array to views
                final NotificacaoAdapter adapter = new NotificacaoAdapter(NotificacoesActivity.this, arrayOfAlunos);
                // Attach the adapter to a ListView
                listaDeNotificacoes.setAdapter(adapter);

                //Povoa uma lista com os nomes dos treinos
                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                //List<String> list = new ArrayList<String>();
                DataSnapshot aux;
                while (it.hasNext()) {
                    //Aqui eu tenho o userID do aluno, com o qual posso achar seu nome e foto
                    aux = it.next();
                    adapter.add(new Notificacao(aux.child("descricao").getValue().toString(), aux.child("id").getValue().toString()));
                }

                listaDeNotificacoes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final Notificacao entry =  (Notificacao) parent.getAdapter().getItem(position);

                        final Dialog dialog = new Dialog(NotificacoesActivity.this);

                        if(entry.descricao.startsWith("Solicitacao")){
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.dialog_notificacao);

                            TextView pergunta = (TextView) dialog.findViewById(R.id.label1);
                            pergunta.setText("Aceitar Solicitacão?");

                        }else {

                        }

                        //TODO relatar treino
                        Button botaoConfirma = (Button) dialog.findViewById(R.id.botaoConfirma);
                        botaoConfirma.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                userDatabaseReference.child("Notificacoes").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final com.google.firebase.database.DataSnapshot dataSnapshot) {
                                        //Povoa uma lista com os nomes dos treinos
                                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                        DataSnapshot aux;
                                        while (it.hasNext()) {
                                            //Aqui eu tenho o userID do aluno, com o qual posso achar seu nome e foto
                                            aux = it.next();
                                            if(aux.child("descricao").getValue().equals(entry.descricao) && aux.child("id").getValue().equals(entry.id) ){
                                                userDatabaseReference.child("Notificacoes").child(aux.getKey()).removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                dialog.dismiss();
                            }
                        });


                        //Cancela o relato de treino
                        Button botaoRecusar = (Button) dialog.findViewById(R.id.botaoRecusar);
                        botaoRecusar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                userDatabaseReference.child("Notificacoes").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(final com.google.firebase.database.DataSnapshot dataSnapshot) {
                                        //Povoa uma lista com os nomes dos treinos
                                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                                        DataSnapshot aux;
                                        while (it.hasNext()) {
                                            //Aqui eu tenho o userID do aluno, com o qual posso achar seu nome e foto
                                            aux = it.next();
                                            if(aux.child("descricao").getValue().toString().equals(entry.descricao) && aux.child("id").getValue().toString().equals(entry.id) ){
                                                userDatabaseReference.child("Notificacoes").child(aux.getKey()).removeValue();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                dialog.dismiss();
                            }
                        });

                        dialog.show();

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}