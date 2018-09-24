package br.com.alura.agenda;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import br.com.alura.agenda.dao.AlunoDao;
import br.com.alura.agenda.modelo.Aluno;

public class FormularioActivity extends AppCompatActivity {

    public static final int CODIGO_CAMERA = 567;
    private FormularioHelper helper;
    private String caminhoFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario);
        helper = new FormularioHelper(this);

        Intent intent = getIntent();
        Aluno aluno = (Aluno) intent.getSerializableExtra("aluno"); //Recupera o aluno que foi "pendurado" na intent
        //Se o aluno não for nulo, ou seja, se o formulario foi chamado pelo opção "editar" e não pela opção "inserir"
        if(aluno != null){
            helper.preencheFormulario(aluno);
        }

        Button botaoFoto = (Button) findViewById(R.id.formulario_botao_foto);
        botaoFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //MediaStore.ACTION_IMAGE_CAPTURE Chama a câmera que o usuário escolher para tirar a foto
                caminhoFoto = getExternalFilesDir(null) + "/" + System.currentTimeMillis() + ".jpg"; //Cria o nome da foto, salvando na pasta da aplicação
                File arquivoFoto = new File(caminhoFoto);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(arquivoFoto));
                startActivityForResult(intentCamera, CODIGO_CAMERA);
            }
        });
    }

    //Método que é chamado após o startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Se o usuário tirou a foto
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == CODIGO_CAMERA) {
                helper.carregaImagem(caminhoFoto);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_formulario, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Opções do menu
        switch (item.getItemId()){
            //Salvar
            case R.id.menu_formulario_ok:
                Aluno aluno = helper.pegaAluno();   //Pega os dados do aluno
                AlunoDao dao = new AlunoDao(this);  //Conecta com o banco

                //Se o id do aluno for diferente de nulo, ou seja, se ele estiver sendo alterado, chama a função para alterar
                if(aluno.getId() != null){
                    dao.altera(aluno);
                }
                //Caso contrário, chama a função para inserir
                else{
                    dao.insere(aluno);  //Insere
                }
                dao.close();    //Fecha a conexão
                Toast.makeText(FormularioActivity.this, "Aluno " + aluno.getNome() + " salvo com sucesso!", Toast.LENGTH_SHORT).show();
                finish();   //Finaliza a Activity, ou seja, volta para a tela da lista de alunos
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}
