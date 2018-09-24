package br.com.alura.agenda.dao;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import br.com.alura.agenda.modelo.Aluno;

public class AlunoDao extends SQLiteOpenHelper{
    public AlunoDao(Context context) {
        super(context, "Agenda", null, 2); //version é a versão do banco de dados, alterar sempre quando o banco for modificado
    }

    @Override
    //Cria um novo banco de dados, caso seja a primeira vez que o usuário esteja usando o app
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE Alunos (id INTEGER PRIMARY KEY, nome TEXT NOT NULL, endereco TEXT, telefone TEXT, site TEXT, nota REAL, caminhoFoto TEXT);";

        db.execSQL(sql);
    }

    @Override
    //Atualiza o bd se a versão que já existe no celular foi diferente da nova versão
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "";
        //Não utiliza o break, pois caso o bd tenha mais versões e o banco esteja na versão 1, por exemplo, ele executa todos os outros cases abaixo, até chegar na versão atual do banco
        switch (oldVersion) {
            case 1:
                sql = "ALTER TABLE Alunos ADD COLUMN caminhoFoto TEXT";
                db.execSQL(sql);
        }
    }

    //Não é feito a inserção no BD da mesma maneira que nos métodos anteriores, para previnir SQL Injections, através do insert
    public void insere(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = pegaDadosDoAluno(aluno);

        db.insert("Alunos", null, dados);   //Sempre deixar null no atributo nullColumnHack
    }

    @NonNull
    private ContentValues pegaDadosDoAluno(Aluno aluno) {
        ContentValues dados = new ContentValues();

        dados.put("nome", aluno.getNome());
        dados.put("endereco", aluno.getEndereco());
        dados.put("telefone", aluno.getTelefone());
        dados.put("site", aluno.getSite());
        dados.put("nota", aluno.getNota());
        dados.put("caminhoFoto", aluno.getCaminhoFoto());

        return dados;
    }

    public List<Aluno> buscaAlunos() {
        String sql = "SELECT * FROM Alunos;";
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(sql, null); //Retorna os resultados da query para o cursos. Null pois não existe nenhum argumento

        List<Aluno> alunos = new ArrayList<Aluno>();    //Cria a lista de alunos
        //Função moveToNext move o cursor para a próxima tupla e retorna um boolean para dizer se ainda tem resultados ou não. Então, enquanto tiver tupla:
        while(c.moveToNext()){
            Aluno aluno = new Aluno();  //Cria um novo objeto

            //getColumnIndex retorna o índice da coluna "nome", retorna para o getString que pega o conteúdo dessa coluna e salva em aluno através do método setNome -- (para todos os atributos)
            aluno.setId(c.getLong(c.getColumnIndex("id")));
            aluno.setNome(c.getString(c.getColumnIndex("nome")));
            aluno.setEndereco(c.getString(c.getColumnIndex("endereco")));
            aluno.setTelefone(c.getString(c.getColumnIndex("telefone")));
            aluno.setSite(c.getString(c.getColumnIndex("site")));
            aluno.setNota(c.getDouble(c.getColumnIndex("nota")));
            aluno.setCaminhoFoto(c.getString(c.getColumnIndex("caminhoFoto")));

            alunos.add(aluno);  //Adiciona no vetor
        }
        c.close();  //Libera o cursor

        return  alunos;
    }

    public void deleta(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        String params[] = {aluno.getId().toString()};

        db.delete("Alunos", "id = ?", params);
    }

    public void altera(Aluno aluno) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = pegaDadosDoAluno(aluno);
        String params[] = {aluno.getId().toString()};

        db.update("Alunos", dados, "id = ?", params);
    }

    public boolean ehAluno(String telefone){
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM ALUNOS WHERE telefone = ?", new String[]{telefone});
        int resultados = c.getCount();
        c.close();

        return resultados > 0;
    }
}
