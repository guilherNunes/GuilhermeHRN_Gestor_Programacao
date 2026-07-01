package com.gestor;

import com.gestor.ui.LoginFrame;
import javax.swing.UIManager;

/**
 * Esta é a "Chave de Ignição" do programa.
 * Tudo começa por aqui!
 */
public class Main {
    public static void main(String[] args) {
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            
        }

        

        // Manda o computador "rodar" a interface gráfica
        java.awt.EventQueue.invokeLater(() -> {
            // Cria e mostra a primeira tela (a de Login)
            new LoginFrame().setVisible(true);
        });
    }
}
