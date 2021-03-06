package com.example.gabriel.fils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * Created by Gabriel on 10/11/2016.
 *
 * Tela que lista os treinos cadastrados e permite cadastrar novos
 */


public class NovoTreino extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private static final String PREFS_NAME = "MyPrefs";
    private String idAtleta;
    private int perfil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.novo_treino_main);

        //Pega dados de autenticação
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        //Pega referencia do banco de dados
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        perfil = settings.getInt("perfil", 0);
        if(perfil == 1){
            idAtleta = user.getUid();
        }else{
            idAtleta = ProfissionalMainActivity.alunoAtual;
        }

        //Pega a referencia da lista de treinos
        mFirebaseDatabaseReference.child("Atletas").child(idAtleta).child("Treinos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final com.google.firebase.database.DataSnapshot dataSnapshot) {
                //Referencia a listview do xml
                ListView listaDeAtividades = (ListView) findViewById(R.id.listaAtividades);

                //Povoa uma lista com os nomes dos treinos
                Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                List<String> list = new ArrayList<String>();
                DataSnapshot aux;
                while (it.hasNext()) {
                    aux = it.next();
                    list.add(aux.child("Tipo").getValue() + ": " + aux.child("Descricao").getValue());
                }

                //Atribui a lista de nomes a listview
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(NovoTreino.this,android.R.layout.simple_list_item_1, list);
                listaDeAtividades.setAdapter(adapter);

                listaDeAtividades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {

                        final String entry = (String) parent.getAdapter().getItem(position);
                        final Dialog dialog = new Dialog(NovoTreino.this);


                        if(entry.startsWith("Corrida")){
                            //dialog.setTitle("Relatar Treino");
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            if(perfil == 1){
                                dialog.setContentView(R.layout.dialog_novotreino_corrida);
                            }else{
                                dialog.setContentView(R.layout.dialog_corrida_aluno);
                            }


                            TextView nome = (TextView) dialog.findViewById(R.id.treinoNome);
                            String s = entry.toString();
                            nome.setText(s.substring(s.lastIndexOf(":") + 1));


                            //DatabaseReference ref = mFirebaseDatabaseReference.child("Treino").child(s.substring(s.lastIndexOf(":") + 2));
                            TextView dur = (TextView) dialog.findViewById(R.id.duracaoText);
                            dur.setText(" " + dataSnapshot.child(s.substring(s.lastIndexOf(":") + 2)).child("Duracao").getValue().toString() + "min");

                            TextView dis = (TextView) dialog.findViewById(R.id.distText);
                            dis.setText(" " + dataSnapshot.child(s.substring(s.lastIndexOf(":") + 2)).child("Distancia").getValue().toString() + "km");

                        }else if(entry.startsWith("Outro")){
                            //dialog.setTitle("Relatar Treino");
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            if(perfil == 1){
                                dialog.setContentView(R.layout.dialog_novotreino_outro);
                            }else{
                                dialog.setContentView(R.layout.dialog_outro_aluno);
                            }


                            TextView nome = (TextView) dialog.findViewById(R.id.treinoNome);
                            String s = entry.toString();
                            nome.setText(s.substring(s.lastIndexOf(":") + 1));


                            //DatabaseReference ref = mFirebaseDatabaseReference.child("Treino").child(s.substring(s.lastIndexOf(":") + 2));
                            TextView dur = (TextView) dialog.findViewById(R.id.duracaoText);
                            dur.setText(" " + dataSnapshot.child(s.substring(s.lastIndexOf(":") + 2)).child("Duracao").getValue().toString() + "min");

                        }else if(entry.startsWith("Grupo")){
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            if(perfil == 1){
                                dialog.setContentView(R.layout.dialog_treino_musc);
                            }else{
                                dialog.setContentView(R.layout.dialog_musc_aluno);
                            }

                            TextView grupo = (TextView) dialog.findViewById(R.id.label1);
                            TextView descText = (TextView) dialog.findViewById(R.id.descText);
                            ListView listaExercicios = (ListView) dialog.findViewById(R.id.listaGrupoExercicios);

                            String s = entry.toString();

                            String[] v = s.split(":");

                            grupo.setText(v[0]);
                            descText.setText(v[1]);

                            Iterator<DataSnapshot> iteratorExer = dataSnapshot.child(v[0]).child("Exercicios").getChildren().iterator();
                            List<String> listExercicios = new ArrayList<String>();
                            DataSnapshot aux;
                            while(iteratorExer.hasNext()){
                                aux = iteratorExer.next();
                                listExercicios.add(aux.child("Descricao").getValue() + " - " + aux.child("Series").getValue() + " - " + aux.child("Repeticoes").getValue());
                            }

                            ArrayAdapter<String> adapterExer = new ArrayAdapter<String>(NovoTreino.this,android.R.layout.simple_list_item_1, listExercicios);
                            listaExercicios.setAdapter(adapterExer);
                        }




                        if(perfil == 1){
                            Button botaoConfirma = (Button) dialog.findViewById(R.id.botaoConfirma);
                            botaoConfirma.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Calendar c = Calendar.getInstance();
                                    Integer ano = c.get(Calendar.YEAR);
                                    Integer dia = c.get(Calendar.DAY_OF_MONTH);
                                    Integer mes = c.get(Calendar.MONTH);
                                    mes++;
                                    Integer hora = c.get(Calendar.HOUR_OF_DAY);
                                    Integer minutos = c.get(Calendar.MINUTE);
                                    Integer segundos = c.get(Calendar.SECOND);
                                    final String s = entry.toString();
                                    String idHistorico = hora.toString()+":"+minutos.toString()+":"+segundos.toString();
                                    mFirebaseDatabaseReference.child("Atletas").child(idAtleta).child("Historico").child(ano.toString()).child(mes.toString()).child(dia.toString()).child(idHistorico).child("Tipo").setValue("Treino");
                                    mFirebaseDatabaseReference.child("Atletas").child(idAtleta).child("Historico").child(ano.toString()).child(mes.toString()).child(dia.toString()).child(idHistorico).child("Id").setValue(s.substring(s.lastIndexOf(":") + 1));
                                    if(entry.startsWith("Corrida")){
                                        mFirebaseDatabaseReference.child("Atletas").child(idAtleta).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot2) {
                                                if(dataSnapshot2.hasChild("Metas")){
                                                    Integer meta = dataSnapshot2.child("Metas").child("Corrida").getValue(Integer.class);
                                                    int distCorridaAtual = Integer.parseInt(dataSnapshot2.child("Treinos").child(s.substring(s.lastIndexOf(":") + 2)).child("Distancia").getValue().toString());
                                                    if(distCorridaAtual >= meta){
                                                        mFirebaseDatabaseReference.child("Atletas").child(idAtleta).child("Notificacoes").push().setValue(new Notificacao("Metas", "Você correu "+distCorridaAtual+"km."));
                                                        if(dataSnapshot2.hasChild("Personal")){
                                                            mFirebaseDatabaseReference.child("Personais").child(dataSnapshot2.child("Personal").getValue().toString()).child("Notificacoes").push().setValue(new Notificacao("Metas", user.getDisplayName()+ "Correu "+distCorridaAtual+"km."));
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    finish();
                                    //dialog.dismiss();
                                }
                            });
                        }



                        //Cancela o relato de treino
                        Button botaoCancela = (Button) dialog.findViewById(R.id.botaoCancela);
                        botaoCancela.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
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




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addTreinoButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NovoTreino.this, AddTreino.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


}
