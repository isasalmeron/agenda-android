package br.com.alura.agenda;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import br.com.alura.agenda.adapter.AlunosAdapter;
import br.com.alura.agenda.converter.AlunoConverter;
import br.com.alura.agenda.dao.AlunoDao;
import br.com.alura.agenda.modelo.Aluno;

public class ListaAlunosActivity extends AppCompatActivity {

    private ListView listaAlunos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_alunos);

        listaAlunos = (ListView) findViewById(R.id.lista_alunos);

        //Trata o clique no aluno para editá-lo. Não foi usado a função setOnClickListener, pois ela trata cliques em qualquer lugar da lista da mesma maneira, e aqui precisamos tratar alunos de diferentes formas.
        listaAlunos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> lista, View item, int position, long id) {
                Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(position);
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                intentVaiProFormulario.putExtra("aluno",aluno); //"Pendura" o aluno na intent para ir pro formulario junto com ela
                startActivity(intentVaiProFormulario);
            }
        });

        Button novoAluno = (Button) findViewById(R.id.novo_aluno);
        novoAluno.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intentVaiProFormulario = new Intent(ListaAlunosActivity.this, FormularioActivity.class);
                startActivity(intentVaiProFormulario);
            }
        });

        //Registra lista de alunos como alguém que tem menu de contexto, que é quando segura um item apertado
        registerForContextMenu(listaAlunos);
    }

    private void carregaLista() {
        AlunoDao dao = new AlunoDao(this);  //Conecta com o banco
        List<Aluno> alunos = dao.buscaAlunos(); //Lista com os alunos salvos no banco
        dao.close();    //Fecha o banco

        AlunosAdapter adapter = new AlunosAdapter(this, alunos);
        listaAlunos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregaLista();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista_alunos, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_enviar_notas:
                new EnviaAlunosTask(this).execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    //Poderia ser feito da mesma maneira do menu do formulario (xml), mas existe essa outra maneira também
    public void onCreateContextMenu(ContextMenu menu, View v, final ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;    //menuInfo contém o item que que foi "segurado", que no caso é uma lista de alunos
        final Aluno aluno = (Aluno) listaAlunos.getItemAtPosition(info.position);   //Retorna o aluno que está na posição informada por info.position

        //Opção para enviar SMS
        MenuItem itemSMS = menu.add("Enviar SMS");
        Intent intentSMS = new Intent(Intent.ACTION_VIEW);  //Intent implicita, quando não sei teoricamente para qual activity ir. Nesse caso sei que é pra um navegador, mas não sei qual o usuário escolheu.
        intentSMS.setData(Uri.parse("sms:" + aluno.getTelefone())); //Uri.parse converte a string em uma uri
        itemSMS.setIntent(intentSMS);

        //Opção para ligar
        MenuItem itemLigar = menu.add("Ligar");
        itemLigar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Se a permissão de ligar ainda não foi concedida pelo usuário, temos que pedir.
                if(ActivityCompat.checkSelfPermission(ListaAlunosActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ListaAlunosActivity.this, new String[] {Manifest.permission.CALL_PHONE}, 123);
                }
                else {
                    Intent intentLigar = new Intent(Intent.ACTION_CALL);
                    intentLigar.setData(Uri.parse("tel:" + aluno.getTelefone()));
                    startActivity(intentLigar);
                }

                return false;
            }
        });

        //Opção para visualizar no mapa
        MenuItem itemMapa = menu.add("Visualizar no mapa");
        Intent intentMapa = new Intent(Intent.ACTION_VIEW);
        intentMapa.setData(Uri.parse("geo:0,0?q=" + aluno.getEndereco()));
        itemMapa.setIntent(intentMapa);

        //Opção para visitar o site
        MenuItem itemSite = menu.add("Visitar site");
        Intent intentSite = new Intent(Intent.ACTION_VIEW);
        String site = aluno.getSite();
        //Verifica se o site salvo possui "http://" antes, se não possuir, concatena
        if(!site.startsWith("http://")){
            site = "http://" + site;
        }
        intentSite.setData(Uri.parse(site));
        itemSite.setIntent(intentSite);

        //Opção para deletar
        MenuItem deletar = menu.add("Deletar");    //Devolve a referência para o item que foi gerado
        deletar.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                AlunoDao dao = new AlunoDao(ListaAlunosActivity.this);  //Conecta com o banco
                dao.deleta(aluno);  //Insere
                dao.close();    //Fecha a conexão

                carregaLista(); //Atualiza a lista de alunos na aplicação, pois como não saiu da aplicação, não passou pelo método onResume, então não atualizou a lista
                return false;
            }
        });
    }
}