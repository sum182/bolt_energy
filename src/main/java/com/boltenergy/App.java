package com.boltenergy;

/**
 * Classe principal da aplicação Bolt Energy
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Bem-vindo à aplicação Bolt Energy!");
        System.out.println("Java version: " + System.getProperty("java.version"));
        
        // Exemplo simples de concatenação de strings
        String nome = "Usuário";
        System.out.println("Olá, " + nome + "!");
        
        // Alternativa usando String.format
        System.out.println(String.format("Seja bem-vindo(a), %s!", nome));
    }
}
