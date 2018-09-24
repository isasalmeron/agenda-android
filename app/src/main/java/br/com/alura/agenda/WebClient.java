package br.com.alura.agenda;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WebClient {
    public String post(String json){
        try {
            URL url = new URL("https://www.caelum.com.br/mobile");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json");  //Mostra o tipo de objeto que está enviando
            connection.setRequestProperty("Accept", "application/json");    //Especifica o tipo de objeto que aceita como resposta

            connection.setDoOutput(true);   //Realiza o método post

            PrintStream output = new PrintStream(connection.getOutputStream());
            output.println(json);

            connection.connect();   //Realiza a conexão com o servidor

            Scanner scanner = new Scanner(connection.getInputStream());
            String resposta = scanner.next();

            return resposta;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
